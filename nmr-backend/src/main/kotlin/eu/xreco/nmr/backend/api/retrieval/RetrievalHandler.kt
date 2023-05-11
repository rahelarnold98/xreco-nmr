package eu.xreco.nmr.backend.api.retrieval

import eu.xreco.nmr.backend.api.Retrieval
import eu.xreco.nmr.backend.config.Config
import eu.xreco.nmr.backend.model.status.ErrorStatus
import eu.xreco.nmr.backend.model.status.ErrorStatusException
import eu.xreco.nmr.backend.model.status.SuccessStatus
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.javalin.http.Context
import io.javalin.openapi.*
import org.vitrivr.cottontail.client.SimpleClient
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dql.Query
import java.util.*

@OpenApi(
    summary = "Get object of given element",
    path = "/api/retrieval/{elementId}/{attributes}",
    tags = [Retrieval],
    operationId = "getAttributesOfElement",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "elementId", type = String::class, "Id of element which will be returned", required = true),
        OpenApiParam(name = "attributes", type = String::class, "List of attributes to be returned", required = true),
    ],
    /* TODO add Responses*/
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
        OpenApiParam(name = "elementId", type = String::class, description = "Id of element which will be returned", required = true),
        OpenApiParam(name = "entity", type = String::class, description = "Descriptor to retrieve data", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(SuccessStatus::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun lookup(context: Context, client: SimpleClient, config: Config) {
    val elementId = context.pathParam("elementId")
    val entity = context.pathParam("entity")
    try {
        // prepare query
        val query = Query("${config.database.schemaName}.${entity}").where(Compare("mediaResourceId", "=", elementId))
            .select("label")

        // execute query
        val results = client.query(query)

        // save results as LinkedList
        val list = LinkedList<String>()
        results.forEach { t ->
            list.add(t.asString("label")!!)
        }
        context.json(list)
    } catch (e: StatusRuntimeException) {
        when (e.status.code) {
            Status.Code.INTERNAL -> {
                throw ErrorStatusException(
                    400,
                    "The requested element '${config.database.schemaName}.${entity}.${elementId} could not be found."
                )
            }

            Status.Code.NOT_FOUND -> throw ErrorStatusException(
                404, "The requested element '${config.database.schemaName}.${entity}.${elementId} could not be found."
            )

            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Connection is currently not available.")

            else -> {
                throw e.message?.let { ErrorStatusException(400, it) }!!
            }
        }
    }
}

@OpenApi(
    summary = "Create a fulltext query",
    path = "/api/retrieval/text/{text}/{pageSize}/{page}",
    tags = [Retrieval],
    operationId = "getFullTextQuery",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "text", type = String::class, description = "Text to query", required = true),
        OpenApiParam(name = "pageSize", type = Int::class, description = "Page size of results", required = true),
        OpenApiParam(name = "page", type = Int::class, description = "Requested page of results", required = true),
    ],
    /* TODO add Responses*/
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
        OpenApiParam(name = "elementId", type = String::class, description = "Id of element to get most similar of", required = true),
        OpenApiParam(name = "pageSize", type = Int::class, description = "Page size of results", required = true),
        OpenApiParam(name = "page", type = Int::class, description = "Request page of results", required = true),
    ],
    /* TODO add Responses*/
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
        OpenApiParam(name = "condition", type = String::class, description = "Condition to filter collection", required = true),
        OpenApiParam(name = "pageSize", type = Int::class, description = "Page size of results", required = true),
        OpenApiParam(name = "page", type = Int::class, description = "Request page of results", required = true),
    ],
    /* TODO add Responses*/
)
fun filter(context: Context) {/* TODO implement*/
}
