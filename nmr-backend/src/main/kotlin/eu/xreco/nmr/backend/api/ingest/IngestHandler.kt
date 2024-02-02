package eu.xreco.nmr.backend.api.ingest

import eu.xreco.nmr.backend.config.MinioConfig
import eu.xreco.nmr.backend.model.api.ingest.IngestStatus
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import eu.xreco.nmr.backend.model.api.status.ErrorStatusException
import eu.xreco.nmr.backend.model.api.status.SuccessStatus
import eu.xreco.nmr.backend.utilities.FileEnding
import io.javalin.http.Context
import io.javalin.openapi.*
import io.javalin.util.FileUtil
import io.minio.GetObjectArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.errors.ErrorResponseException
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionServer
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionStatus
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import org.vitrivr.engine.core.source.file.MimeType
import org.vitrivr.engine.core.source.file.MimeType.Companion.getMimeType
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.deleteIfExists



@OpenApi(
    summary = "Get type of given element",
    path = "/api/ingest/{schema}",
    tags = ["Ingest"],
    operationId = "postIngest",
    methods = [HttpMethod.POST],
    responses = [
        OpenApiResponse("200", [OpenApiContent(IngestStatus::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ],
    requestBody = OpenApiRequestBody(
        content = [
            OpenApiContent(mimeType = "multipart/form-data"),
        ]
    ),
    pathParams = [
        OpenApiParam(name = "schema", type = String::class, "Schema to ingest into", required = true),
    ]
)
fun ingest(context: Context, minio: MinioClient, manager: SchemaManager, executor: ExecutionServer) {
    /* Upload assets to MinIO. */
    val assetIds = uploadAssets(context, minio)

    val schema = manager.getSchema(context.pathParam("schema"))

    val firstFile = context.uploadedFiles("files")[0].filename()

    val pipeline = chooseIngestPipeline(firstFile)

    val filePath: Path = FileSystems.getDefault().getPath(pipeline)

    // TODO asset must be downloaded from minio and checked for mime-type
    // MinIO Source --> source id, name, type, ...
    // not file source but MinIO source in pipeline
    // Intern -> Stream
    // Extern -> Link to MinIO source
    // Enumerator maybe not needed (Pseudo-Enumerator) as only one used

    // Thumbnailexporter --> MinIO --> Resolver (?)

    /* Construct extraction pipeline */

    val pipelineBuilder = schema?.getPipelineBuilder(pipeline) ?: throw ErrorStatusException(
        400,
        "Invalid request: Pipeline '$pipeline' not valid."
    )

    val filestream: MutableList<Path> = mutableListOf()
    // folder with threadId to avoid deleting files from other threads
    val uuid = UUID.randomUUID()
    val basePath = Path.of("upload/$uuid/")

    try {/* Handle uploaded file. */
        context.uploadedFiles("files").forEach { uploadedFile ->
            val path = Path.of("$basePath/${uploadedFile.filename()}")
            FileUtil.streamToFile(uploadedFile.content(), path.toString())
            filestream.add(path)
        }

        // Debug statements -> TODO delete when working
        filestream.forEach { filePath ->
            println("File in filestream: $filePath")
        }

        val stream = filestream.stream()

        val p = pipelineBuilder.getApiPipeline(stream)/* Schedule pipeline and return job ID. */
        val jobId = executor.extractAsync(p)
        context.json(IngestStatus(jobId.toString(), assetIds.map { it.toString() }, System.currentTimeMillis()))
    } catch (e: Exception) {
        throw ErrorStatusException(400, "Invalid request: ${e.message}")
    } finally {
        filestream.forEach { file -> file.deleteIfExists() }
        basePath.deleteIfExists()
    }
}

fun chooseIngestPipeline(assetIds: String): String {
    // TODO change from MimeType to FileEndings --> specify which types are supported
    val fileExtension = assetIds.substringAfterLast('.').lowercase(Locale.getDefault())

    // Using FileEnding to get the object type
    return when (val objectType = FileEnding.objectType(fileExtension)) {
        "image" -> "Image"
        "video" -> "Video"
        "3d" -> "M3D"
        else -> throw ErrorStatusException(400, "File type is unknown to the system: $objectType")
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

    val status = executor.status(jobId)

    when (status) {
        ExecutionStatus.RUNNING -> {
            context.status(200).json(IngestStatus(jobId.toString(), emptyList(), System.currentTimeMillis()))
        }

        ExecutionStatus.COMPLETED -> {
            context.status(200).json(SuccessStatus("Ingest job completed"))
        }

        ExecutionStatus.FAILED -> {
            context.status(500).json(ErrorStatus(500, "Ingest job failed"))
        }

        ExecutionStatus.UNKNOWN -> {
            context.status(404).json(ErrorStatus(404, "Ingest job not found"))
        }
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
 *
 * @param ctx The [Context] of the request.
 * @param minio The [MinioClient].

 */
private fun uploadAssets(ctx: Context, minio: MinioClient): List<UUID> = ctx.uploadedFiles("files").map { file ->
    // TODO add original name
    val assetId = UUID.randomUUID()
    try {
        file.content().use { input ->
            minio.putObject(
                PutObjectArgs.builder().bucket(MinioConfig.ASSETS_BUCKET).`object`(assetId.toString())
                    .contentType(file.contentType()).headers(
                        mapOf(
                            "filename" to file.filename(),
                            "timestamp" to System.currentTimeMillis().toString(),
                        )
                    ).stream(input, file.size(), -1).build()
            )
        }
        assetId
    } catch (e: ErrorResponseException) {
        throw ErrorStatusException(500, "Failed to upload asset ${file.filename()} due to a min.io error: ${e.message}")
    }
}


/**
 * This method downloads a file using its unique [UUID] from the min.io server.
 *
 * @param minio The [MinioClient].
 * @param assetId The [UUID] of the asset to download.
*/
private fun downloadAsset(minio: MinioClient, assetId: UUID): ByteArray {
    try {
        val getObjectResponse: InputStream = minio.getObject(
            GetObjectArgs.builder()
                .bucket(MinioConfig.ASSETS_BUCKET)
                .`object`(assetId.toString())
                .build()
        )

        val outputStream = ByteArrayOutputStream()

        getObjectResponse.use { input ->
            outputStream.use { output ->
                input.transferTo(output)
            }
        }

        return outputStream.toByteArray()

    } catch (e: ErrorResponseException) {
        throw ErrorStatusException(500, "Failed to download asset $assetId from min.io: ${e.message}")
    }
}

