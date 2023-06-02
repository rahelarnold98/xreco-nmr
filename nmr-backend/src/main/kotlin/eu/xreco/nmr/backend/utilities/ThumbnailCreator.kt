package eu.xreco.nmr.backend.utilities

import kotlinx.coroutines.runBlocking
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import java.awt.Image
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import javax.imageio.ImageIO


/**
 * A helper class that can be used to create thumbnails from media resources. Currently supported are videos and images.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
object ThumbnailCreator {

    /** A [Java2DFrameConverter] used for [Frame] conversion. */
    private val converter by lazy { Java2DFrameConverter()  }

    /**
     * Extracts and returns a thumbnail from the provided image.
     *
     * @param path [Path] to the image file to extract thumbnail from.
     * @param maximumSize The maximum size of the thumbnail (i.e., the maximum bounding box).
     * @return [BufferedImage]
     */
    fun thumbnailFromImage(path: Path, maximumSize: Int) = Files.newInputStream(path, StandardOpenOption.READ).use { input ->
        resize(ImageIO.read(input), maximumSize)
    }

    /**
     * Extracts and returns a thumbnail from the provided video.
     *
     * @param path [Path] to the video file to extract thumbnail from.
     * @param frameNumber The frame number within the video to generate thumbnail for.
     * @param maximumSize The maximum size of the thumbnail (i.e., the maximum bounding box).
     * @return [BufferedImage]
     */
    fun thumbnailFromVideo(path: Path, time: Long, maximumSize: Int): BufferedImage? = Files.newInputStream(path, StandardOpenOption.READ).use {
         input ->
        // TODO check
        val grabber = FFmpegFrameGrabber(input)
        grabber.start()
        grabber.setVideoTimestamp(time)
        val frame = grabber.grabKeyFrame() ?: return null
        val image = converter.getBufferedImage(frame) ?: return null
        grabber.stop()

        /* Scale image. */
        return resize(image, maximumSize)
    }

    /**
     * Resized the provided [BufferedImage] and returns the resized version.
     *
     * @param image The [BufferedImage] to resize.
     * @param maximumSize The maximum size of the image (i.e., the maximum bounding box).
     * @return [BufferedImage]
     */
    fun resize(image: BufferedImage, maximumSize: Int): BufferedImage {
        val (width, height) = if (image.height > image.width) {
           (image.width.toDouble() / image.height * maximumSize).toInt() to maximumSize
        } else {
            maximumSize to (image.height.toDouble() / image.width * maximumSize).toInt()
        }

        /* Resize image. */
        val thumbnail = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = thumbnail.createGraphics()
        graphics.drawImage(image.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null)
        graphics.dispose()
        return thumbnail
    }
}

private operator fun Number.times(i: Int): Long {
    return this * i
}
