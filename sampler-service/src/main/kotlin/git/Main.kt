package git

import arrow.data.extensions.list.monad.map
import app.Properties
import org.eclipse.egit.github.core.*
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.client.RequestException
import org.eclipse.egit.github.core.service.CommitService
import org.eclipse.egit.github.core.service.ContentsService
import org.eclipse.egit.github.core.service.DataService
import org.eclipse.egit.github.core.service.RepositoryService


fun main() {
    val client = GitHubClient()
    client.setOAuth2Token(Properties.githubToken)

    val repositoryService = RepositoryService(client)

//    val toRepoId = RepositoryId.create("2Pit", "kotlin-native-samples")
//    val fromRepoId = RepositoryId.create("JetBrains", "kotlin-native")
    val fromRepoId = RepositoryId.create("2Pit", "test")
    val toRepoId = RepositoryId.create("2Pit", "test-2")

    try {
        repositoryService.getRepository(toRepoId)
    } catch (e: RequestException) {
        repositoryService.createRepository(Repository().apply { name = toRepoId.name })
    }

    val commitService = CommitService(client)
    val dataService = DataService(client)
    val contentsService = ContentsService(client)

//    val commits = commitService.getCommits(fromRepoId, null, "samples").map { it.commit }.sortedBy { it.author.date }


    val commits = commitService.getCommits(fromRepoId).map { it.commit }.sortedBy { it.author.date }
    val treeShaSet = commits.mapTo(mutableSetOf()) { it.tree.sha }
    val groupedNodes = treeShaSet.flatMap { dataService.getTree(fromRepoId, it, true).tree }.groupBy { it.type }
    val shaToBlobsFrom = groupedNodes[TreeEntry.TYPE_BLOB]
        ?.associateBy({ it.sha }, { dataService.getBlob(fromRepoId, it.sha) })
        ?: mapOf()

    val oldToNewSha = shaToBlobsFrom.mapValues { (_, blob) -> dataService.createBlob(toRepoId, blob) }

    fun pushTreeNode(sha: String) {
        val tree = dataService.getTree(fromRepoId, sha)
        tree.tree.filter { it.type == "tree" }.forEach { pushTreeNode(it.sha) }
        dataService.createTree(toRepoId, tree.tree)
    }

    var cmt: Commit?
    commits.first().also { commit ->
        val sha = commit.url.substringAfterLast("/")
        pushTreeNode(sha)
        commit.parents = commitService.getCommits(toRepoId).map { it.commit }
        commit.parents.forEach {
            it.sha = it.url.substringAfterLast("/")
        }
        cmt = dataService.createCommit(toRepoId, commit)
    }
    commits.drop(1).forEach { commit ->
        val sha = commit.url.substringAfterLast("/")
        pushTreeNode(sha)
        cmt = dataService.createCommit(toRepoId, commit)
    }

    dataService.getReferences(fromRepoId).forEach {
        dataService.editReference(toRepoId, it.apply { this.`object`.sha = cmt?.url?.substringAfterLast("/") })
    }
}

