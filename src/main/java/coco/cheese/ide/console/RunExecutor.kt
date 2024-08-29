package coco.cheese.ide.console

import coco.cheese.ide.utils.IconsUtils
import com.intellij.execution.Executor
import com.intellij.execution.ExecutorRegistry
import javax.swing.Icon



class RunExecutor : Executor() {
    override fun getToolWindowId(): String {
        return TOOL_WINDOW_ID
    }

    override fun getToolWindowIcon(): Icon {
        return IconsUtils.getImage("cheese.svg")
    }

    override fun getIcon(): Icon {
        return IconsUtils.getImage("cheese.svg")
    }

    override fun getDisabledIcon(): Icon {
        return IconsUtils.getImage("cheese.svg")
    }

    override fun getDescription(): String {
        return TOOL_WINDOW_ID
    }

    override fun getActionName(): String {
        return TOOL_WINDOW_ID
    }

    override fun getId(): String {
        return "plugin id"
    }

    override fun getStartActionText(): String {
        return TOOL_WINDOW_ID
    }

    override fun getContextActionId(): String {
        return "custom context action id"
    }

    override fun getHelpId(): String {
        return TOOL_WINDOW_ID
    }

    companion object {
        const val TOOL_WINDOW_ID: String = "cheese console"
        val getRunExecutorInstance: Executor?
            get() = ExecutorRegistry.getInstance().getExecutorById("plugin id")
    }
}