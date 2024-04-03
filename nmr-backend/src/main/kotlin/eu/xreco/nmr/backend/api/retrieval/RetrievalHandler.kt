package eu.xreco.nmr.backend.api.retrieval

import eu.xreco.nmr.backend.api.Retrieval
import eu.xreco.nmr.backend.model.api.retrieval.*
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import eu.xreco.nmr.backend.model.api.status.ErrorStatusException
import io.javalin.http.Context
import io.javalin.openapi.*
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionServer
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.query.model.api.InformationNeedDescription
import org.vitrivr.engine.query.model.api.input.RetrievableIdInputData
import org.vitrivr.engine.query.model.api.input.TextInputData
import org.vitrivr.engine.query.model.api.operator.RetrieverDescription
import org.vitrivr.engine.query.model.api.operator.TransformerDescription
import org.vitrivr.engine.query.parsing.QueryParser
import java.util.*
import kotlin.FloatArray
import kotlin.Int
import kotlin.String

@OpenApi(
    summary = "Returns the descriptor for the given media resource.",
    path = "/api/retrieval/lookup/{field}/{retrievableId}",
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
    val fieldName = context.pathParam("field")
    val retrievableId = UUID.fromString(context.pathParam("retrievableId"))

    /* Extract field and return it. */
    val field = schema[fieldName] ?: throw ErrorStatusException(404, "Could not find field '${fieldName}' in schema ${schema.name}.")
    val reader = field.getReader()

    /* Extract descriptor and return it. */
    when (val descriptor = reader.getBy(retrievableId, "retrievableId")) {
        is FloatVectorDescriptor -> context.json(descriptor.vector)
        is StringDescriptor -> context.json(Text(descriptor.value.value))
        else -> throw ErrorStatusException(400, "Unsupported feature type.")
    }
}

@OpenApi(
    summary = "Get type of given retrievable.",
    path = "/api/retrieval/type/{retrievableId}",
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
    val type = reader[retrievableId]?.type?.let { type ->
        type.split(":").getOrNull(1)?.uppercase()?.let { MediaType.valueOf(it) } ?: MediaType.UNKNOWN
    } ?: throw ErrorStatusException(404, "Failed to find retrievable for ID $retrievableId.")
    context.json(type)
}


@OpenApi(
    summary = "Issues a fulltext query.",
    path = "/api/retrieval/text/{field}/{text}/{pageSize}",
    tags = [Retrieval],
    operationId = "getSearchFulltext",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "field", type = String::class, description = "Name of the field to query (i.e., the feature to compare).", required = true),
        OpenApiParam(name = "text", type = String::class, description = "Text to search for.", required = true),
        OpenApiParam(name = "pageSize", type = Int::class, description = "Number of results requested.", required = true),
    ],
    queryParams = [
        OpenApiParam(name = "attribute", type = String::class, description = "Name of the attribute in case of a struct field.", required = false),
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
    val attribute = context.queryParam("attribute")

    /* Construct and parse query. */
    val query = InformationNeedDescription(
        inputs = mapOf("text" to TextInputData(text)),
        operations = mapOf(
            "retriever" to RetrieverDescription(input = "text", field = fieldName),
            "time" to TransformerDescription("FieldLookup", input = "retriever", properties = mapOf("field" to "time", "keys" to "start,end")),
            "metadata1" to TransformerDescription("FieldLookup", input = "time", properties = mapOf("field" to "metadata", "keys" to "title,description,license")),
            "relations" to TransformerDescription("RelationExpander", input = "metadata1", properties = mapOf("outgoing" to "partOf")),
            "metadata2" to TransformerDescription("ObjectFieldLookup", input = "relations", properties = mapOf("field" to "metadata", "keys" to "title,description,license")),
        ),
        output = "metadata2",
        context = QueryContext(global = mapOf("limit" to pageSize.toString()), local = attribute?.let { mapOf(fieldName to mapOf("attribute" to attribute)) } ?: emptyMap())
    )

    val retriever = QueryParser(schema).parse(query)

    /* Execute query and return results. */
    val results = RetrievalResult(items = executor.query(retriever).map(ScoredResult::from))
    context.json(results)
}

@OpenApi(
    summary = "Issues a similarity query based on a provided retrievable ID (i.e., finds entries considered similar given the example).",
    path = "/api/retrieval/similarity/{field}/{retrievableId}/{pageSize}",
    tags = [Retrieval],
    operationId = "getSearchSimilar",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "field", type = String::class, description = "Name of the field to query (i.e., the feature to compare).", required = true),
        OpenApiParam(name = "retrievableId", type = String::class, description = "ID of the retrievable to find similar entries for.", required = true),
        OpenApiParam(name = "pageSize", type = Int::class, description = "Number of results requested.", required = true),
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
    val retrievableId = context.pathParam("retrievableId")
    val pageSize = context.pathParam("pageSize").toInt()

    /* Construct and parse query. */
    val query = InformationNeedDescription(
        inputs = mapOf("feature" to RetrievableIdInputData(retrievableId)),
        operations = mapOf(
            "retriever" to RetrieverDescription(input = "feature", field = fieldName),
            "time" to TransformerDescription("FieldLookup", input = "retriever", properties = mapOf("field" to "time", "keys" to "start,end")),
            "metadata1" to TransformerDescription("FieldLookup", input = "time", properties = mapOf("field" to "metadata", "keys" to "title,description,license")),
            "relations" to TransformerDescription("RelationExpander", input = "metadata1", properties = mapOf("outgoing" to "partOf")),
            "metadata2" to TransformerDescription("ObjectFieldLookup", input = "relations", properties = mapOf("field" to "metadata", "keys" to "title,description,license")),
        ),
        output = "metadata2",
        context = QueryContext(global = mapOf("limit" to pageSize.toString()))
    )
    val retriever = QueryParser(schema).parse(query)

    /* Execute query and return results. */
    val results = RetrievalResult(items = executor.query(retriever).map(ScoredResult::from))
    context.json(results)
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
