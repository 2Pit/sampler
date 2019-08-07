package gitmove

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


fun main() {
    val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
        val request = chain.request().newBuilder()
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("Authorization", "token 147d2090ec01f251b04912916f14d4ce3646b5b7")
                .build()
        chain.proceed(request)
    }.build()

    val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()

    val outService = SpecialService(
            retrofit.create(GitHubService::class.java),
            Repository("2Pit", "test")
    )

    val ans: List<Reference> = runBlocking {
        outService.getAllRefs()
    }
//    val ans = outService.getAllRefs()
    ans.forEach { println(it) }
}
