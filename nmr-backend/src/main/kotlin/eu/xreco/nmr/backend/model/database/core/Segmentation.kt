package eu.xreco.nmr.backend.model.database.core

import eu.xreco.nmr.backend.model.database.Entity
import eu.xreco.nmr.backend.model.database.EntityObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.vitrivr.cottontail.client.language.ddl.CreateEntity
import org.vitrivr.cottontail.client.language.ddl.CreateIndex
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.types.Types
import org.vitrivr.cottontail.grpc.CottontailGrpc

/**
 * Represents a media resource in the XRECO data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0.
 */
@Serializable
data class Segmentation(
    val mediaResourceId: String,
    val segment: Int,
    val start:Float,
    val end: Float,
    val rep: Float
) : EntityObject {

    @Transient
    override val entity: Entity = Segmentation

    companion object : Entity {
        override val name: String = "segmentation"
        override fun create(schema: String): CreateEntity = CreateEntity("$schema.$name")
            .column(name = "mediaResourceId", type = "String", nullable = false)
            .column(name = "segment", type ="Int", nullable = false)
            .column(name = "start", type ="Float", nullable = false)
            .column(name = "end", type ="Float", nullable = false)
            .column(name = "rep", type ="Float", nullable = false)
    }
}