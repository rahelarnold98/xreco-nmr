package eu.xreco.nmr.backend.model

import org.vitrivr.cottontail.client.language.ddl.CreateEntity
import org.vitrivr.cottontail.client.language.ddl.CreateIndex

interface Table {
    /** The name of this [Table]. */
    val name: String

    /**
     * The [CreateEntity] command for this [Table].
     *
     * @return [CreateEntity] command that should be executed as part of the setup.
     */
    fun create(): CreateEntity

    /**
     * The [CreateIndex] commands that must be executed for this [Table].
     *
     * @return A [List] of [CreateIndex] commands that should be executed as part of the setup.
     */
    fun indexes(): List<CreateIndex> = emptyList()
}