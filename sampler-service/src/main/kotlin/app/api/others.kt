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
data class GitAuthor(
        @Serializable(with = DateSerializer::class)
        val date: Date,
        val name: String,
        val email: String
)

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
data class GitRepository(
        val id: Int,
        val name: String,
        @SerialName("full_name") val fullName: String
)

@Serializable
data class GitPusher(val name: String, val email: String)
