package app.project

import app.api.events.InstallationEvent
import io.ktor.util.pipeline.Pipeline
import io.ktor.util.pipeline.PipelinePhase
import org.slf4j.LoggerFactory

object CheckIn {
    private val log = LoggerFactory.getLogger(CheckIn::class.java)

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
