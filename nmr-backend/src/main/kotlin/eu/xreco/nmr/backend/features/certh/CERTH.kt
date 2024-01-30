package eu.xreco.nmr.backend.features.certh


import org.vitrivr.engine.base.features.external.common.ExternalWithFloatVectorDescriptorAnalyser
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.model3d.ModelHandler
import java.util.*
import kotlin.reflect.KClass

/**
 * Implementation of the [CERTH] [ExternalAnalyser], which derives the CERTH feature from an [Model3DContent] as [FloatVectorDescriptor].
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class CERTH : ExternalWithFloatVectorDescriptorAnalyser<ContentElement<*>>() {


    // TOFIX: This is a hack to make the analyser work with the new model3d content element
    override val contentClasses: Set<KClass<out ContentElement<*>>> = setOf(ImageContent::class)
    override val descriptorClass = FloatVectorDescriptor::class

    // Size and list for prototypical descriptor
    override val size = 512
    override val featureList = List(size) { 0.0f }

    /**
     * Requests the CERTH feature descriptor for the given [ContentElement].
     *
     * @param content The [ContentElement] for which to request the CERTH feature descriptor.
     * @return A list of CERTH feature descriptors.
     */
    override fun requestDescriptor(content: ContentElement<*>): List<Float> {
        return httpRequest(content, "http://160.40.53.193:8000/3D model retrieval/extract/?usecase=Tourism")
    }

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [CERTH].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [FloatVectorDescriptor]
     */
    override fun prototype(): FloatVectorDescriptor = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), this.featureList, true)

    /**
     * Generates and returns a new [Extractor] instance for this [Analyser].
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     * @param persisting True, if the results of the [Extractor] should be persisted.
     *
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(
        field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>,
        input: Operator<Retrievable>,
        context: IndexContext,
        persisting: Boolean,
        parameters: Map<String, Any>
    ): Extractor<ContentElement<*>, FloatVectorDescriptor> {
        require(field.analyser == this) { "" }
        return CERTHExtractor(input, field, persisting, this)
    }

    /**
     * Generates and returns a new [Retriever] instance for this [CERTH].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [ContentElement]s to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForContent(
        field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>,
        content: Collection<ContentElement<*>>,
        context: QueryContext
    ): Retriever<ContentElement<*>, FloatVectorDescriptor> {
        return this.newRetrieverForDescriptors(field, this.processContent(content), context)
    }

    /**
     * Generates and returns a new [Retriever] instance for this [CERTH].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [FloatVectorDescriptor] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForDescriptors(
        field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>,
        descriptors: Collection<FloatVectorDescriptor>,
        context: QueryContext
    ): Retriever<ContentElement<*>, FloatVectorDescriptor> {
        require(field.analyser == this) { }
        return CERTHRetriever(field, descriptors.first(), context)
    }
}
