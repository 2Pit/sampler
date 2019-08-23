package app.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// https://developer.github.com/v3/projects/

@Serializable
data class GitProject(
        val id: Int,
        val name: String,
        val body: String,
        val number: Int,
        val state: String,
        val creator: GitUser
)

@Serializable
data class GitColumn(val Id: Long, val name: String)

@Serializable
data class GitCard(
        val id: Long,
        val note: String,
        val creator: GitUser,
        val archived: Boolean,
        @SerialName("content_url") val contentUrl: String
) {
    val issueNumber = contentUrl.substringAfterLast("/").toInt()
}
