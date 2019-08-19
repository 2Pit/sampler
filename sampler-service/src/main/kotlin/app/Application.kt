package app

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import app.api.events.InstallationEvent
import app.api.events.InstallationEvent.Action.*
import app.api.events.PushEvent
import app.project.Installation
import app.project.Push
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.jackson.jackson
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
@UnstableDefault
@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val json = Json(JsonConfiguration(strictMode = false))
    Properties.githubToken = this.environment.config.property("ktor.security.github.token").getString()
    Properties.init()

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
        }
    }

    routing {
        post("/add") {
            call.respond("OK")

            val text = call.receiveText()
            val event = call.request.headers["x-github-event"]!!
//            val timestamp = call.request.headers["timestamp"]!!
//            val payload = call.request.headers["x-github-delivery"]!!
            when (event) {
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
                    Push.pipeline
                }
                else -> {
                }
            }
        }

        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/json/jackson") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}