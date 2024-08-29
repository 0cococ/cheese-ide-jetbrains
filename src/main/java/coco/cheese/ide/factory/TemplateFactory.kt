package coco.cheese.ide.factory

import coco.cheese.ide.factory.template.AndroidTemplate
import coco.cheese.ide.factory.template.IOSTemplate

import coco.cheese.ide.utils.IconsUtils
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
        return arrayOf(IOSTemplate(), AndroidTemplate())
    }
}
