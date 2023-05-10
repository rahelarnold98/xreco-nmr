package eu.xreco.nmr.backend.model.features

import eu.xreco.nmr.backend.model.Table
import org.vitrivr.cottontail.client.language.basics.Type
import org.vitrivr.cottontail.client.language.ddl.CreateEntity

/**
 * Represents a label-based landmark feature in the XRECO data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class LandmarkFeature(override val mediaResourceId: String, override val label: String, override val start: Long? = null, override val end: Long? = null): LabelFeature {
    companion object: Table {
        override val name: String = "features_landmark"

        override fun create(): CreateEntity = CreateEntity(this.name)
            .column(name = "mediaResourceId", type = Type.STRING, nullable = false)
            .column(name = "label", type = Type.STRING, nullable = false)
            .column(name = "start", type = Type.LONG, nullable = false)
            .column(name = "end", type = Type.LONG, nullable = false)
    }
}