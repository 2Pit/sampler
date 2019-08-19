package app.api.events

import app.api.GitCommit
import app.api.GitPusher
import app.api.GitRepository
import app.api.GitUser
import kotlinx.serialization.Serializable

@Serializable
data class PushEvent(
        val ref: String,
        val before: String,
        val after: String,
        val created: Boolean,
        val deleted: Boolean,
        val forced: Boolean,
        val base_ref: String?,
        val commits: List<GitCommit>,
        val headCommit: GitCommit?,
        val repository: GitRepository,
        val pusher: GitPusher,
        val sender: GitUser
)
