package app.project

import app.api.events.PushEvent
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

    init {
        pipeline.intercept(findCard) {
            val event = context
            val fullName = event.repository.full_name
            val card = CardManager.get(fullName)
            if (card == null) {
                log.error { "Unknown card $fullName." }
                return@intercept
            }

            event.ref

            when (card.status) {
                CardStatus.checkIn -> {
                }
                CardStatus.added -> {
                }
                CardStatus.updated -> {
                }
                CardStatus.stopped -> {
                }
            }
        }
    }
}