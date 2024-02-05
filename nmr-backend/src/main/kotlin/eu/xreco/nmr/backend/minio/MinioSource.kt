package eu.xreco.nmr.backend.minio

import eu.xreco.nmr.backend.config.MinioConfig
import io.minio.GetObjectArgs
import io.minio.GetObjectTagsArgs
import io.minio.MinioClient
import io.minio.messages.Tags
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.Source
import org.vitrivr.engine.core.source.SourceId
import java.io.InputStream
import java.lang.IllegalArgumentException

/**
 *  A [Source] implementation that is backed by a min.io BLOB store.
 *
 *  @author Rahel Arnold
 *  @author Ralph Gasser
 *  @version 1.0.0
 */
data class MinioSource(
    /** [SourceId] associated with this [MinioSource]. Used for lookup in minio. */
    override val sourceId: SourceId,

    /** The name of the min.io bucket associated with this [MinioSource]. */
    val bucket: String,

    /** The [MinioClient] used to connect to min.io. */
    val minio: MinioClient
) : Source {
    companion object {
        const val FILENAME_TAG_NAME = "filename"
        const val TIMESTAMP_TAG_NAME = "timestamp"
        const val MEDIA_TYPE_TAG_NAME = "media_type"
        const val MIME_TYPE_TAG_NAME = "mime_type"
    }

    /** The [Tags] associated with this [MinioSource]. Are used */
    private val tags: Tags by lazy {
        this.minio.getObjectTags(
            GetObjectTagsArgs.builder().bucket(this.bucket).`object`(this.sourceId.toString()).build()
        ) ?: Tags()
    }

    /** [MutableMap] of metadata items associated with this [MinioSource]. */
    override val metadata: MutableMap<String, Any> by lazy {
        this.tags.get().toMutableMap()
    }

    /** The name of this [MinioSource]. */
    override val name: String
        get() = this.tags.get()[FILENAME_TAG_NAME]?: ""

    /** The mime type of this [MinioSource]. */
    val mimeType: String
        get() = this.tags.get()[MIME_TYPE_TAG_NAME]?: ""

    /** The [MediaType] associated with this [MinioSource]. */
    override val type: MediaType
        get() = this.tags.get()[MEDIA_TYPE_TAG_NAME]?.let { MediaType.valueOf(it) } ?: throw IllegalArgumentException("Unknown media type")

    /** The name of this [MinioSource]. */
    override val timestamp: Long
        get() = this.tags.get()[TIMESTAMP_TAG_NAME]?.toLongOrNull() ?: System.currentTimeMillis()

    /**
     * Loads the asset from minio and returns it as an [InputStream].
     *
     * @return [InputStream] of the object.
     */
    override fun newInputStream(): InputStream = this.minio.getObject(
        GetObjectArgs.builder().bucket(MinioConfig.ASSETS_BUCKET).`object`(this.sourceId.toString()).build()
    )
}