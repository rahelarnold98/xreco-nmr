package eu.xreco.nmr.backend.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * XRECO NMR backend configuration, deserialized form of config.json-like files.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
@Serializable
data class Config(val api: APIConfig = APIConfig(), val database: DatabaseConfig = DatabaseConfig(), val mediaResourceConfig: MediaResourceConfig = MediaResourceConfig()
) {
    companion object {
        fun read(path: Path): Config {
            Files.newInputStream(path, StandardOpenOption.READ).use {
                return Json.decodeFromStream<Config>(it)
            }
        }
    }
}
