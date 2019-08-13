package gitmove.services

import gitmove.*
import retrofit2.Response

open class SpecialService(
        private val service: GitHubService,
        override val repository: Repository
) : SpecialServiceI {
    override suspend fun getRef(ref: String): Response<GitReference> = service.getRef(repository.owner, repository.repo, ref)
    override suspend fun getAllRefs(): Response<List<GitReference>> = service.getAllRefs(repository.owner, repository.repo)
    override suspend fun createRef(ref: String, sha: String): Response<GitReference> = service.createRef(repository.owner, repository.repo, ref, sha)

    override suspend fun getTree(treeSha: String): Response<GitTree> = service.getTree(repository.owner, repository.repo, treeSha)
    override suspend fun getTreeRecursive(treeSha: String): Response<GitTree> = service.getTreeRecursive(repository.owner, repository.repo, treeSha)
    override suspend fun createTree(tree: GitTree, baseTree: String?): Response<GitTree.GitTreeNode> = service.createTree(repository.owner, repository.repo, tree, baseTree)

    override suspend fun getCommit(commitSha: String): Response<GitCommit> = service.getCommit(repository.owner, repository.repo, commitSha)
    override suspend fun createCommit(message: String, tree: String, parents: List<String>, author: GitAuthor?, committer: GitAuthor?, signature: String?): Response<GitCommit> = service.createCommit(repository.owner, repository.repo, message, tree, parents, author, committer, signature)

    override suspend fun getBlob(fileSha: String): Response<GitBlob> = service.getBlob(repository.owner, repository.repo, fileSha)
    override suspend fun createBlob(content: String, encoding: GitBlob.GitBlobContentType): Response<GitCommit.GitTreeI> = service.createBlob(repository.owner, repository.repo, content, encoding)
}


interface SpecialServiceI {
    val repository: Repository

    suspend fun getRef(ref: String): Response<GitReference>
    suspend fun getAllRefs(): Response<List<GitReference>>
    suspend fun createRef(ref: String, sha: String): Response<GitReference>

    suspend fun getTree(treeSha: String): Response<GitTree>
    suspend fun getTreeRecursive(treeSha: String): Response<GitTree>
    suspend fun createTree(tree: GitTree, baseTree: String?): Response<GitTree.GitTreeNode>

    suspend fun getCommit(commitSha: String): Response<GitCommit>
    suspend fun createCommit(message: String, tree: String, parents: List<String>, author: GitAuthor?, committer: GitAuthor?, signature: String?): Response<GitCommit>

    suspend fun getBlob(fileSha: String): Response<GitBlob>
    suspend fun createBlob(content: String, encoding: GitBlob.GitBlobContentType = GitBlob.GitBlobContentType.`utf-8`): Response<GitCommit.GitTreeI>
}