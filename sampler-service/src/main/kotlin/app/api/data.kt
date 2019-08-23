package app.api

import kotlinx.serialization.Serializable

// https://developer.github.com/v3/git/

@Serializable
data class GitBlob(
        val sha: String,
        val size: Int,
        val content: GitBlobContentType,
        val encoding: String
) {
    enum class GitBlobContentType { `utf-8`, base64 }
}

@Serializable
data class GitCommit(
        val sha: String,
        val author: GitAuthor,
        val committer: GitAuthor,
        val message: String,
        val tree: GitTreeI,
        val parents: List<GitTreeI>
) {
    @Serializable
    data class GitTreeI(val sha: String)

    @Serializable
    data class GitVerification(
            val verified: Boolean,
            val reason: String,
            val signature: String,
            val payload: String
    )
}

@Serializable
data class GitTree(
        val sha: String,
        val tree: List<GitTreeNode>,
        val truncated: Boolean
) {
    @Serializable
    data class GitTreeNode(
            val sha: String,
            val path: String,
            val mode: String,
            val type: GitTreeType,
            val size: Int
    ) {
        enum class GitTreeType { blob, tree }
    }
}