package eu.xreco.nmr.backend.api.ingest

import eu.xreco.nmr.backend.config.MinioConfig
import eu.xreco.nmr.backend.model.api.ingest.IngestStatus
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import eu.xreco.nmr.backend.model.api.status.ErrorStatusException
import io.javalin.http.Context
import io.javalin.openapi.*
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.errors.ErrorResponseException
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionServer
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import java.util.*


@OpenApi(
    summary = "Get type of given element",
    path = "/api/ingest",
    tags = ["Ingest"],
    operationId = "postIngest",
    methods = [HttpMethod.POST],
    responses = [
        OpenApiResponse("200", [OpenApiContent(IngestStatus::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun ingest(context: Context, minio: MinioClient, manager: SchemaManager, executor: ExecutionServer) {
    /* Upload assets to MinIO. */
    val assetIds = uploadAssets(context, minio)

    /* TODO: Select pre-configured ingest pipeline (based on mime-type).  */
    val jobId: UUID = TODO("Hand assets to ingest pipeline.")

    /* Return ingest status. */
    context.json(IngestStatus(jobId.toString(), assetIds.map { it.toString() }, System.currentTimeMillis()))
}

@OpenApi(
    summary = "Queries the ingest status for the provided job ID.",
    path = "/api/ingest/{jobId}/status",
    tags = ["Ingest"],
    operationId = "getIngestStatus",
    methods = [HttpMethod.GET],
    responses = [
        OpenApiResponse("200", [OpenApiContent(IngestStatus::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun ingestStatus(context: Context, manager: SchemaManager, executor: ExecutionServer) {
    val jobId = try {
        UUID.fromString(context.pathParam("jobId"))
    } catch (e: Exception) {
        throw ErrorStatusException(400, "Invalid request: ${e.message}")
    }
    TODO()
}

@OpenApi(
    summary = "Tries to abort an ongoing ingest.",
    path = "/api/ingest/{jobId}/abort",
    tags = ["Ingest"],
    operationId = "deleteIngestAbort",
    methods = [HttpMethod.DELETE],
    responses = [
        OpenApiResponse("200", [OpenApiContent(IngestStatus::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun ingestAbort(context: Context, manager: SchemaManager, executor: ExecutionServer) {
    val jobId = try {
        UUID.fromString(context.pathParam("jobId"))
    } catch (e: Exception) {
        throw ErrorStatusException(400, "Invalid request: ${e.message}")
    }
    if (executor.cancel(jobId)) {
        TODO()
    } else {
        throw ErrorStatusException(404, "Job ${jobId} not found.")
    }
}


/**
 * This method assigns every uploaded asset a unique [UUID] and tries to upload it to the min.io server.
 *
 * @param ctx The [Context] of the request.
 * @param minio The [MinioClient].

 */
private fun uploadAssets(ctx: Context, minio: MinioClient): List<UUID> = ctx.uploadedFiles("files").map { file ->
    val assetId = UUID.randomUUID()
    try {
        file.content().use { input ->
            minio.putObject(
                PutObjectArgs.builder()
                    .bucket(MinioConfig.ASSETS_BUCKET)
                    .`object`(assetId.toString())
                    .contentType(file.contentType())
                    .headers(
                        mapOf(
                            "filename" to file.filename(),
                            "timestamp" to System.currentTimeMillis().toString(),
                        )
                    )
                    .stream(input, file.size(), -1)
                    .build()
            )
        }
        assetId
    } catch (e: ErrorResponseException) {
        throw ErrorStatusException(500, "Failed to upload asset ${file.filename()} due to a min.io error: ${e.message}")
    }
}
