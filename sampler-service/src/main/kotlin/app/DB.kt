package app

import app.db.*
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object DB {
    val connect = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

    fun init() {
        transaction {
            // print sql to std-out
            addLogger(StdOutSqlLogger)


            SchemaUtils.create(
                Repositories,
                Samples,
                Snapshots,
                Jobs
            )

            Repositories.insert {
                it[owner] = "owner1"
                it[repo] = "repo1"
                it[branch] = "master"
            }
            Repositories.insert {
                it[owner] = "owner2"
                it[repo] = "repo2"
                it[branch] = "master"
            }
            Repositories.insert {
                it[owner] = "owner3"
                it[repo] = "repo3"
                it[branch] = "master"
            }

            Samples.insert {
                it[repositoryId] = EntityID(1, Repositories)
                it[path] = "path1"
                it[name] = "name1"
            }

            Snapshots.insert {
                it[sampleId] = EntityID(1, Samples)
                it[status] = "OK"
                it[sha] = "sha1"
                it[buildSystem] = "GRADLE"
                it[readme] = "readme1"
            }
            Snapshots.insert {
                it[sampleId] = EntityID(1, Samples)
                it[status] = "OK"
                it[sha] = "sha2"
                it[buildSystem] = "GRADLE"
                it[readme] = "readme2"
            }
            Snapshots.insert {
                it[sampleId] = EntityID(1, Samples)
                it[status] = "OK"
                it[sha] = "sha3"
                it[buildSystem] = "GRADLE"
                it[readme] = "readme3"
            }

            Samples.update({ Samples.id eq 1 }) {
                it[validSnapshotId] = EntityID(3, Snapshots)
            }
        }
    }
}