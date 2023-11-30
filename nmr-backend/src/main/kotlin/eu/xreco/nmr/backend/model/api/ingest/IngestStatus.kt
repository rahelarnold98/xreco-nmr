package eu.xreco.nmr.backend.model.api.ingest

import kotlinx.serialization.Serializable

/**
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Serializable
data class IngestStatus(val jobId: String, val assetId: String, val timestamp: Long = System.currentTimeMillis())