package org.jetbrains.sampler

import com.intellij.openapi.application.JBProtocolCommand
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.ProjectManager
import java.util.regex.Pattern

class JBProtocolSamplerCommand : JBProtocolCommand("sampler") {
    companion object {
        val navigate by lazy { findCommand("navigate")!! }

//        TODO fix pattern in JbProtocolNavigateCommand
        val pattern = Pattern.compile("(?<path>[^:]*)(:(?<line>[\\d]+))(:(?<column>[\\d]+))?")
    }

    override fun perform(sampleName: String, parameters: MutableMap<String, String>) {
        ProjectManager.getInstance().openProjects.filter { project ->
            ModuleManager.getInstance(project).modules.any { it.name == sampleName }
        }.forEach {
            navigate.perform("reference", parameters + ("project" to it.name))
        }
    }
}