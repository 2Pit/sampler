package app.api.events

import app.api.GitRepository
import app.api.GitUser
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
}