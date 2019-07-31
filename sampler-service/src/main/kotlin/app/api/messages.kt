package app.api

import app.db.RepositoryRow
import kotlinx.serialization.Serializable

@Serializable
data class AddRequest(
    val owner: String,
    val repo: String,
    val branch: String = "master",
    val path: String,
    val name: String
)

@Serializable
data class RepoInfo(
    val owner: String,
    val repo: String,
    val branch: String,
    val samples: List<SampleInfo>
) {
    constructor(repository: RepositoryRow, samples: List<SampleInfo>) : this(
        repository.owner,
        repository.repo,
        repository.branch,
        samples
    )
}

@Serializable
data class SampleInfo(
    val name: String,
    val readMe: String,
    val buildSystem: String,
    val sha: String
)