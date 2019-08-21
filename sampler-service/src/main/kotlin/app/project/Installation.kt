package app.project

import app.api.events.InstallationEvent
import app.model.*
import app.services.CardContentType
import app.services.ProjectService
import app.services.Services
import io.ktor.util.pipeline.Pipeline
import io.ktor.util.pipeline.PipelinePhase
import kotlinx.coroutines.async
import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.Issue
import org.slf4j.LoggerFactory
import java.io.IOException

object Installation {
    private val log = LoggerFactory.getLogger(Installation::class.java)
    //    TODO get id from github code
    const val CHECK_IN_COLUMN_ID = 6215563L
    const val ORGANIZATION = "ksamples"

    private val split = PipelinePhase("split")
    val splitRepos = Pipeline<Unit, InstallationEvent>(split)

    private val createIssue = PipelinePhase("createIssue")
    private val createCard = PipelinePhase("createCard")
    private val doFork = PipelinePhase("doFork")
    private val prepareTest = PipelinePhase("prepareTest")
    private val initCard = Pipeline<InitCardSubject, InstallationEvent>(createIssue, createCard, doFork, prepareTest)

    private val testRef = PipelinePhase("testRef")
    private val testPipeline = Pipeline<Unit, Pair<Card, RegularProcess>>(testRef)

    private val issueService = Services.issueService
    private val repositoryService = Services.repositoryService
    private val projectService = Services.projectService
    private val dataService = Services.dataService

    class InitCardSubject(val base: IRepositoryIdProvider) {
        lateinit var issue: Issue
        lateinit var forkProcess: SpecialProcess
        lateinit var card: Card
    }


    init {
        splitRepos.intercept(split) {
            context.repositories.forEach {
                log.debug { "Split repos ${it.full_name}." }
                val oneRepoEvent = context.copy(repositories = listOf(it))
                initCard.execute(oneRepoEvent, InitCardSubject(IRepositoryIdProvider { it.full_name }))
            }
        }

        initCard.intercept(createIssue) {
            val fullNameRepo = subject.base.generateId()
            log.debug { "Init issue $fullNameRepo." }
            val forkProcess = SpecialProcess(SpecialPT.fork)
            subject.forkProcess = forkProcess
            subject.issue = issueService.createIssue(
                    Consts.mainRepo,
                    Issue().apply { this.title = fullNameRepo; body = forkProcess.render() }
            )
        }

        initCard.intercept(createCard) {
            log.debug { "Init note ${subject.base.generateId()}." }
            val gCard = projectService.createCard(
                    CHECK_IN_COLUMN_ID,
                    ProjectService.CreateRequest(subject.issue.id, CardContentType.Issue.name)
            )
            subject.card = Card(
                    gCard.id,
                    subject.issue.number,
                    subject.base.generateId(),
                    _specialProcesses = listOf(subject.forkProcess)
            )
        }

        initCard.intercept(doFork) {
            val card = subject.card
            val process = subject.forkProcess
            log.debug { "Fork ${card.fullName}." }
            process.update(status = ProcessStatus.inProgress)
            try {
                val repo = repositoryService.forkRepository({ card.fullName }, ORGANIZATION)
                card.update(forkedRefs = dataService.getReferences(repo).map { it.ref })
                process.update(status = ProcessStatus.finished)
            } catch (e: IOException) {
                log.error(e.localizedMessage)
                process.update(status = ProcessStatus.error, errorDescription = e.localizedMessage)
                throw e
            }
        }

        initCard.intercept(prepareTest) {
            val card = subject.card
            val processes = card.forkedRefs.map { ref ->
                RegularProcess(RegularPT.testRef, card.fullName.substringBefore("/"), card.repo, ref)
            }
            card.update(regularProcesses = processes)
            card.updateUI()
            processes.forEach {
                async {
                    testPipeline.execute(card to it, Unit)
                }
            }
        }

        testPipeline.intercept(testRef) {
            val (card, process) = context
            log.debug { process.renderLabel() }
//            Do smth
            process.update(status = ProcessStatus.finished)
            card.updateUI()
        }
    }
}