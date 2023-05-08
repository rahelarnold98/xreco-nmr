package eu.xreco.nmr.backend.cli

import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.CliktHelpFormatter

class
BaseCommand : NoOpCliktCommand(name = "NMR") {

  init {
    context { helpFormatter = CliktHelpFormatter() }
  }
}
