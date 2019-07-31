package app.db

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 *
 *  Location <--+ Sample <--+ Snapshot <--+ File
 *
 *  Job
 */
interface Service<R : DataRow, F : DataFilter> {
    fun getBy(id: Int): R?

    fun getBy(filter: F): List<R>

    fun convert(row: ResultRow): R
}

abstract class AbstractService<R : DataRow, F : DataFilter> : IntIdTable(), Service<R, F> {
    final override fun getBy(id: Int): R? = transaction { buildIdQuery(id).map { convert(it) }.firstOrNull() }

    final override fun getBy(filter: F): List<R> = transaction { buildFilterQuery(filter).map { convert(it) } }

    private fun buildIdQuery(id: Int): Query = select { this@AbstractService.id eq id }

    protected abstract fun buildFilterQuery(filter: F): Query
}

interface DataRow

interface DataFilter
