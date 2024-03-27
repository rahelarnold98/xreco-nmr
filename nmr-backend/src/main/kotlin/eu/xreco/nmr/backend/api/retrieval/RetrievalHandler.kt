package eu.xreco.nmr.backend.api.retrieval

import eu.xreco.nmr.backend.api.Retrieval
import eu.xreco.nmr.backend.model.api.retrieval.*
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import eu.xreco.nmr.backend.model.api.status.ErrorStatusException
import io.javalin.http.Context
import io.javalin.openapi.*
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionServer
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.attributes.DescriptorAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.query.model.api.InformationNeedDescription
import org.vitrivr.engine.query.model.api.input.TextInputData
import org.vitrivr.engine.query.model.api.input.VectorInputData
import org.vitrivr.engine.query.model.api.operator.RetrieverDescription
import org.vitrivr.engine.query.model.api.operator.TransformerDescription
import org.vitrivr.engine.query.parsing.QueryParser
import java.math.BigDecimal
import java.util.*
import kotlin.FloatArray
import kotlin.Int
import kotlin.String

@OpenApi(
    summary = "Returns the descriptor for the given media resource.",
    path = "/api/retrieval/lookup/{retrievableId}/{entity}",
    tags = [Retrieval],
    operationId = "getDescriptor",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "retrievableId", type = String::class, description = "ID of retrievable for which to return the descriptor.", required = true),
        OpenApiParam(name = "field", type = String::class, description = "Descriptor to retrieve data for.", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(Text::class)]),
        OpenApiResponse("200", [OpenApiContent(FloatArray::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun lookup(context: Context, schema: Schema) {
    /* Extract necessary parameters. */
    val resourceId = UUID.fromString(context.pathParam("resourceId"))
    val fieldName = context.pathParam("field")

    /* Extract field and return it. */
    val field = schema[fieldName] ?: throw ErrorStatusException(404, "Could not find field '${fieldName}' in schema ${schema.name}.")
    val reader = field.getReader()

    /* Extract descriptor and return it. */
    when (val descriptor = reader[resourceId]) {
        is FloatVectorDescriptor -> context.json(descriptor.vector)
        is StringDescriptor -> context.json(Text(descriptor.value.value))
        else -> throw ErrorStatusException(400, "Unsupported feature type.")
    }
}

@OpenApi(
    summary = "Get type of given retrievable.",
    path = "/api/retrieval/type/{mediaResourceId}",
    tags = [Retrieval],
    operationId = "getRetrievableType",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "retrievableId", type = String::class, description = "ID of retrievable which to lookup the (media) type for.", required = true)
     ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(MediaType::class)]),
        OpenApiResponse("200", [OpenApiContent(FloatArray::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun type(context: Context, schema: Schema) {
    /* Extract necessary parameters. */
    val retrievableId = UUID.fromString(context.pathParam("retrievableId"))

    /* Extract field and return it. */
    val reader = schema.connection.getRetrievableReader()
    val type = reader[retrievableId]?.type?.let { MediaType.valueOf(it) } ?: throw ErrorStatusException(404, "Failed to find retrievable for ID $retrievableId.")
    context.json(type)
}


@OpenApi(
    summary = "Issues a fulltext query.",
    path = "/api/retrieval/text/{field}/{text}/{pageSize}/{page}",
    tags = [Retrieval],
    operationId = "getSearchFulltext",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "field", type = String::class, description = "Name of the field to query.", required = true),
        OpenApiParam(name = "text", type = String::class, description = "Text to search for.", required = true),
        OpenApiParam(name = "pageSize", type = Int::class, description = "Page size of a single results page.", required = true),
        OpenApiParam(name = "page", type = Int::class, description = "Requested page of results. Zero-based index (first page = 0).", required = true)
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(RetrievalResult::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)])
    ]
)

