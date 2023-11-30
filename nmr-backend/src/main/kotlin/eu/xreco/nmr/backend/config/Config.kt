package eu.xreco.nmr.backend.config

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.config.ConnectionConfig
import org.vitrivr.engine.core.config.SchemaConfig

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

        /* TODO: Specify fields. */
        fields = listOf(),

        /* TODO: Specify exporters. */
        exporters = listOf()
    ),

    /** Configuration of MinIO. */
    val minio: MinioConfig = MinioConfig(
        url = (System.getenv("MINIO_HOST") ?: "http://localhost:9000"),
        accessKey = (System.getenv("MINIO_ACCESS_KEY") ?: "nmr-backend"),
        secretKey = (System.getenv("MINIO_SECRET_KEY") ?: "nmr-backend")
    )
)
