package eu.xreco.nmr.backend.api.authentification

import eu.xreco.nmr.backend.api.Authentication
import io.javalin.http.Context
import io.javalin.openapi.HttpMethod
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiParam

@OpenApi(
    summary = "Login function for a user.",
    /* TODO create a secure login */
    path = "/api/authentication/{username}/{password}",
    tags = [Authentication],
    operationId = "getLogin",
    methods = [HttpMethod.GET],
    pathParams =
        [
            OpenApiParam("username", String::class, "Username of user logging in"),
            OpenApiParam("password", String::class, "Password of user logging in"),
        ],
    /* TODO add Responses*/
)
fun login(context: Context) {
  /* TODO implement*/
}

@OpenApi(
    summary = "Logout function for a user.",
    path = "/api/authentication/logout/{username}",
    tags = [Authentication],
    operationId = "getLogout",
    methods = [HttpMethod.GET],
    pathParams =
        [
            OpenApiParam("username", String::class, "Username of user logging out"),
        ],
    /* TODO add Responses*/
)
fun logout(context: Context) {
  /* TODO implement*/
}
