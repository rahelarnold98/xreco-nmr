package eu.xreco.nmr.backend.database

import eu.xreco.nmr.backend.config.CottontailDBConfig
import io.grpc.ManagedChannelBuilder
import java.util.*
import org.vitrivr.cottontail.client.SimpleClient
import org.vitrivr.cottontail.client.language.basics.predicate.Expression
import org.vitrivr.cottontail.client.language.dql.Query

/**
 * A [CottontailDBClient] holds a SimpleClient to exectute queries
 *
 * @author Rahel Arnold
 */
class CottontailDBClient(config: CottontailDBConfig) {

  private val channel =
      ManagedChannelBuilder.forAddress(config.host, config.port).usePlaintext().build()
  private val client = SimpleClient(channel)

  private val schema = config.schemaName

  fun getSchemaName(): String {
    return schema
  }

  fun lookup(entity: String, select: String): LinkedList<FloatArray> {
    // prepare query
    val query = Query("${schema}.${entity}").where(Expression("id", "=", select)).select("feature")
    // execute query
    val results = this.client.query(query)
    // save results as LinkedList
    val list = LinkedList<FloatArray>()
    results.forEach { t -> list.add(t.asFloatVector("feature")!!) }

    return list
  }
}
