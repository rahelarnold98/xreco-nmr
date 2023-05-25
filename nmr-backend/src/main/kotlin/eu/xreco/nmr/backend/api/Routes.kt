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
        path("authentication") {
            ApiBuilder.get("{username}/{password}") { login(it) }
            ApiBuilder.get("logout/{username}") { logout(it) }
        }
        path("retrieval") {
            ApiBuilder.get("{elementId}") { retrieve(it, client, config) }
            ApiBuilder.get("lookup/{elementId}/{entity}") { lookup(it, client, config) }
            ApiBuilder.get("text/{entity}/{text}/{pageSize}/{page}") { fullText(it, client, config) }
            ApiBuilder.get("similarity/{elementId}/{pageSize}/{page}") { similarity(it, client, config) }
            ApiBuilder.get("filter/{condition}/{pageSize}/{page}") { filter(it) }
        }
        path("basket") {
            ApiBuilder.post("{basketName}") { createBasket(it, client, config) }
            ApiBuilder.delete("{basketName}") { dropBasket(it, client, config) }
            ApiBuilder.get("{basketName}") { listElements(it, client, config) }
            ApiBuilder.get("list/all") { listAll(it, client, config) }

            path("{basketName}") {
                ApiBuilder.put("{elementId}") { addElement(it, client, config) }
                ApiBuilder.delete("{elementId}") { dropElement(it, client, config) }
            }
            ApiBuilder.get("list/{userId}") { listUser(it, client, config) }
        }
        path("media") {
            ApiBuilder.get("thumbnail/{mediaResourceId}/{timeStamp}") { getThumbnail(it, client, config) }
            ApiBuilder.get("video/{mediaResourceId}") { getVideo(it, client, config) }
            ApiBuilder.get("representativeFrame/{mediaResourceId}/{timeStamp}") { getRepresentativeFrame(it, client, config) }
        }
    }
}