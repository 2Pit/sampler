package org.jetbrains.sampler.pw

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.util.io.FileUtil
import git4idea.commands.Git
import org.jetbrains.plugins.gradle.service.project.open.linkAndRefreshGradleProject
import org.jetbrains.sampler.downloadSampleDescriptions
import java.io.File


class SamplerModuleBuilder : ModuleBuilder() {
    companion object {
        val LOG: Logger = Logger.getInstance(SamplerModuleBuilder::class.java)
    }

    private val allTemplates by lazy { downloadSampleDescriptions() }

    private val settingsComponents by lazy { SamplerTemplateList(allTemplates) }

    override fun getGroupName(): String = "Sampler"

    override fun getBuilderId(): String = "SamplerBuilderId"

    override fun getModuleType(): ModuleType<*> = SamplerModuleType()

    override fun modifySettingsStep(settingsStep: SettingsStep): ModuleWizardStep? {
        settingsStep.addSettingsComponent(settingsComponents.mainPanel)
        return super.modifySettingsStep(settingsStep)
    }

    override fun setupRootModel(model: ModifiableRootModel) {
        val info = settingsComponents.selectedTemplate!!
        val project = model.project

        ApplicationManager.getApplication().invokeLater({
            object : Task.Modal(project, "Cloning Sample...", false) {
                override fun run(indicator: ProgressIndicator) {
                    val tmpDir = FileUtil.createTempDirectory("clone", info.name)
                    val res = Git.getInstance().clone(project, tmpDir.parentFile, info.url, tmpDir.name)
                    FileUtil.moveDirWithContent(tmpDir, File(project.basePath!!))
                    if (!res.success()) LOG.error(res.toString()) else LOG.debug(res.toString())

                    linkAndRefreshGradleProject(project.presentableUrl!!, project)
                }
            }.queue()
        }, ModalityState.current())
    }
}

