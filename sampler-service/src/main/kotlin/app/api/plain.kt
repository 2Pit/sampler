package app.api

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import java.util.*

@Serializable
data class GitReference(
        val ref: String,
        val `object`: GitRefObject
) {
    @Serializable
    data class GitRefObject(
            val type: GitRefType,
            val sha: String
    ) {
        enum class GitRefType { commit, tag }
    }
}

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
data class GitAuthor(
        @Serializable(with = DateSerializer::class)
        val date: Date,
        val name: String,
        val email: String
)

@Serializable
data class GitCommit(
//        override val sha: String,
        val sha: String,
        val author: GitAuthor,
        val committer: GitAuthor,
        val message: String,
        val tree: GitTreeI,
//        val verification: Verification,
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

@Serializer(forClass = DateSerializer::class)
object DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor =
            StringDescriptor.withName("DateSerializer")

    override fun serialize(encoder: Encoder, obj: Date) {
        encoder.encodeString(obj.time.toString())
    }

    override fun deserialize(decoder: Decoder): Date {
        return Date(decoder.decodeString().toLong())
    }
}

@Serializable
data class GitUser(
        val id: Int,
        val login: String,
        val type: Type
) {
    enum class Type { User }
}

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
data class GitRepository(
        val id: Int,
        val name: String,
        @SerialName("full_name") val fullName: String
//        val owner: GitUser
)


@Serializable
data class GitPusher(val name: String, val email: String)

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
//          val user
    )

    enum class State { open, close }
}