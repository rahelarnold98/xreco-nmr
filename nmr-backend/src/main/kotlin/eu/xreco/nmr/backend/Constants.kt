package eu.xreco.nmr.backend

import eu.xreco.nmr.backend.model.database.Entity
import eu.xreco.nmr.backend.model.database.basket.Basket
import eu.xreco.nmr.backend.model.database.basket.BasketElement
import eu.xreco.nmr.backend.model.database.core.MediaResource
import eu.xreco.nmr.backend.model.database.features.LandmarkFeature

/**
 * A series of application constants.
 */
object Constants {
    /** List of entities that are used by the XRECO backend. */
    val ENTITIES = arrayOf<Entity>(MediaResource, LandmarkFeature, Basket, BasketElement)
}