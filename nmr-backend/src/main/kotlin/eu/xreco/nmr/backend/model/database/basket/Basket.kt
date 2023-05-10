package eu.xreco.nmr.backend.model.database.basket

import eu.xreco.nmr.backend.model.database.EntityObject
import eu.xreco.nmr.backend.model.database.Entity
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.vitrivr.cottontail.client.language.ddl.CreateEntity
import org.vitrivr.cottontail.core.types.Types

/**
 * A [Basket] in the XRECO data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Serializable
data class Basket(val baskedId: Int, val name: String): EntityObject {

    @Transient
    override val entity: Entity = Basket

    companion object: Entity {
        override val name: String = "baskets"
        override fun create(): CreateEntity = CreateEntity(name)
        .column(name = "baskedId", type = Types.Int, nullable = false, autoIncrement = true)
        .column(name = "name", type = Types.String, nullable = false)
    }
}