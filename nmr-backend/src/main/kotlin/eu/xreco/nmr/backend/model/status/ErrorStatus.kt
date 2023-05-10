package eu.xreco.nmr.backend.model.status

import kotlinx.serialization.Serializable

/**
 * An [ErrorStatus] as returned by the NMR API.
 *
 * @author Rahel Arnold
 */
@Serializable data class ErrorStatus(val code: Int, val description: String)
