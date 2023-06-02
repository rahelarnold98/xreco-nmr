package eu.xreco.nmr.backend.model.api.retrieval

/**
 * A scored [MediaResource] as returned by the by XRECO NRM backend retrieval API.
 *
 * @author Rahel Arnold
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class ScoredMediaItem(val id: String, val score: Double, val start: Float? = null, val end: Float? = null, val rep: Float? = null)
