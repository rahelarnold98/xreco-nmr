package eu.xreco.nmr.backend.config

import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** NMR backend configuration, deserialized form of config.json-like files. */
@Serializable
data class Config(
    val cottontailDB: CottontailDBConfig,
    val api: APIConfig,
) {
  companion object {
    private const val DEFAULT_CONFIG_FILE = "config.json"

    fun readConfig(config: String = DEFAULT_CONFIG_FILE): Config {
      val jsonString = File(config).readText()
      return Json.decodeFromString(serializer(), jsonString)
    }
  }
}
