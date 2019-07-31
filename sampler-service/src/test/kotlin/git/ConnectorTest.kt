package git

import arrow.core.extensions.`try`.monad.binding
import app.api.AddRequest
import app.db.Repository
import app.git.Connector
import app.git.SampleFile
import app.git.compress
import app.git.write
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class ConnectorTest {

    @Test
    fun testGetBlobs() {
        val addRequest = AddRequest("2Pit", "test", "master", "a/b", "SOME_SAMPLE_NAME")
        val files: List<SampleFile> =
            binding {
                val repository = Repository(addRequest.owner, addRequest.repo, addRequest.branch)
                val (commitSha) = Connector.getLatestCommitSha(repository, addRequest.path)
                val (dirSha) = Connector.getDirSha(repository, commitSha, addRequest.path)
                val (sampleFiles) = Connector.downloadSample(repository, dirSha)
                sampleFiles
            }.fold({ e -> throw e }, { it })

        assertEquals(
            listOf(
                SampleFile("B1.txt", fileContent("B1")),
                SampleFile("B2.txt", fileContent("B2")),
                SampleFile("c/C1.txt", fileContent("C1")),
                SampleFile("c/C2.txt", fileContent("C2"))
            ),
            files
        )
    }

    @Test
    fun testGetBlobs_2() {
        val addRequest = AddRequest("2Pit", "test", "master", "", "SOME_SAMPLE_NAME")
        val files: List<SampleFile> =
            binding {
                val repository = Repository(addRequest.owner, addRequest.repo, addRequest.branch)
                val (commitSha) = Connector.getLatestCommitSha(repository, addRequest.path)
                val (dirSha) = Connector.getDirSha(repository, commitSha, addRequest.path)
                val (sampleFiles) = Connector.downloadSample(repository, dirSha)
                sampleFiles
            }.fold({ e -> throw e }, { it })

        assertEquals(
            listOf(
                SampleFile("0.txt", fileContent("0")),
                SampleFile("a/A1.txt", fileContent("A1")),
                SampleFile("a/A2.txt", fileContent("A2")),
                SampleFile("a/b/B1.txt", fileContent("B1")),
                SampleFile("a/b/B2.txt", fileContent("B2")),
                SampleFile("a/b/c/C1.txt", fileContent("C1")),
                SampleFile("a/b/c/C2.txt", fileContent("C2"))
            ),
            files
        )
    }

    @Test
    fun testGetBlobs_3() {
        val addRequest = AddRequest("2Pit", "test", "master", "a", "SOME_SAMPLE_NAME")
        val files: List<SampleFile> =
            binding {
                val repository = Repository(addRequest.owner, addRequest.repo, addRequest.branch)
                val (commitSha) = Connector.getLatestCommitSha(repository, addRequest.path)
                val (dirSha) = Connector.getDirSha(repository, commitSha, addRequest.path)
                val (sampleFiles) = Connector.downloadSample(repository, dirSha)
                sampleFiles
            }.fold({ e -> throw e }, { it })


        assertEquals(
            listOf(
                SampleFile("A1.txt", fileContent("A1")),
                SampleFile("A2.txt", fileContent("A2")),
                SampleFile("b/B1.txt", fileContent("B1")),
                SampleFile("b/B2.txt", fileContent("B2")),
                SampleFile("b/c/C1.txt", fileContent("C1")),
                SampleFile("b/c/C2.txt", fileContent("C2"))
            ),
            files
        )
    }

    @Test
    fun testWrite() {
        val files = listOf(
            SampleFile("A1.txt", fileContent("A1")),
            SampleFile("A2.txt", fileContent("A2")),
            SampleFile("b/B1.txt", fileContent("B1")),
            SampleFile("b/B2.txt", fileContent("B2")),
            SampleFile("b/c/C1.txt", fileContent("C1")),
            SampleFile("b/c/C2.txt", fileContent("C2"))
        )

        val zipFile = File(
            "/home/peter.bogdanov/IdeaProjects/csc-practice/out/test/a/b/c/d",
            "test.zip"
        )
        files.compress().write(zipFile)
    }

    @Test
    fun getDirSha_1() {
        val repo = Repository("2Pit", "test", "master")

        val res = Connector.contentService.getContents(repo.toRepositoryId(), null, "master")
        println(res)
    }

    @Test
    fun test() {
//        val repo = RepositoryId.create("2Pit", "test")
//        val conntents = Connector.contentService.getContents(repo, "a/b", "57c0ab03d58612c6e3f24a7846069f07a10f9cc9")

        val repo = Repository("2Pit", "test", "master")
        val asd = Connector.getDirSha(repo, "57c0ab03d58612c6e3f24a7846069f07a10f9cc9", "a/b")

        println(asd)
    }

    private fun fileContent(name: String): String {
        return "Hello from $name file!\n2\n3\n4\n"
    }
}