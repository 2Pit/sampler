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
    val pullRequestService = PullRequestService(Properties.client)
    val projectService = initProjectService()

    private fun initProjectService(): ProjectService {
        val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/vnd.github.inertia-preview+json")
                    .addHeader("Authorization", "token ${Properties.githubToken}")
                    .build()
            chain.proceed(request)
        }
                .addInterceptor { chain ->
                    val logger = LoggerFactory.getLogger(ProjectService::class.java)
                    val r = chain.request()
                    val buffer = Buffer()
                    r.body()?.writeTo(buffer)

                    logger.info(buffer.readUtf8())
                    logger.info(r.url().toString())
                    logger.info(r.headers().toString())

                    chain.proceed(r)
                }
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build()

        return retrofit.create(ProjectService::class.java)
    }
}