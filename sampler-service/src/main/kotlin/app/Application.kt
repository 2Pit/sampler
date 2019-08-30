package app

import app.api.events.InstallationEvent
import app.api.events.InstallationEvent.Action.*
import app.api.events.PushEvent
import app.project.Installation
import app.project.Push
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

object Application {
    @JvmStatic
    fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)
}

@KtorExperimentalAPI
@UnstableDefault
@Suppress("unused")
fun Application.module() {
    val json = Json(JsonConfiguration(strictMode = false))

    routing {
        post("/add") {
            call.respond("OK")

            val text = call.receiveText()
            when (val event = call.request.headers["x-github-event"]!!) {
                "installation" -> {
                    val installationEvent = json.parse(InstallationEvent.serializer(), text)
                    when (installationEvent.action) {
                        created -> Installation.splitRepos.execute(installationEvent, Unit)
                        deleted -> TODO()
                        new_permissions_accepted -> TODO("UNSUPPORTED")
                    }
                }
                "push" -> {
                    val pushEvent = json.parse(PushEvent.serializer(), text)
                    Push.pipeline.execute(pushEvent, Unit)
                }
                else -> TODO("Unsupported event: $event")
            }
        }
    }
}