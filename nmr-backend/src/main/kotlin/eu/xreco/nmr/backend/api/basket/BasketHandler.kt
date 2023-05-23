package eu.xreco.nmr.backend.api.basket

import eu.xreco.nmr.backend.api.Basket
import eu.xreco.nmr.backend.config.Config
import eu.xreco.nmr.backend.model.api.basekt.BasketList
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
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dml.Delete
import org.vitrivr.cottontail.client.language.dml.Insert
import org.vitrivr.cottontail.client.language.dql.Query
import org.vitrivr.cottontail.core.values.IntValue
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
    path = "/api/basket/{basketName}",
    tags = [Basket],
    operationId = "deleteBasket",
    methods = [HttpMethod.DELETE],
    pathParams = [
        OpenApiParam(name = "basketName", type = String::class, "Name of basket which gets deleted", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(SuccessStatus::class)]), // or just success?
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun dropBasket(context: Context, client: SimpleClient, config: Config) {
    val name = context.pathParam("basketName")
    try {

        val delete = Delete("${config.database.schemaName}.${"baskets"}").where(Compare("name", "=", name))
        client.delete(delete).close()
        context.json(SuccessStatus("Successfully deleted $name from ${config.database.schemaName}.${"baskets"}"))

    } catch (e: StatusRuntimeException) {
        when (e.status.code) {
            Status.Code.INTERNAL -> {
                throw ErrorStatusException(
                    400, "The requested table '${config.database.schemaName}.${"basket"} could not be found."
                )
            }

            Status.Code.NOT_FOUND -> throw ErrorStatusException(
                404, "The requested table '${config.database.schemaName}.${"basket"} could not be found."
            )

            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Connection is currently not available.")

            else -> {
                throw e.message?.let { ErrorStatusException(400, it) }!!
            }
        }
    }
}

@OpenApi(
    summary = "Adds a specific element to a specific basket.",
    path = "/api/basket/{basketName}/{elementId}",
    tags = [Basket],
    operationId = "putToBasket",
    methods = [HttpMethod.PUT],
    pathParams = [
        OpenApiParam(name = "basketName", type = String::class, description = "Name of basket", required = true),
        OpenApiParam(
            name = "elementId", type = String::class, description = "Id of element that will be added", required = true
        ),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(SuccessStatus::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun addElement(context: Context, client: SimpleClient, config: Config) {/* TODO implement*/
    val basketName = context.pathParam("basketName")
    val elementId = context.pathParam("elementId")
    try {
        //get BasketId
        val queryId = Query("${config.database.schemaName}.${"baskets"}").where(Compare("name", "=", basketName))
            .select("basketId")

        // execute query
        val resultsId = client.query(queryId)

        // save results as LinkedList

        val list = LinkedList<Int>()
        resultsId.forEach { t ->

            list.add((t.asInt("basketId")!!))
        }

        val id = list[0]

        //insert
        val query = Insert("${config.database.schemaName}.${"basket_elements"}").any("basketId", IntValue(id))
            .any("mediaResourceId", StringValue(elementId))
        client.insert(query).close()
        context.json(SuccessStatus("Successfully inserted $elementId into ${config.database.schemaName}.${"baskets"}.$basketName"))
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
    summary = "Drops a specific element of a specific basket.",
    path = "/api/basketName/{basketName}/{elementId}",
    tags = [Basket],
    operationId = "deleteFromBasket",
    methods = [HttpMethod.DELETE],
    pathParams = [
        OpenApiParam(name = "basketName", type = String::class, description = "Name of basket", required = true),
        OpenApiParam(
            name = "elementId", type = String::class, description = "Id of element that will be added", required = true
        ),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(BasketElement::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun dropElement(context: Context, client: SimpleClient, config: Config) {/* TODO implement*/
    val basketName = context.pathParam("basketName")
    val elementId = context.pathParam("elementId")
    try {
        //get BasketId
        val queryId = Query("${config.database.schemaName}.${"baskets"}").where(Compare("name", "=", basketName))
            .select("basketId")

        // execute query
        val resultsId = client.query(queryId)

        // save results as LinkedList

        val list = LinkedList<Int>()
        resultsId.forEach { t ->

            list.add((t.asInt("basketId")!!))
        }

        val id = list[0]

        //delete
        val query = Delete("${config.database.schemaName}.${"basket_elements"}").where(Compare(
            "mediaResourceId", "=", elementId
        ).also { Compare("basketId", "=", id) })
        client.delete(query).close()
        context.json(SuccessStatus("Successfully deleted $elementId from ${config.database.schemaName}.${"baskets"}.$basketName"))
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
        val queryId = Query("${config.database.schemaName}.${"baskets"}").where(Compare("name", "=", basketName))
            .select("basketId")

        // execute query
        val resultsId = client.query(queryId)

        // save results as LinkedList

        val listId = LinkedList<Int>()
        resultsId.forEach { t ->

            listId.add((t.asInt("basketId")!!))
        }

        val id = listId[0]

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
    summary = "List all existing baskets",
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
fun listAll(context: Context, client: SimpleClient, config: Config) {/* TODO implement */
    try {
        // prepare query
        val query = Query("${config.database.schemaName}.${"baskets"}").select("name")

        // execute query
        val results = client.query(query)

        // save results as LinkedList

        val list = LinkedList<Text>()
        results.forEach { t ->
            list.add(Text(t.asString("name")!!))
        }
        context.json(list)


    } catch (e: StatusRuntimeException) {
        when (e.status.code) {
            Status.Code.INTERNAL -> {
                throw ErrorStatusException(
                    400, "The requested entity '${config.database.schemaName}.${"baskets"} could not be found."
                )
            }

            Status.Code.NOT_FOUND -> throw ErrorStatusException(
                404, "The requested entity '${config.database.schemaName}.${"baskets"} could not be found."
            )

            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Connection is currently not available.")

            else -> {
                throw e.message?.let { ErrorStatusException(400, it) }!!
            }
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
