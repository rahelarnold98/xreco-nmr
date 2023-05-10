package eu.xreco.nmr.backend.model.cineast

import kotlinx.serialization.Serializable

/**
 * Represents a media object in the Cineast data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0.
 */
@Serializable
data class MediaObject(val objectid: String, val mediatype: Int, val name: String, val path: String)