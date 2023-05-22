package eu.xreco.nmr.backend.model.api.retrieval

import kotlinx.serialization.Serializable

@Serializable
data class Media(
    val mediaResourceId: String?,
    val title: String? = null,
    val type: Int? = null,
    val description: String? = null,
    val uri: String?,
    val path: String?
)
