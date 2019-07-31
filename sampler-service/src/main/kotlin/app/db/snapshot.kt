package app.db

import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow


object Snapshots : AbstractService<SnapshotRow, SnapshotFilter>() {
    val sampleId = reference("sample_id", Samples)
    val sha = varchar("sha", 50)
    val status = varchar("status", 10)
    val buildSystem = varchar("buildSystem", 10)
    val readme = text("readme")
//    val downloadLink = varchar("download_link", 100)
//    val checksum = varchar("checksum", 100)

    override fun buildFilterQuery(filter: SnapshotFilter): Query {
        TODO("not implemented")
    }

    override fun convert(row: ResultRow): SnapshotRow {
        return SnapshotRow(
            id = row[id].value,
            sampleId = row[sampleId].value,
            sha = row[sha],
            status = SnapshotStatus.valueOf(row[status]),
            buildSystem = BuildSystem.valueOf(row[buildSystem]),
            readme = row[readme]
//            downloadLink = row[downloadLink],
//            checksum = row[checksum]
        )
    }
}

enum class BuildSystem {
    MAVEN, GRADLE
}

enum class SnapshotStatus {
    NONE, PLANNED, IN_PROGRESS, OK, ERROR
}

data class SnapshotRow(
    val id: Int,
    val sampleId: Int,
    val sha: String,
    val status: SnapshotStatus,
    val buildSystem: BuildSystem,
    val readme: String
//    val downloadLink: String,
//    val checksum: String
) : DataRow

data class SnapshotFilter(
    val id: Int? = null,
    val sampleId: Int? = null,
    val sha: String? = null,
    val status: SnapshotStatus? = null,
    val buildSystem: BuildSystem? = null
//    val downloadLink: String,
//    val checksum: String
) : DataFilter
