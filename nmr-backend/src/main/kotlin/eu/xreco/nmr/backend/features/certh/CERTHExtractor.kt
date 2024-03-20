package eu.xreco.nmr.backend.features.certh

import eu.xreco.nmr.backend.features.landmarks.Landmarks
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.Model3DContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.ContentAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.model3d.ModelHandler

/**
 * [CERTHExtractor] implementation of an [AbstractExtractor] for [CERTH].
 *
 * @param field Schema field for which the extractor generates descriptors.
 * @param input Operator representing the input data source.
 * @param persisting Flag indicating whether the descriptors should be persisted.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class CERTHExtractor(input: Operator<Retrievable>, field: Schema.Field<Model3DContent, FloatVectorDescriptor>, persisting: Boolean = true, private val certh: CERTH) : AbstractExtractor<Model3DContent, FloatVectorDescriptor>(input, field, persisting) {
    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *
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
    override fun extract(retrievable: Retrievable): List<FloatVectorDescriptor> {
        check(retrievable.filteredAttributes(ContentAttribute::class.java).any { it.type == ContentType.MESH }) { "Incoming retrievable is not a retrievable with MESH content. This is a programmer's error!" }
        val content = retrievable.filteredAttributes(ContentAttribute::class.java)

        return content.map {
            c -> FloatVectorDescriptor(retrievableId = retrievable.id, vector = certh.requestDescriptor(c.content),
            transient = !this.persisting) }
    }
}
