import app.Services
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

fun main() {
    val json = Json(JsonConfiguration(strictMode = false, prettyPrint = true))
    val projectService = Services.projectService

    runBlocking {

        val projectId = 3064867
        val issueId = 481107077L

//        val columns = projectService.getColumns(3064867).body()!!
//        columns.forEach {
//            println(json.stringify(GColumn.serializer(), it))
//        }

//        val res = Services.projectService.createCard(CheckIn.columnId, ProjectService.CreateRequest(issueId, CardContentType.Issue.name))
//        if (!res.isSuccessful) {
//            res.errorBody()
//            val buffer = Buffer()
//            println(res.errorBody()?.string())
//        }
//        println(res.isSuccessful)
//        println(res.errorBody())
//        println(res.body())

        val issue = Services.issueService.getIssue({ "ksamples/main" }, 5)
        println(issue)

    }
    println("end")
}