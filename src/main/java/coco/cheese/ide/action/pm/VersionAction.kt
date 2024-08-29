package coco.cheese.ide.action.pm




import coco.cheese.ide.action.pm.TestAction.Companion.writeCommandToTerminal
import coco.cheese.ide.data.SettingConfig

import coco.cheese.ide.utils.ToastUtils
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.annotations.NotNull


class VersionAction : AnAction() {

    override fun actionPerformed(@NotNull e: AnActionEvent) {
        val configValue = SettingConfig.CHEESE_IDE_V
        if (StringUtil.isEmpty(configValue)) {
            ToastUtils.error("版本号错误")
        }
        ToastUtils.info(configValue)
//        writeCommandToTerminal(connector!!, "node -v")
    }
    override fun getActionUpdateThread(): ActionUpdateThread {
        // 指定在 EDT（事件调度线程）上执行
        return ActionUpdateThread.EDT
    }

}