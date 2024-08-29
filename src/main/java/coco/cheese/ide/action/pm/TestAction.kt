package coco.cheese.ide.action.pm



import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.history.core.Content
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.terminal.JBTerminalSystemSettingsProviderBase
import com.intellij.terminal.JBTerminalWidget
import com.intellij.terminal.pty.PtyProcessTtyConnector
import com.intellij.ui.content.ContentFactory
import com.jediterm.terminal.TtyConnector
import com.pty4j.PtyProcess
import com.pty4j.PtyProcessBuilder
import org.jetbrains.annotations.NotNull
import java.io.IOException
import java.nio.charset.StandardCharsets
import com.intellij.openapi.diagnostic.Logger
import javax.swing.SwingUtilities

var connector: TtyConnector?=null
class TestAction : AnAction() {

    override fun actionPerformed(@NotNull e: AnActionEvent) {
        val project = e.project ?: return
        val toolWindowPanel = SimpleToolWindowPanel(true, true)

        val terminalWidget = createTerminal(project, Disposer.newDisposable())

        toolWindowPanel.setContent(terminalWidget.component)
        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.getToolWindow("Terminal")
        toolWindow?.let {
            val contentFactory = ApplicationManager.getApplication().getService(ContentFactory::class.java)
            val content = contentFactory.createContent(toolWindowPanel, "cheese-shell", false)
            it.contentManager.addContent(content)
            it.show()
        } ?: run {
            Logger.getInstance(TestAction::class.java).error("ToolWindow not found")
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    private fun createTerminal(project: Project, parent: Disposable): JBTerminalWidget {
        val settingsProvider = JBTerminalSystemSettingsProviderBase()
        val terminalWidget = JBTerminalWidget(project, settingsProvider, parent)
         connector = createLocalShellTtyConnector()
        terminalWidget.start(connector)

        return terminalWidget
    }

    private fun createLocalShellTtyConnector(): TtyConnector {
        return try {
            val command = if (SystemInfo.isWindows) {
                arrayOf("cmd.exe")
            } else {
                arrayOf("/bin/bash")
            }
            val ptyProcess = PtyProcessBuilder().setCommand(command).start()
            PtyProcessTtyConnector(ptyProcess, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            Logger.getInstance(TestAction::class.java).error("Failed to start the terminal process", e)
            throw RuntimeException("Failed to start the terminal process", e)
        }
    }
    companion object{
        fun writeCommandToTerminal(connector: TtyConnector, command: String) {

            connector.write(command + "\r\n")

        }
    }
}
