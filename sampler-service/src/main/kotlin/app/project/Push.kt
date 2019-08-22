package app.project

import app.api.events.PushEvent
import app.model.Card
import app.model.CardManager
import app.services.PullRequestService
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
                return@intercept // Ignore. PR already exist
            }

            createPrPipeline.execute(event to card, Unit)
        }

        createPrPipeline.intercept(first) {
            val (event, card) = context
            val ref = event.ref
            val body = PullRequestService.CreateRequest(
                    "PR title",
                    head = ref,
                    base = ""
            )
            pullRequestService.create(card.owner, card.repo, body)
        }
    }
}