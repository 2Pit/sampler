package app.model

import com.test.Settings
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonException
import kotlinx.serialization.list
import kotlinx.serialization.modules.SerializersModule
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

object CardManager {
    private val log = LoggerFactory.getLogger(CardManager::class.java)
    private val cards: MutableMap<String, Card> = mutableMapOf()
    private val file = File(Settings.storageDir, "cards.json")
    private val json: Json

    init {
        val processModule = SerializersModule {
            polymorphic(RegularProcessI::class) {
                RegularProcess::class with RegularProcess.serializer()
            }
            polymorphic(SpecialProcessI::class) {
                SpecialProcess::class with SpecialProcess.serializer()
            }
        }
        json = Json(JsonConfiguration.Stable, context = processModule)


        try {
            if (file.exists())
                json.parse(Card.serializer().list, file.readText()).associateByTo(cards) { it.fullName }
        } catch (e: IOException) {
            log.error(e.message)
        } catch (e: JsonException) {
            log.error(e.message)
        } catch (e: SerializationException) {
            log.error(e.message)
        }
    }

    fun commit() {
        try {
            file.writeText(json.stringify(Card.serializer().list, cards.values.toList()))
        } catch (e: IOException) {
            log.error(e.message)
        } catch (e: JsonException) {
            log.error(e.message)
        } catch (e: SerializationException) {
            log.error(e.message)
        }
    }

    fun addCard(card: Card) {
        cards[card.fullName] = card
        commit()
    }

    fun getAll(): List<Card> = cards.values.toList()

    fun get(fullName: String): Card? = cards[fullName]

    fun getBy(status: CardStatus): List<Card> {
        return cards.values.filter { it.status == status }
    }
}