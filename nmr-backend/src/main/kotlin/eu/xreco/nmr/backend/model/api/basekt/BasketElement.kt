package eu.xreco.nmr.backend.model.api.basekt

import kotlinx.serialization.Serializable

@Serializable
data class BasketElement(val basketId: Int, val name: String, val elements: List<String>)