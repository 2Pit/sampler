package app.storage

import app.Properties
import app.db.BuildSystem
import app.db.SnapshotStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.eclipse.egit.github.core.RepositoryId
import java.util.*

@Serializable
class SamplerInfo() {
//    private val repoToSamples: MutableMap<Repository, TreeSet<Sample>> = mutableMapOf()
//    private val sampleToSnapshots: MutableMap<Sample, TreeSet<Snapshot>> = mutableMapOf()
    private val repoToSamples: MutableMap<Repository, MutableSet<Sample>> = mutableMapOf()
    private val sampleToSnapshots: MutableMap<Sample, MutableSet<Snapshot>> = mutableMapOf()

    fun containsRepo(repo: Repository): Boolean = repoToSamples.containsKey(repo)
    fun containsSample(repo: Repository, sample: Sample): Boolean = sampleToSnapshots.containsKey(sample)

    fun addRepo(repo: Repository) {
        repoToSamples.getOrPut(repo) { sampleSet() }
    }

    fun addSample(repo: Repository, sample: Sample) {
        repoToSamples.getOrPut(repo) { sampleSet() }.add(sample)
        sampleToSnapshots.getOrPut(sample) { snapshotSet() }
    }

    fun addSnapshot(repo: Repository, sample: Sample, snapshot: Snapshot) {
        repoToSamples.getOrPut(repo) { sampleSet() }.add(sample)
        sampleToSnapshots.getOrPut(sample) { snapshotSet() }.add(snapshot)
    }

    fun all(): List<RepoSampleSnapshot> {
        return repoToSamples.flatMap { (repo, samples) ->
            samples.flatMap { sample ->
                sampleToSnapshots[sample]!!.map { RepoSampleSnapshot(repo, sample, it) }
            }
        }
    }

    private fun sampleSet() = TreeSet<Sample>(compareBy { it.name })
    private fun snapshotSet() = TreeSet<Snapshot>(compareByDescending { it.date })
}


object SamplerUtil {
    private val json = Json(JsonConfiguration.Stable)

    var current = initSamplerInfo()

    fun readSamplerInfo(): SamplerInfo? {
        if (!Properties.samplerInfoFile.exists()) return null
        val samplerJson = Properties.samplerInfoFile.readText()
        return json.parse(SamplerInfo.serializer(), samplerJson)
    }

    fun initSamplerInfo(): SamplerInfo {
        return readSamplerInfo() ?: SamplerInfo()
    }

    fun update(info: SamplerInfo) {
        val infoJson = json.stringify(SamplerInfo.serializer(), info)
        Properties.samplerInfoFile.outputStream().use {
            it.write(infoJson.toByteArray())
        }
    }
}

interface RepositoryI {
    val owner: String
    val repo: String
}

@Serializable
data class Repository(
    override val owner: String,
    override val repo: String
//    val samples: MutableList<Sample>
) : RepositoryI {
    fun toRepositoryId(): RepositoryId = RepositoryId.create(owner, repo)
}


@Serializable
data class Sample(
    override val name: String,
    override val path: String,
    override val branch: String,
    override val tags: List<String>
//    val snapshots: TreeSet<Snapshot> = TreeSet(compareBy { it.date })
) : SampleI

interface SampleI {
    val name: String
    val path: String
    val branch: String
    val tags: List<String>

}

@Serializable
data class Snapshot(
    override val sha: String,
    override val status: SnapshotStatus,
//    override val date: Date,
    override val date: String,
    override val buildSystem: BuildSystem,
    override val readme: String
) : SnapshotI

interface SnapshotI {
    val sha: String
    val status: SnapshotStatus
//    val date: Date
    val date: String
    val buildSystem: BuildSystem
    val readme: String
}

@Serializable
data class RepoSample(
    private val repository: Repository,
    private val sample: Sample
) : RepositoryI by repository, SampleI by sample

@Serializable
data class RepoSampleSnapshot(
    private val repository: Repository,
    private val sample: Sample,
    private val snapshot: Snapshot
) : RepositoryI by repository, SampleI by sample, SnapshotI by snapshot