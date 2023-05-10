package eu.xreco.nmr.backend.model.database.features

/**
 * Represents a float vector based [Feature] in the XRECO data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0.
 */
interface FloatVectorFeature: Feature {
    val feature: FloatArray
}