fun getFulltext(context: Context, schema: Schema, executor: ExecutionServer) {
    /* Extract necessary parameters. */
    val fieldName = context.pathParam("field")
    val text = context.pathParam("text")
    val pageSize = context.pathParam("pageSize").toInt()
    val page = context.pathParam("page").toInt()

    // TODO: Number of pages. */

    /* Construct and parse query. */
    val query = InformationNeedDescription(
        inputs = mapOf("text" to TextInputData(text)),
        operations = mapOf(
            "fulltext" to RetrieverDescription(input = "text", field = fieldName),
            "time" to TransformerDescription("FieldLookup", input = "fulltext", properties = mapOf("field" to "time", "keys" to "start,end")),
            "relations" to TransformerDescription("RelationExpander", input = "time", properties = mapOf("outgoing" to "partOf"))
        ),
        output = "relations"
    )
    val retriever = QueryParser(schema).parse(query)

    /* Execute query and return results. */
    context.json(RetrievalResult(page, pageSize, count = 0L, items = executor.query(retriever).map { retrieved ->
        val temporal = retrieved.attributes.filterIsInstance<DescriptorAttribute>().firstOrNull { it.descriptor is TemporalMetadataDescriptor }?.descriptor as? TemporalMetadataDescriptor
        val startSeconds = temporal?.startNs?.let { BigDecimal(it.value).divide(BigDecimal("10e9")) }
        val endSeconds = temporal?.endNs?.let { BigDecimal(it.value).divide(BigDecimal("10e9")) }
        ScoredMediaItem(
            retrieved.id.toString(),
            retrieved.attributes.filterIsInstance<ScoreAttribute>().first().score.toDouble(),
            start = startSeconds?.toFloat(),
            end = endSeconds?.toFloat(),
        )
    }))

}

@OpenApi(
    summary = "Issues a similarity query based on a provided retrievable ID.",
    path = "/api/retrieval/similarity/{entity}/{retrievableId}/{pageSize}/{page}",
    tags = [Retrieval],
    operationId = "getSearchSimilar",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "retrievableId", type = String::class, description = "ID of the retrievable to find similar entries for.", required = true),
        OpenApiParam(name = "field", type = String::class, description = "Name of the field to query.", required = true),
        OpenApiParam(name = "timestamp", type = Long::class, description = "The exact timestamp to find similar entries for.", required = true),
        OpenApiParam(name = "pageSize", type = Int::class, description = "Page size of results", required = true),
        OpenApiParam(name = "page", type = Int::class, description = "Request page of results", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(RetrievalResult::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun getSimilar(context: Context, schema: Schema, executor: ExecutionServer) {/* TODO implement*/
    /* Extract necessary parameters. */
    val fieldName = context.pathParam("field")
    val retrievableId = UUID.fromString("retrievableId")
    val pageSize = context.pathParam("pageSize").toInt()
    val page = context.pathParam("page").toInt()

    /* Fetch descriptor for field. */
    val field = schema[fieldName] ?: throw ErrorStatusException(404, "Unknown field '${fieldName}' for schema ${schema.name}.")
    val descriptor = field.getReader().getBy(retrievableId, "retrievableId") ?: throw ErrorStatusException(404, "Could not find descriptor for field '${fieldName}' and retrievable ID ${retrievableId}.")
    if (descriptor !is FloatVectorDescriptor) {
        throw ErrorStatusException(404, "Could not find valid descriptor for field '${fieldName}' and retrievable ID ${retrievableId}.")
    }

    /* Construct and parse query. */
    val query = InformationNeedDescription(
        inputs = mapOf("feature" to VectorInputData(descriptor.vector.map { it.value })),
        operations = mapOf(
            "retriever" to RetrieverDescription(input = "feature", field = fieldName),
            "time" to TransformerDescription("FieldLookup", input = "fulltext", properties = mapOf("field" to "time", "keys" to "start,end")),
            "relations" to TransformerDescription("RelationExpander", input = "time", properties = mapOf("outgoing" to "partOf"))
        ),
        output = "relations"
    )
    val retriever = QueryParser(schema).parse(query)

    /* Execute query and return results. */
    context.json(RetrievalResult(page, pageSize, count = 0L, items = executor.query(retriever).map { retrieved ->
        val temporal = retrieved.attributes.filterIsInstance<DescriptorAttribute>().firstOrNull { it.descriptor is TemporalMetadataDescriptor }?.descriptor as? TemporalMetadataDescriptor
        val startSeconds = temporal?.startNs?.let { BigDecimal(it.value).divide(BigDecimal("10e9")) }
        val endSeconds = temporal?.endNs?.let { BigDecimal(it.value).divide(BigDecimal("10e9")) }
        ScoredMediaItem(
            retrieved.id.toString(),
            retrieved.attributes.filterIsInstance<ScoreAttribute>().first().score.toDouble(),
            start = startSeconds?.toFloat(),
            end = endSeconds?.toFloat(),
        )
    }))
}

@OpenApi(
    summary = "Apply a filter to a collection",
    path = "/api/retrieval/filter/{condition}/{pageSize}/{page}",
    tags = [Retrieval],
    operationId = "getFilterQuery",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "condition", type = String::class, description = "Condition to filter collection", required = true),
        OpenApiParam(name = "pageSize", type = Int::class, description = "Page size of results", required = true),
        OpenApiParam(name = "page", type = Int::class, description = "Request page of results", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(RetrievalResult::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun filter(context: Context) {/* TODO implement*/
    // TODO check if this is still needed
}
