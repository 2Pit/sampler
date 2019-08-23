package app.project

import app.Properties
import app.api.events.InstallationEvent
import app.model.*
import app.services.CardContentType
import app.services.ProjectService
import app.services.Services
import io.ktor.util.pipeline.Pipeline
import io.ktor.util.pipeline.PipelinePhase
import kotlinx.coroutines.async
import org.eclipse.egit.github.core.Issue
import org.slf4j.LoggerFactory
import java.io.IOException

object Installation {
    private val log = LoggerFactory.getLogger(Installation::class.java)
    //    TODO get id from github code
    private const val CHECK_IN_COLUMN_ID = 6215563L
    private const val ORGANIZATION = "ksamples"

    private val split = PipelinePhase("split")
    val splitRepos = Pipeline<Unit, InstallationEvent>(split)


    //TODO to enum
    private val createIssue = PipelinePhase("createIssue")
    private val createCard = PipelinePhase("createCard")
    private val doFork = PipelinePhase("doFork")
    private val prepareTest = PipelinePhase("prepareTest")

    private val initCard = Pipeline<InitCardSubject, InstallationEvent>(createIssue, createCard, doFork, prepareTest)

    private val testRef = PipelinePhase("testRef")
    private val testPipeline = Pipeline<Unit, Pair<CardI, RegularProcessI>>(testRef)

    private val issueService = Services.issueService
    private val repositoryService = Services.repositoryService
    private val projectService = Services.projectService
    private val dataService = Services.dataService

    class InitCardSubject(val owner: String, val repo: String) {
        val fullName = "$owner/$repo"
        var issue: Issue? = null
        var forkProcess: SpecialProcessI? = null
        var card: CardI? = null
    }


    init {
        splitRepos.intercept(split) {
            context.repositories.forEach {
                log.debug { "Split repos ${it.fullName}." }
                val oneRepoEvent = context.copy(repositories = listOf(it))
                initCard.execute(
                        oneRepoEvent,
                        InitCardSubject(it.fullName.substringBefore("/"), it.fullName.substringAfter("/"))
                )
            }
        }

        initCard.intercept(createIssue) {
            log.debug { "Init issue ${subject.fullName}." }
            val forkProcess = SpecialProcess.create(SpecialPT.FORK)
            subject.forkProcess = forkProcess
            subject.issue = issueService.createIssue(
                    Properties.mainRepo,
                    Issue().apply { title = subject.fullName; body = forkProcess.render() }
            )
        }

        initCard.intercept(createCard) {
            log.debug { "Init note ${subject.fullName}." }
            val gCard = projectService.createCard(
                    CHECK_IN_COLUMN_ID,
                    ProjectService.CreateRequest(subject.issue!!.id, CardContentType.Issue.name)
            )
            subject.card = Card.create(
                    gCard.id,
                    subject.issue!!.number,
                    subject.owner,
                    subject.repo,
                    specialProcesses = listOf(subject.forkProcess!!)
            )
        }

        initCard.intercept(doFork) {
            val card = subject.card!!
            val process = subject.forkProcess!!
            log.debug { "Fork ${card.fullName}." }
            process.update { status = ProcessStatus.IN_PROGRESS }
            try {
                //use suspend functions
                val repo = repositoryService.forkRepository({ card.fullName }, ORGANIZATION)
                card.update { forkedRefs = dataService.getReferences(repo).map { it.ref } }
                process.update { status = ProcessStatus.FINISHED }
            } catch (e: IOException) {
                log.error(e.localizedMessage)
                process.update { status = ProcessStatus.ERROR; errorDescription = e.localizedMessage }
                throw e
            }
        }

        initCard.intercept(prepareTest) {
            val card = subject.card!!
            val processes = card.forkedRefs.map { ref ->
                RegularProcess.create(RegularPT.TEST_REF, card.fullName.substringBefore("/"), card.repo, ref)
            }
            card.update { regularProcesses = processes }
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
            // TODO test implementation
            process.update { status = ProcessStatus.FINISHED }
            card.updateUI()
        }
    }
}