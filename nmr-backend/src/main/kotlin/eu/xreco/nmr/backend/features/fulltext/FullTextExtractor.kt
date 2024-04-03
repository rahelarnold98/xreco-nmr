package eu.xreco.nmr.backend.features.fulltext

import eu.xreco.nmr.backend.features.metadata.XRecoMetadataDescriptor
import okhttp3.internal.wait
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.DescriptorAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.source.MediaType
import java.lang.Thread.sleep
import java.util.*

class FullTextExtractor(
    input: Operator<Retrievable>, field: Schema.Field<ContentElement<*>, StringDescriptor>, persisting: Boolean = true
) : AbstractExtractor<ContentElement<*>, StringDescriptor>(input, field, persisting, bufferSize = 1) {
    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * [FullTextExtractor] implementation only works with [Retrievable] that contain a [SourceAttribute].
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
    override fun extract(retrievable: Retrievable): List<StringDescriptor> {
        val source = retrievable.filteredAttribute(SourceAttribute::class.java)?.source
            ?: throw IllegalArgumentException("Incoming retrievable is not a retrievable with source. This is a programmer's error!")

        var text = (source.metadata[XRecoMetadataDescriptor.FIELD_NAME_TITLE] as? String? // title
                + " " + source.metadata[XRecoMetadataDescriptor.FIELD_NAME_DESCRIPTION] as? String // description
                )

        if (source.type == MediaType.IMAGE) {
            // landmarks
            for (attribute in retrievable.filteredAttributes(DescriptorAttribute::class.java)) {
                if (attribute.descriptor is LabelDescriptor) {
                    text += " " + (attribute.descriptor as LabelDescriptor).label.value
                }
            }
        }

        return listOf(
            StringDescriptor(
                id = UUID.randomUUID(), retrievableId = retrievable.id,
                value = Value.String(text),
                transient = !persisting
            )
        )

    }
}