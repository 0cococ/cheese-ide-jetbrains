package coco.cheese.ide.action.epm

import coco.cheese.ide.Env.OS.separator
import coco.cheese.ide.console.ConsoleExecutor.Companion.getConsoleView
import coco.cheese.ide.data.SettingConfig
import coco.cheese.ide.utils.FileUtils
import coco.cheese.ide.utils.StorageUtils
import coco.cheese.ide.utils.ToastUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.SystemIndependent
import java.io.File
import javax.swing.Icon

class UpdateProject(name: String, description: String,icon: Icon) : AnAction(name, description, icon) {
    override fun actionPerformed(e: AnActionEvent) {
        if(StorageUtils.getString(SettingConfig.CHEESE_HOME).isNullOrEmpty()){
            ToastUtils.error("CHEESE_HOME 环境路径未设置 无法更新项目")
            return
        }
        val baseDir: @SystemIndependent @NonNls String? = e.project!!.basePath
//        val virtualFile: VirtualFile? = e.getData(CommonDataKeys.VIRTUAL_FILE)
        baseDir?.let {
            val node_modules = "$it${separator}node_modules"
          File(node_modules+separator+"cheese-core").delete()
            Thread{
                FileUtils.copyDirectory(StorageUtils.getString(SettingConfig.CHEESE_HOME)+ separator +"project"+separator+"node_modules"+separator+"cheese-core",node_modules+separator+"cheese-core")
            }.start()

        }


    }
}