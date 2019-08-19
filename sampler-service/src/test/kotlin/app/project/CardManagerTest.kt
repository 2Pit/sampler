package app.project

import app.model.CardManager
import app.model.ProcessManager
import org.junit.Test
import org.junit.jupiter.api.Assertions.*

internal class CardManagerTest {

    @Test
    fun asd123() {
        assert(CardManager.getAll().isEmpty())
        assert(ProcessManager.getAll().isEmpty())

        val p1 = ProcessManager.create("fork")
        val p2 = ProcessManager.create("test")

        val card = CardManager.create(1, 1, "owner/repo", listOf(p1, p2))
        card.copy(id = 4)
        CardManager.commit()
    }

    @Test
    fun asd456() {
        val all = CardManager.getAll()
        assert(all.size == 1)

        println(all.first())
    }

}