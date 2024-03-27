package eu.xreco.nmr.backend.features.certh


import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.vitrivr.engine.base.features.external.ExternalAnalyser
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.Model3DContent
import org.vitrivr.engine.core.model.descriptor.vector.IntVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.basics.Distance
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
 * Implementation of the [CERTH] [ExternalAnalyser], which derives the CERTH feature from an [Model3DContent] as [IntVectorDescriptor].
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class CERTH : ExternalAnalyser<Model3DContent, IntVectorDescriptor>() {

    companion object {
        // Size of descriptor
        const val VECTOR_SIZE = 32
    }

    override val contentClasses: Set<KClass<out ContentElement<*>>> = setOf(Model3DContent::class)
    override val descriptorClass = IntVectorDescriptor::class

    /**
     * Requests the CERTH feature descriptor for the given [ContentElement].
     *
     * @param content The [ContentElement] for which to request the CERTH feature descriptor.
     * @param hostname The hostname of the external feature descriptor service.
     * @return A list of CERTH feature descriptors.
     */
    override fun analyse(content: Model3DContent, hostname: String): IntVectorDescriptor {
        val list: List<Value.Int> = executeApiRequest("http://160.40.53.193:8000/3D model retrieval/extract/?usecase=Tourism", content)
        return IntVectorDescriptor(UUID.randomUUID(), null, list, true)
    }


    /**
     * Requests the CERTH feature descriptor for the given [ContentElement].
     *
     * @param content The [ContentElement] for which to request the CERTH feature descriptor.
     * @return A list of CERTH feature descriptors.
     */
    fun requestDescriptor(content: ContentElement<*>): List<Value.Int> {
        require(content is Model3DContent) { "Unsupported content type." }
        return executeApiRequest("http://160.40.53.193:8000/3D model retrieval/extract/?usecase=Tourism", content)
    }

    /**
     *
     */
    private fun executeApiRequest(url: String, content: Model3DContent): List<Value.Int> {
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

            // Parse the JSON string to List<Int> using Gson
            return if (responseJson != null) {
                try {
                    Json.decodeFromString(ListSerializer(Int.serializer()), responseJson).map { Value.Int(it) }
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
     * Generates a prototypical [IntVectorDescriptor] for this [CERTH].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [IntVectorDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>) = IntVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), List(VECTOR_SIZE) { Value.Int(0) }, true)

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
    override fun newExtractor(field: Schema.Field<Model3DContent, IntVectorDescriptor>, input: Operator<Retrievable>, context: IndexContext, persisting: Boolean, parameters: Map<String, Any>): Extractor<Model3DContent, IntVectorDescriptor> {
        require(field.analyser == this) {  "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
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
    override fun newRetrieverForContent(field: Schema.Field<Model3DContent, IntVectorDescriptor>, content: Collection<Model3DContent>, context: QueryContext): Retriever<Model3DContent, IntVectorDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }

        /* Extract URL for external content transformation service. */
        val host = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT

        /* Extract parameters and construct query. */
        val descriptor = content.map { this.analyse(it, host) }.first()
        val k = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        val returnDescriptor = context.getProperty(field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false
        val query = ProximityQuery<Value.Int>(value = descriptor.vector, k = k.toLong(), fetchVector = returnDescriptor, distance = Distance.COSINE)

        /* Construct retriever. */
        return this.newRetrieverForQuery(field, query, context)
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
    override fun newRetrieverForQuery(field: Schema.Field<Model3DContent, IntVectorDescriptor>, query: Query, context: QueryContext): Retriever<Model3DContent, IntVectorDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        require(query is ProximityQuery<*> && query.value.first() is Value.Int) { "" }

        @Suppress("UNCHECKED_CAST")
        return CERTHRetriever(field, query as ProximityQuery<Value.Int>, context)
    }
}
