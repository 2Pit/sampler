package gitmove

import com.test.Settings
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.map
import retrofit2.http.*
import java.io.File

class CachingSpecialService(service: GitHubService, repository: Repository) : SpecialService(service, repository) {
    private val treeCache: MutableMap<String, GitTree>
    private val commitCache: MutableMap<String, GitCommit>
    private val blobCache: MutableMap<String, GitBlob>

    private val json = Json(JsonConfiguration.Stable)

    private val storage = File(Settings.storageDir, "${repository.owner}/${repository.repo}")
    private val treeCacheFile = File(storage, "treeCache.json")
    private val commitCacheFile = File(storage, "commitCache.json")
    private val blobCacheFile = File(storage, "blobCache.json")


    init {
        treeCache = if (treeCacheFile.exists())
            json.parse((StringSerializer to GitTree.serializer()).map, treeCacheFile.readText()).toMutableMap()
        else mutableMapOf()

        commitCache = if (commitCacheFile.exists())
            json.parse((StringSerializer to GitCommit.serializer()).map, commitCacheFile.readText()).toMutableMap()
        else mutableMapOf()

        blobCache = if (blobCacheFile.exists())
            json.parse((StringSerializer to GitBlob.serializer()).map, blobCacheFile.readText()).toMutableMap()
        else mutableMapOf()
    }

    fun save() {
        if (!storage.exists()) storage.mkdirs()
        if (!treeCacheFile.exists()) treeCacheFile.createNewFile()
        if (!commitCacheFile.exists()) commitCacheFile.createNewFile()
        if (!blobCacheFile.exists()) blobCacheFile.createNewFile()

        treeCacheFile.writeText(
                json.stringify((StringSerializer to GitTree.serializer()).map, treeCache)
        )
        commitCacheFile.writeText(
                json.stringify((StringSerializer to GitCommit.serializer()).map, commitCache)
        )
        blobCacheFile.writeText(
                json.stringify((StringSerializer to GitBlob.serializer()).map, blobCache)
        )
    }

    override suspend fun getTree(treeSha: String): GitTree = treeCache.getOrPut(treeSha) { super.getTree(treeSha) }
    override suspend fun getCommit(commitSha: String): GitCommit = commitCache.getOrPut(commitSha) { super.getCommit(commitSha) }
    override suspend fun getBlob(fileSha: String): GitBlob = blobCache.getOrPut(fileSha) { super.getBlob(fileSha) }
}

open class SpecialService(
        protected val service: GitHubService,
        val repository: Repository
) {
    suspend fun getAllRefs() = service.getAllRefs(repository.owner, repository.repo)
    suspend fun createRef(ref: String, sha: String): GitReference = service.createRef(repository.owner, repository.repo, ref, sha)

    open suspend fun getTree(treeSha: String): GitTree = service.getTree(repository.owner, repository.repo, treeSha)
    suspend fun getTreeRecursive(treeSha: String): GitTree = service.getTreeRecursive(repository.owner, repository.repo, treeSha)
    suspend fun createTree(tree: GitTree, baseTree: String?): GitTree.GitTreeNode = service.createTree(repository.owner, repository.repo, tree, baseTree)

    open suspend fun getCommit(commitSha: String): GitCommit = service.getCommit(repository.owner, repository.repo, commitSha)
    suspend fun createCommit(message: String, tree: String, parents: List<String>, author: GitAuthor?, committer: GitAuthor?, signature: String?): GitCommit = service.createCommit(repository.owner, repository.repo, message, tree, parents, author, committer, signature)

    open suspend fun getBlob(fileSha: String) = service.getBlob(repository.owner, repository.repo, fileSha)
    suspend fun createBlob(content: String, encoding: GitBlob.GitBlobContentType = GitBlob.GitBlobContentType.`utf-8`) = service.createBlob(repository.owner, repository.repo, content, encoding)
}

class Repository(
        val owner: String,
        val repo: String
)


interface GitHubService {
    @GET("/repos/{owner}/{repo}/git/refs")
    suspend fun getAllRefs(
            @Path("owner") owner: String,
            @Path("repo") repo: String
    ): List<GitReference>

    @POST("/repos/{owner}/{repo}/git/refs")
    suspend fun createRef(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Query("ref") ref: String,
            @Query("sha") sha: String
    ): GitReference

    @GET("/repos/{owner}/{repo}/git/commits/{commit_sha}")
    suspend fun getCommit(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("commit_sha") commitSha: String
    ): GitCommit

    @POST("/repos/{owner}/{repo}/git/commits")
    suspend fun createCommit(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Query("message") message: String,
            @Query("tree") tree: String,
            @Query("parents") parents: List<String>,
            @Query("author") author: GitAuthor?,
            @Query("committer") committer: GitAuthor?,
            @Query("signature") signature: String?
    ): GitCommit

    @GET("/repos/{owner}/{repo}/git/trees/{tree_sha}")
    suspend fun getTree(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("tree_sha") treeSha: String
    ): GitTree

    @GET("/repos/{owner}/{repo}/git/trees/{tree_sha}?recursive=1")
    suspend fun getTreeRecursive(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("tree_sha") treeSha: String
    ): GitTree

    @POST("/repos/{owner}/{repo}/git/trees")
    suspend fun createTree(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Query("tree") tree: GitTree,
            @Query("base_tree") baseTree: String?
    ): GitTree.GitTreeNode

    @GET("/repos/{owner}/{repo}/git/blobs/{file_sha}")
    suspend fun getBlob(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("file_sha") fileSha: String
    ): GitBlob

    @POST("/repos/{owner}/{repo}/git/blobs")
    suspend fun createBlob(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Query("content") content: String,
            @Query("encoding") encoding: GitBlob.GitBlobContentType = GitBlob.GitBlobContentType.`utf-8`
    )
}