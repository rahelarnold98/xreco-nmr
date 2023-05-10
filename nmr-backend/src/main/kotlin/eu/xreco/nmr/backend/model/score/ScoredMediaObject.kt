package eu.xreco.nmr.backend.model.score

import kotlinx.serialization.Serializable

/**
 * Represents a scored (segment) of a media object as returned by  the retrieval backend.
 *
 * @author Ralph Gasser
 * @version 1.0.0.
 */
@Serializable
data class ScoredMediaObject(val mediaObjectId: String, val score: Double, val start: Long? = null, val end: Long? = null)