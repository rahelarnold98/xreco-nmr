package eu.xreco.nmr.backend.api.ingest

import eu.xreco.nmr.backend.config.Config
import eu.xreco.nmr.backend.config.MinioConfig
import eu.xreco.nmr.backend.minio.MinioSource
import eu.xreco.nmr.backend.minio.MinioSource.Companion.FILENAME_TAG_NAME
import eu.xreco.nmr.backend.minio.MinioSource.Companion.MEDIA_TYPE_TAG_NAME
import eu.xreco.nmr.backend.minio.MinioSource.Companion.MIME_TYPE_TAG_NAME
import eu.xreco.nmr.backend.minio.MinioSource.Companion.TIMESTAMP_TAG_NAME
import eu.xreco.nmr.backend.model.api.ingest.IngestStatus
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import eu.xreco.nmr.backend.model.api.status.ErrorStatusException
import eu.xreco.nmr.backend.model.api.status.SuccessStatus
import eu.xreco.nmr.backend.utilities.FileEnding
import eu.xreco.nmr.backend.utilities.MimeTypeHelper
import io.javalin.http.Context
import io.javalin.openapi.*
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.errors.ErrorResponseException
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionServer
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionStatus
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import org.vitrivr.engine.index.enumerate.ListEnumerator
import java.util.*

