package eu.xreco.nmr.backend.api.media

import eu.xreco.nmr.backend.api.Basket
import eu.xreco.nmr.backend.config.Config
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import eu.xreco.nmr.backend.model.api.status.ErrorStatusException
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.javalin.http.Context
import io.javalin.openapi.*
import org.vitrivr.cottontail.client.SimpleClient
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dql.Query
import java.util.*

@OpenApi(
    summary = "Get a preview of a media item based in a given timestamp",
    path = "/api/media/thumbnail/{mediaResourceId}/{timeStamp}",
    tags = [Basket],
    operationId = "postBasket",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "mediaResourceId", type = String::class, "Id of media", required = true),
        OpenApiParam(name = "timeStamp", type = Float::class, "Timestamp of required preview", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(String::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun getThumbnail(context: Context, client: SimpleClient, config: Config) {
    val mediaResourceId = context.pathParam("mediaResourceId")
    val timeStamp = context.pathParam("timeStamp").toFloat()
    try {
        val query = Query("${config.database.schemaName}.${"segmentation"}").where(Compare("mediaResourceId", "=", mediaResourceId)).where(Compare("start", "<=", timeStamp)).where(Compare("end", ">=", timeStamp)).select("segment")
        val resultsVec = client.query(query)

        val resQVec = LinkedList<Int>()
        //
        resultsVec.forEach { t ->
            resQVec.add(t.asInt("segment")!!)
        }
        val seg = resQVec.get(0)

        context.json(config.mediaResourceConfig.thumbnails + "/" + mediaResourceId+"_"+seg+".jpg")

    } catch (e: StatusRuntimeException) {
        when (e.status.code) {
            Status.Code.INTERNAL -> {
                throw ErrorStatusException(
                    400, "The requested table '${config.database.schemaName}.${"segmentation"} could not be found."
                )
            }

            Status.Code.NOT_FOUND -> throw ErrorStatusException(
                404, "The requested table '${config.database.schemaName}.${"segmentation"} could not be found."
            )

            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Connection is currently not available.")

            else -> {
                throw e.message?.let { ErrorStatusException(400, it) }!!
            }
        }
    }
}

@OpenApi(
    summary = "Get a path to video",
    path = "/api/media/video/{mediaResourceId}",
    tags = [Basket],
    operationId = "postBasket",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "mediaResourceId", type = String::class, "Id of media", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(String::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun getVideo(context: Context, client: SimpleClient, config: Config) {
    val mediaResourceId = context.pathParam("mediaResourceId")
    try {
        // TODO set videos to same format
        context.json(config.mediaResourceConfig.videos + "/" + mediaResourceId +".mp4")

    } catch (e: StatusRuntimeException) {
        when (e.status.code) {
            Status.Code.INTERNAL -> {
                throw ErrorStatusException(
                    400, "The requested table '${config.database.schemaName}.${"segmentation"} could not be found."
                )
            }

            Status.Code.NOT_FOUND -> throw ErrorStatusException(
                404, "The requested table '${config.database.schemaName}.${"segmentation"} could not be found."
            )

            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Connection is currently not available.")

            else -> {
                throw e.message?.let { ErrorStatusException(400, it) }!!
            }
        }
    }
}

@OpenApi(
    summary = "Get a representative frame of a media item based in a given timestamp",
    path = "/api/media/representativeFrame/{mediaResourceId}{timeStamp}",
    tags = [Basket],
    operationId = "postBasket",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "mediaResourceId", type = String::class, "Id of media", required = true),
        OpenApiParam(name = "timeStamp", type = Float::class, "Timestamp of required preview", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(Float::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun getRepresentativeFrame(context: Context, client: SimpleClient, config: Config) {
    val mediaResourceId = context.pathParam("mediaResourceId")
    val timeStamp = context.pathParam("timeStamp").toFloat()
    try {
        val query = Query("${config.database.schemaName}.${"segmentation"}").where(Compare("mediaResourceId", "=", mediaResourceId)).where(Compare("start", "<=", timeStamp)).where(Compare("end", ">=", timeStamp)).select("rep")
        val resultsVec = client.query(query)

        val resQVec = LinkedList<Float>()
        //
        resultsVec.forEach { t ->
            resQVec.add(t.asFloat("rep")!!)
        }
        val seg = resQVec.get(0)

        context.json(seg)

    } catch (e: StatusRuntimeException) {
        when (e.status.code) {
            Status.Code.INTERNAL -> {
                throw ErrorStatusException(
                    400, "The requested table '${config.database.schemaName}.${"segmentation"} could not be found."
                )
            }

            Status.Code.NOT_FOUND -> throw ErrorStatusException(
                404, "The requested table '${config.database.schemaName}.${"segmentation"} could not be found."
            )

            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Connection is currently not available.")

            else -> {
                throw e.message?.let { ErrorStatusException(400, it) }!!
            }
        }
    }
}