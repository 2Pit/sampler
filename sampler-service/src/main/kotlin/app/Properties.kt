package app

import com.typesafe.config.ConfigFactory
import io.ktor.config.HoconApplicationConfig
import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.client.GitHubClient

object Properties {
    private val config = HoconApplicationConfig(ConfigFactory.load())
    val githubToken: String = config.property("ktor.security.github.token").getString()
    val client: GitHubClient = GitHubClient().apply { setOAuth2Token(githubToken) }
    val mainRepo = IRepositoryIdProvider { "ksamples/main" }
}
