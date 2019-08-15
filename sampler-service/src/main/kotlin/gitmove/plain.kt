package gitmove

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
data class InstallationEvent(
        val action: Action,
        val installation: Installation,
        val repositories: List<Repository>,
        val sender: GSender
) {
    enum class Action { created, deleted, new_permissions_accepted }

    @Serializable
    class Installation(
            val id: Int,
            val account: Account,
            val app_id: Int,
            val target_id: Int,
            val target_type: TargetType,
            val created_at: Long,
            val updated_at: Long
    ) {
        @Serializable
        data class Account(
                val login: String,
                val id: Int,
                val type: TargetType
        )

        enum class TargetType { Organization }
    }

    @Serializable
    data class Repository(
            val id: Int,
            val name: String,
            val full_name: String
    )
}

@Serializable
data class GSender(
        val id: Int,
        val login: String,
        val type: Type
) {
    enum class Type { User }
}

@Serializable
data class GProject(
        val id: Int,
        val name: String,
        val body: String,
        val number: Int,
        val state: String,
        val creator: GSender
)