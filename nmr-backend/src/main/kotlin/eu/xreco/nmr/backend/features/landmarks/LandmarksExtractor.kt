package eu.xreco.nmr.backend.features.landmarks

import org.vitrivr.engine.base.features.external.ExternalAnalyser
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.ContentAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.RelationshipAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor

/**
 * [LandmarksExtractor] implementation of an [AbstractExtractor] for [Landmarls].
 *
 * @param field Schema field for which the extractor generates descriptors.
 * @param input Operator representing the input data source.
 * @param persisting Flag indicating whether the descriptors should be persisted.
 *
 * @author Rahel Arnold
 * @version 1.1.0
 */
class LandmarksExtractor(input: Operator<Retrievable>, field: Schema.Field<ContentElement<*>, LabelDescriptor>, persisting: Boolean = true) : AbstractExtractor<ContentElement<*>, LabelDescriptor>(input, field, persisting) {

    /** The host of the external [Landmarks] service. */
    private val host: String = field.parameters[ExternalAnalyser.HOST_PARAMETER_NAME] ?: ExternalAnalyser.HOST_PARAMETER_DEFAULT

    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *<
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean = retrievable.filteredAttributes(ContentAttribute::class.java).any { it.type == ContentType.BITMAP_IMAGE }

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<LabelDescriptor> {
        check(retrievable.filteredAttributes(ContentAttribute::class.java).any { it.type == ContentType.BITMAP_IMAGE }) { "Incoming retrievable is not a retrievable with IMAGE content. This is a programmer's error!" }

        val source = (((retrievable as Ingested).filteredAttributes(RelationshipAttribute::class.java) as HashSet).toArray()[0] as Relationship).obj.first
        val content = retrievable.filteredAttributes(ContentAttribute::class.java)

        return content.flatMap { c ->
            val analysisResults = (this.field.analyser as? Landmarks)?.analyseList(c.content, source) ?: emptyList()
            analysisResults.map { result ->
                result.copy(retrievableId = retrievable.id, transient = !this.persisting)
            }
        }
    }
}
