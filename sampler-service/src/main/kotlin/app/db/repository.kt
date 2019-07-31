package app.db

import org.eclipse.egit.github.core.RepositoryId
import org.jetbrains.exposed.sql.*

object Repositories : AbstractService<RepositoryRow, RepositoryFilter>() {
    val owner = varchar("owner", 50)
    val repo = varchar("name", 50)
    val branch = varchar("branch", 50)

    override fun buildFilterQuery(filter: RepositoryFilter): Query = select {
        (filter.id?.let { id eq it } ?: Op.TRUE) and
                (filter.owner?.let { owner eq it } ?: Op.TRUE) and
                (filter.repo?.let { repo eq it } ?: Op.TRUE) and
                (filter.branch?.let { branch eq it } ?: Op.TRUE)
    }

    override fun convert(row: ResultRow): RepositoryRow {
        return RepositoryRow(
            id = row[id].value,
            owner = row[owner],
            repo = row[repo],
            branch = row[branch]
        )
    }
}

data class Repository(
    val owner: String,
    val name: String,
    val branch: String
) {
    fun toRepositoryId(): RepositoryId = RepositoryId.create(owner, name)
}

class RepositoryRow(
    override val id: Int,
    override val owner: String,
    override val repo: String,
    override val branch: String
) : DataRow, RepositoryFilter(id, owner, repo, branch)

open class RepositoryFilter(
    open val id: Int? = null,
    open val owner: String? = null,
    open val repo: String? = null,
    open val branch: String? = null
) : DataFilter
