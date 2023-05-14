package eu.xreco.nmr.backend.cli.import

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import eu.xreco.nmr.backend.model.cineast.LegacyLandmarks
import eu.xreco.nmr.backend.model.cineast.MediaObject
import eu.xreco.nmr.backend.model.cineast.MediaSegment
import eu.xreco.nmr.backend.model.cineast.VectorFeature
import eu.xreco.nmr.backend.model.database.core.MediaResource
import eu.xreco.nmr.backend.model.database.features.LandmarkFeature
import kotlinx.serialization.json.DecodeSequenceMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeToSequence
import org.vitrivr.cottontail.client.SimpleClient
import org.vitrivr.cottontail.client.language.dml.BatchInsert
import org.vitrivr.cottontail.core.values.FloatVectorValue
import org.vitrivr.cottontail.core.values.IntValue
import org.vitrivr.cottontail.core.values.LongValue
import org.vitrivr.cottontail.core.values.StringValue
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * A [CliktCommand] that can be used to import basic metadata in the Cineast format.
 *
 * @author Ralph Gasser
 * @version 1.0.1
 */
class CineastImport(private val client: SimpleClient, private val schema: String = "xreco") :
    CliktCommand(name = "cineast") {

    companion object {
        const val FILENAME_CINEAST_MEDIAOBJECT = "cineast_multimediaobject.json"
        const val FILENAME_CINEAST_SEGMENT = "cineast_segment.json"
        const val FILENAME_FEATURE_LANDMARK = "features_landmarks.json"
        const val FILENAME_FEATURE_CLIP = "features_clip.json"

    }

    /** The [Path] to the input file. */
    private val input: Path by option(
        "-i", "--input", help = "The path to the folder that contains the data to import."
    ).convert { Paths.get(it) }.required()

    override fun run() {/* Start data import with media resources. */
        if (!this.importMediaObjects(this.input.resolve(FILENAME_CINEAST_MEDIAOBJECT))) {
            return
        }

        /* Generates a segment mapping. */
        val segmentMap = this.createSegmentMapping(this.input.resolve(FILENAME_CINEAST_SEGMENT))
        if (segmentMap.isEmpty()) {
            return
        }

        /* Start feature import. */
        this.loadLegacyLandmarks(this.input.resolve(FILENAME_FEATURE_LANDMARK), segmentMap)
        this.loadClip(this.input.resolve(FILENAME_FEATURE_CLIP), segmentMap)
    }

    /**
     * Imports a CLIP vector feature
     *
     * @param path The [Path] to the file that contains the landmarks.
     * @param mapping A [Map] that maps segment IDs to media resource IDs and start/end times.
     * @return True on success, false on failure
     */
    private fun loadClip(path: Path, mapping: Map<String, Triple<String, Long, Long>>): Boolean {
        if (!Files.exists(path)) {
            System.err.println("Import of CLIP feature failed. File $path does not seem to exist.")
            return false
        }

        var counter = 0
        var success = true
        val txId = this.client.begin()
        try {
            Files.newInputStream(path).use {
                val insert =
                    BatchInsert("$schema.${LandmarkFeature.name}").columns("mediaResourceId", "feature", "start", "end")
                        .txId(txId)
                for (l in Json.decodeToSequence<VectorFeature>(it, DecodeSequenceMode.ARRAY_WRAPPED)) {
                    val segment = mapping[l.id] ?: continue
                    if (!insert.values(
                            StringValue(segment.first),
                            FloatVectorValue(l.feature),
                            LongValue(segment.second),
                            LongValue(segment.second)
                        )
                    ) {
                        this.client.insert(insert).close()
                        insert.clear()
                        insert.values(
                            StringValue(segment.first),
                            FloatVectorValue(l.feature),
                            LongValue(segment.second),
                            LongValue(segment.second)
                        )
                    }
                    counter += 1
                }

                if (insert.count() > 0) {
                    this.client.insert(insert).close()
                }
            }
        } catch (e: Throwable) {
            System.err.println("An error occurred while importing CLIP features: ${e.message}")
            success = false
        }

        if (success) {
            this.client.commit(txId)
            println("Successfully imported $counter CLIP features.")
        } else {
            this.client.rollback(txId)
            println("An error occurred while importing CLIP features.")
        }
        return success
    }

    /**
     * Imports the legacy landmark annotations.
     *
     * TODO: Data developed during the XRECO project should adhere to the formulated data model.
     *
     * @param path The [Path] to the file that contains the landmarks.
     * @param mapping A [Map] that maps segment IDs to media resource IDs and start/end times.
     * @return True on success, false on failure
     */
    @Deprecated("Legacy data format. Should not be used anymore.")
    private fun loadLegacyLandmarks(path: Path, mapping: Map<String, Triple<String, Long, Long>>): Boolean {
        if (!Files.exists(path)) {
            System.err.println("Import of landmarks failed. File $path does not seem to exist.")
            return false
        }

        var counter = 0
        var success = true
        val txId = this.client.begin()
        try {
            Files.newInputStream(path).use {
                val insert =
                    BatchInsert("$schema.${LandmarkFeature.name}").columns("mediaResourceId", "label", "start", "end")
                        .txId(txId)
                for (l in Json.decodeToSequence<LegacyLandmarks>(it, DecodeSequenceMode.ARRAY_WRAPPED)) {
                    val segment = mapping[l.id] ?: continue
                    if (!insert.values(
                            StringValue(segment.first),
                            StringValue(l.feature),
                            LongValue(segment.second),
                            LongValue(segment.second)
                        )
                    ) {
                        this.client.insert(insert).close()
                        insert.clear()
                        insert.values(
                            StringValue(segment.first),
                            StringValue(l.feature),
                            LongValue(segment.second),
                            LongValue(segment.second)
                        )
                    }
                    counter += 1
                }

                if (insert.count() > 0) {
                    this.client.insert(insert).close()
                }
            }
        } catch (e: Throwable) {
            System.err.println("An error occurred while importing landmark annotations: ${e.message}")
            success = false
        }

        if (success) {
            this.client.commit(txId)
            println("Successfully imported $counter landmark annotations.")
        } else {
            this.client.rollback(txId)
            println("An error occurred while importing landmark annotations.")
        }
        return success
    }

    /**
     * Loads cineast_multimediaobject.json and imports all the media objects.
     *
     * @param path The [Path] to the file that contains the media objects.
     * @return True on success, false otherwise.
     */
    private fun importMediaObjects(path: Path): Boolean {
        if (!Files.exists(path)) {
            System.err.println("Import of media objects failed. File $path does not seem to exist.")
            return false
        }

        var counter = 0
        var success = true
        val txId = this.client.begin()
        try {
            Files.newInputStream(path).use {
                val insert = BatchInsert("$schema.${MediaResource.name}").columns(
                    "mediaResourceId", "type", "title", "description", "uri", "path"
                ).txId(txId)
                for (o in Json.decodeToSequence<MediaObject>(it, DecodeSequenceMode.ARRAY_WRAPPED)) {
                    if (!insert.values(
                            StringValue(o.objectid),
                            IntValue(o.mediatype),
                            null,
                            null,
                            StringValue(o.path),
                            StringValue(o.path)
                        )
                    ) {
                        this.client.insert(insert).close()
                        insert.clear()
                        insert.values(
                            StringValue(o.objectid),
                            IntValue(o.mediatype),
                            null,
                            null,
                            StringValue(o.path),
                            StringValue(o.path)
                        )
                    }
                    counter += 1
                }
                if (insert.count() > 0) {
                    this.client.insert(insert).close()
                }
            }
        } catch (e: Throwable) {
            System.err.println("An error occurred while importing media resources: ${e.message}")
            success = false
        }

        if (success) {
            this.client.commit(txId)
            println("Successfully imported $counter media resources.")
        } else {
            this.client.rollback(txId)
            println("An error occurred while importing media resources.")
        }
        return success
    }

    /**
     * Loads cineast_segment.json and creates a segment mapping for future reference.
     *
     * @param path The [Path] to the file that contains the media objects.
     * @return Segment mapping
     */
    private fun createSegmentMapping(path: Path): Map<String, Triple<String, Long, Long>> {
        if (!Files.exists(this.input.resolve(FILENAME_CINEAST_SEGMENT))) {
            System.err.println("Creation of segment map failed. File $path does not seem to exist. ")
            return emptyMap()
        }
        val map = HashMap<String, Triple<String, Long, Long>>()
        try {
            Files.newInputStream(path).use {
                for (s in Json.decodeToSequence<MediaSegment>(it, DecodeSequenceMode.ARRAY_WRAPPED)) {
                    map[s.segmentid] = Triple(s.objectid, s.segmentrepresentative, s.segmentrepresentative)
                }
            }
        } catch (e: Throwable) {
            System.err.println("An error occurred while creating segment map: ${e.message}")
        }

        return map
    }
}