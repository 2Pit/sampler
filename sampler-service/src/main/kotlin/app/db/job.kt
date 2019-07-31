package app.db

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Jobs : AbstractService<JobRow, JobFilter>() {
    val processId = integer("process_id").autoIncrement().index()
    val status = varchar("status", 50) // new, in_progress, done, error
    val description = text("description")
    val context = text("context") // context in json format

    fun insert(item: JobRow): Int {
        return transaction {
            Jobs.insertAndGetId {
                if (item.processId > 0) it[processId] = item.processId
                it[status] = item.status.name
                it[description] = item.description
                it[context] = item.context
            }
        }.value
    }

    override fun buildFilterQuery(filter: JobFilter): Query = select {
        (filter.id?.let { id eq it } ?: Op.TRUE) and
                (filter.processId?.let { processId eq it } ?: Op.TRUE) and
                (filter.status?.let { status eq it.name } ?: Op.TRUE) and
                (filter.description?.let { description eq it } ?: Op.TRUE) and
                (filter.context?.let { context eq it } ?: Op.TRUE)
    }

    override fun convert(row: ResultRow): JobRow {
        return JobRow(
            id = row[id].value,
            processId = row[processId],
            status = JobStatus.valueOf(row[status]),
            description = row[description],
            context = row[context]
        )
    }
}

enum class JobStatus {
    NEW, IN_PROGRESS, DONE, ERROR
}

@Serializable
data class JobRow(
    val id: Int,
    val processId: Int,
    val status: JobStatus,
    val description: String,
    val context: String
) : DataRow

data class JobFilter(
    val id: Int? = null,
    val processId: Int? = null,
    val status: JobStatus? = null,
    val description: String? = null,
    val context: String? = null
) : DataFilter