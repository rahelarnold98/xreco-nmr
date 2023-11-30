package eu.xreco.nmr.backend.api

import eu.xreco.nmr.backend.api.authentification.login
import eu.xreco.nmr.backend.api.authentification.logout
import eu.xreco.nmr.backend.api.ingest.ingest
import eu.xreco.nmr.backend.api.ingest.ingestAbort
import eu.xreco.nmr.backend.api.ingest.ingestStatus
import eu.xreco.nmr.backend.api.media.*
import eu.xreco.nmr.backend.api.retrieval.*
import eu.xreco.nmr.backend.config.Config
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder
import io.javalin.apibuilder.ApiBuilder.path
import kotlinx.coroutines.runBlocking
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import org.vitrivr.engine.query.execution.RetrievalRuntime

/**
 * Extension function that generates the relevant routes of the XRECO NMR backend API.
 *
 * @param config The application configuration.
 * @param manager The [SchemaManager] instance
 * @param runtime The [RetrievalRuntime] instance.
 */
fun Javalin.initializeRoutes(config: Config, manager: SchemaManager, runtime: RetrievalRuntime): Javalin =  this.routes  { runBlocking {  }
    path("api") {
        path("authentication") {
            ApiBuilder.get("{username}/{password}") { login(it) }
            ApiBuilder.get("logout/{username}") { logout(it) }
        }

        /* Endpoints related to data ingest. */
        ApiBuilder.post("ingest") { ingest(it, manager, runtime) }
        path("ingest") {
            path("{jobId}") {
                delete("abort") { ingestAbort(it, manager, runtime) }
                get("status") { ingestStatus(it, manager, runtime) }
            }
        }

        /* Endpoints related to retrieval- */
        path("retrieval") {
            ApiBuilder.get("lookup/{elementId}/{entity}") { lookup(it, manager, runtime) }
            ApiBuilder.get("type/{elementId}") { type(it, manager, runtime) }
            ApiBuilder.get("text/{entity}/{text}/{pageSize}/{page}") { getFulltext(it, manager, runtime) }
            ApiBuilder.get("similarity/{entity}/{mediaResourceId}/{timestamp}/{pageSize}/{page}") {
                getSimilar(it, manager, runtime)
            }
            ApiBuilder.get("filter/{condition}/{pageSize}/{page}") { filter(it) }
        }

        /* Access to MinIO resources. */
        path("resource") {
            val minio = config.minio.newClient()
            ApiBuilder.get("{mediaResourceId}") { getAssetResource(it, minio) }
            ApiBuilder.get("{mediaResourceId}/metadata") { getMetadata(it, manager, runtime) }
        }
    }
}