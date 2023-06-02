package eu.xreco.nmr.backend.api

import eu.xreco.nmr.backend.api.authentification.login
import eu.xreco.nmr.backend.api.authentification.logout
import eu.xreco.nmr.backend.api.basket.*
import eu.xreco.nmr.backend.api.media.*
import eu.xreco.nmr.backend.api.retrieval.*
import eu.xreco.nmr.backend.config.Config
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder
import io.javalin.apibuilder.ApiBuilder.path
import kotlinx.coroutines.runBlocking
import org.vitrivr.cottontail.client.SimpleClient

/**
 * Extension function that generates the relevant routes of the XRECO NMR backend API.
 *
 * @param client The [SimpleClient] instance used for database communication.
 * @param config The application configuration file.
 */
fun Javalin.initializeRoutes(client: SimpleClient, config: Config): Javalin =  this.routes  { runBlocking {  }
    path("api") {
        path("authentication") {
            ApiBuilder.get("{username}/{password}") { login(it) }
            ApiBuilder.get("logout/{username}") { logout(it) }
        }
        path("retrieval") {
            ApiBuilder.get("lookup/{elementId}/{entity}") { lookup(it, client, config) }
            ApiBuilder.get("text/{entity}/{text}/{pageSize}/{page}") { getFulltext(it, client, config) }
            ApiBuilder.get("similarity/{entity}/{mediaResourceId}/{timestamp}/{pageSize}/{page}") {
                getSimilar(
                    it,
                    client,
                    config
                )
            }
            ApiBuilder.get("filter/{condition}/{pageSize}/{page}") { filter(it) }
        }
        path("basket") {
            ApiBuilder.post("{basketName}") { createBasket(it, client, config) }
            ApiBuilder.delete("{basketId}") { deleteBasket(it, client, config) }
            ApiBuilder.get("{basketId}") { listElements(it, client, config) }
            ApiBuilder.get("list/all") { listAll(it, client, config) }

            path("{basketId}") {
                ApiBuilder.put("{mediaResourceId}") { addBasketElement(it, client, config) }
                ApiBuilder.delete("{mediaResourceId}") { dropBasketElement(it, client, config) }
            }
            ApiBuilder.get("list/{userId}") { listUser(it, client, config) }
        }
        path("resource") {
            ApiBuilder.get("{mediaResourceId}") { getResource(it, client, config) }
            ApiBuilder.get("{mediaResourceId}/metadata") { getMetadata(it, client, config) }
            ApiBuilder.get("{mediaResourceId}/preview/{time}") {  getPreview (it, client, config) }
        }

}
}