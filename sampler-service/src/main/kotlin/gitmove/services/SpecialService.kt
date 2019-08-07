package gitmove.services

import gitmove.*

open class SpecialService(
        private val service: GitHubService,
        override val repository: Repository
) : SpecialServiceI {
    override suspend fun getRef(ref: String): GitReference = service.getRef(repository.owner, repository.repo, ref)
    override suspend fun getAllRefs(): List<GitReference> = service.getAllRefs(repository.owner, repository.repo)
    override suspend fun createRef(ref: String, sha: String): GitReference = service.createRef(repository.owner, repository.repo, ref, sha)

    override suspend fun getTree(treeSha: String): GitTree = service.getTree(repository.owner, repository.repo, treeSha)
    override suspend fun getTreeRecursive(treeSha: String): GitTree = service.getTreeRecursive(repository.owner, repository.repo, treeSha)
    override suspend fun createTree(tree: GitTree, baseTree: String?): GitTree.GitTreeNode = service.createTree(repository.owner, repository.repo, tree, baseTree)

    override suspend fun getCommit(commitSha: String): GitCommit = service.getCommit(repository.owner, repository.repo, commitSha)
    override suspend fun createCommit(message: String, tree: String, parents: List<String>, author: GitAuthor?, committer: GitAuthor?, signature: String?): GitCommit = service.createCommit(repository.owner, repository.repo, message, tree, parents, author, committer, signature)

    override suspend fun getBlob(fileSha: String): GitBlob = service.getBlob(repository.owner, repository.repo, fileSha)
    override suspend fun createBlob(content: String, encoding: GitBlob.GitBlobContentType) = service.createBlob(repository.owner, repository.repo, content, encoding)
}


interface SpecialServiceI {
    val repository: Repository

    suspend fun getRef(ref: String): GitReference
    suspend fun getAllRefs(): List<GitReference>
    suspend fun createRef(ref: String, sha: String): GitReference

    suspend fun getTree(treeSha: String): GitTree
    suspend fun getTreeRecursive(treeSha: String): GitTree
    suspend fun createTree(tree: GitTree, baseTree: String?): GitTree.GitTreeNode

    suspend fun getCommit(commitSha: String): GitCommit
    suspend fun createCommit(message: String, tree: String, parents: List<String>, author: GitAuthor?, committer: GitAuthor?, signature: String?): GitCommit

    suspend fun getBlob(fileSha: String): GitBlob
    suspend fun createBlob(content: String, encoding: GitBlob.GitBlobContentType = GitBlob.GitBlobContentType.`utf-8`)
}