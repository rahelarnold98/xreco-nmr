package eu.xreco.nmr.backend.features.metadata

import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query

/**
 *
 */
class XRecoMetadataRetriever(field: Schema.Field<ContentElement<*>, XRecoMetadataDescriptor>, query: Query, context: QueryContext) : AbstractRetriever<ContentElement<*>, XRecoMetadataDescriptor>(field, query, context)