package eu.xreco.nmr.backend.cli

import com.github.ajalt.clikt.core.NoOpCliktCommand
import kotlin.system.exitProcess

class QuitCommand :
    NoOpCliktCommand(name = CliCommands.QUIT.name.lowercase(), help = "Terminates NMR backend") {
  override fun run() {
    exitProcess(0)
  }
}
