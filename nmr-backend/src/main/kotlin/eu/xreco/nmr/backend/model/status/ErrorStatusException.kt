package eu.xreco.nmr.backend.model.status

/**
 * A [ErrorStatusException] as used by the NMR API.
 *
 * @author Rahel Arnold
 */
data class ErrorStatusException(val code: Int, override val message: String) : Exception(message) {
    fun toStatus() = ErrorStatus(this.code, this.message)
}
