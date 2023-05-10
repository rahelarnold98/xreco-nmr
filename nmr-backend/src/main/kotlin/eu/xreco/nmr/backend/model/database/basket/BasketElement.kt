package eu.xreco.nmr.backend.model.database.basket

import eu.xreco.nmr.backend.model.database.Entity
import eu.xreco.nmr.backend.model.database.EntityObject
import eu.xreco.nmr.backend.model.database.core.MediaResource
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.vitrivr.cottontail.client.language.ddl.CreateEntity
import org.vitrivr.cottontail.core.types.Types

/**
 * A [BasketElement] in the XRECO data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Serializable
data class BasketElement(val baskedId: Int, val mediaResourceId: String): EntityObject {

    @Transient
    override val entity: Entity = BasketElement

    companion object: Entity {
        override val name: String = "basket_elements"

        override fun create(): CreateEntity = CreateEntity(name)
            .column(name = "baskedId", type = Types.Int, nullable = false)
            .column(name = "mediaResourceId", type = Types.String, nullable = false)

    }
}