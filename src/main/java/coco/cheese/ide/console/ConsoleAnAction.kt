package coco.cheese.ide.console

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import javax.swing.Icon

class ConsoleAnAction(text: String?, description: String?, icon: Icon?) :
    AnAction(text, description, icon) {
    override fun actionPerformed(e: AnActionEvent) {
        Messages.showMessageDialog("Custom action", "Custom Action", Messages.getInformationIcon())
    }
}
