package eu.xreco.nmr.backend.model.database.features

import eu.xreco.nmr.backend.model.database.Entity
import kotlinx.serialization.Transient
import org.vitrivr.cottontail.client.language.ddl.CreateEntity
import org.vitrivr.cottontail.core.types.Types

/**
 * Represents a label-based landmark feature in the XRECO data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class LandmarkFeature(override val mediaResourceId: String, override val label: String, override val start: Long? = null, override val end: Long? = null): LabelFeature {

    @Transient
    override val entity: Entity = LandmarkFeature

    companion object: Entity {
        override val name: String = "features_landmark"

        override fun create(): CreateEntity = CreateEntity(name)
            .column(name = "mediaResourceId", type = Types.String, nullable = false)
            .column(name = "label", type = Types.String, nullable = false)
            .column(name = "start", type = Types.Long, nullable = false)
            .column(name = "end", type = Types.Long, nullable = false)
    }
}