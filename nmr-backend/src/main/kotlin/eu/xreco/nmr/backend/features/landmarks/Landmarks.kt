package eu.xreco.nmr.backend.features.landmarks

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import eu.xreco.nmr.backend.config.Config
import eu.xreco.nmr.backend.config.MinioConfig
import org.vitrivr.engine.base.features.external.ExternalAnalyser
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * Implementation of the [Landmarks] [ExternalAnalyser], which derives the Landmarks feature from an [ImageContent] as [LabelDescriptor].
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class Landmarks :  ExternalAnalyser<ContentElement<*>, LabelDescriptor>() {
    override val contentClasses = setOf(ImageContent::class)
    override val descriptorClass = LabelDescriptor::class


    private val url = "https://ep33exn95a.execute-api.eu-west-1.amazonaws.com/extract/landmark"

    /**
     * Generates a prototypical [LabelDescriptor] for this [Landmarks].
     *
     * @return [LabelDescriptor]
     */
    override fun prototype(field: Schema.Field<*,*>) = LabelDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.String(""), Value.Float(0.0f), true)

    /**
     * Generates and returns a new [Extractor] instance for this [Landmarks].
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     * @param persisting True, if the results of the [Extractor] should be persisted.
     *
     * @return A new [Extractor] instance for this [Landmarks]
     * @throws [UnsupportedOperationException], if this [Landmarks] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(
        field: Schema.Field<ContentElement<*>, LabelDescriptor>,
        input: Operator<Retrievable>,
        context: IndexContext,
        persisting: Boolean,
        parameters: Map<String, Any>
    ): Extractor<ContentElement<*>, LabelDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        return LandmarksExtractor(input, field, persisting)
    }

    /**
     * Generates and returns a new [Retriever] instance for this [Landmarks].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [ContentElement] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Landmarks]
     * @throws [UnsupportedOperationException], if this [Landmarks] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, LabelDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): Retriever<ContentElement<*>, LabelDescriptor> {
        /* Extract parameters and construct query. */
        val k = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        val descriptor = content.filterIsInstance<TextContent>()
        val query = SimpleFulltextQuery(Value.String(descriptor.first().content), limit = k, attributeName = "label")

        /* Generate retriever. */
        return this.newRetrieverForQuery(field, query, context)
    }

    /**
     * Generates and returns a new [Retriever] instance for this [Landmarks].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Landmarks]
     * @throws [UnsupportedOperationException], if this [Landmarks] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, LabelDescriptor>, query: Query, context: QueryContext): Retriever<ContentElement<*>, LabelDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        require(query is SimpleFulltextQuery) { "" }
        return LandmarksRetriever(field, query, context)
    }

    override fun analyse(content: ContentElement<*>, hostname: String): LabelDescriptor {
        TODO("Not yet implemented")
    }

    /**
     * Requests the Landmarks feature descriptor for the given [ContentElement].
     *
     * @param content The [ContentElement] for which to request the Landmarks feature descriptor.
     * @return A list of Landmarks feature descriptors.
     */
    // TODO removed override fun analyse
    // TODO (rgasser): This is ugly and should be reconciliated with analyser().
    fun analyseList(content: ContentElement<*>, source: UUID): List<LabelDescriptor> = when(content){
        is ImageContent -> {
            val results = executeImageApiRequest(source)
            val labelDescriptors: List<LabelDescriptor> = results.filter { it.confidence >= 0.7f }.map { res ->
                LabelDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.String(res.label.replace("_", " ")), Value.Float(res.confidence), true)
            }
            labelDescriptors
        }
        else -> throw IllegalArgumentException("Content '$content' not supported")
    }


    private fun executeImageApiRequest(source: UUID): List<LabelPair> {
        val connection = URL(url).openConnection() as HttpURLConnection

        val minio = Config().minio

        val json = LandmarksRequest(
            data = "${minio.url}/${MinioConfig.ASSETS_BUCKET}/$source",
            last = true, // as it is an image
            additionalProp1 = emptyMap<String, Any>() // Replace with the appropriate type and value
        )

        val jsonString = """
        {
          "data": "${json.data}",
          "last": ${json.last},
          "additionalProp1": ${json.additionalProp1}
        }
        """.trimIndent()

        try {
            // Set up the connection for a POST request
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")

            // Write the JSON body to the output stream
            val outputStream: OutputStream = connection.outputStream
            val writer = BufferedWriter(OutputStreamWriter(outputStream))
            writer.write(jsonString)
            writer.flush()
            writer.close()
            outputStream.close()

            // Get the response code and handle the response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read and parse the response JSON
                val inputStream = BufferedReader(InputStreamReader(connection.inputStream))
                val responseJson = inputStream.readText()
                inputStream.close()
                val labelPairsType = object : TypeToken<List<LabelPair>>() {}.type
                return Gson().fromJson(responseJson, labelPairsType)
            } else {
                // Handle non-200 responses
                println("Request failed with status code: $responseCode")
                // Optionally, parse the error response if available
                val errorResponseJson = BufferedReader(InputStreamReader(connection.errorStream)).readText()
                println("Error response: $errorResponseJson")
                println("Request body: $jsonString")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // TODO: Handle exceptions as needed
        } finally {
            connection.disconnect()
        }
        return emptyList()
    }

    data class LabelPair(
        val label: String,
        val confidence: Float
    )

}