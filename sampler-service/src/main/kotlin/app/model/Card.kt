package app.model

import app.Properties
import app.services.Services
import kotlinx.serialization.Serializable


interface CardI {
    val id: Long
    val issueNumber: Int
    val owner: String
    val repo: String
    val status: CardStatus
    val regularProcesses: List<RegularProcessI>
    val specialProcesses: List<SpecialProcessI>
    val forkedRefs: List<String>
    val prBranches: List<String>

    val fullName: String

    //    TODO sync on object
    fun update(upd: Card.() -> Unit)

    fun updateUI() {
        val issue = Services.issueService.getIssue(Properties.mainRepo, this.issueNumber)
        // TODO set lables
        issue.body = this.regularProcesses.joinToString("\n") { it.render() }
        Services.issueService.editIssue(Properties.mainRepo, issue)
    }
}


@Serializable
class Card private constructor(
        override val id: Long,
        override val issueNumber: Int,
        override val owner: String,
        override val repo: String,
        override var status: CardStatus = CardStatus.CHECK_IN,
        override var regularProcesses: List<RegularProcessI>,
        override var specialProcesses: List<SpecialProcessI>,
        override var forkedRefs: List<String>,
        override var prBranches: List<String>
) : CardI {
    companion object {
        fun create(
                id: Long,
                issueNumber: Int,
                owner: String,
                repo: String,
                status: CardStatus = CardStatus.CHECK_IN,
                regularProcesses: List<RegularProcessI> = emptyList(),
                specialProcesses: List<SpecialProcessI> = emptyList(),
                forkedRefs: List<String> = emptyList(),
                prBranches: List<String> = emptyList()
        ): CardI {
            val card = Card(id, issueNumber, owner, repo, status, regularProcesses, specialProcesses, forkedRefs, prBranches)
            CardManager.addCard(card)
            return card
        }
    }

    override val fullName: String = "$owner/$repo"

    override fun update(upd: Card.() -> Unit) {
        this.upd()
        CardManager.commit()
    }
}

enum class CardStatus { CHECK_IN, ADDING, UPDATING, STOPPING }
