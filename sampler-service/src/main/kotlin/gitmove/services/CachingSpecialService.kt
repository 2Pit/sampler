package gitmove.services

import com.test.Settings
import gitmove.GitBlob
import gitmove.GitCommit
import gitmove.GitTree
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.map
import java.io.File

open class CachingSpecialService(protected val service: SpecialService) : SpecialServiceI by service {
    private val storage = File(Settings.storageDir, "${service.repository.owner}/${service.repository.repo}")

    private val treeCache = Cache(File(storage, "treeCache.json"), GitTree.serializer())
    private val commitCache = Cache(File(storage, "commitCache.json"), GitCommit.serializer())
    private val blobCache = Cache(File(storage, "blobCache.json"), GitBlob.serializer())

    init {
        if (!storage.exists()) storage.mkdirs()
    }

    protected class Cache<T>(val file: File, private val serializer: KSerializer<T>) {
        private val json = Json(JsonConfiguration.Stable)

        val map: MutableMap<String, T> =
                if (file.exists())
                    json.parse((StringSerializer to serializer).map, file.readText()).toMutableMap()
                else mutableMapOf()

        fun save() {
            if (!file.exists()) file.createNewFile()
            file.writeText(
                    json.stringify((StringSerializer to serializer).map, map)
            )
        }
    }

    open fun save() {
        treeCache.save()
        commitCache.save()
        blobCache.save()
    }

    override suspend fun getTree(treeSha: String): GitTree = treeCache.map.getOrPut(treeSha) { service.getTree(treeSha) }
    override suspend fun getCommit(commitSha: String): GitCommit = commitCache.map.getOrPut(commitSha) { service.getCommit(commitSha) }
    override suspend fun getBlob(fileSha: String): GitBlob = blobCache.map.getOrPut(fileSha) { service.getBlob(fileSha) }
}