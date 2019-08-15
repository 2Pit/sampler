package gitmove.services

import gitmove.GProject
import gitmove.GSender
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ProjectService {
    @GET("/orgs/{org}/projects")
    suspend fun getProjects(@Path("org") org: String): Response<List<GProject>>

    @GET("/repos/{owner}/{repo}/projects")
    suspend fun getProjects(@Path("owner") owner: String, @Path("repo") repo: String): Response<List<GProject>>

    @GET("/projects/{project_id}")
    suspend fun getProject(@Path("project_id") projectId: Long): Response<GProject>

    @GET("/projects/{project_id}/columns")
    suspend fun getColumns(@Path("project_id") projectId: Long): Response<List<GColumn>>

    @GET("/projects/columns/{column_id}/cards")
    suspend fun getCards(@Path("column_id") columnId: Long): Response<List<GCard>>

    @POST("/projects/columns/{column_id}/cards")
    suspend fun createCard(@Path("column_id") columnId: Long): Response<GCard>

//    suspend fun createCard(
//            columnId: Long,
//            contentId: Long,
//            contentType: String
//    ): GCard = createCard(columnId, CreateRequest(contentId, contentType))

    @POST("/projects/columns/{column_id}/cards")
    suspend fun createCard(
            @Path("column_id") columnId: Long,
            @Body content: CreateRequest
    ): Response<GCard>

    class CreateRequest(val content_id: Long, val content_type: String)
}

@Serializable
data class GColumn(val Id: Long, val name: String)

data class GCard(
        val id: Long,
        val note: String,
        val creator: GSender,
        val archived: Boolean
)

enum class CardContentType { Issue, PullRequest }

