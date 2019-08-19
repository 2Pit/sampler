package app.services

import app.api.GitBlob
import app.api.GitCommit
import app.api.GitReference
import app.api.GitTree
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import retrofit2.Response
import java.util.concurrent.atomic.AtomicInteger


class LoggerService(private val service: SpecialServiceI) : SpecialServiceI by service {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    private val counter = AtomicInteger(0)

    override suspend fun getRef(ref: String): Response<GitReference> {
        val id = counter.incrementAndGet()
        log.debug("getRef$id params={owner: ${repository.owner}, repo: ${repository.repo}, ref: $ref}")
        return service.getRef(ref).logResponse(id)
    }

    override suspend fun getAllRefs(): Response<List<GitReference>> {
        val id = counter.incrementAndGet()
        log.debug("getAllRefs$id params={owner: ${repository.owner}, repo: ${repository.repo}}")
        return service.getAllRefs().logResponse(id)
    }

    override suspend fun getCommit(commitSha: String): Response<GitCommit> {
        val id = counter.incrementAndGet()
        log.debug("getCommit$id params={owner: ${repository.owner}, repo: ${repository.repo}, commitSha: $commitSha}")
        return service.getCommit(commitSha).logResponse(id)
    }

    override suspend fun getTree(treeSha: String): Response<GitTree> {
        val id = counter.incrementAndGet()
        log.debug("getTree$id params={owner: ${repository.owner}, repo: ${repository.repo}, ref: $treeSha}")
        return service.getTree(treeSha).logResponse(id)
    }

    override suspend fun getBlob(fileSha: String): Response<GitBlob> {
        val id = counter.incrementAndGet()
        log.debug("getBlob$id params={owner: ${repository.owner}, repo: ${repository.repo}, ref: $fileSha}")
        return service.getBlob(fileSha).logResponse(id)
    }

    private fun <T> Response<T>.logResponse(id: Int): Response<T> {
        if (!this.isSuccessful) {
            log.error("Failed loading $id")
        } else {
            log.info("Success loading $id")
        }
        return this
    }
}
