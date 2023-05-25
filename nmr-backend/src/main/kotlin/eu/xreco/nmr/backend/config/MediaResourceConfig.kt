package eu.xreco.nmr.backend.config

import kotlinx.serialization.Serializable

/**
 * Configuration for the XRECO NMR backend API.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
@Serializable
data class MediaResourceConfig(val thumbnails: String = "../thumbnails", val videos: String  = "../videos")
