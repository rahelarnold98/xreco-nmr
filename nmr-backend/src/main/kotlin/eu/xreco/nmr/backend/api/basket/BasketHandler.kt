package eu.xreco.nmr.backend.api.basket

import eu.xreco.nmr.backend.api.Basket
import io.javalin.http.Context
import io.javalin.openapi.HttpMethod
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiParam

@OpenApi(
    summary = "Creates a new basket.",
    path = "/api/basket/{basketId}",
    tags = [Basket],
    operationId = "postBasket",
    methods = [HttpMethod.POST],
    pathParams = [
        OpenApiParam(name = "basketId", type = String::class, "Id of basket which gets deleted"),
    ],
    /* TODO add Responses, RequestBody*/
)
fun createBasket(context: Context) {/* TODO implement*/
}

@OpenApi(
    summary = "Deletes a specific basket.",
    path = "/api/basket/{basketId}",
    tags = [Basket],
    operationId = "deleteBasket",
    methods = [HttpMethod.DELETE],
    pathParams = [
        OpenApiParam(name = "basketId", type = String::class, "Id of basket which gets deleted", required = true),
    ],
    /* TODO add Responses*/
)
fun dropBasket(context: Context) {/* TODO implement*/
}

@OpenApi(
    summary = "Adds a specific element to a specific basket.",
    path = "/api/basket/{basketId}/{elementId}",
    tags = [Basket],
    operationId = "putToBasket",
    methods = [HttpMethod.PUT],
    pathParams = [
        OpenApiParam(name = "basketId", type = String::class, description = "Id of basket", required = true),
        OpenApiParam(name = "elementId", type = String::class, description = "Id of element that will be added", required = true),
    ],
    /* TODO add Responses*/
)
fun addElement(context: Context) {/* TODO implement*/
}

@OpenApi(
    summary = "Drops a specific element of a specific basket.",
    path = "/api/basket/{basketId}/{elementId}",
    tags = [Basket],
    operationId = "deleteFromBasket",
    methods = [HttpMethod.DELETE],
    pathParams = [
        OpenApiParam(name = "basketId", type = String::class, description = "Id of basket", required = true),
        OpenApiParam(name = "elementId", type = String::class, description = "Id of element that will be added", required = true),
    ],
    /* TODO add Responses*/
)
fun dropElement(context: Context) {/* TODO implement*/
}

@OpenApi(
    summary = "List all elements of a specific basket.",
    path = "/api/basket/{basketId}",
    tags = [Basket],
    operationId = "getElementsInBasket",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "basketId", type = String::class, description = "Id of basket", required = true),
    ],
    /* TODO add Responses*/
)
fun listElements(context: Context) {/* TODO implement*/
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
    /* TODO add Responses*/
)
fun list(context: Context) {/* TODO implement*/
}
