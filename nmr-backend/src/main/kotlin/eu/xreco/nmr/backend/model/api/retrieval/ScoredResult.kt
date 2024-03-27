package eu.xreco.nmr.backend.model.api.retrieval

/**
 * A scored [MediaResource] as returned by the by XRECO NRM backend retrieval API.
 *
 * @author Rahel Arnold
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class ScoredResult(
    /** The ID of the source media item (i.e., the video, image or 3D model). */
    val sourceId: String,

    /** The ID of the retrievable (i.e., the segment). */
    val retrievableId: String,

    /** The assigned score. */
    val score: Double,

    /** Start  timestamp of the result (video only). */
    val start: Float? = null,

    /** End  timestamp of the result (video only). */
    val end: Float? = null
)
