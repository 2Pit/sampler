package gitmove

import gitmove.GitTree.GitTreeNode.GitTreeType.blob
import gitmove.GitTree.GitTreeNode.GitTreeType.tree
import gitmove.services.*
import gitmove.tree.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


fun main() {
    val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
        val request = chain.request().newBuilder()
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("Authorization", "token 147d2090ec01f251b04912916f14d4ce3646b5b7")
                .build()
        chain.proceed(request)
    }.build()

    val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()

    val outService = TestCachingSpecialService(SpecialService(
            retrofit.create(GitHubService::class.java),
            Repository("2Pit", "test"))
    )

    val ref = runBlocking {
        buildRef(outService.getAllRefs().body()!!.first(), outService)
    }

    val root = ref.commit.node as Tree
    val a = extractPath(root, "a", "b/c")

    outService.save()
}

suspend fun buildRef(gitRef: GitReference, service: SpecialServiceI): Ref {
    assert(gitRef.`object`.type == GitReference.GitRefObject.GitRefType.commit)

    return Ref(gitRef.ref, buildCommit(gitRef.`object`.sha, service))
}

suspend fun buildCommit(commitSha: String, service: SpecialServiceI): Commit = coroutineScope {
    val gitCmt = service.getCommit(commitSha).body()!!
    Commit(
            gitCmt.message,
            buildNodeFromCommit(gitCmt, service),
            gitCmt.parents.map { async { buildCommit(it.sha, service) } }.awaitAll()
    )
}

suspend fun buildNodeFromCommit(gitCommit: GitCommit, service: SpecialServiceI): Node = coroutineScope {
    val gitTree = service.getTree(gitCommit.tree.sha).body()!!
    val children = gitTree.tree.map { async { it.path to buildNode(it, service) } }.awaitAll().toMap()
    Tree("ROOT", children)
}

suspend fun buildNode(gitTreeNode: GitTree.GitTreeNode, service: SpecialServiceI): Node = coroutineScope {
    when (gitTreeNode.type) {
        blob -> Blob(gitTreeNode.path)//, gitTreeNode.sha)
        tree -> {
            val gitTree = service.getTree(gitTreeNode.sha).body()!!
            val children = gitTree.tree.map { async { it.path to buildNode(it, service) } }.awaitAll().toMap()
            Tree(gitTreeNode.path, children)
        }
    }
}
