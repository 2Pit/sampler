package app.project

import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.slf4j.Logger

object Consts {
    //    TODO get ids from github code
    val projectId = 3064867

    val checkInColumnId = 6215563L
    val addedColumnId = 6215567L
    val updatedColumnId = 6215568L
    val stoppedColumnId = 6215569L
}

inline fun Logger.debug(producer: () -> String) {
    if (this.isDebugEnabled) this.debug(producer())
}

inline fun Logger.error(producer: () -> String) {
    if (this.isErrorEnabled) this.error(producer())
}
