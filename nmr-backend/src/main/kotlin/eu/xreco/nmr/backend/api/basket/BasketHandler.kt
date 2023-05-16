package eu.xreco.nmr.backend.api.basket

import eu.xreco.nmr.backend.api.Basket
import eu.xreco.nmr.backend.model.api.basekt.BasketList
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import eu.xreco.nmr.backend.model.api.status.SuccessStatus
import eu.xreco.nmr.backend.model.database.basket.BasketElement
import io.javalin.http.Context
import io.javalin.openapi.*

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
fun createBasket(context: Context) {/* TODO implement*/
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
fun dropBasket(context: Context) {/* TODO implement*/
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
        OpenApiResponse("200", [OpenApiContent(BasketElement::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun addElement(context: Context) {/* TODO implement*/
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
fun dropElement(context: Context) {/* TODO implement*/
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
fun listElements(context: Context) {/* TODO implement*/
}

@OpenApi(
    summary = "List all existing baskets",
    path = "/api/basket/list",
    tags = [Basket],
    operationId = "getAllBaskets",
    methods = [HttpMethod.GET],
    responses = [
        OpenApiResponse("200", [OpenApiContent(BasketList::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun list(context: Context) {/* TODO implement */
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
fun listUser(context: Context) {/* TODO implement --> depends on authentication*/
}
