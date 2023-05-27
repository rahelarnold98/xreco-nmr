package eu.xreco.nmr.backend.api.basket

import eu.xreco.nmr.backend.api.Basket
import eu.xreco.nmr.backend.config.Config
import eu.xreco.nmr.backend.model.api.basket.BasketList
import eu.xreco.nmr.backend.model.api.basket.BasketPreview
import eu.xreco.nmr.backend.model.api.retrieval.Text
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import eu.xreco.nmr.backend.model.api.status.ErrorStatusException
import eu.xreco.nmr.backend.model.api.status.SuccessStatus
import eu.xreco.nmr.backend.model.database.basket.BasketElement
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.javalin.http.Context
import io.javalin.openapi.*
import org.vitrivr.cottontail.client.SimpleClient
import org.vitrivr.cottontail.client.language.basics.predicate.And
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dml.Delete
import org.vitrivr.cottontail.client.language.dml.Insert
import org.vitrivr.cottontail.client.language.dql.Query
import org.vitrivr.cottontail.core.values.StringValue
import java.util.*

@OpenApi(
    summary = "Creates a new basket.",
    path = "/api/basket/{basketName}",
    tags = [Basket],
    operationId = "postBasket",
    methods = [HttpMethod.POST],
    pathParams = [
        OpenApiParam(name = "basketName", type = String::class, "Name of basket which gets deleted", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(SuccessStatus::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]/* TODO add RequestBody*/
)
fun createBasket(context: Context, client: SimpleClient, config: Config) {
    val name = context.pathParam("basketName")
    try {
        val query = Insert("${config.database.schemaName}.${"baskets"}").value("name", StringValue(name))
        client.insert(query).close()
        context.json(SuccessStatus("Successfully inserted $name into ${config.database.schemaName}.${"baskets"}"))

    } catch (e: StatusRuntimeException) {
        when (e.status.code) {
            Status.Code.INTERNAL -> {
                throw ErrorStatusException(
                    400, "The requested table '${config.database.schemaName}.${"baskets"} could not be found."
                )
            }

            Status.Code.NOT_FOUND -> throw ErrorStatusException(
                404, "The requested table '${config.database.schemaName}.${"baskets"} could not be found."
            )

            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Connection is currently not available.")

            else -> {
                throw e.message?.let { ErrorStatusException(400, it) }!!
            }
        }
    }
}

@OpenApi(
    summary = "Deletes a specific basket.",
    path = "/api/basket/{basketId}",
    tags = [Basket],
    operationId = "deleteBasket",
    methods = [HttpMethod.DELETE],
    pathParams = [
        OpenApiParam(name = "basketId", type = Int::class, "ID of basket that should be deleted.", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(SuccessStatus::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)])
    ]
)
fun deleteBasket(context: Context, client: SimpleClient, config: Config) {
    val basketId = context.pathParam("basketId").toIntOrNull() ?: throw ErrorStatusException(400, "Valid basket ID required.")
    val txId = client.begin(false)
    try {
        client.delete(Delete("${config.database.schemaName}.${eu.xreco.nmr.backend.model.database.basket.Basket.name}").where(Compare("basketId", "=", basketId)).txId(txId)).close()
        client.delete(Delete("${config.database.schemaName}.${BasketElement.name}").where(Compare("basketId", "=", basketId)).txId(txId)).close()
        client.commit(txId)
        context.json(SuccessStatus("Successfully deleted basked $basketId."))
    } catch (e: StatusRuntimeException) {
        client.rollback(txId)
        when (e.status.code) {
            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Database connection is currently not available.")
            else -> throw ErrorStatusException(500, e.message ?: "Unknown error")
        }
    }
}

@OpenApi(
    summary = "Adds a specific element to a specific basket.",
    path = "/api/basket/{basketId}/{mediaResourceId}",
    tags = [Basket],
    operationId = "putToBasket",
    methods = [HttpMethod.PUT],
    pathParams = [
        OpenApiParam(name = "basketId", type = String::class, description = "ID of the basket to add element to.", required = true),
        OpenApiParam(name = "mediaResourceId", type = String::class, description = "ID of the media resource to add.", required = true)
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(SuccessStatus::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun addBasketElement(context: Context, client: SimpleClient, config: Config) {/* TODO implement*/
    val basketId = context.pathParam("basketId").toIntOrNull() ?: throw ErrorStatusException(400, "Valid basket ID required.")
    val mediaResourceId = context.pathParam("mediaResourceId")
    try {
        val query = Insert("${config.database.schemaName}.${BasketElement.name}").any("basketId", basketId).any("mediaResourceId", mediaResourceId)
        val inserted = client.insert(query).next().asLong(0)!!
        if (inserted > 0L) {
            context.json(SuccessStatus("Successfully inserted media resource $mediaResourceId into basket $basketId."))
        } else {
            context.json(SuccessStatus("Did not insert media resource  $mediaResourceId into basket $basketId."))
        }
    } catch (e: StatusRuntimeException) {
        when (e.status.code) {
            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Connection is currently not available.")
            else -> throw ErrorStatusException(400, e.message ?: "Unknown error!")
        }
    }
}

@OpenApi(
    summary = "Drops a specific element of a specific basket.",
    path = "/api/basketName/{basketId}/{mediaResourceId}",
    tags = [Basket],
    operationId = "deleteFromBasket",
    methods = [HttpMethod.DELETE],
    pathParams = [
        OpenApiParam(name = "basketId", type = String::class, description = "ID of the basket to remove element from.", required = true),
        OpenApiParam(name = "mediaResourceId", type = String::class, description = "ID of the media resource that will be added.", required = true)
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(BasketElement::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun dropBasketElement(context: Context, client: SimpleClient, config: Config) {/* TODO implement*/
    val basketId = context.pathParam("basketId").toIntOrNull() ?: throw ErrorStatusException(400, "Valid basket ID required.")
    val mediaResourceId = context.pathParam("mediaResourceId")
    try {
        val query = Delete("${config.database.schemaName}.${"basket_elements"}").where(
            And(Compare("mediaResourceId", "=", mediaResourceId), Compare("basketId", "=", basketId))
        )
        val affected = client.delete(query).next().asLong(0)!!
        if (affected >= 0L) {
            context.json(SuccessStatus("Successfully deleted $mediaResourceId from basket $basketId."))
        } else {
            context.json(SuccessStatus("No element removed from basket $basketId."))
        }
    } catch (e: StatusRuntimeException) {
        when (e.status.code) {
            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Connection is currently not available.")
            else -> throw ErrorStatusException(400, e.message ?: "Unknown error.")
        }
    }
}

@OpenApi(
    summary = "List all elements of a specific basket.",
    path = "/api/basket/{basketName}",
    tags = [Basket],
    operationId = "getElementsInBasket",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "basketName", type = String::class, description = "Name of basket", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(BasketElement::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun listElements(context: Context, client: SimpleClient, config: Config) {/* TODO implement*/
    val basketName = context.pathParam("basketName")
    try {
        //get BasketId
        val id = getBasketID(config, client, basketName)

        //insert
        val query = Query("${config.database.schemaName}.${"basket_elements"}").where(Compare("basketId", "=", id))
            .select("mediaResourceId")

        val results = client.query(query)

        val list = LinkedList<Text>()
        results.forEach { t ->
            list.add(Text(t.asString("mediaResourceId")!!))
        }
        context.json(list)


    } catch (e: StatusRuntimeException) {
        when (e.status.code) {
            Status.Code.INTERNAL -> {
                throw ErrorStatusException(
                    400, "The requested table '${config.database.schemaName}.${"baskets"} could not be found."
                )
            }

            Status.Code.NOT_FOUND -> throw ErrorStatusException(
                404, "The requested table '${config.database.schemaName}.${"baskets"} could not be found."
            )

            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Connection is currently not available.")

            else -> {
                throw e.message?.let { ErrorStatusException(400, it) }!!
            }
        }
    }
}

@OpenApi(
    summary = "List all existing baskets.",
    path = "/api/basket/list/all",
    tags = [Basket],
    operationId = "getAllBaskets",
    methods = [HttpMethod.GET],
    responses = [
        OpenApiResponse("200", [OpenApiContent(BasketList::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ],

    )
fun listAll(context: Context, client: SimpleClient, config: Config) {
    try {
        val list = LinkedList<Pair<Int,String>>()
        client.query(Query("${config.database.schemaName}.${eu.xreco.nmr.backend.model.database.basket.Basket.name}")).forEach { t ->
            list.add(t.asInt("basketId")!! to t.asString("name")!!)
        }

        val counts = list.map {
            client.query(
                Query("${config.database.schemaName}.${BasketElement.name}")
                .where(Compare("basketId", "=", it.first))
                .count()
            ).next().asLong(0)!!
        }

        context.json(BasketList(list.zip(counts).map { BasketPreview(it.first.first, it.first.second, it.second.toInt()) }))
    } catch (e: StatusRuntimeException) {
        when (e.status.code) {
            Status.Code.INTERNAL -> throw ErrorStatusException(400, "The requested entity '${config.database.schemaName}.${"baskets"} could not be found.")
            Status.Code.NOT_FOUND -> throw ErrorStatusException(404, "The requested entity '${config.database.schemaName}.${"baskets"} could not be found.")
            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Connection is currently not available.")
            else -> throw e.message?.let { ErrorStatusException(400, it) }!!
        }
    }
}

@OpenApi(
    summary = "List all baskets of a user.",
    path = "/api/basket/list/{userId}",
    tags = [Basket],
    operationId = "getBasketsOfUser",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "userId", type = String::class, description = "Id of user", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(SuccessStatus::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun listUser(context: Context, client: SimpleClient, config: Config) {/* TODO implement --> depends on authentication*/
}


fun getBasketID(config: Config, client: SimpleClient, basketName: String): Int {
    val queryId =
        Query("${config.database.schemaName}.${"baskets"}").where(Compare("name", "=", basketName)).select("basketId")

    // execute query
    val resultsId = client.query(queryId)

    // save results as LinkedList
    val listId = LinkedList<Int>()
    resultsId.forEach { t ->
        listId.add((t.asInt("basketId")!!))
    }
    return listId[0]
}