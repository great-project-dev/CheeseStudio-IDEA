package net.codeocean.cheese.factory


import net.codeocean.cheese.module.CModuleBuilder
import net.codeocean.cheese.utils.IconsUtils
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import javax.swing.Icon

class CModuleType : ModuleType<CModuleBuilder>(ID) {
    companion object {
        private const val ID = "DEMO_MODULE_TYP"
        fun getInstance(): CModuleType {
            return ModuleTypeManager.getInstance().findByID(ID) as CModuleType
        }
    }
    override fun createModuleBuilder(): CModuleBuilder {
        return CModuleBuilder()
    }
    override fun getName(): String {
        return "Cheese Module Type"
    }
    override fun getDescription(): String {
        return "Example custom module type"
    }
    override fun getNodeIcon(isOpened: Boolean): Icon {
        return IconsUtils.getImage("cheese.svg")
    }

    override fun createWizardSteps(
        wizardContext: WizardContext,
        moduleBuilder: CModuleBuilder,
        modulesProvider: ModulesProvider
    ): Array<ModuleWizardStep> {
        return ModuleWizardStep.EMPTY_ARRAY // 返回空数组
    }
}