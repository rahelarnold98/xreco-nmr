package eu.xreco.nmr.backend.features.metadata

import eu.xreco.nmr.backend.features.metadata.XRecoMetadataDescriptor.Companion.FIELD_NAME_DESCRIPTION
import eu.xreco.nmr.backend.features.metadata.XRecoMetadataDescriptor.Companion.FIELD_NAME_LICENSE
import eu.xreco.nmr.backend.features.metadata.XRecoMetadataDescriptor.Companion.FIELD_NAME_TITLE
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import java.util.*

/**
 * An [AbstractExtractor] that extracts [XRecoMetadataDescriptor]s from [Ingested] objects.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class XRecoMetadataExtractor(input: Operator<Retrievable>, field: Schema.Field<ContentElement<*>, XRecoMetadataDescriptor>, persisting: Boolean = true) : AbstractExtractor<ContentElement<*>, XRecoMetadataDescriptor>(input, field, persisting, bufferSize = 1) {
    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * [XRecoMetadataExtractor] implementation only works with [Retrievable] that contain a [SourceAttribute].
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean = retrievable.hasAttribute(SourceAttribute::class.java)

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<XRecoMetadataDescriptor> {
        val source = retrievable.filteredAttribute(SourceAttribute::class.java)?.source ?: throw IllegalArgumentException("Incoming retrievable is not a retrievable with source. This is a programmer's error!")
        return if (source.metadata.containsKey(FIELD_NAME_TITLE) || source.metadata.containsKey(FIELD_NAME_DESCRIPTION) || source.metadata.containsKey(FIELD_NAME_LICENSE)) {
            listOf(
                XRecoMetadataDescriptor(
                    id = UUID.randomUUID(),
                    retrievableId = retrievable.id,
                    title = Value.String((source.metadata[FIELD_NAME_TITLE] as? String) ?: ""),
                    description = Value.String((source.metadata[FIELD_NAME_DESCRIPTION] as? String) ?: ""),
                    license = Value.String((source.metadata[FIELD_NAME_LICENSE] as? String) ?: ""),
                    transient = !persisting
                )
            )
        } else {
            emptyList()
        }
    }
}