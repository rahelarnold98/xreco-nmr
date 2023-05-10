package eu.xreco.nmr.backend.model.database.core

import eu.xreco.nmr.backend.model.database.Entity
import eu.xreco.nmr.backend.model.database.EntityObject
import eu.xreco.nmr.backend.model.database.basket.Basket
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.vitrivr.cottontail.client.language.ddl.CreateEntity
import org.vitrivr.cottontail.core.types.Types

/**
 * Represents a media resource in the XRECO data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0.
 */
@Serializable
data class MediaResource(val mediaResourceId: String, val title: String? = null, val description: String? = null, val uri: String, val path: String): EntityObject {

    @Transient
    override val entity: Entity = MediaResource

    companion object: Entity {
        override val name: String = "media_resources"
        override fun create(schema: String): CreateEntity = CreateEntity("$schema.${Basket.name}")
            .column(name = "mediaResourceId", type = Types.String, nullable = false)
            .column(name = "type", type = Types.Int, nullable = false)
            .column(name = "title", type = Types.String, nullable = true)
            .column(name = "description", type = Types.String, nullable = true)
            .column(name = "uri", type = Types.String, nullable = false)
            .column(name = "path", type = Types.String, nullable = false)
    }
}