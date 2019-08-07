package gitmove

import java.util.*

data class Reference(
        val ref: String,
        val `object`: RefObject
)

data class RefObject(
        val type: RefType,
        val sha: String
)

enum class RefType { commit, tag }

data class Blob(
        override val sha: String,
        val size: Int,
        val content: BlobContentType,
        val encoding: String
) : TreeI

enum class BlobContentType { `utf-8`, base64 }

data class Author(
        val date: Date,
        val name: String,
        val email: String
)

interface TreeI {
    val sha: String
}

interface CommitI : TreeI

data class Commit(
        override val sha: String,
        val author: Author,
        val committer: Author,
        val message: String,
        val tree: TreeI,
        val parents: List<CommitI>,
        val verification: Verification
) : CommitI

data class Verification(
        val verified: Boolean,
        val reason: String,
        val signature: String,
        val payload: String
)

data class Tree(
        override val sha: String,
        val tree: List<TreeNode>,
        val truncated: Boolean
) : TreeI

data class TreeNode(
        override val sha: String,
        val path: String,
        val mode: String,
        val type: String,
        val size: Int
) : TreeI