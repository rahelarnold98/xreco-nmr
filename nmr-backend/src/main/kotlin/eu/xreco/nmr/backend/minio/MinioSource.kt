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
import java.util.*

data class MinioSource(
    override val sourceId: SourceId, val minio: MinioClient, val bucket: String
) : Source {
    override val metadata: MutableMap<String, Any> = mutableMapOf()

    private val tags: Tags by lazy {  minio.getObjectTags(
        GetObjectTagsArgs.builder().bucket(this.bucket).`object`(this.sourceId.toString()).build()) }

    override val name: String
        get() = tags.get()["filename"]?: throw IllegalArgumentException("Filename not defined")

    override val timestamp: Long
        get() = tags.get()["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()


    override val type: MediaType
        get() = tags.get()["type"]?.let { MediaType.valueOf(it) } ?: throw IllegalArgumentException("Unkown media type")


    override fun newInputStream(): InputStream = minio.getObject(
        GetObjectArgs.builder().bucket(MinioConfig.ASSETS_BUCKET).`object`(this.sourceId.toString()).build()
    )
}