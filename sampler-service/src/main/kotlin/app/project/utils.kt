package app.project

import app.model.Card
import app.services.Services
import kotlinx.coroutines.runBlocking
import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.slf4j.Logger
import java.util.regex.Pattern
import app.model.Process
import app.model.ProcessStatus

fun renderBodyText(processes: List<Process>): String {
    return StringBuilder().apply {
        processes.forEach { process ->
            appendln(
                    when (process.status) {
                        ProcessStatus.open -> "- [ ] ${process.name}"
                        ProcessStatus.inProgress -> "- [ ] ${process.name}"
                        ProcessStatus.finished -> "- [x] ${process.name}"
                        ProcessStatus.error -> "- [ ] ${process.name} [error]"
                    }
            )
        }
    }.toString()
}


object Consts {
    //    TODO get ids from github code
    val projectId = 3064867

    val checkInColumnId = 6215563L
    val addedColumnId = 6215567L
    val updatedColumnId = 6215568L
    val stoppedColumnId = 6215569L

    val mainRepo = IRepositoryIdProvider { "ksamples/main" }
}

inline fun Logger.debug(producer: () -> String) {
    if (this.isDebugEnabled) this.debug(producer())
}

inline fun Logger.error(producer: () -> String) {
    if (this.isErrorEnabled) this.error(producer())
}
