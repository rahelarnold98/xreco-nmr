package eu.xreco.nmr.backend.model.cineast

import kotlinx.serialization.Serializable

/**
 * Represents an ordinary, vector-based feature in the Cineast data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Serializable
data class VectorFeature(val id: String, val feature: FloatArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VectorFeature) return false

        if (id != other.id) return false
        if (!feature.contentEquals(other.feature)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + feature.contentHashCode()
        return result
    }
}