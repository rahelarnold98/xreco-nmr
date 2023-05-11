package eu.xreco.nmr.backend.cli.base

import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.CliktHelpFormatter
import eu.xreco.nmr.backend.cli.import.CineastImport
import org.vitrivr.cottontail.client.SimpleClient


/**
 * A collection of commands related to data import.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ImportCommand(client: SimpleClient, schema: String = "xreco") : NoOpCliktCommand(name = "import") {
    init {
        context { helpFormatter = CliktHelpFormatter() }
        this.subcommands(CineastImport(client, schema))
    }
}
