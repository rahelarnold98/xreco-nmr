package eu.xreco.nmr.backend.api.retrieval

import eu.xreco.nmr.backend.api.Retrieval
import eu.xreco.nmr.backend.config.Config
import eu.xreco.nmr.backend.model.api.retrieval.Media
import eu.xreco.nmr.backend.model.api.retrieval.MediaList
import eu.xreco.nmr.backend.model.api.retrieval.SimilarityMedia
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
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dql.Query
import org.vitrivr.cottontail.core.values.FloatVectorValue
import java.util.*
import kotlin.FloatArray
import kotlin.Int
import kotlin.String

@OpenApi(
    summary = "Get attributes of given element",
    path = "/api/retrieval/{elementId}",
    tags = [Retrieval],
    operationId = "getAttributesOfElement",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(
            name = "elementId", type = String::class, "Id of element which attributes will be returned", required = true
        ),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(Media::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun retrieve(context: Context, client: SimpleClient, config: Config) {/* TODO implement*/
    val elementId = context.pathParam("elementId")

    try {
        // prepare query
        val query = Query("${config.database.schemaName}.${"media_resources"}").where(
            Compare(
                "mediaResourceId", "=", elementId
            )
        ).select("*")

        // execute query
        val results = client.query(query)

        // save results as LinkedList
        var media: Media
        val list = LinkedList<Text>()
        results.forEach { t ->
            media = Media(
                t.asString("mediaResourceId"),
                t.asString("description"),
                t.asInt("type"),
                t.asString("title"),
                t.asString("uri"),
                t.asString("path")
            )
            context.json(media)
        }

    } catch (e: StatusRuntimeException) {
        when (e.status.code) {
            Status.Code.INTERNAL -> {
                throw ErrorStatusException(
                    400,
                    "The requested element '${config.database.schemaName}.mediaResourceId.${elementId}' could not be found."
                )
            }

            Status.Code.NOT_FOUND -> throw ErrorStatusException(
                404,
                "The requested element '${config.database.schemaName}.mediaResourceId.${elementId}' could not be found."
            )

            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Connection is currently not available.")

            else -> {
                throw e.message?.let { ErrorStatusException(400, it) }!!
            }
        }
    }
}

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
    summary = "Create a fulltext query",
    path = "/api/retrieval/text/{text}/{entity}/{pageSize}/{page}",
    tags = [Retrieval],
    operationId = "getFullTextQuery",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(name = "text", type = String::class, description = "Text to query", required = true),
        OpenApiParam(name = "entity", type = String::class, description = "Entity to query", required = true),
        OpenApiParam(name = "pageSize", type = Int::class, description = "Page size of results", required = true),
        OpenApiParam(name = "page", type = Int::class, description = "Requested page of results", required = true),
    ],
    responses = [
        OpenApiResponse(
            "200", [OpenApiContent(MediaList::class)]
        ), // TODO check what is the order --> could be a [ScoredMediaList]
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)

fun fullText(context: Context, client: SimpleClient, config: Config) {/* TODO implement*/
    val text = context.pathParam("text")
    val entity = context.pathParam("entity")
    val pageSize = context.pathParam("pageSize").toInt()
    val page = context.pathParam("page").toInt()
    try {
        // prepare query
        val query = Query("${config.database.schemaName}.${entity}").fulltext("label", text, "score")
            //.select("mediaResourceId")
            //.select("score")
            .order("score", Direction.DESC)

            .limit((page * pageSize).toLong())
        //.order("label", Direction.DESC)
        //val query = Query("${config.database.schemaName}.${entity}").select("*").limit((page * pageSize).toLong())
        /*
                    .select("label")
                    .fulltext(label, text, DB_DISTANCE_VALUE_QUALIFIER)
                    .queryId(generateQueryID("ft-rows", queryConfig))
                    .order(DB_DISTANCE_VALUE_QUALIFIER, Direction.DESC)
                    .limit(rows);*/

        // execute query
        val results = client.query(query)

        // save results as LinkedList
        val list = LinkedList<String>()
        var iterator = 0

        results.forEach { t ->
            // verify if result is on desired page
            if ((page - 1) * pageSize <= iterator && iterator <= page * pageSize) {
                list.add(t.asString("label")!!)
            }
            iterator++
        }
        context.json(list)
    } catch (e: StatusRuntimeException) {
        when (e.status.code) {
            Status.Code.INTERNAL -> {
                throw ErrorStatusException(
                    400, "The requested element '${config.database.schemaName}.${entity}' could not be found."
                )
            }

            Status.Code.NOT_FOUND -> throw ErrorStatusException(
                404, "The requested element '${config.database.schemaName}.${entity}' could not be found."
            )

            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Connection is currently not available.")

            else -> {
                throw e.message?.let { ErrorStatusException(400, it) }!!
            }
        }
    }
}

@OpenApi(
    summary = "Create a similarity query based on a given element id",
    path = "/api/retrieval/similarity/{elementId}/{pageSize}/{page}",
    tags = [Retrieval],
    operationId = "getSimilarityQuery",
    methods = [HttpMethod.GET],
    pathParams = [
        OpenApiParam(
            name = "elementId",
            type = String::class,
            description = "Id of element to get most similar of",
            required = true
        ),
        OpenApiParam(name = "pageSize", type = Int::class, description = "Page size of results", required = true),
        OpenApiParam(name = "page", type = Int::class, description = "Request page of results", required = true),
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(SimilarityMedia::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun similarity(context: Context, client: SimpleClient, config: Config) {/* TODO implement*/

    //TODO why not always same results???
    val elementId = context.pathParam("elementId")
    val pageSize = context.pathParam("pageSize").toInt()
    val page = context.pathParam("page").toInt()

    val similarityFeature = "features_clip"
    // TODO get segment based on time !

    try {
        val queryVec = Query("${config.database.schemaName}.${similarityFeature}").where(
            Compare(
                "mediaResourceId", "=", elementId
            )
        ).select("feature")

        // execute query
        //
        val resultsVec = client.query(queryVec)

        // save results as LinkedList
        //
        //val resQVec = LinkedList<Float>()
        //
        val resQVec = LinkedList<FloatArray>()
        //
        resultsVec.forEach { t ->
            resQVec.add(t.asFloatVector("feature")!!)
        }
        // TODO change after using time
        val floatArray = resQVec.get(0)


        var query = Query("${config.database.schemaName}.${similarityFeature}").distance(
            "feature", FloatVectorValue(floatArray), Distances.COSINE, "score"
        ).select("mediaResourceId")
        // TODO  include score


        // execute query
        val results = client.query(query)

        // save results as LinkedList
        val list = LinkedList<String>()
        var iterator = 0

        results.forEach { t ->
            // verify if result is on desired page
            if ((page - 1) * pageSize <= iterator && iterator <= page * pageSize) {
                // TODO add score here
                list.add(t.asString("mediaResourceId")!!)
            }
            iterator++
        }
        context.json(list)
    } catch (e: StatusRuntimeException) {
        when (e.status.code) {
            Status.Code.INTERNAL -> {
                //TODO Fill
                throw ErrorStatusException(
                    400, "The requested element '${config.database.schemaName}'.feature  could not be found."
                )
            }

            Status.Code.NOT_FOUND -> throw ErrorStatusException(
                //TODO Fill
                404, "The requested element '${config.database.schemaName}'.FEATURE could not be found."
            )

            Status.Code.UNAVAILABLE -> throw ErrorStatusException(503, "Connection is currently not available.")

            else -> {
                throw e.message?.let { ErrorStatusException(400, it) }!!
            }
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
        OpenApiResponse("200", [OpenApiContent(MediaList::class)]),
        OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
    ]
)
fun filter(context: Context) {/* TODO implement*/
    // TODO check if this is still needed
}
