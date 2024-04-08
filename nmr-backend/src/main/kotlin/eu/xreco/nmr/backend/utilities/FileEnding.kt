package eu.xreco.nmr.backend.utilities

import org.vitrivr.engine.core.source.MediaType


object FileEnding {
    private val endingMap = mapOf(
        "jpg" to MediaType.IMAGE,
        "jpeg" to MediaType.IMAGE,
        "png" to MediaType.IMAGE,
        "gif" to MediaType.IMAGE,
        "mp4" to MediaType.VIDEO,
        "mov" to MediaType.VIDEO,
        "obj" to MediaType.MESH,
        "gltf" to MediaType.MESH,
    )

    fun objectType(fileExtension: String) = this.endingMap[fileExtension] ?: throw IllegalArgumentException("File ending not supported.")
}