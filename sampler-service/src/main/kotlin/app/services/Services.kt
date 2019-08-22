package app.services

import app.Properties
import okhttp3.OkHttpClient
import okio.Buffer
import org.eclipse.egit.github.core.service.*
import org.slf4j.LoggerFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Services {
    val issueService = IssueService(Properties.client)
    val repositoryService = RepositoryService(Properties.client)
    val commitService = CommitService(Properties.client)
    val contentService = ContentsService(Properties.client)
    val dataService = DataService(Properties.client)
    val projectService = initRetrofitService(ProjectService::class.java)
    val pullRequestService = initRetrofitService(PullRequestService::class.java)

    private fun <T> initRetrofitService(clazz: Class<T>): T {
        val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/vnd.github.inertia-preview+json")
                    .addHeader("Authorization", "token ${Properties.githubToken}")
                    .build()
            chain.proceed(request)
        }.addInterceptor { chain ->
            val logger = LoggerFactory.getLogger(clazz)
            val request = chain.request()
            val buffer = Buffer()
            request.body()?.writeTo(buffer)

            logger.info(buffer.readUtf8())
            logger.info(request.url().toString())
            logger.info(request.headers().toString())

            chain.proceed(request)
        }.build()

        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build()

        return retrofit.create(clazz)
    }
}