package app.project

import org.slf4j.Logger

inline fun Logger.debug(producer: () -> String) {
    if (this.isDebugEnabled) this.debug(producer())
}

inline fun Logger.error(producer: () -> String) {
    if (this.isErrorEnabled) this.error(producer())
}
