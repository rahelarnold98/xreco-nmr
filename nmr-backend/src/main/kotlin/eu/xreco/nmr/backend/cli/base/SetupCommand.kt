package eu.xreco.nmr.backend.cli.base

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import eu.xreco.nmr.backend.Constants
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
        /* Drop old schema if option has been specified. */
        if (this.drop) {
            try {
                this.client.drop(DropSchema(this.schema)).close()
            }  catch (e: StatusRuntimeException) {
                if (e.status.code != Status.Code.NOT_FOUND) {
                    println("An error occurred while dropping the schema ${this.schema}: ${e.message}")
                    return
                }
            }
        }

        /* Create schema. */
        try {
            this.client.create(CreateSchema(this.schema)).close()
        } catch (e: StatusRuntimeException) {
            if (e.status.code == Status.Code.ALREADY_EXISTS) {
                println("XRECO backend schema with name ${this.schema} already exists.")
                if (!this.force) {
                    return
                }
            } else {
                println("An error occurred while creating the schema ${this.schema}: ${e.message}")
                return
            }
        }

        /* Create and setup entities. */
        for (table in Constants.ENTITIES) {
            try {
                println("Creating entity '${table.name}'...")
                this.client.create(table.create()).close() /* Create entities. */

                for (i in table.indexes()) {
                    this.client.create(i).close() /* Create indexes. */
                }
            } catch (e: StatusRuntimeException) {
                if (e.status.code == Status.Code.ALREADY_EXISTS) {
                    println("XRECO entitiy with name '${this.schema}.${table.name}' already exists.")
                    if (this.force) {
                        return
                    }
                } else {
                    println("An error occurred while creating the entity '${this.schema}.${table.name}': ${e.message}")
                    return
                }
            }
        }

        println("Setup completed successfully.")
    }
}