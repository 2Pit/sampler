package gitmove.services

import com.test.Settings
import gitmove.GitReference
import java.io.File

class TestCachingSpecialService(service: SpecialService) : CachingSpecialService(service) {
    private val storage = File(Settings.storageDir, "${service.repository.owner}/${service.repository.repo}")

    private val refCache = Cache(File(storage, "refCache.json"), GitReference.serializer())

    override fun save() {
        refCache.save()
        super.save()
    }

    override suspend fun getRef(ref: String): GitReference = refCache.map.getOrPut(ref) { service.getRef(ref) }

    override suspend fun getAllRefs(): List<GitReference> {
        val values = refCache.map.values.toList()
        val res = if (values.isNotEmpty()) values else service.getAllRefs()
        res.associateByTo(refCache.map) { it.ref }
        return res
    }
}