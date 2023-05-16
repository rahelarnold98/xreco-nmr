package eu.xreco.nmr.backend.api.retrieval

import eu.xreco.nmr.backend.api.Retrieval
import eu.xreco.nmr.backend.model.api.retrieval.*
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import io.javalin.http.Context
import io.javalin.openapi.*

@OpenApi(
    summary = "Get attributes of given element",
    path = "/api/retrieval/{elementId}/",
    tags = [Retrieval],
    operationId = "getAttributesOfElement",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(
            name = "elementId", type = String::class, "Id of element which attributes will be returned", required = true
        ),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(Media::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun retrieve(context: Context) {/* TODO implement*/
}

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
        OpenApiResponse("200", [OpenApiContent(FloatVector::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun lookup(context: Context) {/* TODO implement*/
}

@OpenApi(
    summary = "Create a fulltext query",
    path = "/api/retrieval/text/{text}/{entity}/{pageSize}/{page}",
    tags = [Retrieval],
    operationId = "getFullTextQuery",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "text", type = String::class, description = "Text to query", required = true),
        OpenApiParam(name = "entity", type = String::class, description = "Entity to query", required = true),
        OpenApiParam(name = "pageSize", type = Int::class, description = "Page size of results", required = true),
        OpenApiParam(name = "page", type = Int::class, description = "Requested page of results", required = true),
    ],
    responses = [
        OpenApiResponse(
            "200", [OpenApiContent(MediaList::class)]
        ), // TODO check what is the order --> could be a [ScoredMediaList]
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)

fun fullText(context: Context) {/* TODO implement*/
}

@OpenApi(
    summary = "Create a similarity query based on a given element id",
    path = "/api/retrieval/similarity/{elementId}/{pageSize}/{page}",
    tags = [Retrieval],
    operationId = "getSimilarityQuery",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(
            name = "elementId",
            type = String::class,
            description = "Id of element to get most similar of",
            required = true
        ),
        OpenApiParam(name = "pageSize", type = Int::class, description = "Page size of results", required = true),
        OpenApiParam(name = "page", type = Int::class, description = "Request page of results", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(SimilarityMedia::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun similarity(context: Context) {/* TODO implement*/
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
        OpenApiResponse("200", [OpenApiContent(MediaList::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun filter(context: Context) {/* TODO implement*/
}
