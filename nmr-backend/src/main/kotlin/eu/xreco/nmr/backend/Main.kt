package eu.xreco.nmr.backend

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.http.staticfiles.Location
import io.javalin.openapi.CookieAuth
import io.javalin.openapi.plugin.OpenApiPlugin
import io.javalin.openapi.plugin.OpenApiPluginConfiguration
import io.javalin.openapi.plugin.SecurityComponentConfiguration
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration
import io.javalin.openapi.plugin.swagger.SwaggerPlugin

/** Version of NMR Backend API. */
const val VERSION = "2.0.0"

/**
 * The main function and entrypoint for the NMR backend application. Reads
 */
fun main(args: Array<String>) {
    println("Starting NMR backend...")

    /* TODO: Potentially, read configuration. */

    /* Create Javalin instance. */
    val javalin = javalin()
    javalin.before {
        /* TODO: Logging. */
    }.exception(Exception::class.java) { e, ctx ->
        /* TODO: Error handling. */
    }.routes {
        path("api") {
            /* TODO: Handlers for API. */
        }
    }.start(8080)
}

/**
 * Generates a [Javalin] instance with the following configuration
 *
 * @return [Javalin] instance
 */
private fun javalin() =  Javalin.create() {
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
                        t.description = "API for XREco Neural Media Repository (NMR) backend, Version $VERSION"
                    }
                    u.withSecurity(
                        SecurityComponentConfiguration().withSecurityScheme("CookieAuth", CookieAuth("SESSIONID"))
                    )
                }
        )
    )

    /* Register Swagger UI. */
    it.plugins.register(
        SwaggerPlugin(
            SwaggerConfiguration().apply {
                this.version = "4.10.3"
                this.documentationPath = "/swagger-docs"
                this.uiPath = "/swagger-ui"
            }
        )
    )

    /* General configuration. */
    it.http.defaultContentType = "application/json"
    it.http.prefer405over404 = true
    it.staticFiles.add("html", Location.CLASSPATH) /* SPA serving. */
    it.spaRoot.addFile("/", "html/index.html")

    /* TODO: Authentication + Authorization and SSL. */
}