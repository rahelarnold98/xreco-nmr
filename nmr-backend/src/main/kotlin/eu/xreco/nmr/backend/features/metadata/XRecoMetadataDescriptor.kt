package eu.xreco.nmr.backend.features.metadata

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/**
 * A [StructDescriptor] used to store metadata about a media object.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class XRecoMetadataDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId?,
    val title: Value.String? = null,
    val description: Value.String? = null,
    val license: Value.String? = null,
    override val transient: Boolean = false
) : StructDescriptor {
    companion object {

        /** Name of the 'title' field. */
        const val FIELD_NAME_TITLE = "title"

        /** Name of the 'description' field. */
        const val FIELD_NAME_DESCRIPTION = "description"

        /** Name of the 'license' field. */
        const val FIELD_NAME_LICENSE = "license"

        /** The field schema associated with a [FileSourceMetadataDescriptor]. */
        private val SCHEMA = listOf(
            FieldSchema(FIELD_NAME_TITLE, Type.STRING),
            FieldSchema(FIELD_NAME_DESCRIPTION, Type.STRING),
            FieldSchema(FIELD_NAME_LICENSE, Type.STRING)
        )

        /** The prototype [XRecoMetadataDescriptor]. */
        val PROTOTYPE = XRecoMetadataDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.String(""), Value.String(""), Value.String(""))
    }

    /**
     * Returns the [FieldSchema] [List ]of this [XRecoMetadataDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = SCHEMA

    /**
     * Returns the fields and its values of this [FileSourceMetadataDescriptor] as a [Map].
     *
     * @return A [Map] of this [FileSourceMetadataDescriptor]'s fields (without the IDs).
     */
    override fun values(): List<Pair<String, Any?>> = listOf(
        FIELD_NAME_TITLE to this.title,
        FIELD_NAME_DESCRIPTION to this.description,
        FIELD_NAME_LICENSE to this.license
    )
}