package eu.xreco.nmr.backend.model.api.retrieval

enum class MediaType(t: String) {
    VIDEO("VIDEO"),
    IMAGE("IMAGE"),
    AUDIO("AUDIO"),
    MODEL3D("MODEL3D"),
    UNKNOWN("UNKNOWN"),
}


fun getMediaType(i: Int): MediaType {
    when(i){
        0 -> return MediaType.VIDEO
        1 -> return MediaType.IMAGE
        2 -> return MediaType.AUDIO
        3 -> return MediaType.MODEL3D
    }
    return MediaType.UNKNOWN
}