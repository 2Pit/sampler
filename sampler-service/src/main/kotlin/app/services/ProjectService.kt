package app.services

import app.api.GitCard
import app.api.GitColumn
import app.api.GitProject
import app.api.GitUser
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ProjectService {
    @GET("/orgs/{org}/projects")
    suspend fun getProjects(@Path("org") org: String): List<GitProject>

    @GET("/repos/{owner}/{repo}/projects")
    suspend fun getProjects(@Path("owner") owner: String, @Path("repo") repo: String): List<GitProject>

    @GET("/projects/{project_id}")
    suspend fun getProject(@Path("project_id") projectId: Long): GitProject

    @GET("/projects/{project_id}/columns")
    suspend fun getColumns(@Path("project_id") projectId: Long): List<GitColumn>

    @GET("/projects/columns/{column_id}/cards")
    suspend fun getCards(@Path("column_id") columnId: Long): List<GitCard>

    @POST("/projects/columns/{column_id}/cards")
    suspend fun createCard(@Path("column_id") columnId: Long): GitCard

    @POST("/projects/columns/{column_id}/cards")
    suspend fun createCard(
            @Path("column_id") columnId: Long,
            @Body content: CreateRequest
    ): GitCard

    class CreateRequest(val content_id: Long, val content_type: String)
}

enum class CardContentType { Issue, PullRequest }
