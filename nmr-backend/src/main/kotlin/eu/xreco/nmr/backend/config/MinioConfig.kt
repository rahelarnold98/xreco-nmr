package eu.xreco.nmr.backend.config

import io.minio.MinioClient
import kotlinx.serialization.Serializable

/**
 * Configuration for the XRECO NMR backend API's MinIO backend.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Serializable
data class MinioConfig(
    /** The URL used by the MinIO client. */
    val url: String = "http://localhost:9000",

    /** The access key used to connect to the MinIO client. */
    val accessKey: String,

    /** The secret key used to connect to the MinIO client. */
    val secretKey: String
) {
    companion object {
        /** Bucket used to store assets. */
        const val ASSETS_BUCKET = "assets"

        /** Bucket used to store thumbnails. */
        const val PREVIEW_BUCKET = "previews"
    }

    /**
     * Creates and returns a new [MinioClient].
     */
    fun newClient(): MinioClient = MinioClient.builder()
        .endpoint(this.url)
        .credentials(this.accessKey, this.secretKey)
        .build()
}