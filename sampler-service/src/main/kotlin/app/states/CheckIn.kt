package app.states

import app.Services
import gitmove.InstallationEvent
import gitmove.services.CardContentType
import gitmove.services.ProjectService
import io.ktor.util.pipeline.Pipeline
import io.ktor.util.pipeline.PipelinePhase
import io.ktor.util.pipeline.execute
import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.Issue
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException

object CheckIn {
    val log = LoggerFactory.getLogger(CheckIn::class.java)
    //    TODO get id from github code
    const val columnId = 6215563L
    const val organization = "ksamples"

    private val split = PipelinePhase("split")
    val splitRepos = Pipeline<Unit, InstallationEvent>(split)

    private val createIssue = PipelinePhase("createIssue")
    private val createCard = PipelinePhase("createCard")
    private val initCard = Pipeline<Subject, InstallationEvent>(createIssue, createCard)

    private val runCard = PipelinePhase("runCard")
    private val cardRunner = Pipeline<Unit, Card>(runCard)


    private val issueService = Services.issueService
    private val repositoryService = Services.repositoryService
    private val projectService = Services.projectService

    class Subject(val base: IRepositoryIdProvider) {
        lateinit var issue: Issue
        lateinit var processes: MutableList<Process>
    }


    init {
        splitRepos.intercept(split) {
            context.repositories.forEach {
                log.debug { "Split repos ${it.full_name}." }
                val oneRepoEvent = context.copy(repositories = listOf(it))
                initCard.execute(oneRepoEvent, Subject(IRepositoryIdProvider { it.full_name }))
            }
        }

        initCard.intercept(createIssue) {
            val fullNameRepo = subject.base.generateId()
            log.debug { "Init issue $fullNameRepo." }
            val processes = mutableListOf(Process("fork"), Process("test"))
            val text = renderBodyText(processes)
            subject.processes = processes
            subject.issue = issueService.createIssue(
                    Consts.mainRepo,
                    Issue().apply { this.title = fullNameRepo; body = text }
            )
        }

        initCard.intercept(createCard) {
            log.debug { "Init note ${subject.base.generateId()}." }
            val gCard = projectService.createCard(columnId, ProjectService.CreateRequest(subject.issue.id, CardContentType.Issue.name)).body()!!
            val card = Card(
                    gCard.id,
                    subject.issue.number,
                    subject.base.generateId().substringBefore("/"),
                    subject.base.generateId().substringAfter("/"),
                    subject.processes)
            cardRunner.execute(card)
        }

        cardRunner.intercept(runCard) {
            val card = context
            var open = card.processes.find { it.status == ProcessStatus.open }
            while (open != null) {
                when (val name = open.name) {
                    "fork" -> fork(card, open)
                    "test" -> test(card, open)
                    else -> log.error { "Unknown process ${name}." }
                }
                update(card)
                open = card.processes.find { it.status == ProcessStatus.open }
            }
        }
    }

    private fun update(card: Card) {
        val issue = Services.issueService.getIssue(Consts.mainRepo, card.issueNumber)
        // TODO Merdge
        // TODO set lables
        issue.body = renderBodyText(card.processes)
        Services.issueService.editIssue(Consts.mainRepo, issue)
    }

    fun fork(card: Card, process: Process) {
        log.debug { "Fork ${card.ownerRepo()}." }
        process.status = ProcessStatus.inProgress
        try {
            repositoryService.forkRepository(card.ownerRepo, organization)
            process.status = ProcessStatus.finished
        } catch (e: IOException) {
            log.error(e.localizedMessage)
            process.status = ProcessStatus.error
            process.errorDescription = e.localizedMessage
        }

    }

    fun test(card: Card, process: Process) {
        log.debug { "Fork ${card.ownerRepo()}." }
        process.status = ProcessStatus.inProgress
        process.status = ProcessStatus.finished
    }

    private fun renderBodyText(processes: List<Process>): String {
        return StringBuilder().apply {
            processes.forEach { process ->
                appendln(
                        when (process.status) {
                            ProcessStatus.open -> "- [ ] ${process.name}"
                            ProcessStatus.inProgress -> "- [ ] ${process.name}"
                            ProcessStatus.finished -> "- [x] ${process.name}"
                            ProcessStatus.error -> "- [ ] ${process.name} [error]"
                        }
                )
            }
        }.toString()
    }
}

object Adding {
    val log = LoggerFactory.getLogger(Adding::class.java)
    const val columnId = 6215567

    private val adding = PipelinePhase("adding")
    val pipeline = Pipeline<Unit, InstallationEvent>(adding)

}

object Updating {
    val log = LoggerFactory.getLogger(Updating::class.java)
    const val columnId = 6215568

    private val createPRs = PipelinePhase("checkIn")
    private val test = PipelinePhase("checkIn")
    //    PushEvent
    val pipeline = Pipeline<Unit, InstallationEvent>(createPRs, test)

}

object Stopping {
    val log = LoggerFactory.getLogger(Stopping::class.java)
    const val columnId = 6215569

    private val stopping = PipelinePhase("stopping")
    val pipeline = Pipeline<Unit, InstallationEvent>(stopping)
}

object Consts {
    val projectId = 3064867
    val mainRepo = IRepositoryIdProvider { "ksamples/main" }
}

inline fun Logger.debug(producer: () -> String) {
    if (this.isDebugEnabled) this.debug(producer())
}

inline fun Logger.error(producer: () -> String) {
    if (this.isErrorEnabled) this.error(producer())
}
