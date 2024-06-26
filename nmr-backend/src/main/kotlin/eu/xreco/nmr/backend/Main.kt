package eu.xreco.nmr.backend

import eu.xreco.nmr.backend.api.initializeRoutes
import eu.xreco.nmr.backend.config.Config
import eu.xreco.nmr.backend.features.metadata.XRecoMetadataDescriptor
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import eu.xreco.nmr.backend.model.api.status.ErrorStatusException
import io.javalin.Javalin
import io.javalin.openapi.CookieAuth
import io.javalin.openapi.plugin.OpenApiPlugin
import io.javalin.openapi.plugin.OpenApiPluginConfiguration
import io.javalin.openapi.plugin.SecurityComponentConfiguration
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration
import io.javalin.openapi.plugin.swagger.SwaggerPlugin
import io.javalin.plugin.bundled.CorsPluginConfig
import io.minio.MinioClient
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionServer
import org.vitrivr.engine.core.database.ConnectionProvider
import org.vitrivr.engine.core.database.Initializer
import org.vitrivr.engine.core.util.extension.loadServiceForName
import org.vitrivr.engine.plugin.cottontaildb.descriptors.struct.StructDescriptorProvider

/** Version of NMR Backend API. */
const val VERSION = "1.0.0"

/** The main function and entrypoint for the NMR backend application. */
fun main(args: Array<String>) {

    /* Try to load config. Use default config upon failure. */
    println("NMR backend starting up")
    val config = Config()

    /* Setup schema manager. */
    val manager = SchemaManager()
    manager.load(config.schema)

    /* Access XReco schema and register custom providers. */
    val schema = manager.getSchema("xreco") ?: throw IllegalStateException("Schema 'xreco' not found.")
    schema.connection.provider.register(XRecoMetadataDescriptor::class, StructDescriptorProvider)

    /* Init Database */
    var initialized = 0
    var initializer: Initializer<*> = schema.connection.getRetrievableInitializer()
    if (!initializer.isInitialized()) {
        initializer.initialize()
        initialized += 1
    }
    for (field in schema.fields()) {
        initializer = field.getInitializer()
        if (!initializer.isInitialized()) {
            initializer.initialize()
            initialized += 1
        }
    }
    println("Successfully initialized schema '${schema.name}'; created $initialized entities.")

    /* Execution server singleton for this instance. */
    val executor = ExecutionServer()

    /* Initialize MinIO client. */
    val minio = config.minio.newClient()

    /* Create and start Javalin instance. */
    javalin().before {
        /* TODO: Logging. */
    }.exception(Exception::class.java) { e, ctx ->
        /* TODO: Error handling. */
    }.initializeRoutes(config, manager, executor, minio).exception(ErrorStatusException::class.java) { e, ctx ->
        ctx.status(e.code).json(e.toStatus())
    }.exception(Exception::class.java) { e, ctx ->
        ctx.status(500).json(ErrorStatus(500, "Internal server error: ${e.localizedMessage}"))
    }.start(config.api.port)

    /* Create and start CLI instance. */
    //Cli(client, config).loop()
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
        SwaggerPlugin(
            SwaggerConfiguration().apply {
            this.documentationPath = "/swagger-docs"
            this.uiPath = "/swagger-ui"
        })
    )

    /* General configuration. */
    it.http.defaultContentType = "application/json"
    it.http.prefer405over404 = true
}
