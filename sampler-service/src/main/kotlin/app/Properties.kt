package app

import org.eclipse.egit.github.core.client.GitHubClient
import java.io.File

object Properties {
    private val projectDir = File("/home/peter.bogdanov/IdeaProjects/csc-practice/")
    private val secretProps: Map<String, String> = Properties.javaClass.getResourceAsStream("prop.txt")
            .reader()
            .readLines()
            .map { it.split("=") }
            .associate { it[0] to it[1] }

    val storagePath = File(projectDir, "_tmp")
    val samplerInfoFile = File(storagePath, "samplerInfo.json")
    val infoFile = File(storagePath, "info.json")
    val githubToken = secretProps["github.oauth2.token"]!!

    val client = GitHubClient().apply { setOAuth2Token(githubToken) }
}
