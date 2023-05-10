package eu.xreco.nmr.backend.model.cineast

import kotlinx.serialization.Serializable

/**
 * Represents a media object in the Cineast data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0.
 */
@Serializable
@Deprecated("This class is deprecated and will be removed in the future.")
data class LegacyLandmarks(val id: String, val feature: String)