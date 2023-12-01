package eu.xreco.nmr.backend.api.ingest

import eu.xreco.nmr.backend.model.api.ingest.IngestStatus
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import io.javalin.http.Context
import io.javalin.openapi.*
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionServer
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import org.vitrivr.engine.query.execution.RetrievalRuntime


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
fun ingest(context: Context, manager: SchemaManager, executor: ExecutionServer) {
    TODO()
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
    TODO()
}

