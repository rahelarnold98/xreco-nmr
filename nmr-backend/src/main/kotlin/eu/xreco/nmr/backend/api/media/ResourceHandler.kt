package eu.xreco.nmr.backend.api.media

import eu.xreco.nmr.backend.api.Resource
import eu.xreco.nmr.backend.config.MinioConfig
import eu.xreco.nmr.backend.model.api.retrieval.MediaResource
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import eu.xreco.nmr.backend.model.api.status.ErrorStatusException
import io.javalin.http.Context
import io.javalin.openapi.*
import io.minio.GetObjectArgs
import io.minio.MinioClient
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionServer
import org.vitrivr.engine.core.model.metamodel.SchemaManager

@OpenApi(
    summary = "Obtains metadata for the asset identified by the given ID.",
    path = "/api/assets/{assetId}",
    tags = [Resource],
    operationId = "getAssetMetadata",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "assetId", type = String::class, "ID of the asset to access metadata for.", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(MediaResource::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun getMetadata(context: Context, manager: SchemaManager, executor: ExecutionServer) {
    val assetId = context.pathParam("assetId")
    TODO()
}

@OpenApi(
    summary = "Gets the media asset provided by the given ID.",
    path = "/api/assets/{assetId}/resource",
    tags = [Resource],
    operationId = "getAssetResource",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "assetId", type = String::class, "ID of the asset to access.", required = true)
    ],
    responses = [
        OpenApiResponse("200"),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun getAssetResource(context: Context, client: MinioClient) {
    val assetId = context.pathParam("assetId")
    try {
        /* Load asset from MinIO. */
        val result = client.getObject(
            GetObjectArgs.Builder().bucket(MinioConfig.ASSETS_BUCKET).`object`(assetId).build()
        )
        val mimeType = result.headers()["Content-Type"] ?: "application/octet-stream"

        /* Send result */
        context.status(200)
        context.header("Content-Type", mimeType)
        context.header("Cache-Control", "max-age=31622400")
        context.result(result)
    } catch (e: Throwable) {
        throw ErrorStatusException(500, "Failed to load asset ${assetId}.")
    }
}