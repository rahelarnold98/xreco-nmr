package eu.xreco.nmr.backend.api

import eu.xreco.nmr.backend.api.ingest.*
import eu.xreco.nmr.backend.api.media.*
import eu.xreco.nmr.backend.api.retrieval.*
import eu.xreco.nmr.backend.config.Config
import eu.xreco.nmr.backend.model.api.status.ErrorStatusException
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
    /* Obtain schema from configuration. */
    val schema = manager.getSchema(config.schema.name) ?: throw ErrorStatusException(404, "Schema '${config.schema.name}' does not exist.")

    /* Build API. */
    ApiBuilder.path("api") {
        /* Endpoints related to data ingest. */
        ApiBuilder.path("ingest") {
            ApiBuilder.post("image") { ingestImage(it, config, minio, manager, runtime) }
            ApiBuilder.post("video") { ingestVideo(it, config, minio, manager, runtime) }
            ApiBuilder.post("model") { ingestModel(it, config, minio, manager, runtime) }
            ApiBuilder.path("{jobId}") {
                ApiBuilder.get("status") { ingestStatus(it, manager, runtime) }
                ApiBuilder.delete("abort") { ingestAbort(it, manager, runtime) }
            }
        }

        /* Endpoints related to retrieval- */
        ApiBuilder.path("retrieval") {
            ApiBuilder.get("lookup/{field}/{retrievableId}/") { lookup(it, schema) }
            ApiBuilder.get("type/{retrievableId}") { type(it, schema) }
            ApiBuilder.get("text/{field}/{text}/{pageSize}") { getFulltext(it, schema, runtime) }
            ApiBuilder.get("similarity/{field}/{retrievableId}/{pageSize}") {
                getSimilar(it, schema, runtime)
            }
            ApiBuilder.get("filter/{condition}/{pageSize}/{page}") { filter(it) }
        }

        /* Access to MinIO resources. */
        ApiBuilder.path("assets") {
            ApiBuilder.get("content/{assetId}") { getAssetResource(it, minio) }
            ApiBuilder.get("preview/{assetId}") { getPreviewResource(it, minio) }
        }
    }
}