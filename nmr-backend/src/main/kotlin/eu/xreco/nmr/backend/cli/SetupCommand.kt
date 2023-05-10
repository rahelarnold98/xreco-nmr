package eu.xreco.nmr.backend.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import eu.xreco.nmr.backend.model.Entities
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.vitrivr.cottontail.client.SimpleClient
import org.vitrivr.cottontail.client.language.ddl.CreateSchema
import org.vitrivr.cottontail.client.language.ddl.DropSchema

/**
 *
 */
class SetupCommand(private val client: SimpleClient, private val schema: String = "xreco"): CliktCommand(name = "setup") {

    /** */
    private val force: Boolean by option("-f", "--force", help = "Forces the setup to be executed even if the schema already exists.").flag(default = false)

    /** Flag indicating, that schema should be dropped before starting setup. */
    private val drop: Boolean by option("-d", "--drop", help = "Tries to drop the schema before executing the setup.").flag(default = false)


    /**
     * Executes the database setup.
     */
    override fun run() {
        val txId = this.client.begin() /* Begin transaction. */

        /* Drop old schema if option has been specified. */
        if (this.drop) {
            try {
                this.client.drop(DropSchema(this.schema).txId(txId)).close()
            }  catch (e: StatusRuntimeException) {
                if (e.status.code != Status.Code.NOT_FOUND) {
                    this.client.rollback(txId)
                    println("An error occurred while dropping the schema ${this.schema}: ${e.message}")
                    return
                }
            }
        }

        /* Create schema. */
        try {
            this.client.create(CreateSchema(this.schema).txId(txId)).close()
        } catch (e: StatusRuntimeException) {
            if (e.status.code == Status.Code.ALREADY_EXISTS) {
                println("XRECO backend schema with name ${this.schema} already exists.")
                if (this.force) {
                    return
                }
            } else {
                println("An error occurred while creating the schema ${this.schema}: ${e.message}")
                this.client.rollback(txId)
            }
        }

        /* Create and setup entities. */
        for (table in Entities.ENTITIES) {
            try {
                println("Creating entity '${table.name}'...")
                this.client.create(table.create().txId(txId)).close() /* Create entities. */

                for (i in table.indexes()) {
                    this.client.create(i.txId(txId)).close() /* Create indexes. */
                }
            } catch (e: StatusRuntimeException) {
                if (e.status.code == Status.Code.ALREADY_EXISTS) {
                    println("XRECO entitiy with name '${this.schema}.${table.name}' already exists.")
                    if (this.force) {
                        this.client.rollback(txId)
                        return
                    }
                } else {
                    println("An error occurred while creating the entity '${this.schema}.${table.name}': ${e.message}")
                    this.client.rollback(txId)
                    return
                }
            }
        }

        this.client.commit(txId)  /* Commit change. */
        println("Setup completed successfully.")
    }
}