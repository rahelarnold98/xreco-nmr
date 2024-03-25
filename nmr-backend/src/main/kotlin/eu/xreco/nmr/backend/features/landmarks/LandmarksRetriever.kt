package eu.xreco.nmr.backend.features.landmarks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved

/**
 * [LandmarksRetriever] implementation for external Landmarks image feature retrieval.
 *
 * @param field Schema field for which the retriever operates.
 * @param query The query vector for proximity-based retrieval.
 * @param context The [QueryContext] used to execute the query with.
 *
 * @see [AbstractRetriever]
 * @see [ProximityQuery]
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class LandmarksRetriever(field: Schema.Field<ContentElement<*>, LabelDescriptor>, query: SimpleFulltextQuery, context: QueryContext) : AbstractRetriever<ContentElement<*>, LabelDescriptor>(field, query, context) {
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> = flow {
        this@LandmarksRetriever.reader.getAll(this@LandmarksRetriever.query).forEach {
            emit(it)
        }
    }
}
