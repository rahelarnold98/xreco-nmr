package eu.xreco.nmr.backend.features.landmarks

import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.query.Query


/**
 * A [LabelQuery] that uses a [:].
 *
 * A [LabelQuery] is typically translated to a fulltext query in the underlying storage engine.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
data class LabelQuery(override val descriptor: LabelDescriptor, val limit: Long = Long.MAX_VALUE) : Query<LabelDescriptor>
