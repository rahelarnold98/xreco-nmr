package eu.xreco.nmr.backend.model.features

import kotlinx.serialization.Serializable

/**
 * Represents a float vector based feature in the XRECO NMR Backend data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0.
 */
@Serializable
data class LabelFeature(val mediaObjectId: String, val label: String, val start: Long? = null, val end: Long? = null) {

}