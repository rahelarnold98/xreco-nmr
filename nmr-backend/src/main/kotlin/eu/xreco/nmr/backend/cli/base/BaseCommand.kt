package eu.xreco.nmr.backend.cli.base

import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.CliktHelpFormatter
import eu.xreco.nmr.backend.config.Config
import org.vitrivr.cottontail.client.SimpleClient

/**
 * Collection of all CLI commands offered by XRECO NMR backend.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class BaseCommand(client: SimpleClient, config: Config) : NoOpCliktCommand(name = "NMR") {

    init {
        context { helpFormatter = CliktHelpFormatter() }

        this.subcommands(
            SetupCommand(client, config.database.schemaName),
            ImportCommand(client, config.database.schemaName),
            QuitCommand()
        )
    }
}
