package eu.xreco.nmr.backend.features.metadata

import org.vitrivr.engine.base.features.fulltext.FulltextRetriever
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
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


    /**
     * Generates and returns a new [XRecoMetadataRetriever] instance for this [XRecoMetadata].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [XRecoMetadataRetriever] instance for this [XRecoMetadata]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, XRecoMetadataDescriptor>, query: Query, context: QueryContext) = XRecoMetadataRetriever(field, query, context)

    /**
     * Generates and returns a new [XRecoMetadataRetriever] instance for this [XRecoMetadata].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [Content] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     * @return [FulltextRetriever]
     */
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, XRecoMetadataDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): XRecoMetadataRetriever {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }

        /* Prepare query parameters. */
        val text = content.filterIsInstance<TextContent>().firstOrNull() ?: throw IllegalArgumentException("No text content found in the provided content.")
        val limit = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        val attribute = context.getProperty(field.fieldName, "attribute") ?: XRecoMetadataDescriptor.FIELD_NAME_DESCRIPTION

        return this.newRetrieverForQuery(field, SimpleFulltextQuery(value = Value.String(text.content), limit = limit, attributeName = attribute), context)
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