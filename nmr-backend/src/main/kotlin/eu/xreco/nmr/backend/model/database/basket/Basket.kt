package eu.xreco.nmr.backend.model.database.basket

import eu.xreco.nmr.backend.model.database.Entity
import eu.xreco.nmr.backend.model.database.EntityObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.vitrivr.cottontail.client.language.ddl.CreateEntity
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.types.Types

/**
 * A [Basket] in the XRECO data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Serializable
data class Basket(val baskedId: Int, val name: String) : EntityObject {

	@Transient
	override val entity: Entity = Basket

	companion object : Entity {
		override val name: String = "baskets"
		override fun create(schema: String): CreateEntity = CreateEntity("$schema.$name")
			.column(name = Name.ColumnName("baskedId"), type = Types.Int, nullable = false, autoIncrement = true)
			.column(name = Name.ColumnName("name"), type = Types.String, nullable = false)
	}
}