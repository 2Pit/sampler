package app.api

import app.db.DataFilter
import app.db.DataRow
import app.db.Service
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.myRoute(path: String, service: Service<out DataRow, out DataFilter>) {
    route(path) {

//        get("/") {
//            val params = call.parameters.toMap()
//            call.respond(service.get(params, null))
//        }

        get("/{id}") {
            val sample = service.getBy(call.parameters["id"]!!.toInt())
            if (sample == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(sample)
            }
        }
    }
}