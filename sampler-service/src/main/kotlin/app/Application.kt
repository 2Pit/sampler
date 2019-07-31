package app

import app.api.AddRequest
import app.api.RepoInfo
import app.api.SampleInfo
import app.db.*
import app.storage.RepoSampleSnapshot
import app.storage.SamplerUtil
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.jackson.jackson
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.util.toMap
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val json = Json(JsonConfiguration.Stable)
//    DB.init()

//    SamplerUtil.current

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
        }
    }

    install(Routing) {
        route("/all") {
            val all = SamplerUtil.current.all()
        }

//        myRoute("/jobs", Jobs)
    }

    routing {

        get("/all") {
            val all = SamplerUtil.current.all()
            val ans = json.stringify(RepoSampleSnapshot.serializer().list, all)
            call.respondText(ans, contentType = ContentType.Application.Json)
        }

        post("/add") {
            val addRequest = json.parse(AddRequest.serializer(), call.receiveText())
            val processId = Jobs.insert(JobRow(-1, -1, JobStatus.NEW, "", addRequest.toString()))
            call.respond(mapOf("processId" to processId))
            Checker.checker.execute(CheckerContext(processId, addRequest), CheckerSubject())
        }

        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/json/jackson") {
            call.respond(mapOf("hello" to "world"))
        }

        get("/jobs") {
            val params = call.parameters.toMap()
            val filter = JobFilter(
                params["id"]?.first()?.toInt(),
                params["processId"]?.first()?.toInt(),
                params["status"]?.first()?.run { JobStatus.valueOf(this) },
                params["description"]?.first(),
                params["context"]?.first()
            )

            val res = Jobs.getBy(filter)
            call.respond(json.stringify(JobRow.serializer().list, res))
        }
    }
}

fun initSamplesInfo(): Boolean {
    val oldInfo = readSamplesInfo()
    val newInfo = buildSamplesInfo()
    if (oldInfo == null || oldInfo != newInfo) {
        writeSamplesInfo(newInfo)
        return true
    }
    return false
}

fun writeSamplesInfo(samplesInfo: List<RepoInfo>) {
    val json = Json(JsonConfiguration.Stable)
    val sInfo = json.stringify(RepoInfo.serializer().list, samplesInfo)
    Properties.infoFile.outputStream().use {
        it.write(sInfo.toByteArray())
    }
}


fun buildSamplesInfo(): List<RepoInfo> {
    val tmpMap = mutableMapOf<RepositoryRow, MutableList<SampleInfo>>()
    transaction {
        (Samples leftJoin Snapshots leftJoin Repositories).select {
            Snapshots.status eq SnapshotStatus.OK.name
            Samples.validSnapshotId eq Snapshots.id
        }.forEach {
            val key = Repositories.convert(it)
            tmpMap.getOrPut(key) { mutableListOf() }.add(
                SampleInfo(
                    it[Samples.name],
                    it[Snapshots.readme],
                    it[Snapshots.buildSystem],
                    it[Snapshots.sha]
                )
            )
        }
    }

    val res = mutableListOf<RepoInfo>()
    tmpMap.forEach { (repositoryRow, samples) -> res.add(RepoInfo(repositoryRow, samples)) }
    return res
}


fun readSamplesInfo(): List<RepoInfo>? {
    val infoFile = Properties.infoFile
    return if (!infoFile.exists()) {
        null
    } else {
        val json = Json(JsonConfiguration.Stable)
        json.parse(RepoInfo.serializer().list, infoFile.readText())
    }
}
