package gitmove.services

import com.test.Settings
import gitmove.GitReference
import retrofit2.Response
import java.io.File

class TestCachingSpecialService(service: SpecialService) : CachingSpecialService(service) {
    private val storage = File(Settings.storageDir, "${service.repository.owner}/${service.repository.repo}")

    private val refCache = Cache(File(storage, "refCache.json"), GitReference.serializer())

    override fun save() {
        refCache.save()
        super.save()
    }

    override suspend fun getRef(ref: String): Response<GitReference> =
            Response.success(refCache.map.getOrPut(ref) { service.getRef(ref).body()!! })

    override suspend fun getAllRefs(): Response<List<GitReference>> {
        val values = refCache.map.values.toList()
        val res = if (values.isNotEmpty()) values else service.getAllRefs().body()!!
        res.associateByTo(refCache.map) { it.ref }
        return Response.success(res)
    }
}