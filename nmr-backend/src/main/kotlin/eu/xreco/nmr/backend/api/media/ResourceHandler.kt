package eu.xreco.nmr.backend.api.media

import eu.xreco.nmr.backend.api.Resource
import eu.xreco.nmr.backend.config.Config
import eu.xreco.nmr.backend.model.api.retrieval.MediaResource
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import eu.xreco.nmr.backend.model.api.status.ErrorStatusException
import eu.xreco.nmr.backend.model.cineast.MediaType
import eu.xreco.nmr.backend.utilities.MimeTypeHelper
import eu.xreco.nmr.backend.utilities.ThumbnailCreator
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.javalin.http.Context
import io.javalin.openapi.*
import org.bytedeco.javacv.FrameGrabber
import org.vitrivr.cottontail.client.SimpleClient
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dql.Query
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import javax.imageio.ImageIO

@OpenApi(
    summary = "Gets the media resource provided by the given ID.",
    path = "/api/resource/{mediaResourceId}",
    tags = [Resource],
    operationId = "getMediaResource",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "mediaResourceId", type = String::class, "ID of the media resource to access.", required = true),
    ],
    responses = [
        OpenApiResponse("200"),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun getResource(context: Context, client: SimpleClient, config: Config) {
    val mediaResourceId = context.pathParam("mediaResourceId")
    val query = Query("${config.database.schemaName}.${eu.xreco.nmr.backend.model.database.core.MediaResource.name}").where(Compare("mediaResourceId", "=", mediaResourceId))
    val (relative, type) = try {
        var path: String? = null
        var type: Int? = null
        client.query(query).forEach {
            path = it.asString("path")
            type = it.asInt("type")
        }
        path to MediaType.values()[type ?: throw ErrorStatusException(404, "Could not find media resource ${mediaResourceId}.")]
    } catch (e: StatusRuntimeException) {
        when (e.status.code) {
            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Connection is currently not available.")
            else -> throw e.message?.let { ErrorStatusException(500, it) }!!
        }
    }

    /* Construct video path. */
    val resourcePath = Paths.get(config.media.resources, relative)
    if (!Files.exists(resourcePath)) {
        throw ErrorStatusException(404, "Could not find file for media resource $mediaResourceId.")
    }

    /* Send or stream file. */
    when(type) {
        MediaType.VIDEO -> context.streamFile(resourcePath)
        else ->  context.sendFile(resourcePath)
    }
}

@OpenApi(
    summary = "Gets the media resource provided by the given ID.",
    path = "/api/resource/{mediaResourceId}/metadata",
    tags = [Resource],
    operationId = "getMediaResourceMetadata",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "mediaResourceId", type = String::class, "ID of the media resource to access metadata for.", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(MediaResource::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun getMetadata(context: Context, client: SimpleClient, config: Config) {
    val mediaResourceId = context.pathParam("mediaResourceId")
    val query = Query("${config.database.schemaName}.${eu.xreco.nmr.backend.model.database.core.MediaResource.name}").where(Compare("mediaResourceId", "=", mediaResourceId))
    try {
        val results = mutableListOf<MediaResource>()
        client.query(query).forEach {
            results.add(
                MediaResource(
                    mediaResourceId = it.asString("mediaResourceId"),
                    type = MediaType.values()[it.asInt("type")!!],
                    title = it.asString("title"),
                    description = it.asString("description"),
                    uri = it.asString("uri"),
                    path = it.asString("path")
                )
            )
        }

        if (results.isEmpty()) throw ErrorStatusException(404, "Could not find media resource with ID $mediaResourceId.")
        context.json(results.first())
    } catch (e: StatusRuntimeException) {
        when (e.status.code) {
            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Connection is currently not available.")
            else -> throw ErrorStatusException(500, e.message ?: "Unknown error!")
        }
    }
}


@OpenApi(
    summary = "Generates and returns a preview of a media resource.",
    path = "/api/resource/{mediaResourceId}/preview/{timestamp}",
    tags = [Resource],
    operationId = "getPreviewForMediaResource",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "mediaResourceId", type = String::class, "ID of the media resource to create preview for.", required = true),
        OpenApiParam(name = "timestamp", type = Long::class, "Timestamp of required preview in seconds (video only).", required = false),
    ],
    responses = [
        OpenApiResponse("200"),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun getPreview(context: Context, client: SimpleClient, config: Config) {
    val mediaResourceId = context.pathParam("mediaResourceId")
    val timestamp = context.pathParam("timestamp").toLongOrNull() ?: 0L

    /* If thumbnail does not exist exists in cache, generate it using JavaCV. */
    val cachePath = Paths.get(config.media.thumbnails, "${mediaResourceId}_$timestamp.jpg")
    if (!Files.exists(cachePath)) {
        /* Find media resource entry. */
        val query = Query("${config.database.schemaName}.${eu.xreco.nmr.backend.model.database.core.MediaResource.name}").where(Compare("mediaResourceId", "=", mediaResourceId))
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
            throw ErrorStatusException(404, "Could not find file for media resource $mediaResourceId.")
        }

        /* Extract image and store it. */
        try {
            val thumbnail = when(type) {
                MediaType.VIDEO -> ThumbnailCreator.thumbnailFromVideo(videoPath, timestamp * 1000L, config.media.thumbnailSize)
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

/**
 * Streams a file via this [Context].
 *
 * @param path The [Path] to the file to stream.
 */
private fun Context.streamFile(path: Path) {
    if (!Files.exists(path)) throw ErrorStatusException(404, "Could not find file $path")
    val mimeType = MimeTypeHelper.mimeType(path.toFile())
    this.status(200)
    this.header("Cache-Control", "max-age=31622400")
    this.contentType(mimeType)
    this.writeSeekableStream(Files.newInputStream(path, StandardOpenOption.READ), mimeType)
}