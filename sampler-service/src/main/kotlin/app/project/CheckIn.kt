package app.project

import app.services.Services
import app.api.events.InstallationEvent
import app.model.Card
import app.model.Process
import app.model.ProcessStatus
import io.ktor.util.pipeline.Pipeline
import io.ktor.util.pipeline.PipelinePhase
import org.slf4j.LoggerFactory
import java.io.IOException

object CheckIn {
    val log = LoggerFactory.getLogger(CheckIn::class.java)
    const val organization = "ksamples"

    private val runCard = PipelinePhase("runCard")
    val cardRunner = Pipeline<Unit, Card>(runCard)

    private val repositoryService = Services.repositoryService

    init {
        cardRunner.intercept(runCard) {
            val card = context
            var open = card.processes.find { it.status == ProcessStatus.open }
            while (open != null) {
                when (val name = open.name) {
                    "fork" -> fork(card, open)
                    "test" -> test(card, open)
                    else -> log.error { "Unknown process $name." }
                }
                update(card)
                open = card.processes.find { it.status == ProcessStatus.open }
            }
        }
    }

    private fun update(card: Card) {
        val issue = Services.issueService.getIssue(Consts.mainRepo, card.issueNumber)
        // TODO set lables
        issue.body = renderBodyText(card.processes)
        Services.issueService.editIssue(Consts.mainRepo, issue)
    }

    fun fork(card: Card, process: Process) {
        log.debug { "Fork ${card.fullName}." }
        process.update(status = ProcessStatus.inProgress)
        try {
            repositoryService.forkRepository({ card.fullName }, organization)
            process.update(status = ProcessStatus.finished)
        } catch (e: IOException) {
            log.error(e.localizedMessage)
            process.update(status = ProcessStatus.error, errorDescription = e.localizedMessage)
        }

    }

    fun test(card: Card, process: Process) {
        log.debug { "Fork ${card.fullName}." }
        process.update(status = ProcessStatus.inProgress)
//        TODO smth
        process.update(status = ProcessStatus.finished)
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
