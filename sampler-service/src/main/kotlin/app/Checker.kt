package app

import arrow.core.extensions.`try`.monad.binding
import arrow.core.getOrElse
import app.api.AddRequest
import app.db.*
import app.git.*
import app.git.Connector.downloadSample
import app.git.Connector.getDirSha
import app.git.Connector.getLatestCommitSha
import app.storage.Sample
import app.storage.SamplerUtil
import io.ktor.util.pipeline.Pipeline
import io.ktor.util.pipeline.PipelinePhase
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.transaction
import java.io.File
import java.lang.RuntimeException

object Checker {
    private val repositoryChecker = PipelinePhase("LocationChecker")
    private val sampleChecker = PipelinePhase("SampleChecker")
    private val snapshotChecker = PipelinePhase("SnapshotChecker")
    private val samplePublisher = PipelinePhase("SamplePublisher")
    private val metaUpdater = PipelinePhase("MetaUpdater")

    val checker = Pipeline<CheckerSubject, CheckerContext>(
        repositoryChecker,
        sampleChecker,
        snapshotChecker,
        samplePublisher,
        metaUpdater
    )

    init {
        checker.intercept(repositoryChecker) {
            val jobId = this.context.jobId
            val addRequest = this.context.addRequest

            val samplerInfo = SamplerUtil.current

            val repository = app.storage.Repository(addRequest.owner, addRequest.repo)
            if (samplerInfo.containsRepo(repository)) {
                Jobs.insert(JobRow(-1, jobId, JobStatus.IN_PROGRESS, "The Location already exist.", ""))
                return@intercept
            } else {
                binding {
                    val (_repository) = Connector.getRepository(addRequest.owner, addRequest.repo)
                    Connector.getContent(_repository, addRequest.path)
                }.getOrElse { ex ->
                    Jobs.insert(JobRow(-1, jobId, JobStatus.ERROR, "No such location.", ""))
                    throw ex
                }

                samplerInfo.addRepo(repository)
                subject.repository = repository
                Jobs.insert(JobRow(-1, jobId, JobStatus.IN_PROGRESS, "The Repository added.", ""))
            }

//            val repositories = Repositories.getBy(
//                RepositoryFilter(
//                    owner = addRequest.owner,
//                    repo = addRequest.repo,
//                    branch = addRequest.branch
//                )
//            )
//            if (repositories.isNotEmpty()) {
//                this.subject.repositoryId = repositories.first().id
//                Jobs.insert(JobRow(-1, jobId, JobStatus.IN_PROGRESS, "The Location already exist.", ""))
//                return@intercept
//            }

//            binding {
//                val (repository) = Connector.getRepository(addRequest.owner, addRequest.repo)
//                Connector.getContent(repository, addRequest.path)
//            }.getOrElse { ex ->
//                Jobs.insert(JobRow(-1, jobId, JobStatus.ERROR, "No such location.", ""))
//                throw ex
//            }

//            val repositoryId: Int = transaction {
//                Repositories.insert {
//                    it[owner] = addRequest.owner
//                    it[repository] = addRequest.repo
//                    it[branch] = addRequest.branch
//                }
//            }[Repositories.id]!!.value
//
//            subject.repositoryId = repositoryId

//            Jobs.insert(JobRow(-1, jobId, JobStatus.IN_PROGRESS, "The Repository added.", ""))
        }

        checker.intercept(sampleChecker) {
            val subject = this.subject
            val jobId = this.context.jobId
            val addRequest = this.context.addRequest

            Jobs.insert(JobRow(-1, jobId, JobStatus.IN_PROGRESS, "Sample checking.", ""))

            val samplerInfo = SamplerUtil.current
            val sample = Sample(addRequest.name, addRequest.path, addRequest.branch, emptyList())
            val repository = subject.repository

            if (samplerInfo.containsSample(repository, sample)) {
                Jobs.insert(JobRow(-1, jobId, JobStatus.DONE, "Sample already exist.", ""))
                throw RuntimeException("Stop pipeline.")
            } else {
                binding {
//                    val repository = Repository(addRequest.owner, addRequest.repo, addRequest.branch)
                    val (commitSha) = getLatestCommitSha(repository, addRequest.path)
                    val (dirSha) = getDirSha(repository, commitSha, addRequest.path)
                    val (sampleFiles) = downloadSample(repository, dirSha)
                    sampleFiles to dirSha
                }.fold(
                    { ex ->
                        Jobs.insert(JobRow(-1, jobId, JobStatus.ERROR, ex.localizedMessage, ""))
                        Unit
                    },
                    { (sampleFiles, dirSha) ->
                        subject.files = sampleFiles
                        subject.sha = dirSha
                    }
                )
            }

            // set status checking
            samplerInfo.addSample(repository, sample)

        }

        checker.intercept(snapshotChecker) {
//                TODO: Turn on Jenkins
//                SampleBuilder.write(sampleRequest, sample)
//                notifyJenkins(sampleRequest)
        }

        checker.intercept(samplePublisher) {
            val subject = this.subject
            val context = this.context
            val jobId = context.jobId
            val files = subject.files
            val addRequest = context.addRequest
            val zipFile = File(
                Properties.storagePath,
                "${addRequest.owner}_${addRequest.repo}_${addRequest.name}_${subject.sha}.zip"
            )
            files.compress().write(zipFile)

            Jobs.insert(JobRow(-1, jobId, JobStatus.DONE, "Zip file created.", ""))
        }

        checker.intercept(metaUpdater) {
            val jobId = context.jobId
            if (initSamplesInfo()) {
                Jobs.insert(JobRow(-1, jobId, JobStatus.DONE, "Meta updated.", ""))
            }
        }
    }

//    private suspend fun notifyJenkins(pathToSample: String) {
//        HttpClient(Apache).use {
//            it.post<Unit>(port = Properties.jenkinsPort, path = "job/item/buildWithParameters") {
//                header("Authorization", "Basic ${Properties.jenkinsAuth}")
//                parameter("token", Properties.jenkinsRunTaskToken)
//                parameter(
//                    "full_sample_path",
//                    pathToSample
//                )
//            }
//        }
//    }
}

data class CheckerContext(val jobId: Int, val addRequest: AddRequest)

class CheckerSubject {
    lateinit var sha: String
    lateinit var files: List<SampleFile>
    lateinit var repository: app.storage.Repository
    lateinit var sample: app.storage.Sample
    lateinit var snapshot: app.storage.Snapshot
    var repositoryId: Int? = null
}