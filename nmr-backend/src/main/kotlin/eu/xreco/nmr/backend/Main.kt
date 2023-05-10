package eu.xreco.nmr.backend

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import eu.xreco.nmr.backend.api.authentification.login
import eu.xreco.nmr.backend.api.authentification.logout
import eu.xreco.nmr.backend.api.basket.*
import eu.xreco.nmr.backend.api.retrieval.*
import eu.xreco.nmr.backend.cli.Cli
import eu.xreco.nmr.backend.config.Config
import eu.xreco.nmr.backend.database.CottontailDBClient
import eu.xreco.nmr.backend.model.status.ErrorStatus
import eu.xreco.nmr.backend.model.status.ErrorStatusException
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.http.Header
import io.javalin.http.staticfiles.Location
import io.javalin.openapi.CookieAuth
import io.javalin.openapi.plugin.OpenApiPlugin
import io.javalin.openapi.plugin.OpenApiPluginConfiguration
import io.javalin.openapi.plugin.SecurityComponentConfiguration
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration
import io.javalin.openapi.plugin.swagger.SwaggerPlugin
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/** Version of NMR Backend API. */
const val VERSION = "2.0.0"

/** The main function and entrypoint for the NMR backend application. */
class Main : CliktCommand(name = "NMR") {

  private val LOGGER: Logger = LogManager.getLogger(Main::class)
  private val config: Config by
      option("-c", "--config", help = "Path to the config file")
          .convert { Config.readConfig(it) }
          .default(Config.readConfig("config.json"))

  override fun run() {
    val config = this.config
    LOGGER.info("NMR backend starting up")
    LOGGER.info("Used config file: " + this.config)

    /* TODO: Potentially, read configuration. */
    val cottontailDBClient = CottontailDBClient(config.cottontailDB)

    /* Create Javalin instance. */
    val javalin = javalin()
    javalin
        .before {
          /* TODO: Logging. */
        }
        .exception(Exception::class.java) { e, ctx ->
          /* TODO: Error handling. */
        }
        .routes {
          ApiBuilder.before { ctx ->
            ctx.method()
            ctx.header(Header.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
            ctx.header(
                Header.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PATCH, PUT, DELETE, OPTIONS")
            ctx.header(Header.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization, Content-Type")
            ctx.header(Header.CONTENT_TYPE, "application/json")
          }

          path("api") {
            /* TODO: Handlers for API. */
            path("authentication") {
              ApiBuilder.get("{username}/{password}") { login(it) }
              ApiBuilder.get("logout/{username}") { logout(it) }
            }
            path("retrieval") {
              ApiBuilder.get("{elementId}/{attributes}") { retrieve(it) }
              ApiBuilder.get("lookup/{elementId}/{entity}") { lookup(it, cottontailDBClient) }
              ApiBuilder.get("text/{text}/{pageSize}/{page}") { fullText(it) }
              ApiBuilder.get("similarity/{elementId}/{pageSize}/{page}") { similarity(it) }
              ApiBuilder.get("filter/{condition}/{pageSize}/{page}") { filter(it) }
            }
            path("basket") {
              path("{basketId}") {
                ApiBuilder.post { createBasket(it) }
                ApiBuilder.delete { dropBasket(it) }
                ApiBuilder.get { listElements(it) }
                path("{elementId}") {
                  ApiBuilder.post { addElement(it) }
                  ApiBuilder.delete { dropElement(it) }
                }
              }
              ApiBuilder.get("list/{userId}") { list(it) }
            }
          }
        }
        .exception(ErrorStatusException::class.java) { e, ctx ->
          ctx.status(e.code).json(e.toStatus())
        }
        .exception(Exception::class.java) { e, ctx ->
          ctx.status(500).json(ErrorStatus(500, "Internal server error: ${e.localizedMessage}"))
        }
        .start(config.api.port)

    Cli.loop()
  }

  /**
   * Generates a [Javalin] instance with the following configuration
   *
   * @return [Javalin] instance
   */
  private fun javalin() =
      Javalin.create {
        /* Enable CORS. */
        it.plugins.enableCors { cors ->
          cors.add { corsPluginConfig ->
            corsPluginConfig.reflectClientOrigin = true
            corsPluginConfig.allowCredentials = true
          }
        }

        /* Register Open API plugin. */
        it.plugins.register(
            OpenApiPlugin(
                OpenApiPluginConfiguration()
                    .withDocumentationPath("/swagger-docs")
                    .withDefinitionConfiguration { _, u ->
                      u.withOpenApiInfo { t ->
                        t.title = "NMR Backend API"
                        t.version = VERSION
                        t.description =
                            "API for XREco Neural Media Repository (NMR) backend, Version $VERSION"
                      }
                      u.withSecurity(
                          SecurityComponentConfiguration()
                              .withSecurityScheme("CookieAuth", CookieAuth("SESSIONID")))
                    }))

        /* Register Swagger UI. */
        it.plugins.register(
            SwaggerPlugin(
                SwaggerConfiguration().apply {
                  this.version = "4.10.3"
                  this.documentationPath = "/swagger-docs"
                  this.uiPath = "/swagger-ui"
                }))

        /* General configuration. */
        it.http.defaultContentType = "application/json"
        it.http.prefer405over404 = true
        it.staticFiles.add("html", Location.CLASSPATH) /* SPA serving. */
        it.spaRoot.addFile("/", "html/index.html")
        /* TODO: Authentication + Authorization and SSL. */

      }
}

fun main(args: Array<String>) {
  Main().main(args)
}
