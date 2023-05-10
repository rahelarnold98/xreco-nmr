package eu.xreco.nmr.backend.cli.import

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import eu.xreco.nmr.backend.model.cineast.MediaObject
import eu.xreco.nmr.backend.model.cineast.MediaSegment
import eu.xreco.nmr.backend.model.database.core.MediaResource
import kotlinx.serialization.json.DecodeSequenceMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeToSequence
import org.vitrivr.cottontail.client.SimpleClient
import org.vitrivr.cottontail.client.language.dml.Insert
import org.vitrivr.cottontail.core.values.IntValue
import org.vitrivr.cottontail.core.values.StringValue
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.HashMap

/**
 * A [CliktCommand] that can be used to import basic metadata in the Cineast format.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class CineastImport(private val client: SimpleClient, private val schema: String = "xreco"): CliktCommand(name = "cineast") {

    companion object {
        const val FILENAME_CINEAST_MEDIAOBJECT = "cineast_multimediaobject.json"
        const val FILENAME_CINEAST_SEGMENT = "cineast_segment.json"
    }

    /** The [Path] to the input file. */
    private val input: Path by option("-i", "--input", help = "The path to the folder that contains the data to import.").convert { Paths.get(it) }.required()

    override fun run() {
        val objectsFile = this.input.resolve(FILENAME_CINEAST_MEDIAOBJECT)
        val segmentsFile = this.input.resolve(FILENAME_CINEAST_SEGMENT)

        /* Start data import with media resources. */
        if (!this.importMediaObjects(path = objectsFile)) {
            return
        }

        /* Generates a segment mapping. */
        val segmentMap = this.createSegmentMapping(segmentsFile)
        if (segmentMap.isEmpty()) {
            return
        }
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
        var sucess = true
        val txId = this.client.begin()
        try {
            Files.newInputStream(path).use {
                for (o in Json.decodeToSequence<MediaObject>(it, DecodeSequenceMode.ARRAY_WRAPPED)){
                    val i = Insert("$schema.${MediaResource.name}")
                        .values(
                            "mediaResourceId" to StringValue(o.objectid),
                            "type" to IntValue(o.mediatype),
                            "title" to null,
                            "description" to null,
                            "rui" to StringValue(o.path),
                            "fieName" to StringValue(o.path),
                        ).txId(txId)
                    counter += 1
                }
            }
        } catch (e: Throwable) {
            System.err.println("An error occurred while importing media resources: ${e.message}")
            sucess = false
        }

        if (sucess) {
            this.client.commit(txId)
            println("Successfully imported $counter media resources.")
        } else {
            this.client.rollback(txId)
            println("An error occurred while importing media resources.")
        }
        return sucess
    }

    /**
     * Loads cineast_segment.json and creates a segment mapping for future reference.
     *
     * @param path The [Path] to the file that contains the media objects.
     * @return Segment mapping
     */
    private fun createSegmentMapping(path: Path): Map<String,Triple<String,Long,Long>> {
        if (!Files.exists(this.input.resolve(FILENAME_CINEAST_SEGMENT))) {
            System.err.println("Creation of segment map failed. File $path does not seem to exist. ")
            return emptyMap()
        }
        val map = HashMap<String,Triple<String,Long,Long>>()
        try {
            Files.newInputStream(path).use {
                for (s in Json.decodeToSequence<MediaSegment>(it, DecodeSequenceMode.ARRAY_WRAPPED)){
                    map[s.segmentid] = Triple(s.objectid, s.segmentrepresentative, s.segmentrepresentative)
                }
            }
        } catch (e: Throwable) {
            System.err.println("An error occurred while creating segment map: ${e.message}")
        }

        return map
    }
}