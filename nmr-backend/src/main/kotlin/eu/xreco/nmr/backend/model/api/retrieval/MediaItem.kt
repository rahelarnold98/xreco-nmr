package eu.xreco.nmr.backend.model.api.retrieval

import kotlinx.serialization.Serializable

/**
 * A [MediaItem] as returned by the by XRECO NRM backend search and retrieval API.
 *
 * @author Rahel Arnold
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Serializable
data class MediaItem(
    val mediaResourceId: String?,
    val title: String? = null,
    val type: Int? = null,
    val description: String? = null,
    val uri: String?,
    val path: String?
)
