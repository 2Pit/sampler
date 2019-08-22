package app.services

import app.api.GitPullRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface PullRequestService {
    @POST("/repos/{owner}/{repo}/pulls")
    suspend fun create(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Body content: CreateRequest
    ): GitPullRequest

    class CreateRequest(
            title: String,
            head: String,
            base: String,
            body: String? = null
//            maintainer_can_modify: Boolean = false,
    )
}