package eu.xreco.nmr.backend.features.landmarks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute

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
class LandmarksRetriever(field: Schema.Field<ContentElement<*>, LabelDescriptor>, query: LabelDescriptor, context: QueryContext) : AbstractRetriever<ContentElement<*>, LabelDescriptor>(field, query, context) {

    companion object {
        fun scoringFunction(retrieved: Retrieved): Float {
            val distance = retrieved.filteredAttribute<DistanceAttribute>()?.distance ?: return 0f
            return 1f - distance
        }
    }

    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> {
        val k = context.getProperty(field.fieldName, "limit")?.toIntOrNull() ?: 1000 //TODO get limit
        val returnDescriptor = context.getProperty(field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false
        val reader = field.getReader()
        val query = LabelQuery(descriptor = this@LandmarksRetriever.query)
        return flow {
            reader.getAll(query).forEach {
                it.addAttribute(ScoreAttribute(scoringFunction(it)))
                emit(
                    it
                )
            }
        }
    }
}
