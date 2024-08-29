package coco.cheese.ide.windows.toolwindow

import coco.cheese.ide.swing.code.NodeUi
import coco.cheese.ide.swing.code.PaintUi
import coco.cheese.ide.utils.IconsUtils
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import groovyjarjarantlr4.v4.runtime.misc.NotNull
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JFrame

class TJ: ToolWindowFactory {
    override fun createToolWindowContent(@NotNull project: Project, @NotNull toolWindow: ToolWindow) {
        val frame: JFrame = PaintUi.getFrame()
        val frame1: JFrame = NodeUi.getFrame()
        // 获取 JFrame 的内容面板
        val contentPane = frame.contentPane as JComponent
        val contentPane1 = frame1.contentPane as JComponent
        // 使用 ContentFactory 创建 Content 并添加到 ToolWindow
        val factory = ContentFactory.getInstance()
        val content1 = factory.createContent(contentPane, "图色", false)
        val content2 = factory.createContent(contentPane1, "节点", false)
        toolWindow.contentManager.addContent(content1)
        toolWindow.contentManager.addContent(content2)
    }

    override val icon: Icon
        get() = IconsUtils.getImage("paint.svg")
}

