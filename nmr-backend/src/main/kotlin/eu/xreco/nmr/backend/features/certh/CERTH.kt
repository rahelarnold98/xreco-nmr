package eu.xreco.nmr.backend.features.certh


import eu.xreco.nmr.backend.config.MinioClientSingleton
import eu.xreco.nmr.backend.config.MinioConfig
import io.minio.GetObjectTagsArgs
import io.minio.messages.Tags
import org.json.JSONObject
import org.vitrivr.engine.base.features.external.ExternalAnalyser
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.Model3DContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.IntVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.reflect.KClass

/**
 * Implementation of the [CERTH] [ExternalAnalyser], which derives the CERTH feature from an [Model3DContent] as [FloatVectorDescriptor].
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class CERTH : ExternalAnalyser<Model3DContent, FloatVectorDescriptor>() {

    companion object {
        // Size of descriptor
        const val VECTOR_SIZE = 32
    }

    override val contentClasses: Set<KClass<out ContentElement<*>>> = setOf(Model3DContent::class)
    override val descriptorClass = FloatVectorDescriptor::class

    /**
     * Requests the CERTH feature descriptor for the given [ContentElement].
     *
     * @param content The [ContentElement] for which to request the CERTH feature descriptor.
     * @param hostname The hostname of the external feature descriptor service.
     * @return A list of CERTH feature descriptors.
     */
    override fun analyse(content: Model3DContent, hostname: String): FloatVectorDescriptor {
        val list: List<Value.Float> =
            executeApiRequest("http://160.40.53.193:8000/3D model retrieval/extract/", content)
        return FloatVectorDescriptor(UUID.randomUUID(), null, list, true)
    }


    /**
     * Requests the CERTH feature descriptor for the given [ContentElement].
     *
     * @param content The [ContentElement] for which to request the CERTH feature descriptor.
     * @return A list of CERTH feature descriptors.
     */
    fun requestDescriptor(content: ContentElement<*>): List<Value.Float> {
        require(content is Model3DContent) { "Unsupported content type." }
        return executeApiRequest("http://160.40.53.193:8000/3D model retrieval/extract/", content)
    }

    /**
     * Retrieves the filename associated with the asset ID from Minio.
     *
     * @param minio The [MinioClient] instance used to connect to Minio.
     * @param bucket The name of the Minio bucket.
     * @param assetId The asset ID for which the filename needs to be retrieved.
     * @return The filename associated with the asset ID.
     */
    private fun getFilenameFromMinio(assetId: String): String {
        val tags: Tags = MinioClientSingleton.minioClient.getObjectTags(
            GetObjectTagsArgs.builder().bucket(MinioConfig.ASSETS_BUCKET).`object`(assetId).build()
        ) ?: Tags()

        // println("assetid $assetId")

        return tags.get()["filename"] ?: ""
    }

    /**
     *
     */
    private fun executeApiRequest(url: String, content: Model3DContent): List<Value.Float> {
        // Create an HttpURLConnection
        val orgNameFull = getFilenameFromMinio(content.id)
        val orgName = orgNameFull.substringBefore('.')
        val completeUrl = "$url$orgName".replace(" ", "%20")
        var connectionPost = URL(completeUrl).openConnection() as HttpURLConnection

        // TODO change as soon as CERTH has changed and localhost of them is no longer needed
        val jsonString = """
        {
          "data": "http://127.0.0.1:9000/public/${orgNameFull}",
          "start": 0,
          "end": 0,
          "last": true
        }
        """.trimIndent()

        var jobId: String = ""
        try {
            // Set up the connection for a POST request
            connectionPost.requestMethod = "POST"
            connectionPost.doOutput = true
            connectionPost.setRequestProperty("Content-Type", "application/json")

            // Write the request body to the output stream
            val outputStream: OutputStream = connectionPost.outputStream
            val writer = BufferedWriter(OutputStreamWriter(outputStream))
            writer.write(jsonString)
            writer.flush()
            writer.close()
            outputStream.close()

            // Get the response code (optional, but useful for error handling)
            val responseCode = connectionPost.responseCode

            // Read the response as a JSON string
            val responseJson = if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = BufferedReader(InputStreamReader(connectionPost.inputStream))
                val response = inputStream.readLine()
                inputStream.close()
                response
            } else {
                null
            }

            // Parse the JSON string
            // println(responseJson)
            val jsonObject =
                JSONObject(responseJson?.substring(IntRange(1, responseJson.length - 2))?.replace("\\", ""))

            // Get the value associated with the "jobId" key
            jobId = jsonObject.getString("jobId")
        } catch (e: Exception) {
            e.printStackTrace()
            // TODO Handle exceptions as needed
        } finally {
            connectionPost.disconnect()
        }
        // println(jobId)

        // Create GET request to get result
        val connectionGet = URL("$completeUrl/$jobId").openConnection() as HttpURLConnection

        // Set the request method to GET
        connectionGet.requestMethod = "GET"
        connectionGet.connectTimeout = 5000 // 5 seconds
        connectionGet.readTimeout = 5000 // 5 seconds

        try {
            // Get the response code (optional, but useful for error handling)
            val responseCode = connectionGet.responseCode
            // Read the response as a JSON string
            val responseJson = if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = BufferedReader(InputStreamReader(connectionGet.inputStream))
                val response = inputStream.use { it.readText() }
                // println("Response: $response")
                inputStream.close()
                response
            } else {
                null
            }

            val jsonObject =
                JSONObject(responseJson?.substring(IntRange(1, responseJson.length - 2))?.replace("\\", ""))
            val descriptor = jsonObject.getJSONArray("result").toString()
            //println(descriptor)

            // Parse the JSON string to List<Int> using Gson
            return if (responseJson != null) {
                try {
                    // Json.decodeFromString(ListSerializer(Float.serializer()), descriptor).map { Value.Float(it) }
                    descriptor.trim('[', ']') // Remove square brackets
                        .split(",") // Split by comma
                        .map { Value.Float(it.toFloat()) } // Convert each string element to float
                        .toList() // Convert to List<Float>
                } catch (e: Exception) {
                    e.printStackTrace()
                    emptyList()
                }
            } else {
                emptyList()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // TODO Handle exceptions as needed
        } finally {
            connectionGet.disconnect()
        }
        return emptyList()
    }

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [CERTH].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [IntVectorDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>) =
        FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), List(VECTOR_SIZE) { Value.Float(0.0f) }, true)

    /**
     * Generates and returns a new [Extractor] instance for this [Analyser].
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     * @param persisting True, if the results of the [Extractor] should be persisted.
     *
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(
        field: Schema.Field<Model3DContent, FloatVectorDescriptor>,
        input: Operator<Retrievable>,
        context: IndexContext,
        persisting: Boolean,
        parameters: Map<String, Any>
    ): Extractor<Model3DContent, FloatVectorDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        return CERTHExtractor(input, field, persisting, this)
    }

    /**
     * Generates and returns a new [Retriever] instance for this [CERTH].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [ContentElement]s to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForContent(field: Schema.Field<Model3DContent, FloatVectorDescriptor>, content: Collection<Model3DContent>, context: QueryContext): Retriever<Model3DContent, FloatVectorDescriptor> {
        val host = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT

        /* Extract vectors from content. */
        val vectors = content.map { analyse(it, host) }

        /* Return retriever. */
        return this.newRetrieverForDescriptors(field, vectors, context)
    }


    /**
     * Generates and returns a new [CERTHRetriever] instance for this [CERTH].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [FloatVectorDescriptor] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForDescriptors(field: Schema.Field<Model3DContent, FloatVectorDescriptor>, descriptors: Collection<FloatVectorDescriptor>, context: QueryContext): Retriever<Model3DContent, FloatVectorDescriptor> {
        /* Prepare query parameters. */
        val k = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        val fetchVector = context.getProperty(field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false

        /* Return retriever. */
        return this.newRetrieverForQuery(field, ProximityQuery(value = descriptors.first().vector, k = k, fetchVector = fetchVector), context)
    }

    /**
     * Generates and returns a new [Retriever] instance for this [CERTH].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query A [Query] elements to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForQuery(
        field: Schema.Field<Model3DContent, FloatVectorDescriptor>, query: Query, context: QueryContext
    ): Retriever<Model3DContent, FloatVectorDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        require(query is ProximityQuery<*> && query.value.first() is Value.Float) { "" }

        @Suppress("UNCHECKED_CAST") return CERTHRetriever(field, query as ProximityQuery<Value.Float>, context)
    }
}
