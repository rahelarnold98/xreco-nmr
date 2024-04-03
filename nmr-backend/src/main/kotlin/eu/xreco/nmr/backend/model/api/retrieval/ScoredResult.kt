package eu.xreco.nmr.backend.model.api.retrieval

import eu.xreco.nmr.backend.features.metadata.XRecoMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.DescriptorAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.RelationshipAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute

/**
 * A scored [MediaResource] as returned by the by XRECO NRM backend retrieval API.
 *
 * @author Rahel Arnold
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class ScoredResult(
    /** The ID of the source media item (i.e., the video, image or 3D model). */
    val sourceId: String,

    /** The ID of the retrievable (i.e., the segment). */
    val retrievableId: String,

    /** The assigned score. */
    val score: Double,

    /** The assigned score. */
    val title: String? = null,

    /** The assigned score. */
    val description: String? = null,

    /** The assigned score. */
    val license: String? = null,

    /** Start  timestamp of the result (video only). */
    val start: Float? = null,

    /** End  timestamp of the result (video only). */
    val end: Float? = null
) {
    companion object {
        /**
         * Generates a [ScoredResult] from a [Retrieved].
         *
         * @param retrieved The [Retrieved] to map.
         * @return [ScoredResult]
         */
        fun from(retrieved: Retrieved): ScoredResult {
            val source = retrieved.attributes.filterIsInstance<RelationshipAttribute>().flatMap { it.relationships }.filter { it.pred == "partOf" }.firstOrNull()?.obj
            val score = retrieved.attributes.filterIsInstance<ScoreAttribute>().firstOrNull()?.score?.toDouble()

            /* Construct result item based on whether source is separate or not. */
            var result = if (source != null)  {
                ScoredResult(sourceId = source.second?.id.toString(), retrieved.id.toString(), score ?: 0.0)
            } else {
                ScoredResult(sourceId =  retrieved.id.toString(), retrieved.id.toString(), score ?: 0.0)
            }

            /* Append descriptive metadata. */
            val description = retrieved.attributes.filterIsInstance<DescriptorAttribute>().firstOrNull { it.descriptor is XRecoMetadataDescriptor }?.descriptor as? XRecoMetadataDescriptor
            result = if (description != null) {
                result.copy(title = description.title?.value, description = description.description?.value, license = description.license?.value)
            } else {
                result
            }

            /* Append temporal metadata. */
            val temporal = retrieved.attributes.filterIsInstance<DescriptorAttribute>().firstOrNull { it.descriptor is TemporalMetadataDescriptor }?.descriptor as? TemporalMetadataDescriptor
            result = if (temporal != null) {
                result.copy(start = temporal.startNs.value / 10e9f, end = temporal.endNs.value / 10e9f)
            } else {
                result
            }

            return result
        }
    }
}
