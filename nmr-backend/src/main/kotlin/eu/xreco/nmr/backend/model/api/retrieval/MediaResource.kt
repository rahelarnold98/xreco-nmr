package eu.xreco.nmr.backend.model.api.retrieval

import eu.xreco.nmr.backend.model.cineast.MediaType
import kotlinx.serialization.Serializable

/**
 * A [MediaResource] as returned by the by XRECO NRM backend search and retrieval API.
 *
 * @author Rahel Arnold
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Serializable
data class MediaResource(
    val mediaResourceId: String?,
    val type: MediaType,
    val title: String? = null,
    val description: String? = null,
    val uri: String?,
    val path: String?
)
