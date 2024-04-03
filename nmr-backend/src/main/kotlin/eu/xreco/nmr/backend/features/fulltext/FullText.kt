package eu.xreco.nmr.backend.features.fulltext

import eu.xreco.nmr.backend.features.metadata.XRecoMetadata
import eu.xreco.nmr.backend.features.metadata.XRecoMetadataExtractor
import org.vitrivr.engine.base.features.fulltext.FulltextRetriever
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

class FullText: Analyser<ContentElement<*>, StringDescriptor> {
    override val contentClasses = setOf(ContentElement::class)
    override val descriptorClass = StringDescriptor::class

    /**
     * Generates a prototypical [StringDescriptor] for this [FullText].
     *
     * @return [StringDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>): StringDescriptor = StringDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.String(""), true)


    /**
     * Generates and returns a new [FullTextRetriever] instance for this [FullText].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [FullTextRetriever] instance for this [XRecoMetadata]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, StringDescriptor>, query: Query, context: QueryContext) = FullTextRetriever(field, query, context)

    /**
     * Generates and returns a new [FullTextRetriever] instance for this [FullText].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [Content] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     * @return [FulltextRetriever]
     */
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, StringDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): FullTextRetriever {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }

        /* Prepare query parameters. */
        val text = content.filterIsInstance<TextContent>().firstOrNull() ?: throw IllegalArgumentException("No text content found in the provided content.")
        val limit = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        val attribute = context.getProperty(field.fieldName, "fulltext")

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
    override fun newExtractor(field: Schema.Field<ContentElement<*>, StringDescriptor>, input: Operator<Retrievable>, context: IndexContext, persisting: Boolean, parameters: Map<String, Any>): FullTextExtractor {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        return FullTextExtractor(input, field, persisting)
    }
}