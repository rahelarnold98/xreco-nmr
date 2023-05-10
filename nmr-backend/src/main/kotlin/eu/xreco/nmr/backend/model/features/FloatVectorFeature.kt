package eu.xreco.nmr.backend.model.features

/**
 * Represents a float vector based [Feature] in the XRECO data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0.
 */
interface FloatVectorFeature: Feature {
    val feature: FloatArray
}