package app

import gitmove.InstallationEvent
import io.ktor.util.pipeline.Pipeline
import io.ktor.util.pipeline.PipelinePhase
import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.Issue
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.IssueService
import org.eclipse.egit.github.core.service.RepositoryService
import org.slf4j.LoggerFactory

object AddSample {
    val log = LoggerFactory.getLogger(AddSample::class.java)

    private val client = GitHubClient().apply { setOAuth2Token(Properties.githubToken) }
    val issueService = IssueService(client)
    val repositoryService = RepositoryService(client)

    private val checkIn = PipelinePhase("checkIn")
    val checkInPipeline = Pipeline<Unit, InstallationEvent>(checkIn)

    private val forkRepo = PipelinePhase("forkRepo")


    private val createIssue = PipelinePhase("CreatePR")
    private val testCode = PipelinePhase("testCode")
    private val updateIssue = PipelinePhase("updateIssue")
    val runner = Pipeline<AddSubject, InstallationEvent>(forkRepo, createIssue, testCode)

    init {
        checkInPipeline.intercept(checkIn) {
            val event = context
            event.repositories.forEach {
                val oneRepoEvent = event.copy(repositories = listOf(it))
                runner.execute(oneRepoEvent, AddSubject(IRepositoryIdProvider { it.full_name }))
            }
        }

        runner.intercept(forkRepo) {
            subject.fork = repositoryService.forkRepository(subject.base, "ksamples")
        }


        runner.intercept(createIssue) {
            subject.issue = issueService.createIssue(
                    subject.fork,
                    Issue().apply {
                        title = "Add the ${subject.base.generateId()} to the Kotlin Sampler."
                        body = "This issue is created automatically. Here Checker will update the status."
                    }
            )

            issueService.createIssue(
                    subject.base,
                    Issue().apply {
                        title = "Your sample is registred to add in Kotlin Sample."
                        body = "All info you can find here <link>. This is an info issue."
                    }
            )
        }

        runner.intercept(testCode) {
            issueService.createComment(subject.fork, subject.issue.number, "check started")
//            DO SMTH
            issueService.createComment(subject.fork, subject.issue.number, "check successfully finished")
        }
    }
}

class AddSubject(val base: IRepositoryIdProvider) {
    lateinit var issue: Issue
    lateinit var fork: IRepositoryIdProvider
}