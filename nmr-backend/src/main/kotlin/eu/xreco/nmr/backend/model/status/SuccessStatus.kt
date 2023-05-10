package eu.xreco.nmr.backend.model.status

import kotlinx.serialization.Serializable

/**
 * A [SuccessStatus] as returned by the NMR API.
 *
 * @author Rahel Arnold
 */
@Serializable data class SuccessStatus(val description: String)
