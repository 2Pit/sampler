package gitmove

import retrofit2.http.*

class SpecialService(
        private val service: GitHubService,
        val repository: Repository
) {
    suspend fun getAllRefs() = service.getAllRefs(repository.owner, repository.repo)
    suspend fun createRef(ref: String, sha: String): Reference = service.createRef(repository.owner, repository.repo, ref, sha)

    suspend fun getTree(treeSha: String): Tree = service.getTree(repository.owner, repository.repo, treeSha)
    suspend fun getTreeRecursive(treeSha: String): Tree = service.getTreeRecursive(repository.owner, repository.repo, treeSha)
    suspend fun createTree(tree: Tree, baseTree: String?): TreeNode = service.createTree(repository.owner, repository.repo, tree, baseTree)

    suspend fun getCommit(commitSha: String): Commit = service.getCommit(repository.owner, repository.repo, commitSha)
    suspend fun createCommit(message: String, tree: String, parents: List<String>, author: Author?, committer: Author?, signature: String?): Commit = service.createCommit(repository.owner, repository.repo, message, tree, parents, author, committer, signature)

    suspend fun getBlob(fileSha: String) = service.getBlob(repository.owner, repository.repo, fileSha)
    suspend fun createBlob(content: String, encoding: BlobContentType = BlobContentType.`utf-8`) = service.createBlob(repository.owner, repository.repo, content, encoding)
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
    ): List<Reference>

    @POST("/repos/{owner}/{repo}/git/refs")
    suspend fun createRef(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Query("ref") ref: String,
            @Query("sha") sha: String
    ): Reference

    @GET("/repos/{owner}/{repo}/git/commits/{commit_sha}")
    suspend fun getCommit(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("commit_sha") commitSha: String
    ): Commit

    @POST("/repos/{owner}/{repo}/git/commits")
    suspend fun createCommit(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Query("message") message: String,
            @Query("tree") tree: String,
            @Query("parents") parents: List<String>,
            @Query("author") author: Author?,
            @Query("committer") committer: Author?,
            @Query("signature") signature: String?
    ): Commit

    @GET("/repos/{owner}/{repo}/git/trees/{tree_sha}")
    suspend fun getTree(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("tree_sha") treeSha: String
    ): Tree

    @GET("/repos/{owner}/{repo}/git/trees/{tree_sha}?recursive=1")
    suspend fun getTreeRecursive(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("tree_sha") treeSha: String
    ): Tree

    @POST("/repos/{owner}/{repo}/git/trees")
    suspend fun createTree(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Query("tree") tree: Tree,
            @Query("base_tree") baseTree: String?
    ): TreeNode

    @GET("/repos/{owner}/{repo}/git/blobs/{file_sha}")
    suspend fun getBlob(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("file_sha") fileSha: String
    ): Blob

    @POST("/repos/{owner}/{repo}/git/blobs")
    suspend fun createBlob(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Query("content") content: String,
            @Query("encoding") encoding: BlobContentType = BlobContentType.`utf-8`
    )
}