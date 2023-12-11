package eu.xreco.nmr.backend.api

import eu.xreco.nmr.backend.api.ingest.ingest
import eu.xreco.nmr.backend.api.ingest.ingestAbort
import eu.xreco.nmr.backend.api.ingest.ingestStatus
import eu.xreco.nmr.backend.api.media.*
import eu.xreco.nmr.backend.api.retrieval.*
import eu.xreco.nmr.backend.config.Config
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.path
import io.minio.MinioClient
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionServer
import org.vitrivr.engine.core.model.metamodel.SchemaManager

/**
 * Extension function that generates the relevant routes of the XRECO NMR backend API.
 *
 * @param config The application configuration.
 * @param manager The vitrivr [SchemaManager] instance.
 * @param runtime The vitrivr [ExecutionServer] instance.
 * @param minio The [MinioClient] instance.
 */
fun Javalin.initializeRoutes(config: Config, manager: SchemaManager, runtime: ExecutionServer, minio: MinioClient): Javalin =  this.routes  {
    path("api") {
        /* Endpoints related to data ingest. */
        post("ingest") { ingest(it, minio, manager, runtime) }
        path("ingest") {
            path("{jobId}") {
                get("status") { ingestStatus(it, manager, runtime) }
                delete("abort") { ingestAbort(it, manager, runtime) }
            }
        }

        /* Endpoints related to retrieval- */
        path("retrieval") {
            get("lookup/{elementId}/{entity}") { lookup(it, manager, runtime) }
            get("type/{elementId}") { type(it, manager, runtime) }
            get("text/{entity}/{text}/{pageSize}/{page}") { getFulltext(it, manager, runtime) }
            get("similarity/{entity}/{mediaResourceId}/{timestamp}/{pageSize}/{page}") {
                getSimilar(it, manager, runtime)
            }
            get("filter/{condition}/{pageSize}/{page}") { filter(it) }
        }

        /* Access to MinIO resources. */
        path("resource") {
            get("{mediaResourceId}") { getAssetResource(it, minio) }
            get("{mediaResourceId}/metadata") { getMetadata(it, manager, runtime) }
        }
    }
}