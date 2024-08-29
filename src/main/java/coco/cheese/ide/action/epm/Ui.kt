package coco.cheese.ide.action.epm


import coco.cheese.ide.Env.OS.separator
import coco.cheese.ide.InteractionServer.glDownload
import coco.cheese.ide.InteractionServer.instruction
import coco.cheese.ide.console.ConsoleExecutor.Companion.printToConsole
import coco.cheese.ide.console.ConsoleExecutor.Companion.setProject
import coco.cheese.ide.data.SettingConfig
import coco.cheese.ide.utils.*
import coco.cheese.ide.utils.FileUtils.convertPath
import coco.cheese.ide.utils.FileUtils.copyDirectory
import coco.cheese.ide.utils.FileUtils.copyFile
import coco.cheese.ide.utils.FileUtils.zipDirectory
import coco.cheese.ide.utils.TerminalUtils.executeCommand
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
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

class Ui(name: String, description: String,icon: Icon) : AnAction(name, description, icon) {

    override fun actionPerformed(e: AnActionEvent) {
        val baseDir: @SystemIndependent @NonNls String? = e.project!!.basePath
        setProject(e.project)
        baseDir?.let {
            val mainPath="$it${separator}src${separator}main"
            val tomlPath="$it${separator}cheese.toml"
            val buildPath="$it${separator}build"
            val node_modules ="$it${separator}node_modules"
//            val jsonString = File(tomlPath).readText()
//            val person = Json.decodeFromString<Config>(jsonString)

            val result: TomlParseResult = Toml.parse(Paths.get(tomlPath))
            result.errors().forEach(Consumer { error: TomlParseError ->
                System.err.println(
                    error.toString()
                )
            })

            val ui = result.getString("ui")
            val ts = result.getBoolean("ts")
            Thread{
//                if (ts!!){
//                    val command = "tsc" // 替换为你想要执行的命令
//                    executeCommand(command, File(it)) { output ->
//                        println(output)
//                    }
//                    copyDirectory("$buildPath${separator}js", "$buildPath${separator}debug${separator}main${separator}js")
//                }else{
//                    copyDirectory("$mainPath${separator}js", "$buildPath${separator}debug${separator}main${separator}js")
//                }
                if (ui=="xml"){
                    copyDirectory("$mainPath${separator}ui", "$buildPath${separator}debug${separator}main${separator}ui")
                }else{
                    executeCommand("npm run build",File(convertPath("$mainPath${separator}ui"))) { output ->
                        printToConsole(e.project,output, ConsoleViewContentType.USER_INPUT)
                    }
                    copyDirectory(convertPath("$mainPath${separator}ui/dist"), "$buildPath${separator}debug${separator}main${separator}ui")

                }
//                copyDirectory(mainPath + "${separator}assets",buildPath + "${separator}debug${separator}main${separator}assets")
//                copyDirectory(node_modules,buildPath + "${separator}debug${separator}node_modules")
//                copyDirectory(mainPath + "${separator}res",buildPath + "${separator}debug${separator}main${separator}res")
                copyFile(tomlPath,buildPath + "${separator}debug${separator}cheese.toml")
                zipDirectory(buildPath + "${separator}debug",buildPath + "${separator}debug.zip")
                glDownload = "$buildPath${separator}debug.zip"
                instruction =2
            }.start()


        }

    }
}