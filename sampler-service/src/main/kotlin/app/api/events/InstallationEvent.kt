package app.api.events

import app.api.GitRepository
import app.api.GitUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstallationEvent(
        val action: Action,
        val installation: Installation,
        val repositories: List<GitRepository>,
        val sender: GitUser
) {
    enum class Action { created, deleted, new_permissions_accepted }

    @Serializable
    class Installation(
            val id: Int,
            val account: Account,
            @SerialName("app_id") val appId: Int,
            @SerialName("target_id") val targetId: Int,
            @SerialName("target_type") val targetType: TargetType
//            val created_at: Long,
//            val updated_at: Long
    ) {
        @Serializable
        data class Account(
                val login: String,
                val id: Int,
                val type: TargetType
        )

        enum class TargetType { Organization }
    }
}