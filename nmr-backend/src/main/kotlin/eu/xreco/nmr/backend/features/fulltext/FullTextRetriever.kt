package eu.xreco.nmr.backend.features.fulltext

import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query

class FullTextRetriever(field: Schema.Field<ContentElement<*>, StringDescriptor>, query: Query, context: QueryContext) : AbstractRetriever<ContentElement<*>, StringDescriptor>(field, query, context)