@OpenApi(
    summary = "Ingest one (or multiple) images into the XRECO NMR backend.",
    path = "/api/ingest/image",
    tags = ["Ingest"],
    operationId = "postIngestImage",
    methods = [HttpMethod.POST],
    responses = [
        OpenApiResponse("200", [OpenApiContent(IngestStatus::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ],
    requestBody = OpenApiRequestBody(content = [OpenApiContent(mimeType = "multipart/form-data")])
)
fun ingestImage(context: Context, config: Config, minio: MinioClient, manager: SchemaManager, executor: ExecutionServer) {
    /* Extract schema. */
    val schema = manager.getSchema(config.schema.name) ?: throw ErrorStatusException(404, "Schema '${config.schema.name}' does not exist.")

    /* Upload assets to MinIO and choose pipeline */
    val assets = uploadAssets(context, minio)

    /* Construct extraction pipeline */
    val pipeline = schema.getPipelineBuilder("IMAGE").getPipeline()/* Schedule pipeline and return job ID. */
    val root = pipeline.getLeaves().first().root()
    if (root is ListEnumerator.Instance){
        for (source in assets){
            root.add(source)
        }
        val jobId = executor.extractAsync(pipeline)
        context.json(IngestStatus(jobId.toString(), assets.map { it.toString() }, System.currentTimeMillis()))
    }
}

@OpenApi(
    summary = "Ingest one (or multiple) videos into the XRECO NMR backend.",
    path = "/api/ingest/video",
    tags = ["Ingest"],
    operationId = "postIngestVideo",
    methods = [HttpMethod.POST],
    responses = [
        OpenApiResponse("200", [OpenApiContent(IngestStatus::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ],
    requestBody = OpenApiRequestBody(content = [OpenApiContent(mimeType = "multipart/form-data"), ])
)
fun ingestVideo(context: Context, config: Config, minio: MinioClient, manager: SchemaManager, executor: ExecutionServer) {
    /* Extract schema. */
    val schema = manager.getSchema(config.schema.name) ?: throw ErrorStatusException(404, "Schema '${config.schema.name}' does not exist.")

    /* Upload assets to MinIO and choose pipeline */
    val assets = uploadAssets(context, minio)

    /* Construct extraction pipeline */
    val pipeline = schema.getPipelineBuilder("VIDEO").getPipeline()/* Schedule pipeline and return job ID. */
    val root = pipeline.getLeaves().first().root()
    if (root is ListEnumerator.Instance){
        for (source in assets){
            root.add(source)
        }
        val jobId = executor.extractAsync(pipeline)
        context.json(IngestStatus(jobId.toString(), assets.map { it.toString() }, System.currentTimeMillis()))
    }
}

@OpenApi(
    summary = "Ingest one (or multiple) models into the XRECO NMR backend.",
    path = "/api/ingest/model",
    tags = ["Ingest"],
    operationId = "postIngestModel",
    methods = [HttpMethod.POST],
    responses = [
        OpenApiResponse("200", [OpenApiContent(IngestStatus::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ],
    requestBody = OpenApiRequestBody(content = [OpenApiContent(mimeType = "multipart/form-data")])
)
fun ingestModel(context: Context, config: Config, minio: MinioClient, manager: SchemaManager, executor: ExecutionServer) {
    /* Extract schema. */
    val schema = manager.getSchema(config.schema.name) ?: throw ErrorStatusException(404, "Schema '${config.schema.name}' does not exist.")

    /* Upload assets to MinIO and choose pipeline */
    val assets = uploadAssets(context, minio)

    /* Construct extraction pipeline */
    val pipeline = schema.getPipelineBuilder("MESH").getPipeline()/* Schedule pipeline and return job ID. */
    val root = pipeline.getLeaves().first().root()
    if (root is ListEnumerator.Instance){
        for (source in assets){
            root.add(source)
        }
        val jobId = executor.extractAsync(pipeline)
        context.json(IngestStatus(jobId.toString(), assets.map { it.toString() }, System.currentTimeMillis()))
    }
}

@OpenApi(
    summary = "Queries the ingest status for the provided job ID.",
    path = "/api/ingest/{jobId}/status",
    tags = ["Ingest"],
    operationId = "getIngestStatus",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "jobId", type = String::class, "Job ID to query status of", required = true),
    ],
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
    when (val status = executor.status(jobId)) {
        ExecutionStatus.RUNNING -> context.status(200).json(IngestStatus(jobId.toString(), emptyList(), System.currentTimeMillis()))
        ExecutionStatus.COMPLETED -> context.status(200).json(SuccessStatus("Ingest job completed"))
        ExecutionStatus.FAILED -> context.status(500).json(ErrorStatus(500, "Ingest job failed"))
        ExecutionStatus.UNKNOWN ->  context.status(404).json(ErrorStatus(404, "Ingest job not found"))
    }
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
    // TODO for future: What is the sematic for such an abort --> cleanup needed?!?
    // XReco discussion --> do we need one at all?
    // vitrivr-engine discussion --> semantic of abort
    val jobId = try {
        UUID.fromString(context.pathParam("jobId"))
    } catch (e: Exception) {
        throw ErrorStatusException(400, "Invalid request: ${e.message}")
    }

    if (executor.cancel(jobId)) {
        context.status(200).json(SuccessStatus("Ingest job aborted"))
    } else {
        throw ErrorStatusException(404, "Job $jobId not found.")
    }
}

/**
 * This method assigns every uploaded asset a unique [UUID] and tries to upload it to the min.io server.
 * Returns a [MinioSource] upon success, which can be processed by the extraction pipeline.
 *
 * @param ctx The [Context] of the request.
 * @param minio The [MinioClient].
 * @return [List] of [MinioSource]s.
 */
private fun uploadAssets(ctx: Context, minio: MinioClient): List<MinioSource> = ctx.uploadedFiles("files").map { file ->
    val assetId = UUID.randomUUID()
    val extension = file.extension().replace(".","")
    try {
        file.content().use { input ->
            minio.putObject(
                PutObjectArgs.builder().bucket(MinioConfig.ASSETS_BUCKET).`object`(assetId.toString())
                    .contentType(file.contentType())
                    .tags(
                        mapOf(
                            MEDIA_TYPE_TAG_NAME to FileEnding.objectType(extension).toString(),
                            MIME_TYPE_TAG_NAME to MimeTypeHelper.mimeType(extension),
                            TIMESTAMP_TAG_NAME to System.currentTimeMillis().toString(),
                        )
                    ).stream(input, file.size(), -1).build()
            )
        }
        MinioSource(assetId, MinioConfig.ASSETS_BUCKET, minio)
    } catch (e: ErrorResponseException) {
        throw ErrorStatusException(500, "Failed to upload asset ${file.filename()} due to a min.io error: ${e.message}")
    }
}