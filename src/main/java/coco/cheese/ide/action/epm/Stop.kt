package coco.cheese.ide.action.epm

import coco.cheese.ide.InteractionServer.instruction

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import javax.swing.Icon

class Stop (name: String, description: String,icon: Icon) : AnAction(name, description, icon) {

    override fun actionPerformed(e: AnActionEvent) {

        // 或者使用日志记录
        // Logger.getInstance(Run::class.java).info("运行Cheese")
        instruction =3
    }
}