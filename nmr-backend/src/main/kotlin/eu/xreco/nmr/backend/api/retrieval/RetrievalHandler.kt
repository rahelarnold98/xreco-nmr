package eu.xreco.nmr.backend.api.retrieval

import eu.xreco.nmr.backend.api.Retrieval
import eu.xreco.nmr.backend.config.Config
import eu.xreco.nmr.backend.model.api.retrieval.RetrievalResult
import eu.xreco.nmr.backend.model.api.retrieval.ScoredMediaItem
import eu.xreco.nmr.backend.model.api.retrieval.Text
import eu.xreco.nmr.backend.model.api.status.ErrorStatus
import eu.xreco.nmr.backend.model.api.status.ErrorStatusException
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.javalin.http.Context
import io.javalin.openapi.*
import org.vitrivr.cottontail.client.SimpleClient
import org.vitrivr.cottontail.client.language.basics.Direction
import org.vitrivr.cottontail.client.language.basics.Distances
import org.vitrivr.cottontail.client.language.basics.predicate.And
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dql.Query
import org.vitrivr.cottontail.core.values.FloatVectorValue
import java.util.*
import kotlin.FloatArray
import kotlin.Int
import kotlin.String

@OpenApi(
    summary = "Get entity of given element",
    path = "/api/retrieval/lookup/{elementId}/{entity}",
    tags = [Retrieval],
    operationId = "getValueOfElement",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(
            name = "elementId",
            type = String::class,
            description = "Id of element which will be returned",
            required = true
        ),
        OpenApiParam(
            name = "entity", type = String::class, description = "Descriptor to retrieve data", required = true
        ),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(Text::class)]),
        OpenApiResponse("200", [OpenApiContent(FloatArray::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun lookup(context: Context, client: SimpleClient, config: Config) {/* TODO implement*/
    val elementId = context.pathParam("elementId")
    val entity = context.pathParam("entity")

    try {

        val column: String
        column = when (entity) {
            "features_landmark" -> {
                "label"
            }

            "features_clip" -> {
                "feature"
            }

            else -> {
                throw ErrorStatusException(
                    400, "The requested element '${config.database.schemaName}.${entity} does not exist."
                )
            }
        }

        // prepare query
        val query = Query("${config.database.schemaName}.${entity}").where(Compare("mediaResourceId", "=", elementId))
            .select(column).distinct(column)

        // execute query
        val results = client.query(query)

        // save results as LinkedList
        when (entity) {
            "features_landmark" -> {
                val list = LinkedList<Text>()
                results.forEach { t ->
                    list.add(Text(t.asString(column)!!))
                }
                context.json(list)

            }

            "features_clip" -> {
                val list = LinkedList<FloatArray>()
                //
                results.forEach { t ->
                    // TODO fill
                    list.add(t.asFloatVector("feature")!!)
                }
                //val floatArray = list.toFloatArray()
                context.json(list)
            }

            else -> {
                throw ErrorStatusException(
                    400, "The requested element '${config.database.schemaName}.${entity} does not exist."
                )
            }
        }

    } catch (e: StatusRuntimeException) {
        when (e.status.code) {
            Status.Code.INTERNAL -> {
                throw ErrorStatusException(
                    400,
                    "The requested element '${config.database.schemaName}.${entity}.${elementId} could not be found."
                )
            }

            Status.Code.NOT_FOUND -> throw ErrorStatusException(
                404, "The requested element '${config.database.schemaName}.${entity}.${elementId} could not be found."
            )

            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Connection is currently not available.")

            else -> {
                throw e.message?.let { ErrorStatusException(400, it) }!!
            }
        }
    }
}

@OpenApi(
    summary = "Issues a fulltext query.",
    path = "/api/retrieval/text/{entity}/{text}/{pageSize}/{page}",
    tags = [Retrieval],
    operationId = "getSearchFulltext",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "entity", type = String::class, description = "Name of the entity to query.", required = true),
        OpenApiParam(name = "text", type = String::class, description = "Text to search for.", required = true),
        OpenApiParam(name = "pageSize", type = Int::class, description = "Page size of a single results page.", required = true),
        OpenApiParam(name = "page", type = Int::class, description = "Requested page of results. Zero-based index (first page = 0).", required = true)
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(RetrievalResult::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)])
    ]
)

