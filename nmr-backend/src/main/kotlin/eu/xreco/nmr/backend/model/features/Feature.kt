package eu.xreco.nmr.backend.model.features

import eu.xreco.nmr.backend.model.core.MediaResource

/**
 * Represents a feature in the XRECO data model. A [Feature] describes a [MediaResource] or parts thereof.
 *
 * @author Ralph Gasser
 * @version 1.0.0.
 */
interface Feature {
    /** The ID of the [MediaResource] this [Feature] describes. */
    val mediaResourceId: String

    /** The start frame of the segment this [Feature] describes. */
    val start: Long?

    /** The end frame of the segment this [Feature] describes. */
    val end: Long?

    /**
     * Returns true, if this [LabelFeature] describes a segment.
     */
    fun isSegment(): Boolean = this.start != null && this.end != null
}