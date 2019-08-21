package app.test

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

fun main() {
    val json = Json(JsonConfiguration(strictMode = false, prettyPrint = true))

    val a = A(1, "a")
    val b = a.copy(name = "b")

    println("end")
}

data class A(val id: Int, val name: String) {
    init {
        println("init $name")
    }
}