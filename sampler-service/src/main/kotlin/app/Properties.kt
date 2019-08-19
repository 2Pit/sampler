package app

import org.eclipse.egit.github.core.client.GitHubClient
import java.io.File

object Properties {
    private val projectDir = File("/home/peter.bogdanov/IdeaProjects/csc-practice/")

    val storagePath = File(projectDir, "_tmp")
    val samplerInfoFile = File(storagePath, "samplerInfo.json")
    val infoFile = File(storagePath, "info.json")
    lateinit var githubToken: String
    lateinit var client: GitHubClient

    fun init() {
        client = GitHubClient().apply { setOAuth2Token(githubToken) }
    }
}
