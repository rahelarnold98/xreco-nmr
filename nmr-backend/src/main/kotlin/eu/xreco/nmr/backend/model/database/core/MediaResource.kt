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
data class MediaResource(val mediaResourceId: String, val title: String? = null, val description: String? = null, val uri: String, val path: String): EntityObject {

    @Transient
    override val entity: Entity = MediaResource

    companion object: Entity {
        override val name: String = "media_resources"
        override fun create(schema: String): CreateEntity = CreateEntity("$schema.$name")
            .column(name = Name.ColumnName("mediaResourceId"), type = Types.String, nullable = false)
            .column(name = Name.ColumnName("type"), type = Types.Int, nullable = false)
            .column(name = Name.ColumnName("title"), type = Types.String, nullable = true)
            .column(name = Name.ColumnName("description"), type = Types.String, nullable = true)
            .column(name = Name.ColumnName("uri"), type = Types.String, nullable = false)
            .column(name = Name.ColumnName("path"), type = Types.String, nullable = false)


        override fun indexes(schema: String): List<CreateIndex> = listOf(
            CreateIndex(Name.EntityName(schema, name), CottontailGrpc.IndexType.BTREE_UQ).name("idx_uq_mediaResourceId").column("mediaResourceId"),
        )
    }
}