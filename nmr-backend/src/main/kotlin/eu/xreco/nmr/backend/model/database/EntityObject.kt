package eu.xreco.nmr.backend.model.database


/**
 * Represents an instance of a database [Entity] (= entry) in the XRECO data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface EntityObject {

    /** The [Entity] this [EntityObject] belongs to. */
    val entity: Entity
}