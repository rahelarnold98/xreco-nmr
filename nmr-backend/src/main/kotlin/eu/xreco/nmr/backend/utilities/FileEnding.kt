package eu.xreco.nmr.backend.utilities

object FileEnding {
    private val endingMap = mapOf(
        "jpg" to "image",
        "jpeg" to "image",
        "png" to "image",
        "gif" to "image",
        "mp4" to "video",
        "mov" to "video",
        "obj" to "3d",
        "gltf" to "3d"
    )

    fun objectType(fileExtension: String) = endingMap[fileExtension] ?: "unknown"
}