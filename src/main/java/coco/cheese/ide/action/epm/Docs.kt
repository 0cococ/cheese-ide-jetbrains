package coco.cheese.ide.action.epm


import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import java.awt.Desktop
import java.net.URI
import javax.swing.Icon

class Docs(name: String, description: String,icon: Icon)  :  AnAction(name, description, icon){


    override fun actionPerformed(e: AnActionEvent) {
        val url = "https://cheese.worldcore.cn" // 替换为你想要打开的网址

        try {
            // 确保 Desktop 类可以在当前环境中使用
            if (Desktop.isDesktopSupported()) {
                val desktop = Desktop.getDesktop()
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(URI(url))
                } else {
                    println("Browse action is not supported on this platform.")
                }
            } else {
                println("Desktop is not supported on this platform.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Failed to open the URL: $url")
        }
    }

}