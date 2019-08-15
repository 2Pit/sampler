package app

import app.states.CheckIn
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import gitmove.InstallationEvent
import gitmove.InstallationEvent.Action.*
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
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@UnstableDefault
@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val json = Json(JsonConfiguration(strictMode = false))

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
            val timestamp = call.request.headers["timestamp"]!!
            val payload = call.request.headers["x-github-delivery"]!!
            when (event) {
                "installation" -> {
                    val installationEvent = json.parse(InstallationEvent.serializer(), text)
                    when (installationEvent.action) {
                        created -> CheckIn.splitRepos.execute(installationEvent, Unit)
                        deleted -> TODO()
                        new_permissions_accepted -> TODO("UNSUPPORTED")
                    }
                }
                "push" -> {
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