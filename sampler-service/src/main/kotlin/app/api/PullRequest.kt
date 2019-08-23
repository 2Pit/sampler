package app.api

import kotlinx.serialization.Serializable

// https://developer.github.com/v3/pulls/

@Serializable
class GitPullRequest(
        val base: Base,
        val body: String,
        val head: Base,
        val number: Int,
        val state: State,
        val title: String,
        val user: GitUser
) {

    @Serializable
    class Base(
            val label: String,
            val ref: String,
            val repo: GitRepository
    )

    enum class State { open, close }
}