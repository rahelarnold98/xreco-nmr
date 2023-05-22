package eu.xreco.nmr.backend.model.api.retrieval

/**
 * A [RetrievalResult] object as returned by XRECO NRM backend retrieval API.
 *
 * @author Rahel Arnold
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class RetrievalResult(val page: Int, val pageSize: Int, val count: Long, val items: List<ScoredMediaItem>)
