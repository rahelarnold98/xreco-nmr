package eu.xreco.nmr.backend.api

import eu.xreco.nmr.backend.api.ingest.ingestImage
import eu.xreco.nmr.backend.api.ingest.ingestAbort
import eu.xreco.nmr.backend.api.ingest.ingestStatus
import eu.xreco.nmr.backend.api.ingest.ingestVideo
import eu.xreco.nmr.backend.api.media.*
import eu.xreco.nmr.backend.api.retrieval.*
import eu.xreco.nmr.backend.config.Config
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder
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
fun Javalin.initializeRoutes(config: Config, manager: SchemaManager, runtime: ExecutionServer, minio: MinioClient): Javalin = this.routes  {
    ApiBuilder.path("api") {
        /* Endpoints related to data ingest. */
        ApiBuilder.path("ingest") {
            ApiBuilder.post("image") { ingestImage(it, config, minio, manager, runtime) }
            ApiBuilder.post("video") { ingestVideo(it, config, minio, manager, runtime) }
            ApiBuilder.path("{jobId}") {
                ApiBuilder.get("status") { ingestStatus(it, manager, runtime) }
                ApiBuilder.delete("abort") { ingestAbort(it, manager, runtime) }
            }
        }

        /* Endpoints related to retrieval- */
        ApiBuilder.path("retrieval") {
            ApiBuilder.get("lookup/{elementId}/{entity}") { lookup(it, manager, runtime) }
            ApiBuilder.get("type/{elementId}") { type(it, manager, runtime) }
            ApiBuilder.get("text/{entity}/{text}/{pageSize}/{page}") { getFulltext(it, manager, runtime) }
            ApiBuilder.get("similarity/{entity}/{mediaResourceId}/{timestamp}/{pageSize}/{page}") {
                getSimilar(it, manager, runtime)
            }
            ApiBuilder.get("filter/{condition}/{pageSize}/{page}") { filter(it) }
        }

        /* Access to MinIO resources. */
        ApiBuilder.path("resource") {
            ApiBuilder.get("{mediaResourceId}") { getAssetResource(it, minio) }
            ApiBuilder.get("{mediaResourceId}/metadata") { getMetadata(it, manager, runtime) }
        }
    }
}