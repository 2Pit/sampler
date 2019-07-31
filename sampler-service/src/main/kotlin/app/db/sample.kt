package app.db

import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow

object Samples : AbstractService<SampleRow, SampleFilter>() {
    val repositoryId = reference("repository_id", Repositories)
    val path = varchar("path", 100)
    val name = varchar("name", 50)
    val validSnapshotId = reference("valid_snapshot_id", Snapshots).nullable()

    override fun buildFilterQuery(filter: SampleFilter): Query {
        TODO("not implemented")
    }

    override fun convert(row: ResultRow): SampleRow {
        return SampleRow(
            id = row[id].value,
            repositoryId = row[repositoryId].value,
            validSnapshotId = row[validSnapshotId]?.value,
            name = row[name],
            path = row[path]
        )
    }
}

data class SampleRow(
    val id: Int,
    val repositoryId: Int,
    val validSnapshotId: Int?,
    val name: String,
    val path: String
) : DataRow

data class SampleFilter(
    val id: Int? = null,
    val repositoryId: Int? = null,
    val validSnapshotId: Int? = null,
    val name: String? = null,
    val path: String? = null
) : DataFilter
