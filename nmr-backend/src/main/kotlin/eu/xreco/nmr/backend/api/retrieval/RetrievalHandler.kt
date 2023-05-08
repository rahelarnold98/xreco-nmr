package eu.xreco.nmr.backend.api.retrieval

import eu.xreco.nmr.backend.api.Retrieval
import io.javalin.http.Context
import io.javalin.openapi.HttpMethod
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiParam

@OpenApi(
    summary = "Get object of given element",
    path = "/api/retrieval/{elementId}/{attributes}",
    tags = [Retrieval],
    operationId = "getAttributesOfElement",
    methods = [HttpMethod.GET],
    pathParams =
        [
            OpenApiParam("elementId", String::class, "Id of element which will be returned"),
            OpenApiParam("attributes", String::class, "List of attributes to be returned"),
        ],
    /* TODO add Responses*/
)
fun retrieve(context: Context) {
  /* TODO implement*/
}

@OpenApi(
    summary = "Get entity of given element",
    path = "/api/retrieval/{elementId}/{entity}",
    tags = [Retrieval],
    operationId = "getValueOfElement",
    methods = [HttpMethod.GET],
    pathParams =
        [
            OpenApiParam("elementId", String::class, "Id of element which will be returned"),
            OpenApiParam("entity", String::class, "Descriptor to retrieve data"),

        ],
    /* TODO add Responses*/
)
fun lookup(context: Context) {
  /* TODO implement*/
}

@OpenApi(
    summary = "Create a fulltext query",
    path = "/api/retrieval/text/{text}/{pageSize}/{page}",
    tags = [Retrieval],
    operationId = "getFullTextQuery",
    methods = [HttpMethod.GET],
    pathParams =
        [
            OpenApiParam("text", String::class, "Text to query"),
            OpenApiParam("pageSize", Int::class, "Page size of results"),
            OpenApiParam("page", Int::class, "Requested page of results"),
        ],
    /* TODO add Responses*/
)
fun fullText(context: Context) {
  /* TODO implement*/
}

@OpenApi(
    summary = "Create a similarity query based on a given element id",
    path = "/api/retrieval/similarity/{elementId}/{pageSize}/{page}",
    tags = [Retrieval],
    operationId = "getSimilarityQuery",
    methods = [HttpMethod.GET],
    pathParams =
        [
            OpenApiParam("elementId", String::class, "Id of element to get most similar of"),
            OpenApiParam("pageSize", Int::class, "Page size of results"),
            OpenApiParam("page", Int::class, "Request page of results"),
        ],
    /* TODO add Responses*/
)
fun similarity(context: Context) {
  /* TODO implement*/
}

@OpenApi(
    summary = "Apply a filter to a collection",
    path = "/api/retrieval/filter/{condition}/{pageSize}/{page}",
    tags = [Retrieval],
    operationId = "getFilterQuery",
    methods = [HttpMethod.GET],
    pathParams =
        [
            OpenApiParam("condition", String::class, "Condition to filter collection"),
            OpenApiParam("pageSize", Int::class, "Page size of results"),
            OpenApiParam("page", Int::class, "Request page of results"),
        ],
    /* TODO add Responses*/
)
fun filter(context: Context) {
  /* TODO implement*/
}
