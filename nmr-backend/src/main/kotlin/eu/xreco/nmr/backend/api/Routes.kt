package eu.xreco.nmr.backend.api

import eu.xreco.nmr.backend.api.authentification.login
import eu.xreco.nmr.backend.api.authentification.logout
import eu.xreco.nmr.backend.api.basket.*
import eu.xreco.nmr.backend.api.retrieval.*
import eu.xreco.nmr.backend.config.Config
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.http.Header
import org.vitrivr.cottontail.client.SimpleClient

/**
 * Extension function that generates the relevant routes of the XRECO NMR backend API.
 *
 * @param client The [SimpleClient] instance used for database communication.
 * @param config The application configuration file.
 */
fun Javalin.initializeRoutes(client: SimpleClient, config: Config): Javalin = this.routes {
      path("api") {
          ApiBuilder.before { ctx ->
              ctx.method()
              ctx.header(Header.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
              ctx.header(
                  Header.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PATCH, PUT, DELETE, OPTIONS")
              ctx.header(Header.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization, Content-Type")
              ctx.header(Header.CONTENT_TYPE, "application/json")
          }
        /* TODO: Handlers for API. */
        path("authentication") {
            ApiBuilder.get("{username}/{password}") { login(it) }
            ApiBuilder.get("logout/{username}") { logout(it) }
        }
          path("retrieval") {
              ApiBuilder.get("{elementId}/{attributes}") { retrieve(it) }
              ApiBuilder.get("lookup/{elementId}/{entity}") { lookup(it, client, config) }
              ApiBuilder.get("text/{text}/{pageSize}/{page}") { fullText(it) }
              ApiBuilder.get("similarity/{elementId}/{pageSize}/{page}") { similarity(it) }
              ApiBuilder.get("filter/{condition}/{pageSize}/{page}") { filter(it) }
          }
        path("basket") {
            post("{basketId}") { createBasket(it) }
            delete("{basketId}") { dropBasket(it) }
            get("{basketId}") { listElements(it) }
            path("{basketId}") {
                post("{elementId}") { addElement(it) }
                delete("{elementId}") { dropElement(it) }
            }
            ApiBuilder.get("list/{userId}") { list(it) }
        }
    }
}