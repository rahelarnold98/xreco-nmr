package eu.xreco.nmr.backend

import eu.xreco.nmr.backend.api.initializeRoutes
import eu.xreco.nmr.backend.cli.Cli
import eu.xreco.nmr.backend.config.Config
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import eu.xreco.nmr.backend.model.api.status.ErrorStatusException
import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import io.javalin.openapi.CookieAuth
import io.javalin.openapi.plugin.OpenApiPlugin
import io.javalin.openapi.plugin.OpenApiPluginConfiguration
import io.javalin.openapi.plugin.SecurityComponentConfiguration
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration
import io.javalin.openapi.plugin.swagger.SwaggerPlugin
import org.vitrivr.cottontail.client.SimpleClient
import java.nio.file.Paths
import kotlin.system.exitProcess

/** Version of NMR Backend API. */
const val VERSION = "1.0.0"

/** The main function and entrypoint for the NMR backend application. */
fun main(args: Array<String>) {

    /* Try to load config. Use default config upon failure. */
    println("NMR backend starting up")
    val config = args.getOrNull(0)?.let {
        try {
            val config = Config.read(Paths.get(it))
            println("Using config file at: $it")
            config
        } catch (e: Throwable) {
            System.err.println("Error while reading config at $it. Resorting to default.")
            null
        }
    } ?: Config()

    /** Create a [SimpleClient] instance. */
    val client = config.database.newClient()
    try {
        if (!client.ping()) {
            System.err.println("The Cottontail DB database ${config.database.host}:${config.database.port} is not reachable. NMR backend is shutting down.")
            exitProcess(1)
        }
    } catch (e: Throwable) {
        System.err.println("Error while pinging database. NMR backend is shutting down.")
        exitProcess(1)
    }

    /* Create and start Javalin instance. */
    javalin().before {
        /* TODO: Logging. */
    }.exception(Exception::class.java) { e, ctx ->
            /* TODO: Error handling. */
        }.initializeRoutes(client, config).exception(ErrorStatusException::class.java) { e, ctx ->
            ctx.status(e.code).json(e.toStatus())
        }.exception(Exception::class.java) { e, ctx ->
            ctx.status(500).json(ErrorStatus(500, "Internal server error: ${e.localizedMessage}"))
        }.start(config.api.port)

    /* Create and start CLI instance. */
    Cli(client, config).loop()
}

/**
 * Generates a [Javalin] instance with the following configuration
 *
 * @return [Javalin] instance
 */
private fun javalin() = Javalin.create {
    /* Enable CORS. */
    it.plugins.enableCors { cors ->
        cors.add { corsPluginConfig ->
            corsPluginConfig.reflectClientOrigin = true
            corsPluginConfig.allowCredentials = true
        }
    }

    /* Register Open API plugin. */
    it.plugins.register(
        OpenApiPlugin(OpenApiPluginConfiguration().withDocumentationPath("/swagger-docs")
            .withDefinitionConfiguration { _, u ->
                u.withOpenApiInfo { t ->
                    t.title = "NMR Backend API"
                    t.version = VERSION
                    t.description = "API for XREco Neural Media Repository (NMR) backend, Version $VERSION"
                }
                u.withSecurity(
                    SecurityComponentConfiguration().withSecurityScheme("CookieAuth", CookieAuth("SESSIONID"))
                )
            })
    )

    /* Register Swagger UI. */
    it.plugins.register(
        SwaggerPlugin(SwaggerConfiguration().apply {
            this.version = "4.10.3"
            this.documentationPath = "/swagger-docs"
            this.uiPath = "/swagger-ui"
        })
    )

    /* General configuration. */
    it.http.defaultContentType = "application/json"
    it.http.prefer405over404 = true
    it.staticFiles.add("html", Location.CLASSPATH) /* SPA serving. */
    it.spaRoot.addFile("/", "html/index.html")/* TODO: Authentication + Authorization and SSL. */

}
