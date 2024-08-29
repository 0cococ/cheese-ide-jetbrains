package coco.cheese.ide.action.epm


import coco.cheese.ide.utils.ToastUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import javax.swing.Icon

class Runx(name: String, description: String,icon: Icon) : AnAction(name, description, icon) {

    override fun actionPerformed(e: AnActionEvent) {
        ApplicationManager.getApplication().invokeLater {
            ToastUtils.info("当前功能属于内测")
        }
    }
}