package eu.xreco.nmr.backend.api.media

import eu.xreco.nmr.backend.api.Resource
import eu.xreco.nmr.backend.config.MinioConfig
import eu.xreco.nmr.backend.model.api.retrieval.ScoredResult
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import eu.xreco.nmr.backend.model.api.status.ErrorStatusException
import io.javalin.http.Context
import io.javalin.openapi.*
import io.minio.GetObjectArgs
import io.minio.MinioClient
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionServer
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.query.model.api.InformationNeedDescription
import org.vitrivr.engine.query.model.api.input.RetrievableIdInputData
import org.vitrivr.engine.query.model.api.operator.RetrieverDescription
import org.vitrivr.engine.query.model.api.operator.TransformerDescription
import org.vitrivr.engine.query.parsing.QueryParser

@OpenApi(
    summary = "Ingest one (or multiple) images into the XReco NMR backend.",
    path = "/api/assets/metadata/{assetId}",
    tags = ["Ingest"],
    operationId = "getAssetMetadata",
    methods = [HttpMethod.GET],
    responses = [
        OpenApiResponse("200", [OpenApiContent(ScoredResult::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)])
    ]
)
fun getAssetMetadata(context: Context, schema: Schema, executor: ExecutionServer) {
    val assetId = context.pathParam("assetId")

    /* Construct and parse query. */
    val query = InformationNeedDescription(
        inputs = mapOf("id" to RetrievableIdInputData(assetId)),
        operations = mapOf(
            "retriever" to RetrieverDescription(input = "id", field = ""),
            "metadata" to TransformerDescription("FieldLookup", input = "retriever", properties = mapOf("field" to "metadata", "keys" to "title,description,license"))
        ),
        output = "metadata"
    )
    val retriever = QueryParser(schema).parse(query)

    /* Execute query and return results. */
    val results = executor.query(retriever).map { ScoredResult.from(it, context)}.firstOrNull()
    if (results == null) {
        throw ErrorStatusException(404, "Could not find asset with ID '${assetId}.")
    }
    context.json(results)
}

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