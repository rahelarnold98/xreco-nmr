package eu.xreco.nmr.backend.features.metadata

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * Implementation of the [XRecoMetadata] [Analyser], which holds metadata about individual elements.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class XRecoMetadata: Analyser<ContentElement<*>, XRecoMetadataDescriptor> {
    override val contentClasses = setOf(ContentElement::class)
    override val descriptorClass = XRecoMetadataDescriptor::class

    /**
     * Generates a prototypical [XRecoMetadataDescriptor] for this [XRecoMetadata].
     *
     * @return [XRecoMetadataDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>): XRecoMetadataDescriptor = XRecoMetadataDescriptor.PROTOTYPE


    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, XRecoMetadataDescriptor>, query: Query, context: QueryContext): Retriever<ContentElement<*>, XRecoMetadataDescriptor> {
        TODO("Not yet implemented")
    }

    /**
     * Generates and returns a new [XRecoMetadataExtractor] for the provided [Schema.Field].
     *
     * @param field The [Schema.Field] for which to create the [XRecoMetadataExtractor].
     * @param input The input [Operator]
     * @param context The [IndexContext]
     * @param persisting Whether the resulting [XRecoMetadataExtractor]s should be persisted.
     *
     * @return [XRecoMetadataExtractor]
     */
    override fun newExtractor(field: Schema.Field<ContentElement<*>, XRecoMetadataDescriptor>, input: Operator<Retrievable>, context: IndexContext, persisting: Boolean, parameters: Map<String, Any>): XRecoMetadataExtractor {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        return XRecoMetadataExtractor(input, field, persisting)
    }
}