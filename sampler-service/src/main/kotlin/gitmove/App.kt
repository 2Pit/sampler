package gitmove

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

fun main() {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    val outService = SpecialService(
            retrofit.create(GitHubService::class.java),
            Repository("2Pit", "test")
    )

    val ans = outService.getAllRefs()
    ans.execute().body()?.forEach { println(it) }
}


class SpecialService(
        private val service: GitHubService,
        val repository: Repository
) {
    fun getBlob(sha: String) = service.getBlob(repository.owner, repository.repo, sha)
    fun getAllRefs() = service.getAllRefs(repository.owner, repository.repo)
}

class Repository(
    val owner: String,
    val repo: String
)

interface GitHubService {
    @Headers(
        "Accept: application/vnd.github.v3+json",
        "Authorization: token 147d2090ec01f251b04912916f14d4ce3646b5b7"
    )
    @GET("/repos/{owner}/{repo}/git/blobs/{sha}")
    fun getBlob(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("sha") sha: String
    ): Call<Blob>

    @GET("/repos/{owner}/{repo}/git/refs")
    fun getAllRefs(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Call<List<Reference>>
}