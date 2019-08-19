package app.services

import app.api.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


class Repository(
        val owner: String,
        val repo: String
)


interface GitHubService {
    @GET("/repos/{owner}/{repo}/git/refs/{ref}")
    suspend fun getRef(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("ref") ref: String
    ): Response<GitReference>

    @GET("/repos/{owner}/{repo}/git/refs")
    suspend fun getAllRefs(
            @Path("owner") owner: String,
            @Path("repo") repo: String
    ): Response<List<GitReference>>

    @POST("/repos/{owner}/{repo}/git/refs")
    suspend fun createRef(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Query("ref") ref: String,
            @Query("sha") sha: String
    ): Response<GitReference>

    @GET("/repos/{owner}/{repo}/git/commits/{commit_sha}")
    suspend fun getCommit(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("commit_sha") commitSha: String
    ): Response<GitCommit>

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
    ): Response<GitCommit>

    @GET("/repos/{owner}/{repo}/git/trees/{tree_sha}")
    suspend fun getTree(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("tree_sha") treeSha: String
    ): Response<GitTree>

    @GET("/repos/{owner}/{repo}/git/trees/{tree_sha}?recursive=1")
    suspend fun getTreeRecursive(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("tree_sha") treeSha: String
    ): Response<GitTree>

    @POST("/repos/{owner}/{repo}/git/trees")
    suspend fun createTree(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Query("tree") tree: GitTree,
            @Query("base_tree") baseTree: String?
    ): Response<GitTree.GitTreeNode>

    @GET("/repos/{owner}/{repo}/git/blobs/{file_sha}")
    suspend fun getBlob(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("file_sha") fileSha: String
    ): Response<GitBlob>

    @POST("/repos/{owner}/{repo}/git/blobs")
    suspend fun createBlob(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Query("content") content: String,
            @Query("encoding") encoding: GitBlob.GitBlobContentType = GitBlob.GitBlobContentType.`utf-8`
    ): Response<GitCommit.GitTreeI>
}