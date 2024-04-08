package eu.xreco.nmr.backend.features.fulltext

import eu.xreco.nmr.backend.features.metadata.XRecoMetadataDescriptor
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
import java.util.*

class FulltextExtractor(
    input: Operator<Retrievable>, field: Schema.Field<ContentElement<*>, StringDescriptor>, persisting: Boolean = true
) : AbstractExtractor<ContentElement<*>, StringDescriptor>(input, field, persisting, bufferSize = 1) {
    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * [FulltextExtractor] implementation only works with [Retrievable] that contain a [SourceAttribute].
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean = retrievable.hasAttribute(DescriptorAttribute::class.java)

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<StringDescriptor> {
        val text = StringBuilder()
        for (attribute in retrievable.filteredAttributes(DescriptorAttribute::class.java)) {
            when (val descriptor = attribute.descriptor) {
                is LabelDescriptor -> text.append(" " + descriptor.label.value)
                is StringDescriptor -> text.append(" " + descriptor.value.value)
                is XRecoMetadataDescriptor -> {
                    text.append(" " + descriptor.title)
                    text.append(" " + descriptor.description)
                    text.append(" " + descriptor.license)
                }
            }
        }
        return listOf(StringDescriptor(id = UUID.randomUUID(), retrievableId = retrievable.id, value = Value.String(text.toString()), transient = !persisting))
    }
}