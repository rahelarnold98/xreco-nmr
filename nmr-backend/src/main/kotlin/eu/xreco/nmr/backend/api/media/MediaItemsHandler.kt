package eu.xreco.nmr.backend.api.media

import eu.xreco.nmr.backend.api.Basket
import eu.xreco.nmr.backend.config.Config
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import eu.xreco.nmr.backend.model.api.status.ErrorStatusException
import eu.xreco.nmr.backend.model.cineast.MediaType
import eu.xreco.nmr.backend.model.database.core.MediaResource
import eu.xreco.nmr.backend.utilities.MimeTypeHelper
import eu.xreco.nmr.backend.utilities.ThumbnailCreator
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.javalin.http.Context
import io.javalin.openapi.*
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import org.vitrivr.cottontail.client.SimpleClient
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dql.Query
import java.awt.Image
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import javax.imageio.ImageIO


@OpenApi(
    summary = "Generates and returns a thumbnail of a media resource.",
    path = "/api/media/thumbnail/{mediaResourceId}/{timestamp}",
    tags = [Basket],
    operationId = "getThumbnailForResource",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "mediaResourceId", type = String::class, "ID of the media resource to create thumbnail for.", required = true),
        OpenApiParam(name = "timestamp", type = Long::class, "Timestamp of required preview (video only).", required = false),
    ],
    responses = [
        OpenApiResponse("200"),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun getThumbnail(context: Context, client: SimpleClient, config: Config) {
    val mediaResourceId = context.pathParam("mediaResourceId")
    val timestamp = context.pathParam("timestamp").toLongOrNull()

    /* If thumbnail does not exist exists in cache, generate it using JavaCV. */
    val cachePath = Paths.get(config.media.thumbnails, "${mediaResourceId}_$timestamp.jpg")
    if (!Files.exists(cachePath)) {
        /* Find media resource entry. */
        val query = Query("${config.database.schemaName}.${MediaResource.name}").where(Compare("mediaResourceId", "=", mediaResourceId))
        val (relative, type) = try {
            var path: String? = null
            var type: Int? = null
            client.query(query).forEach {
                path = it.asString("path")
                type = it.asInt("type")
            }
            path to MediaType.values()[type ?: throw ErrorStatusException(404, "Could not find media resource ${mediaResourceId}.")]
        } catch (e: StatusRuntimeException) {
            null
        } ?: throw ErrorStatusException(404, "Could not find media resource ${mediaResourceId}.")

        /* Construct video path. */
        val videoPath = Paths.get(config.media.resources, relative)
        if (!Files.exists(videoPath)) {
            throw ErrorStatusException(404, "Could not find thumbnails for media resource $mediaResourceId.")
        }

        /* Extract image and store it. */
        try {
            val thumbnail = when(type) {
                MediaType.VIDEO -> {
                    if (timestamp == null) throw ErrorStatusException(400, "Must specify timestamp to extract thumbnail from video.")
                    ThumbnailCreator.thumbnailFromVideo(videoPath, timestamp, config.media.thumbnailSize)
                }
                MediaType.IMAGES -> ThumbnailCreator.thumbnailFromImage(videoPath, config.media.thumbnailSize)
                else -> throw ErrorStatusException(400, "Generation of thumbnails is currently not supported for media type $type.")
            }
            Files.newOutputStream(cachePath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE).use { output ->
                ImageIO.write(thumbnail, "jpg", output)
            }
        } catch (e: FrameGrabber.Exception) {
            throw ErrorStatusException(500, "Failed to create thumbnail for video: ${e.message}")
        }
    }

    /* Send image. */
    context.sendFile(cachePath)
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
        context.json(config.media.resources + "/" + mediaResourceId +".mp4")

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

/**
 * Sends a file via this [Context].
 *
 * @param path The [Path] to the file to send.
 */
private fun Context.sendFile(path: Path) {
    if (!Files.exists(path)) throw ErrorStatusException(404, "Could not find file $path")
    val mimeType = MimeTypeHelper.mimeType(path.toFile())
    this.status(200)
    this.header("Cache-Control", "max-age=31622400")
    this.contentType(mimeType)
    this.result(Files.newInputStream(path, StandardOpenOption.READ))
}