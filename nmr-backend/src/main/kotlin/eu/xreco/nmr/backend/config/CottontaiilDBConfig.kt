package eu.xreco.nmr.backend.config

import kotlinx.serialization.Serializable

@Serializable
data class CottontailDBConfig(
    val host: String,
    val port: Int,
    val schemaName: String,
)
