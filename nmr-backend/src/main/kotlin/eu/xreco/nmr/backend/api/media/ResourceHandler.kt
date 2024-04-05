package eu.xreco.nmr.backend.api.media

import eu.xreco.nmr.backend.api.Resource
import eu.xreco.nmr.backend.config.MinioConfig
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import eu.xreco.nmr.backend.model.api.status.ErrorStatusException
import io.javalin.http.Context
import io.javalin.openapi.*
import io.minio.GetObjectArgs
import io.minio.MinioClient

@OpenApi(
    summary = "Gets the media asset content for the asset specified by the provided ID.",
    path = "/api/assets/content/{assetId}",
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

        /* Set header. */
        context.status(200)
        context.header("Content-Type", result.headers()["Content-Type"] ?: "application/octet-stream")
        context.header("Content-Length", result.headers()["Content-Length"] ?: "0")
        context.header("Cache-Control", "max-age=31622400")

        /* Send result */
        context.result(result)
    } catch (e: Throwable) {
        throw ErrorStatusException(500, "Failed to load asset ${assetId}.")
    }
}

@OpenApi(
    summary = "Gets the media asset preview for the asset specified by the provided ID.",
    path = "/api/assets/preview/{assetId}",
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
fun getPreviewResource(context: Context, client: MinioClient) {
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