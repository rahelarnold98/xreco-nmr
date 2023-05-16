package eu.xreco.nmr.backend.api.authentification

import eu.xreco.nmr.backend.api.Authentication
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import eu.xreco.nmr.backend.model.api.status.SuccessStatus
import io.javalin.http.Context
import io.javalin.openapi.*


@OpenApi(
    summary = "Login function for a user.",
    /* TODO create a secure login */
    path = "/api/authentication/{username}/{password}",
    tags = [Authentication],
    operationId = "getLogin",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "username", type = String::class, description = "Username of user logging in", required = true),
        OpenApiParam(name = "password", type = String::class, description = "Password of user logging in", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(SuccessStatus::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun login(context: Context) {/* TODO implement*/
}

@OpenApi(
    summary = "Logout function for a user.",
    path = "/api/authentication/logout/{username}",
    tags = [Authentication],
    operationId = "getLogout",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "username", type = String::class, description = "Username of user logging out", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(SuccessStatus::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun logout(context: Context) {/* TODO implement*/
}
