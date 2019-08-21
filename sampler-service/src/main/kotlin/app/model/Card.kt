package app.model

import app.project.Consts
import app.services.Services
import arrow.core.extensions.StringMonoid
import arrow.data.extensions.list.foldable.fold
import kotlinx.serialization.Serializable


@Serializable
data class Card internal constructor(
        val id: Long,
        val issueNumber: Int,
        val fullName: String,
        private var _status: CardStatus = CardStatus.checkIn,
        private var _regularProcesses: List<RegularProcess> = emptyList(),
        private var _specialProcesses: List<SpecialProcess> = emptyList(),
        private var _forkedRefs: List<String> = emptyList(),
        private var _prBranches: List<String> = emptyList()
) {
    init {
        CardManager.addCard(this)
    }

    val repo = fullName.substringAfter("/")
    val status: CardStatus = _status
    val regularProcesses = _regularProcesses
    val specialProcesses = _specialProcesses
    val forkedRefs: List<String> = _forkedRefs
    val prBranches: List<String> = _prBranches

    //    TODO sync on object
    fun update(
            status: CardStatus = this._status,
            regularProcesses: List<RegularProcess> = this._regularProcesses,
            specialProcesses: List<SpecialProcess> = this._specialProcesses,
            forkedRefs: List<String> = this._forkedRefs,
            prBranches: List<String> = this._prBranches
    ) {
        _status = status
        _regularProcesses = regularProcesses
        _specialProcesses = specialProcesses
        _forkedRefs = forkedRefs
        _prBranches = prBranches
        commit()
    }

    private fun commit() = CardManager.commit()

    fun updateUI() {
        val issue = Services.issueService.getIssue(Consts.mainRepo, this.issueNumber)
        // TODO set lables
        issue.body = this.regularProcesses.map { it.render() }.fold(object : StringMonoid {
            override fun String.combine(b: String): String = "$this\n$b"
        })
        Services.issueService.editIssue(Consts.mainRepo, issue)
    }
}

enum class CardStatus { checkIn, added, updated, stopped }
