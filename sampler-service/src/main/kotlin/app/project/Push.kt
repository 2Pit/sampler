package app.project

import app.api.events.PushEvent
import app.model.Card
import app.model.CardManager
import app.model.CardStatus
import app.services.Services
import io.ktor.util.pipeline.Pipeline
import io.ktor.util.pipeline.PipelinePhase
import org.slf4j.LoggerFactory

object Push {
    val log = LoggerFactory.getLogger(Push::class.java)
    private val findCard = PipelinePhase("findCard")
    val pipeline = Pipeline<Unit, PushEvent>(findCard)
    val projectService = Services.projectService

    private val first = PipelinePhase("first")
    val createPrPipeline = Pipeline<Unit, Pair<PushEvent, Card>>(first)

    val pullRequestService = Services.pullRequestService

    init {
        pipeline.intercept(findCard) {
            val event = context
            val fullName = event.repository.fullName
            val card = CardManager.get(fullName)
            if (card == null) {
                log.error { "Unknown card $fullName." }
                return@intercept
            }

            val ref = event.ref
            if (card.prBranches.contains(ref)) {
//                Ignore. PR already exist
                return@intercept
            }

            when (card.status) {
                CardStatus.CHECK_IN -> {
                }
                CardStatus.ADDING -> {
                }
                CardStatus.UPDATING -> {
                }
                CardStatus.STOPPING -> {
                }
            }
        }

        createPrPipeline.intercept(first) {
            //            val (event, card) = context
//            val ref = event.ref
//            val pr = PullRequest().apply {
//                title = ""
//                head = PullRequestMarker().apply { }
//                base = ""
//            }
//            pullRequestService.createPullRequest()
        }
    }
}