package org.jetbrains.sampler.pw

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.GrowPolicy
import com.intellij.ui.layout.panel
import com.test.SampleDescription
import org.jetbrains.sampler.downloadSampleDescriptions
import javax.swing.JComponent
import javax.swing.JTextPane


class DemoModuleWizardStep : ModuleBuilder() {
    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
    }

    override fun getModuleType(): ModuleType<*> {
        return ModuleType.EMPTY //or it could be any other module type
    }

    override fun createWizardSteps(wizardContext: WizardContext, modulesProvider: ModulesProvider): Array<ModuleWizardStep> {
        return arrayOf(createFirstStep(downloadSampleDescriptions()))
    }

    private fun createFirstStep(descriptionOfSamples: List<SampleDescription>): ModuleWizardStep {
        lateinit var search: JBTextField
        val list = JBList(descriptionOfSamples)


        return object : ModuleWizardStep() {
            override fun updateDataModel() {
            }

            override fun getComponent(): JComponent {
                return panel {
                    row {
                        JBTextField().apply { search = this }(growX, pushX)
                    }
                    row {
                        JBScrollPane(list)(growX, pushX, growPolicy = GrowPolicy.MEDIUM_TEXT)
                        JTextPane().apply {
                            val description = this
                            isRequestFocusEnabled = true
                            list.addListSelectionListener {
                                list.selectedValue?.let {
                                    description.text = it.readme
                                    description.caretPosition = 0
                                }
                            }
                        }(growX, pushX)
                    }
                }
            }
        }
    }
}
