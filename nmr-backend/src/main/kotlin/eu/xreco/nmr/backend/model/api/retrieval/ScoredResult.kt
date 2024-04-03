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
    /**
     * The ID of the source asset (i.e., the video, image or 3D model) as ingested.
     *
     * Can be used to access the original asset file.
     */
    val assetId: String,

    /**
     * The ID of the part of the asset (i.e., the video segment)
     *
     * Can be used to access preview images or perform similarity search.
     */
    val partId: String,

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
         * @param part The [Retrieved] to map.
         * @return [ScoredResult]
         */
        fun from(part: Retrieved): ScoredResult {
            val asset = part.attributes.filterIsInstance<RelationshipAttribute>().flatMap { it.relationships }.firstOrNull { it.pred == "partOf" }?.obj
            val score = part.attributes.filterIsInstance<ScoreAttribute>().firstOrNull()?.score?.toDouble()

            /* Construct result item based on whether source is separate or not. */
            return if (asset != null) {
                var result = ScoredResult(assetId = asset.first.toString(), partId = part.id.toString(), score ?: 0.0)

                /* Append temporal metadata. */
                val temporal = part.attributes.filterIsInstance<DescriptorAttribute>().firstOrNull { it.descriptor is TemporalMetadataDescriptor }?.descriptor as? TemporalMetadataDescriptor
                result = if (temporal != null) {
                    result.copy(start = temporal.startNs.value / 10e9f, end = temporal.endNs.value / 10e9f)
                } else {
                    result
                }

                /* Append descriptive metadata. */
                val description = asset.second?.attributes?.filterIsInstance<DescriptorAttribute>()?.firstOrNull { it.descriptor is XRecoMetadataDescriptor }?.descriptor as? XRecoMetadataDescriptor
                if (description != null) {
                    result.copy(title = description.title?.value, description = description.description?.value, license = description.license?.value)
                } else {
                    result
                }
            } else {
                val result = ScoredResult(assetId = part.id.toString(), partId = part.id.toString(), score ?: 0.0)

                /* Append descriptive metadata. */
                val description = part.attributes.filterIsInstance<DescriptorAttribute>().firstOrNull { it.descriptor is XRecoMetadataDescriptor }?.descriptor as? XRecoMetadataDescriptor
                if (description != null) {
                    result.copy(title = description.title?.value, description = description.description?.value, license = description.license?.value)
                } else {
                    result
                }
            }
        }
    }
}
