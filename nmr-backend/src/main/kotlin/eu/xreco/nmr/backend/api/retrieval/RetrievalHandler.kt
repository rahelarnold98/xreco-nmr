package eu.xreco.nmr.backend.api.retrieval

import eu.xreco.nmr.backend.api.Retrieval
import eu.xreco.nmr.backend.model.api.retrieval.*
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import io.javalin.http.Context
import io.javalin.openapi.*
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionServer
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import org.vitrivr.engine.query.execution.RetrievalRuntime
import kotlin.FloatArray
import kotlin.Int
import kotlin.String

@OpenApi(
    summary = "Get entity of given element",
    path = "/api/retrieval/lookup/{elementId}/{entity}",
    tags = [Retrieval],
    operationId = "getValueOfElement",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(
            name = "elementId",
            type = String::class,
            description = "Id of element which will be returned",
            required = true
        ),
        OpenApiParam(
            name = "entity", type = String::class, description = "Descriptor to retrieve data", required = true
        ),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(Text::class)]),
        OpenApiResponse("200", [OpenApiContent(FloatArray::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun lookup(context: Context, manager: SchemaManager, executor: ExecutionServer) {/* TODO implement*/
   TODO()
}

@OpenApi(
    summary = "Get type of given element",
    path = "/api/retrieval/type/{elementId}",
    tags = [Retrieval],
    operationId = "getTypeOfElement",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(
            name = "elementId",
            type = String::class,
            description = "Id of element to retrieve type of",
            required = true
        )
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(MediaType::class)]),
        OpenApiResponse("200", [OpenApiContent(FloatArray::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun type(context: Context, manager: SchemaManager, executor: ExecutionServer) {
    TODO()
}


@OpenApi(
    summary = "Issues a fulltext query.",
    path = "/api/retrieval/text/{entity}/{text}/{pageSize}/{page}",
    tags = [Retrieval],
    operationId = "getSearchFulltext",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "entity", type = String::class, description = "Name of the entity to query.", required = true),
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

fun getFulltext(context: Context, manager: SchemaManager, executor: ExecutionServer) {
    TODO()
}

@OpenApi(
    summary = "Issues a similarity query based on a provided media resource id.",
    path = "/api/retrieval/similarity/{entity}/{mediaResourceId}/{timestamp}/{pageSize}/{page}",
    tags = [Retrieval],
    operationId = "getSearchSimilar",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "entity", type = String::class, description = "Name of the entity to query.", required = true),
        OpenApiParam(name = "mediaResourceId", type = String::class, description = "ID of the media resource to find similar entries for.", required = true),
        OpenApiParam(name = "timestamp", type = Long::class, description = "The exact timestamp of to find similar entries for.", required = true),
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
fun getSimilar(context: Context, manager: SchemaManager, executor: ExecutionServer) {/* TODO implement*/
    TODO()
}

@OpenApi(
    summary = "Apply a filter to a collection",
    path = "/api/retrieval/filter/{condition}/{pageSize}/{page}",
    tags = [Retrieval],
    operationId = "getFilterQuery",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(
            name = "condition", type = String::class, description = "Condition to filter collection", required = true
        ),
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
