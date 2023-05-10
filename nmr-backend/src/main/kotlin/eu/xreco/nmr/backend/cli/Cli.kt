package eu.xreco.nmr.backend.cli

import com.github.ajalt.clikt.core.*
import eu.xreco.nmr.backend.config.Config
import java.io.IOException
import java.util.regex.Pattern
import kotlin.system.exitProcess
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.jline.builtins.Completers
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.reader.impl.completer.AggregateCompleter
import org.jline.reader.impl.completer.EnumCompleter
import org.jline.terminal.TerminalBuilder
import org.vitrivr.cottontail.client.SimpleClient

class Cli(client: SimpleClient, config: Config) {

  companion object {
    private const val PROMPT = "NMR Backend> "
    private val LOGGER: Logger = LogManager.getLogger(Cli::class)

  }

  /**
   *
   */
  private val clikt: CliktCommand = BaseCommand().subcommands(
    SetupCommand(client, config.database.schemaName),
    HelpCommand(),
    QuitCommand()
  )

  fun loop() {
    val terminal =
        try {
          TerminalBuilder.builder().build()
        } catch (e: IOException) {
          LOGGER.error("Could not initialize terminal: ${e.message}")
          exitProcess(-1)
        }

    val completer =
        AggregateCompleter(
            EnumCompleter(CliCommands::class.java),
            Completers.FileNameCompleter(),
            Completers.FileNameCompleter())

    val lineReader = LineReaderBuilder.builder().terminal(terminal).completer(completer).build()

    while (true) {
      try {
        val line = lineReader.readLine(PROMPT).trim()
        if (line.isNotBlank()) {
          try {
            clikt.parse(splitLine(line))
          } catch (e: Exception) {
            when (e) {
              is PrintHelpMessage -> LOGGER.info(e.command.getFormattedHelp())
              is NoSuchSubcommand -> LOGGER.info("No such command exists")
              is UsageError -> LOGGER.info("Usage error")
              else -> e.printStackTrace()
            }
          }
        }
      } catch (e: EndOfFileException) {
        LOGGER.error("Error while reading from terminal due to EOF.")
        break
      } catch (e: UserInterruptException) {
        LOGGER.error("Error UserInterruptException.")
        break
      }
    }
  }

  private val lineSplitRegex: Pattern = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'")

  /**
   * This method split a string according to space except surrounding quotes.
   * https://stackoverflow.com/questions/366202/regex-for-splitting-a-string-using-space-when-not-surrounded-by-single-or-double/366532
   */
  private fun splitLine(line: String?): List<String> {
    val matchList: MutableList<String> = ArrayList()
    val regexMatcher = line?.let { lineSplitRegex.matcher(it) }
    if (regexMatcher != null) {
      while (regexMatcher.find()) {
        if (regexMatcher.group(1) != null) {
          // Add double-quoted string without the quotes
          matchList.add(regexMatcher.group(1))
        } else if (regexMatcher.group(2) != null) {
          // Add single-quoted string without the quotes
          matchList.add(regexMatcher.group(2))
        } else {
          // Add unquoted word
          matchList.add(regexMatcher.group())
        }
      }
    }
    return matchList
  }
}
