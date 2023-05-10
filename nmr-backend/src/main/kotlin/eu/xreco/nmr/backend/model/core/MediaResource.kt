package eu.xreco.nmr.backend.model.core

import eu.xreco.nmr.backend.model.Table
import kotlinx.serialization.Serializable
import org.vitrivr.cottontail.client.language.basics.Type
import org.vitrivr.cottontail.client.language.ddl.CreateEntity

/**
 * Represents a media resource in the XRECO data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0.
 */
@Serializable
data class MediaResource(val mediaResourceId: String, val title: String? = null, val description: String? = null, val uri: String, val path: String) {
    companion object: Table {
        override val name: String = "media_resources"
        override fun create(): CreateEntity = CreateEntity(name)
            .column(name = "mediaResourceId", type = Type.STRING, nullable = false)
            .column(name = "title", type = Type.STRING, nullable = true)
            .column(name = "description", type = Type.STRING, nullable = true)
            .column(name = "uri", type = Type.STRING, nullable = false)
            .column(name = "path", type = Type.STRING, nullable = false)
    }
}