package org.jetbrains.sampler.pw

import com.intellij.openapi.module.ModuleType
import javax.swing.Icon

class SamplerModuleType : ModuleType<SamplerModuleBuilder>("JAVA_MODULE") {
    override fun createModuleBuilder(): SamplerModuleBuilder = SamplerModuleBuilder()

    override fun getName(): String = "Sampler"

    override fun getDescription(): String = "Cloning a tutorial from list."

    override fun getNodeIcon(isOpened: Boolean): Icon = Icons.KOTLIN_SMALL_LOGO
}