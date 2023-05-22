package eu.xreco.nmr.backend.model.api.basekt

import kotlinx.serialization.Serializable

@Serializable
data class BasketList(val baskets: List<BasketElement>)
