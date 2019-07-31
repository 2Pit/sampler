package git

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GitHubUrlParserTest {

    @Test
    fun parseGitHubUrl1() {
        val result = GitHubUrlParser.parse("https://github.com/mockk/mockk")
        val expected = GitHubInfo(
            owner = "mockk",
            repo = "mockk"
        )
        assertEquals(expected, result)
    }

    @Test
    fun parseGitHubUrl2() {
        val result = GitHubUrlParser.parse("https://github.com/mockk/mockk/tree/master/gradle")
        val expected = GitHubInfo(
            owner = "mockk",
            repo = "mockk",
            branch = "master",
            path = "gradle"
        )
        assertEquals(expected, result)
    }

    @Test
    fun parseGitHubUrl3() {
        val result = GitHubUrlParser.parse("https://github.com/mockk/mockk/blob/master/gradle/common-module.gradle")
        val expected = GitHubInfo(
            owner = "mockk",
            repo = "mockk",
            branch = "master",
            path = "gradle",
            file = "common-module.gradle"
        )
        assertEquals(expected, result)
    }

    @Test
    fun parseGitHubUrl4() {
        val result = GitHubUrlParser.parse("https://github.com/mockk/mockk/blob/kotlin13/ANDROID.md")
        val expected = GitHubInfo(
            owner = "mockk",
            repo = "mockk",
            branch = "kotlin13",
            path = "",
            file = "ANDROID.md"
        )
        assertEquals(expected, result)
    }
}