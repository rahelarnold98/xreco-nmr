package eu.xreco.nmr.backend.config

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.config.*

/**
 * XRECO NMR backend configuration, deserialized form of config.json-like files.
 *
 * @author Rahel Arnold
 * @author Ralph Gasser
 * @version 1.1.0
 */
@Serializable
data class Config(
    /** Configuration for the RESTful API. */
    val api: APIConfig = APIConfig(
        System.getenv("NMR_BACKEND_HOST")?.toInt() ?: 7070
    ),

    /** Configuration of vitrivr schema. */
    val schema: SchemaConfig = SchemaConfig(
        name = "xreco",
        connection = ConnectionConfig(
            database = "CottontailConnectionProvider",
            parameters = mapOf(
                "host" to (System.getenv("COTTONTAILDB_HOST") ?: "localhost"),
                "port" to (System.getenv("COTTONTAILDB_PORT") ?: "1865")
            )
        ),

        fields = listOf(
            FieldConfig("file", "FileSourceMetadata"),
            //FieldConfig("video", "VideoSourceMetadata"),
            FieldConfig("time", "TemporalMetadata"),
            FieldConfig("averagecolor", "AverageColor"),
            //FieldConfig("clip", "CLIP", mapOf("host" to "http://localhost:8888/" )),
            //FieldConfig("sphericalHarmonics", "SphericalHarmonics")
            // TODO add landmarks, cdva (?) and CERTH feature as soon as ready
            //FieldConfig("landmark", "Landmark")
        ),

        exporters = listOf(
            ExporterConfig(
                "thumbnail",
                "ThumbnailExporter",
                ResolverConfig("DiskResolver")
            )
        ),
        extractionPipelines = listOf(
            PipelineConfig("Image", "./IngestPipelines/Image.json"),
            PipelineConfig("Video", "./IngestPipelines/Video.json"),
            //PipelineConfig("M3D", "./IngestPipelines/M3D.json"),
        )
    ),

    /** Configuration of MinIO. */
    val minio: MinioConfig = MinioConfig(
        url = (System.getenv("MINIO_HOST") ?: "http://localhost:9000"),
        accessKey = (System.getenv("MINIO_ACCESS_KEY") ?: "nmr-backend"),
        secretKey = (System.getenv("MINIO_SECRET_KEY") ?: "nmr-backend")
    )
)
