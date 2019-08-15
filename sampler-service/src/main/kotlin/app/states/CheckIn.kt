package app.states

import app.Services
import gitmove.InstallationEvent
import io.ktor.util.pipeline.Pipeline
import io.ktor.util.pipeline.PipelinePhase
import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.Issue
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object CheckIn {
    val log = LoggerFactory.getLogger(CheckIn::class.java)
    //    TODO get id from github code
    const val columnId = 6215563L

    private val initIssue = PipelinePhase("initIssue")
    private val initNote = PipelinePhase("initNote")
    private val fork = PipelinePhase("fork")
    private val test = PipelinePhase("test")
    val pipeline = Pipeline<Subject, InstallationEvent>(initIssue, initNote, fork, test)

    private val split = PipelinePhase("split")
    val splitRepos = Pipeline<Unit, InstallationEvent>(split)

    val issueService = Services.issueService
    val repositoryService = Services.repositoryService
    val projectService = Services.projectService

    class Subject(val base: IRepositoryIdProvider) {
        lateinit var issue: Issue
        lateinit var fork: IRepositoryIdProvider
        val issueState = mutableMapOf("fork" to false, "test" to false)
    }


    init {
        splitRepos.intercept(split) {
            context.repositories.forEach {
                log.debug { "Split repos ${it.full_name}." }
                val oneRepoEvent = context.copy(repositories = listOf(it))
                pipeline.execute(oneRepoEvent, Subject(IRepositoryIdProvider { it.full_name }))
            }
        }

        pipeline.intercept(initIssue) {
            val fullNameRepo = subject.base.generateId()
            log.debug { "Init issue $fullNameRepo." }
            val text = renderBodyText(subject.issueState)
            subject.issue = issueService.createIssue(
                    Consts.mainRepo,
                    Issue().apply { this.title = fullNameRepo; body = text }
            )
        }

        pipeline.intercept(initNote) {
            log.debug { "Init note ${subject.base.generateId()}." }
//            projectService.createCard(columnId, subject.issue.id, CardContentType.Issue.name)
        }

        pipeline.intercept(fork) {
            log.debug { "Fork ${subject.base.generateId()}." }
            subject.fork = repositoryService.forkRepository(subject.base, "ksamples")
            subject.issueState["fork"] = true
            updateIssue(subject.issue, subject.issueState)
        }

        pipeline.intercept(test) {
            log.debug { "Test ${subject.base.generateId()}." }
            subject.issueState["test"] = true
            updateIssue(subject.issue, subject.issueState)
        }
    }

    private fun updateIssue(issue: Issue, state: Map<String, Boolean>) {
        issue.body = renderBodyText(state)
        Services.issueService.editIssue(Consts.mainRepo, issue)
    }

    private fun renderBodyText(state: Map<String, Boolean>): String {
        return StringBuilder().apply {
            state.forEach { (prop, flag) ->
                appendln(if (flag) "- [x] $prop" else "- [ ] $prop")
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