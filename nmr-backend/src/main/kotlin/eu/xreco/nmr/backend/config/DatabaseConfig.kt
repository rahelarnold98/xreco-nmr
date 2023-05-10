package eu.xreco.nmr.backend.config

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.serialization.Serializable
import org.vitrivr.cottontail.client.SimpleClient

/**
 * Configuration for Cottontail DB database.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
@Serializable
data class DatabaseConfig(
    val host: String = "127.0.0.1",
    val port: Int = 1865,
    val schemaName: String = "xreco",
) {
    /**
     * Creates and returns a new [ManagedChannel] for this [DatabaseConfig].
     *
     * Only a single [ManagedChannel] should be opened per application instance!
     *
     * @return A new [ManagedChannel] for this [DatabaseConfig].
     */
    fun newChannel() = ManagedChannelBuilder.forAddress(this.host, this.port).usePlaintext().build()

    /**
     * Creates and returns a new [SimpleClient] for this [DatabaseConfig].
     *
     * @return A new [SimpleClient] for this [DatabaseConfig].
     */
    fun newClient() = SimpleClient(this.newChannel())
}
