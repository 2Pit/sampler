package app

import gitmove.services.ProjectService
import okhttp3.OkHttpClient
import okio.Buffer
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.CommitService
import org.eclipse.egit.github.core.service.ContentsService
import org.eclipse.egit.github.core.service.IssueService
import org.eclipse.egit.github.core.service.RepositoryService
import org.slf4j.LoggerFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

object Properties {
    private val projectDir = File("/home/peter.bogdanov/IdeaProjects/csc-practice/")
    private val secretProps: Map<String, String> = Properties.javaClass.getResourceAsStream("prop.txt")
            .reader()
            .readLines()
            .map { it.split("=") }
            .associate { it[0] to it[1] }

    val storagePath = File(projectDir, "_tmp")
    val samplerInfoFile = File(storagePath, "samplerInfo.json")
    val infoFile = File(storagePath, "info.json")
    val githubToken = secretProps["github.oauth2.token"]!!

    val client = GitHubClient().apply { setOAuth2Token(Properties.githubToken) }
}

object Services {
    val issueService = IssueService(Properties.client)
    val repositoryService = RepositoryService(Properties.client)
    val commitService = CommitService(Properties.client)
    val contentService = ContentsService(Properties.client)
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
                    //            chain.request().body()LoggerFactory.getLogger(ProjectService::class.java)
                    val r = chain.request()
                    val buffer = Buffer()
                    r.body()?.writeTo(buffer)
                    LoggerFactory.getLogger(ProjectService::class.java).info(buffer.readUtf8())
                    LoggerFactory.getLogger(ProjectService::class.java).info(r.url().toString())
                    LoggerFactory.getLogger(ProjectService::class.java).info(r.headers().toString())
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