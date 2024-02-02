package eu.xreco.nmr.backend.features.certh


import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.vitrivr.engine.base.features.external.ExternalAnalyser
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.Model3DContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
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
        val list: List<Float> = executeApiRequest("http://160.40.53.193:8000/3D model retrieval/extract/?usecase=Tourism", content)
        return FloatVectorDescriptor(UUID.randomUUID(), null, list, true)
    }

    // Size and list for prototypical descriptor
    // TODO change value as soon as the actual size is known
    val size = 512
    val featureList = List(size) { 0.0f }

    /**
     * Requests the CERTH feature descriptor for the given [ContentElement].
     *
     * @param content The [ContentElement] for which to request the CERTH feature descriptor.
     * @return A list of CERTH feature descriptors.
     */
    fun requestDescriptor(content: ContentElement<*>): List<Float> {
        val c = when (content) {
            is Model3DContent -> content.content
            else -> throw IllegalArgumentException("Unsupported content type")
        }
        return executeApiRequest("http://160.40.53.193:8000/3D model retrieval/extract/?usecase=Tourism", content)

    }

    private fun executeApiRequest(url: String, content: Model3DContent): List<Float> {
        // Create an HttpURLConnection
        val connection = URL(url).openConnection() as HttpURLConnection

        try {
            // Set up the connection for a POST request
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            // Write the request body to the output stream
            val outputStream: OutputStream = connection.outputStream
            val writer = BufferedWriter(OutputStreamWriter(outputStream))
            writer.write("data=$content") // TODO change to work with irl of min.io
            writer.flush()
            writer.close()
            outputStream.close()

            // Get the response code (optional, but useful for error handling)
            val responseCode = connection.responseCode

            // Read the response as a JSON string
            val responseJson = if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = BufferedReader(InputStreamReader(connection.inputStream))
                val response = inputStream.readLine()
                inputStream.close()
                response
            } else {
                null
            }

            // Parse the JSON string to List<Float> using Gson
            return if (responseJson != null) {
                try {
                    Json.decodeFromString(ListSerializer(Float.serializer()), responseJson)
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
            connection.disconnect()
        }
        return emptyList()
    }

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [CERTH].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [FloatVectorDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>) = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), this.featureList, true)


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
        require(field.analyser == this) { "" }
        return CERTHExtractor(input, field, persisting, this)
    }




    /*override fun newRetrieverForContent(
        field: Schema.Field<Model3DContent, FloatVectorDescriptor>,
        content: Collection<Model3DContent>,
        context: QueryContext
    ): Retriever<Model3DContent, FloatVectorDescriptor> {
        TODO("Not yet implemented")
    }*/


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
    override fun newRetrieverForContent(
        field: Schema.Field<Model3DContent, FloatVectorDescriptor>,
        content: Collection<Model3DContent>,
        context: QueryContext
    ): Retriever<Model3DContent, FloatVectorDescriptor> {
        val host = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT
        return this.newRetrieverForDescriptors(field, content.map { this.analyse(it, host) }, context)
    }

    /**
     * Generates and returns a new [Retriever] instance for this [CERTH].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [FloatVectorDescriptor] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForDescriptors(
        field: Schema.Field<Model3DContent, FloatVectorDescriptor>,
        descriptors: Collection<FloatVectorDescriptor>,
        context: QueryContext
    ): Retriever<Model3DContent, FloatVectorDescriptor> {
        require(field.analyser == this) { }
        return CERTHRetriever(field, descriptors.first(), context)
    }
}
