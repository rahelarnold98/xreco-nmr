package eu.xreco.nmr.backend.model.cineast

import kotlinx.serialization.Serializable

/**
 * Represents a media segment in the Cineast data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0.
 */
@Serializable
data class MediaSegment(
    val segmentid: String,
    val objectid: String,
    val segmentnumber: Int,
    val segmentstart: Long,
    val segmentend: Long,
    val segmentrepresentative: Long,
    val segmentstartabs: Float,
    val segmentendabs: Float,
    val segmentrepresentativeabs: Float,
) {
}