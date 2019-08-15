package app.states

class Card(
        val id: Long,
        val issueNumber: Int,
        val owner: String,
        val repo: String,
        val processes: MutableList<Process>,
        var status: CardStatus = CardStatus.checkIn
//        val lables: MutableList<GLables>
) {
    val ownerRepo = { "$owner/$repo" }
}

class Process(
        val name: String,
        var status: ProcessStatus = ProcessStatus.open,
        var errorDescription: String = ""
)

enum class ProcessStatus { open, inProgress, finished, error }
enum class CardStatus { checkIn, adding, updating, stopped }