package eu.xreco.nmr.backend.model.api.basket

import kotlinx.serialization.Serializable

/**
 * A preview of a [Basket] as returned by the XRECO NMR API.
 * 
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Serializable
data class BasketPreview(val basketId: Int, val name: String, val size: Int)