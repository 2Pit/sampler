package app.git

import arrow.core.Try
import arrow.core.extensions.`try`.monadThrow.bindingCatch
import app.Properties
import git.base64toUtf8
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.RepositoryContents
import org.eclipse.egit.github.core.RepositoryId
import org.eclipse.egit.github.core.TreeEntry
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.CommitService
import org.eclipse.egit.github.core.service.ContentsService
import org.eclipse.egit.github.core.service.DataService
import org.eclipse.egit.github.core.service.RepositoryService
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import app.db.Repository as Repo


interface GitInteraction {
    fun getLatestCommitSha(repo: app.storage.Repository, path: String): Try<String>
    fun getDirSha(repo: app.storage.Repository, commitSha: String, path: String = ""): Try<String>
    fun downloadSample(repo: app.storage.Repository, dirSha: String): Try<List<SampleFile>>
}

object Connector : GitInteraction {
    private val client = GitHubClient().apply { setOAuth2Token(Properties.githubToken) }
    val repositoryService = RepositoryService(client)
    val commitService = CommitService(client)
    val contentService = ContentsService(client)
    val dataService = DataService(client)

    fun getRepository(owner: String, name: String): Try<Repository> {
        return Try { repositoryService.getRepository(owner, name) }
    }

    fun getContent(repository: Repository, path: String): Try<MutableList<RepositoryContents>> {
        return Try { contentService.getContents(repository, path) }
    }

    override fun getDirSha(repo: app.storage.Repository, commitSha: String, path: String): Try<String> =
        bindingCatch {
            if (path.isEmpty()) {
                commitSha
            } else {
                val (parent, child) = File(path).run { parent to name }
                val repository = repositoryService.getRepository(repo.owner, repo.repo)
                contentService.getContents(repository, parent, commitSha)
                    .first { it.name == child }
                    .sha
            }

//            contentService.getContents(repo.toRepositoryId(), branch, path)
        }


    fun getDirSha2(repo: app.storage.Repository, branch: String, path: String): Try<String> =
        bindingCatch {

            contentService.getContents(repo.toRepositoryId(), branch, path).toString()
        }


    // TODO fix it (have to use branch)
    override fun getLatestCommitSha(repo: app.storage.Repository, branch: String): Try<String> {
        return Try {
            val cmts = Connector.commitService.getCommits(repo.toRepositoryId())
            cmts.sortByDescending { it.commit.author.date }
            cmts.first().sha
        }
    }

    fun getLatestCommitSha2(
        repo: app.storage.Repository,
        branch: String,
        path: String
    ): Try<String> {
        return Try {
            contentService.getContents(repo.toRepositoryId(), branch, path)
            val cmts = Connector.commitService.getCommits(repo.toRepositoryId())
            cmts.sortByDescending { it.commit.author.date }
            cmts.first().sha
        }
    }


    override fun downloadSample(repo: app.storage.Repository, dirSha: String): Try<List<SampleFile>> {
        return Try {
            val repositoryId = RepositoryId.create(repo.owner, repo.repo)
            getBlobs(repositoryId, dirSha)
                .map { treeEntry ->
                    SampleFile(
                        treeEntry.path,
                        dataService.getBlob(repositoryId, treeEntry.sha).content.base64toUtf8()
                    )
                }
        }
    }

    private fun getBlobs(repository: RepositoryId, sha: String): List<TreeEntry> {
        val tree = dataService.getTree(repository, sha, true)
//        if (tree.truncated) TODO: fix it
        return tree.tree.filter { it.type == "blob" }
    }
}

fun List<SampleFile>.compress(): ByteArrayOutputStream {
    val zipResult = ByteArrayOutputStream()

    val zipOutputStream = ZipOutputStream(BufferedOutputStream(zipResult))
    zipOutputStream.use { zos ->
        this.forEach { file ->
            zos.putNextEntry(ZipEntry(file.path))
            zos.write(file.content.toByteArray())
            zos.closeEntry()
        }
    }
    return zipResult
}

fun ByteArrayOutputStream.write(file: File) {
    file.parentFile.mkdirs()
    this.writeTo(file.outputStream())
}

data class SampleFile(
    val path: String,
//    val name: String,
    val content: String
)