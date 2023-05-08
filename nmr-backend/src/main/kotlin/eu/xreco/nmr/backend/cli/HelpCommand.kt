package eu.xreco.nmr.backend.cli

import com.github.ajalt.clikt.core.NoOpCliktCommand

class HelpCommand :
    NoOpCliktCommand(name = CliCommands.HELP.name.lowercase(), help = "Prints all CLI commands") {
  override fun run() {
    println(Cli.clikt.getFormattedHelp())
  }
}
