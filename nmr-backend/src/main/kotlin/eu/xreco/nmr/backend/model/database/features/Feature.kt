package eu.xreco.nmr.backend.model.database.features

import eu.xreco.nmr.backend.model.database.EntityObject
import eu.xreco.nmr.backend.model.database.core.MediaResource

/**
 * Represents a feature in the XRECO data model. A [Feature] describes a [MediaResource] or parts thereof.
 *
 * @author Ralph Gasser
 * @version 1.0.0.
 */
interface Feature : EntityObject {
    /** The ID of the [MediaResource] this [Feature] describes. */
    val mediaResourceId: String

    /** The start time of the segment this [Feature] describes. */
    val start: Float?

    /** The end time of the segment this [Feature] describes. */
    val end: Float?

    /** The representative time of the segment this [Feature] describes. */
    val rep: Float?


    /**
     * Returns true, if this [LabelFeature] describes a segment.
     */
    fun isSegment(): Boolean = this.start != null && this.end != null && this.rep != null
}