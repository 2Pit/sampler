package app.model

import com.test.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import java.io.File

/*Do not create this class directly. Use ProcessManager.*/
@Serializable
class Process internal constructor(
        val id: Long,
        val name: String,
        val status: ProcessStatus = ProcessStatus.open,
        val errorDescription: String? = null
) {
    fun update(
            status: ProcessStatus = this.status,
            errorDescription: String? = this.errorDescription
    ): Process {
        return ProcessManager.update(id, name, status, errorDescription)
    }
}

enum class ProcessStatus { open, inProgress, finished, error }


object ProcessManager {
    private val processes: MutableMap<Long, Process> = mutableMapOf()
    private val file = File(Settings.storageDir, "processes.json")
    private val json = Json(JsonConfiguration.Stable)
    private var counter: Long

    init {
        if (file.exists())
            json.parse(Process.serializer().list, file.readText()).associateByTo(processes) { it.id }
        counter = processes.values.map { it.id }.max() ?: 0
    }

    fun create(
            name: String,
            status: ProcessStatus = ProcessStatus.open,
            errorDescription: String? = null
    ): Process {
        val res = Process(counter++, name, status, errorDescription)
        processes[res.id] = res
        commit()
        return res
    }

    fun update(
            id: Long,
            name: String,
            status: ProcessStatus = ProcessStatus.open,
            errorDescription: String? = null
    ): Process {
        val res = Process(id, name, status, errorDescription)
        processes[id] = res
        commit()
        return res
    }

    fun get(id: Long): Process? = processes[id]

    fun get(ids: List<Long>): List<Process> = ids.map { get(it)!! }

    fun getAll(): List<Process> = processes.values.toList()

    private fun commit() {
        file.writeText(json.stringify(Process.serializer().list, processes.values.toList()))
    }
}