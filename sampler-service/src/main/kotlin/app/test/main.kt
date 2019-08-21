package app.test

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

fun main() {
    val json = Json(JsonConfiguration(strictMode = false, prettyPrint = true))

    val a = A.create(listOf(1))

    println(a)
    a.update { l = listOf(1, 2, 4) }
    println(a)


    println("end")
}

interface I {
    val l: List<Int>

    fun update(up: A.() -> Unit) {
        up(this as A)
        commit()
    }

    fun commit() {
        println("commit")
    }
}

@Serializable
data class A constructor(override var l: List<Int>) : I {
    companion object {
        fun create(l: List<Int>): A {
            return A(l)
        }
    }


    class Updater {
        var l: List<Int>? = null
    }

    fun update1(upd: Updater.() -> Unit) {
        Updater().apply(upd).also { updater ->
            updater.l?.let { this.l = it }
        }
    }

}