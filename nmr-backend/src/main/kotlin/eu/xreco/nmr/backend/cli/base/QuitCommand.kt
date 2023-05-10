package eu.xreco.nmr.backend.cli.base

import com.github.ajalt.clikt.core.NoOpCliktCommand
import eu.xreco.nmr.backend.cli.CliCommands
import kotlin.system.exitProcess

/**
 * A command that terminates the NMR backend.
 */
class QuitCommand :
    NoOpCliktCommand(name = CliCommands.QUIT.name.lowercase(), help = "Terminates XRECO NMR backend.") {
  override fun run() {
    exitProcess(0)
  }
}
