package app.model

import kotlinx.serialization.Serializable

enum class ProcessStatus { OPEN, IN_PROGRESS, FINISHED, ERROR }

interface ProcessType
enum class RegularPT : ProcessType { CREATE_PR, TEST_REF }
enum class SpecialPT : ProcessType { FORK, MOVE_TO, ADDING, STOPPING, UPDATING }

interface ProcessRendering {
    val type: ProcessType
    val status: ProcessStatus
    val errorDescription: String?

    fun renderLabel(): String

    fun render(): String {
        return when (status) {
            ProcessStatus.OPEN -> "- [ ] ${renderLabel()}"
            ProcessStatus.IN_PROGRESS -> "- [ ] ${renderLabel()}"
            ProcessStatus.FINISHED -> "- [x] ${renderLabel()}"
            ProcessStatus.ERROR -> "- [ ] ${renderLabel()}\nerror: $errorDescription"
        }
    }
}

interface RegularProcessI : ProcessRendering {
    override val type: RegularPT
    val owner: String
    val repo: String
    val ref: String

    override fun renderLabel(): String = when (type) {
        RegularPT.CREATE_PR -> "createPR $ref"
        RegularPT.TEST_REF -> {
            val own = if (owner == "ksamples") "" else owner
            "test $own/$ref"
        }
    }

    fun update(upd: RegularProcess.() -> Unit)
}

@Serializable
class RegularProcess private constructor(
        override val type: RegularPT,
        override val owner: String,
        override val repo: String,
        override val ref: String,
        override var status: ProcessStatus,
        override var errorDescription: String?
) : RegularProcessI {

    companion object {
        fun create(
                type: RegularPT,
                owner: String,
                repo: String,
                ref: String,
                status: ProcessStatus = ProcessStatus.OPEN,
                errorDescription: String? = null
        ): RegularProcessI = RegularProcess(type, owner, repo, ref, status, errorDescription)
    }

    override fun update(upd: RegularProcess.() -> Unit) {
        this.upd()
        CardManager.commit()
    }
}

interface SpecialProcessI : ProcessRendering {
    fun update(upd: SpecialProcess.() -> Unit)
}

@Serializable
class SpecialProcess private constructor(
        override val type: SpecialPT,
        override var status: ProcessStatus,
        override var errorDescription: String?
) : SpecialProcessI {
    companion object {
        fun create(
                type: SpecialPT,
                status: ProcessStatus = ProcessStatus.OPEN,
                errorDescription: String? = null
        ): SpecialProcessI = SpecialProcess(type, status, errorDescription)
    }

    override fun update(upd: SpecialProcess.() -> Unit) {
        this.upd()
        CardManager.commit()
    }

    override fun renderLabel(): String = type.name
}
