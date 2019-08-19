package app.project

import app.api.events.InstallationEvent
import app.model.CardManager
import app.model.Process
import app.model.ProcessManager
import app.services.CardContentType
import app.services.ProjectService
import app.services.Services
import io.ktor.util.pipeline.Pipeline
import io.ktor.util.pipeline.PipelinePhase
import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.Issue
import org.slf4j.LoggerFactory

object Installation {
    private val log = LoggerFactory.getLogger(Installation::class.java)
    //    TODO get id from github code
    const val CHECK_IN_COLUMN_ID = 6215563L
    const val ORGANIZATION = "ksamples"

    private val split = PipelinePhase("split")
    val splitRepos = Pipeline<Unit, InstallationEvent>(split)

    private val createIssue = PipelinePhase("createIssue")
    private val createCard = PipelinePhase("createCard")
    private val initCard = Pipeline<Subject, InstallationEvent>(createIssue, createCard)


    private val issueService = Services.issueService
    private val repositoryService = Services.repositoryService
    private val projectService = Services.projectService

    class Subject(val base: IRepositoryIdProvider) {
        lateinit var issue: Issue
        lateinit var processes: List<Process>
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
            val processes = mutableListOf(ProcessManager.create("fork"), ProcessManager.create("test"))
            val text = renderBodyText(processes)
            subject.processes = processes
            subject.issue = issueService.createIssue(
                    Consts.mainRepo,
                    Issue().apply { this.title = fullNameRepo; body = text }
            )
        }

        initCard.intercept(createCard) {
            log.debug { "Init note ${subject.base.generateId()}." }
            val gCard = projectService.createCard(
                    CHECK_IN_COLUMN_ID,
                    ProjectService.CreateRequest(subject.issue.id, CardContentType.Issue.name)
            )
            val card = CardManager.create(
                    gCard.id,
                    subject.issue.number,
                    subject.base.generateId(),
                    subject.processes)
            CheckIn.cardRunner.execute(card, Unit)
        }
    }
}