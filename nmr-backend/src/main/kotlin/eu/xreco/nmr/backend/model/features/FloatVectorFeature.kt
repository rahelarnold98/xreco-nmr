package eu.xreco.nmr.backend.model.features

import kotlinx.serialization.Serializable

/**
 * Represents a float vector based feature in the XRECO NMR Backend data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0.
 */
@Serializable
data class FloatVectorFeature(val mediaObjectId: String, val feature: FloatArray, val start: Long? = null, val end: Long? = null) {

    /** Returns the dimension of the feature vector. */
    val size: Int
        get() = this.feature.size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FloatVectorFeature

        if (mediaObjectId != other.mediaObjectId) return false
        if (!feature.contentEquals(other.feature)) return false
        if (start != other.start) return false
        return end == other.end
    }

    override fun hashCode(): Int {
        var result = mediaObjectId.hashCode()
        result = 31 * result + feature.contentHashCode()
        result = 31 * result + (start?.hashCode() ?: 0)
        result = 31 * result + (end?.hashCode() ?: 0)
        return result
    }
}