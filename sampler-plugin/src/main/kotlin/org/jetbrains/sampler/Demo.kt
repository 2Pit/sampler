package org.jetbrains.sampler

import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.openapi.module.ModuleType
import javax.swing.JComponent
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.ide.util.projectWizard.WizardContext
import javax.swing.JLabel


class DemoModuleWizardStep : ModuleBuilder() {
    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {

    }

    override fun getModuleType(): ModuleType<*> {
        return ModuleType.EMPTY //or it could be any other module type
    }

    override fun createWizardSteps(wizardContext: WizardContext, modulesProvider: ModulesProvider): Array<ModuleWizardStep> {
        return arrayOf(object : ModuleWizardStep() {
            override fun getComponent(): JComponent {
                return JLabel("Put your content here")
            }

            override fun updateDataModel() {

            }
        })
    }
}
