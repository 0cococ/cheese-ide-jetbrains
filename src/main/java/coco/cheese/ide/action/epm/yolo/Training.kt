package coco.cheese.ide.action.epm.yolo

import coco.cheese.ide.utils.ToastUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import java.awt.Desktop
import java.net.URI
import javax.swing.Icon

class Training(name: String, description: String,icon: Icon)  :  AnAction(name, description, icon){

    override fun actionPerformed(e: AnActionEvent) {
        ToastUtils.info("当前功能属于内测")

    }

}