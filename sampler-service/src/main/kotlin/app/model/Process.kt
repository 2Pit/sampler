package app.model

import kotlinx.serialization.Serializable

enum class ProcessStatus { open, inProgress, finished, error }

interface ProcessType
enum class RegularPT : ProcessType { createPR, testRef }
enum class SpecialPT : ProcessType { fork, moveTo, adding, stopping, updating }

abstract class AbstractProcess(
        open val type: ProcessType,
        protected var _status: ProcessStatus = ProcessStatus.open,
        protected var _errorDescription: String? = null
) : ProcessRendering {
    init {
        commit()
    }

    override val status = _status
    override val errorDescription = _errorDescription

    //    TODO: sync on object
    fun update(
            status: ProcessStatus = this._status,
            errorDescription: String? = this._errorDescription
    ) {
        _status = status
        _errorDescription = errorDescription
        commit()
    }

    private fun commit() = CardManager.commit()
}

@Serializable
class RegularProcess(
        override val type: RegularPT,
        val owner: String,
        val repo: String,
        val ref: String,
        status: ProcessStatus = ProcessStatus.open,
        errorDescription: String? = null
) : AbstractProcess(type, status, errorDescription) {
    override fun renderLabel(): String = when (type) {
        RegularPT.createPR -> "createPR $ref"
        RegularPT.testRef -> {
            val own = if (owner == "ksamples") "" else owner
            "test $own/$ref"
        }
    }
}

@Serializable
class SpecialProcess(
        override val type: SpecialPT,
        status: ProcessStatus = ProcessStatus.open,
        errorDescription: String? = null
) : AbstractProcess(type, status, errorDescription) {
    override fun renderLabel(): String = type.name
}

interface ProcessRendering {
    val status: ProcessStatus
    val errorDescription: String?

    fun renderLabel(): String

    fun render(): String {
        return when (status) {
            ProcessStatus.open -> "- [ ] ${renderLabel()}"
            ProcessStatus.inProgress -> "- [ ] ${renderLabel()}"
            ProcessStatus.finished -> "- [x] ${renderLabel()}"
            ProcessStatus.error -> "- [ ] ${renderLabel()}\nerror: $errorDescription"
        }
    }
}
