package eu.xreco.nmr.backend.model.api.retrieval

data class Media(
    val mediaResourceId: String,
    val title: String? = null,
    val description: String? = null,
    val uri: String,
    val path: String
)
