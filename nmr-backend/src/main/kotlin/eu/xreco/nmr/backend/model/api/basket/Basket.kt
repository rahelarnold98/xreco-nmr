package eu.xreco.nmr.backend.model.api.basket

import kotlinx.serialization.Serializable

@Serializable
data class Basket(val basketId: Int, val name: String, val elements: List<String>)