package org.jetbrains.sampler.pw

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.roots.ModifiableRootModel
import org.jetbrains.sampler.downloadSampleDescriptions


class DemoModuleWizardStep : ModuleBuilder() {

    private val allTemplates by lazy {
        downloadSampleDescriptions()
    }

    private val settingsComponents by lazy {
        SamplerTemplateList(allTemplates)
    }

    override fun getGroupName(): String = "Sampler"

    override fun getBuilderId(): String = "SamplerBuilderId"

    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
    }

    override fun getModuleType(): ModuleType<*> {
        return ModuleType.EMPTY //or it could be any other module type
    }

    override fun modifySettingsStep(settingsStep: SettingsStep): ModuleWizardStep? {
        settingsStep.addSettingsComponent(settingsComponents.mainPanel)
        return super.modifySettingsStep(settingsStep)
    }
}
