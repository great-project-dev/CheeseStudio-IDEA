package net.codeocean.cheese.factory

import net.codeocean.cheese.factory.template.AndroidTemplate
import net.codeocean.cheese.factory.template.IOSTemplate

import net.codeocean.cheese.utils.IconsUtils
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.platform.ProjectTemplate
import com.intellij.platform.ProjectTemplatesFactory
import javax.swing.Icon

class TemplateFactory : ProjectTemplatesFactory() {

    override fun getGroups(): Array<String> {
        return arrayOf("Cheese")
    }

    override fun getGroupIcon(group: String): Icon {
        return IconsUtils.getImage("cheese.svg")
    }

    override fun createTemplates(group: String?, context: WizardContext): Array<ProjectTemplate> {
//        return when (group) {
//            "Cheese Android" -> arrayOf(AndroidTemplate(),)
//            "Cheese IOS" -> arrayOf(IOSTemplate())
//            else -> emptyArray()
//        }
        return arrayOf( AndroidTemplate(),IOSTemplate())
    }
}
