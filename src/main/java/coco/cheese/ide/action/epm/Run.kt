package coco.cheese.ide.action.epm


import coco.cheese.ide.Env.OS.separator
import coco.cheese.ide.InteractionServer.glDownload
import coco.cheese.ide.InteractionServer.instruction
import coco.cheese.ide.console.ConsoleExecutor.Companion.getConsoleView
import coco.cheese.ide.console.ConsoleExecutor.Companion.setProject
import coco.cheese.ide.utils.*
import coco.cheese.ide.utils.FileUtils.copyDirectory
import coco.cheese.ide.utils.FileUtils.copyFile
import coco.cheese.ide.utils.FileUtils.zipDirectory
import coco.cheese.ide.utils.TerminalUtils.executeCommand
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.ModalTaskOwner.project
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.SystemIndependent
import org.tomlj.Toml
import org.tomlj.TomlParseError
import org.tomlj.TomlParseResult
import java.io.File
import java.nio.file.Paths
import java.util.function.Consumer
import javax.swing.Icon


class Run(name: String, description: String,icon: Icon) : AnAction(name, description, icon) {

    override fun actionPerformed(e: AnActionEvent) {
        val baseDir: @SystemIndependent @NonNls String? = e.project!!.basePath
//        val virtualFile: VirtualFile? = e.getData(CommonDataKeys.VIRTUAL_FILE)
        setProject(e.project)
        getConsoleView(e.project).clear()
        baseDir?.let {



            val mainPath = "$it${separator}src${separator}main"
            val tomlPath = "$it${separator}cheese.toml"
            val buildPath = "$it${separator}build"
            val node_modules = "$it${separator}node_modules"
//            val jsonString = File(tomlPath).readText()
//            val person = Json.decodeFromString<Config>(jsonString)

            val result: TomlParseResult = Toml.parse(Paths.get(tomlPath))
            result.errors().forEach(Consumer { error: TomlParseError ->
                System.err.println(
                    error.toString()
                )
            })



            val ts = result.getBoolean("ts")
            Thread{
                if (ts!!){
                    val command = "tsc" // 替换为你想要执行的命令
                    executeCommand(command, File(it)) { output ->
                        println(output)
                    }
                    copyDirectory("$buildPath${separator}js", "$buildPath${separator}debug${separator}main${separator}js")
                }else{
                    copyDirectory("$mainPath${separator}js", "$buildPath${separator}debug${separator}main${separator}js")
                }
                copyDirectory("$mainPath${separator}language", "$buildPath${separator}debug${separator}main${separator}language")
//                copyDirectory("$mainPath${separator}py", "$buildPath${separator}debug${separator}main${separator}py")
//                copyDirectory("$mainPath${separator}java", "$buildPath${separator}debug${separator}main${separator}java")
//                copyDirectory("$mainPath${separator}kt", "$buildPath${separator}debug${separator}main${separator}kt")
                copyDirectory(mainPath + "${separator}assets",buildPath + "${separator}debug${separator}main${separator}assets")
                copyDirectory(node_modules,buildPath + "${separator}debug${separator}node_modules")
                copyFile(tomlPath,buildPath + "${separator}debug${separator}cheese.toml")
                zipDirectory(buildPath + "${separator}debug",buildPath + "${separator}debug.zip")
                glDownload= "$buildPath${separator}debug.zip"
                instruction=1
            }.start()


        }


        // 或者使用日志记录
        // Logger.getInstance(Run::class.java).info("运行Cheese")
    }
}