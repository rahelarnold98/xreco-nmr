package eu.xreco.nmr.backend.features.certh

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.Model3DContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.core.model.types.Value

/**
 * [CERTHRetriever] implementation for external CERTH 3d model feature retrieval.
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
class CERTHRetriever(field: Schema.Field<Model3DContent, FloatVectorDescriptor>, query: ProximityQuery<Value.Float>, context: QueryContext) : AbstractRetriever<Model3DContent, FloatVectorDescriptor>(field, query, context) {

    companion object {
        fun scoringFunction(retrieved: Retrieved): Float {
            val distance = retrieved.filteredAttribute<DistanceAttribute>()?.distance ?: return 0f
            return 1f - distance
        }
    }

    override fun toFlow(scope: CoroutineScope) = flow {
        this@CERTHRetriever.reader.getAll(this@CERTHRetriever.query).forEach {
            it.addAttribute(ScoreAttribute(scoringFunction(it)))
            emit(it)
        }
    }
}
