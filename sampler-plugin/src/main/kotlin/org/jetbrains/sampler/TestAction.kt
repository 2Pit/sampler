package org.jetbrains.sampler

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.JBProtocolCommand

class TestAction : AnAction("Test Action") {
    override fun actionPerformed(e: AnActionEvent) {
        JBProtocolCommand.findCommand("sampler")!!
                .perform(
                        "coroutines",
                        mapOf(
//                                "fqn1" to "contributors.GitHubService#getRepoContributorsCall"
                                "path1" to "src/contributors/GitHubService.kt"
                        ))
    }
}