package eu.xreco.nmr.backend.model.database.features

import eu.xreco.nmr.backend.model.database.Entity
import kotlinx.serialization.Transient
import org.vitrivr.cottontail.client.language.ddl.CreateEntity
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.types.Types

/** Represents a [Float] vector based CLIP in the XRECO data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class ClipFeature(
    override val mediaResourceId: String,
    override val feature: FloatArray,
    override val start: Long? = null,
    override val end: Long? = null
) : FloatVectorFeature {

    @Transient
    override val entity: Entity = ClipFeature

    companion object : Entity {
        override val name: String = "features_clip"
        override fun create(schema: String): CreateEntity = CreateEntity("$schema.$name")
            .column(name = Name.ColumnName("mediaResourceId"), type = Types.String, nullable = false)
            .column(name = Name.ColumnName("feature"), type = Types.FloatVector(512), nullable = false)
            .column(name = Name.ColumnName("start"), type = Types.Long, nullable = false)
            .column(name = Name.ColumnName("end"), type = Types.Long, nullable = false)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClipFeature) return false

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