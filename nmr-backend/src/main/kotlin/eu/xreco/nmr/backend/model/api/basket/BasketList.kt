package eu.xreco.nmr.backend.model.api.basket

import kotlinx.serialization.Serializable

@Serializable
data class BasketList(val baskets: List<Basket>)
