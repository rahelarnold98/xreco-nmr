package eu.xreco.nmr.backend.model.api.retrieval

import org.vitrivr.cottontail.core.types.Types
import org.vitrivr.cottontail.core.values.FloatVectorValue
import org.vitrivr.cottontail.grpc.CottontailGrpc

data class FloatVector(val feature: FloatVectorValue)
