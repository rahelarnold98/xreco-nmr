package eu.xreco.nmr.backend.model.database.features

import eu.xreco.nmr.backend.model.database.Entity
import kotlinx.serialization.Transient
import org.vitrivr.cottontail.client.language.ddl.CreateEntity

/** Represents a [Float] vector based CLIP in the XRECO data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class CdvaFeature(
    override val mediaResourceId: String,
    override val feature: FloatArray,
    override val start: Long? = null,
    override val end: Long? = null
) : FloatVectorFeature {

    @Transient
    override val entity: Entity = CdvaFeature

    companion object : Entity {
        override val name: String = "features_cdva"
        override fun create(schema: String): CreateEntity = CreateEntity("$schema.$name")
            .column(name = "mediaResourceId", type = "String", nullable = false)
            .column(name = "feature", type = "Float_Vector", length = 512, nullable = false)
            .column(name = "start", type = "Long", nullable = false)
            .column(name = "end", type = "Long", nullable = false)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CdvaFeature) return false

        if (mediaResourceId != other.mediaResourceId) return false
        if (!feature.contentEquals(other.feature)) return false
        if (start != other.start) return false
        if (end != other.end) return false
        if (entity != other.entity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mediaResourceId.hashCode()
        result = 31 * result + feature.contentHashCode()
        result = 31 * result + (start?.hashCode() ?: 0)
        result = 31 * result + (end?.hashCode() ?: 0)
        result = 31 * result + entity.hashCode()
        return result
    }
}