fun getFulltext(context: Context, client: SimpleClient, config: Config) {
    val text = context.pathParam("text")
    val entity = context.pathParam("entity")
    val pageSize = context.pathParam("pageSize").toInt()
    val page = context.pathParam("page").toInt()
    try {
        /* Determine how many entries can be found by the query. */
        var count = 0L
        val countQuery = Query("${config.database.schemaName}.$entity").fulltext("label", text, "score").count()
        client.query(countQuery).forEach {
            count = it.asLong(0)!!
        }

        /* Query and save results to list*/
        val list = ArrayList<ScoredMediaItem>(pageSize)
        val query = Query("${config.database.schemaName}.$entity")
            .fulltext("label", text, "score")
            .select("mediaResourceId")
            .select("start")
            .select("end")
            .select("rep")
            .order("score", Direction.DESC)
            .limit(pageSize.toLong()).skip(page * pageSize.toLong())

        client.query(query).forEach { t ->
            list.add(ScoredMediaItem(t.asString("mediaResourceId")!!, t.asDouble("score")!!, t.asFloat("start")!!, t.asFloat("end")!!,  t.asFloat("rep")!!))
        }

        /* Send results. */
        context.json(RetrievalResult(page, pageSize, count, list))
    } catch (e: StatusRuntimeException) {
        when (e.status.code) {
            Status.Code.NOT_FOUND -> throw ErrorStatusException(404, "The requested entity '${config.database.schemaName}.${entity}' could not be found.")
            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Connection is currently not available.")
            else -> throw ErrorStatusException(400, e.message ?: "Unknown error")
        }
    }
}

@OpenApi(
    summary = "Issues a similarity query based on a provided media resource id.",
    path = "/api/retrieval/similarity/{entity}/{mediaResourceId}/{timestamp}/{pageSize}/{page}",
    tags = [Retrieval],
    operationId = "getSearchSimilar",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "entity", type = String::class, description = "Name of the entity to query.", required = true),
        OpenApiParam(name = "mediaResourceId", type = String::class, description = "ID of the media resource to find similar entries for.", required = true),
        OpenApiParam(name = "timestamp", type = Long::class, description = "The exact timestamp of to find similar entries for.", required = true),
        OpenApiParam(name = "pageSize", type = Int::class, description = "Page size of results", required = true),
        OpenApiParam(name = "page", type = Int::class, description = "Request page of results", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(RetrievalResult::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun getSimilar(context: Context, client: SimpleClient, config: Config) {/* TODO implement*/
    /* Extract path parameters. */
    val mediaResourceId = context.pathParam("mediaResourceId")
    val entity = context.pathParam("entity")
    val timestamp = (context.pathParam("timestamp").toLongOrNull() ?: 0L)
    val pageSize = context.pathParam("pageSize").toInt()
    val page = context.pathParam("page").toInt()

    /* Handle similarity (more-like-this) query. */
    try {
        /* Extract query vector. */
        val exampleQuery = Query("${config.database.schemaName}.${entity}").where(
            And(
                Compare("mediaResourceId", "=", mediaResourceId),
                And(
                    Compare("start", ">=", timestamp),
                    Compare("end", "<=", timestamp)
                )
            )
        ).select("feature")
        val vector: FloatArray = client.query(exampleQuery).use { result ->
            if (result.hasNext()) {
                result.next().asFloatVector("feature")!!
            } else {
                throw ErrorStatusException(404, "Could not find feature '${entity}' for media resource ${mediaResourceId}.")
            }
        }

        /* Determine how many entries can be found by the query. */
        val countQuery = Query("${config.database.schemaName}.$entity").count()
        val count = client.query(countQuery).use {
            it.next().asLong(0)!!
        }

        /* Issue similarity search. */
        val list = ArrayList<ScoredMediaItem>(pageSize)
        val query = Query("${config.database.schemaName}.${entity}").distance(
            "feature", FloatVectorValue(vector), Distances.EUCLIDEAN, "score"
        ).select("mediaResourceId")
        .select("start")
        .select("end")
            .select("rep")
        .order("score", Direction.ASC)
        .limit(pageSize.toLong()).skip(page * pageSize.toLong())

        client.query(query).forEach { t ->
            list.add(ScoredMediaItem(t.asString("mediaResourceId")!!, t.asDouble("score")!!, t.asFloat("start")!!, t.asFloat("end")!!,  t.asFloat("rep")!!))
        }

        /* Send results. */
        context.json(RetrievalResult(page, pageSize, count, list))
    } catch (e: StatusRuntimeException) {
        when (e.status.code) {
            Status.Code.NOT_FOUND -> throw ErrorStatusException(404, "The requested entity '${config.database.schemaName}.${entity}' could not be found.")
            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Database is currently not available.")
            else -> ErrorStatusException(400, e.message ?: "Unknown error")
        }
    }
}

@OpenApi(
    summary = "Apply a filter to a collection",
    path = "/api/retrieval/filter/{condition}/{pageSize}/{page}",
    tags = [Retrieval],
    operationId = "getFilterQuery",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(
            name = "condition", type = String::class, description = "Condition to filter collection", required = true
        ),
        OpenApiParam(name = "pageSize", type = Int::class, description = "Page size of results", required = true),
        OpenApiParam(name = "page", type = Int::class, description = "Request page of results", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(RetrievalResult::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun filter(context: Context) {/* TODO implement*/
    // TODO check if this is still needed
}
