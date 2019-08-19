package app.model

import com.test.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import java.io.File


/*Do not create this class directly. Use CardManager.*/
@Serializable
data class Card internal constructor(
        val id: Long,
        val issueNumber: Int,
        val fullName: String,
        val processIds: List<Long>,
        val status: CardStatus = CardStatus.checkIn,
        val prIds: List<Long> = emptyList()
) {
    @Transient
    val processes: List<Process>
        get() = ProcessManager.get(processIds)

    @Transient
    val repo = fullName.substringAfter("/")

    fun update(
            processes: List<Process> = this.processes,
            status: CardStatus = this.status
    ): Card {
        return CardManager.create(id, issueNumber, fullName, processes, status)
    }
}

enum class CardStatus { checkIn, added, updated, stopped }

object CardManager {
    private val cards: MutableMap<String, Card> = mutableMapOf()
    private val file = File(Settings.storageDir, "cards.json")
    private val json = Json(JsonConfiguration.Stable)

    init {
        if (file.exists())
            json.parse(Card.serializer().list, file.readText()).associateByTo(cards) { it.fullName }
    }

    fun commit() {
        file.writeText(json.stringify(Card.serializer().list, cards.values.toList()))
    }

    fun create(
            id: Long,
            issueNumber: Int,
            fullName: String,
            processes: List<Process>,
            status: CardStatus = CardStatus.checkIn
    ): Card {
        val res = Card(id, issueNumber, fullName, processes.map { it.id }, status)
        cards[fullName] = res
        commit()
        return res
    }

    fun getAll(): List<Card> = cards.values.toList()

    fun get(fullName: String): Card? = cards[fullName]

    fun getBy(status: CardStatus): List<Card> {
        return cards.values.filter { it.status == status }
    }
}
