package coco.cheese.ide.project
import coco.cheese.ide.Env.OS.separator
import coco.cheese.ide.console.ConsoleExecutor.Companion.printToConsole
import coco.cheese.ide.data.SettingConfig
import coco.cheese.ide.domain.model.vo.ProjectConfigVO
import coco.cheese.ide.utils.FileUtils
import coco.cheese.ide.utils.FileUtils.convertPath
import coco.cheese.ide.utils.StorageUtils
import coco.cheese.ide.utils.ToastUtils
import com.intellij.execution.ui.ConsoleViewContentType


import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project

class ProjectGeneratorImpl : AbstractProjectGenerator() {

    override fun generateProjectConfig(project: Project, entryPath: String, projectConfig: ProjectConfigVO) {
        writeFile(project, separator, entryPath, "cheese.toml", "cheese.ftl", projectConfig)
        val type = projectConfig.getType()
        when  {
            type!!.contains("ts")  -> {
                writeFile(project, separator, entryPath, "tsconfig.json", "tsconfig.ftl", projectConfig)
            }
            type!!.contains("js")  -> {
                writeFile(project, separator, entryPath, "jsconfig.json", "jsconfig.ftl", projectConfig)
            }
            else -> println("Unknown type")
        }
    }

    override fun generateProjectCode(project: Project, entryPath: String, projectConfig: ProjectConfigVO) {

        val project_path="${StorageUtils.getString(SettingConfig.CHEESE_HOME)}${separator}project${separator}template${separator}script"
        val node_modules="${StorageUtils.getString(SettingConfig.CHEESE_HOME)}${separator}project${separator}node_modules"
        val package_json="${StorageUtils.getString(SettingConfig.CHEESE_HOME)}${separator}project${separator}template${separator}script${separator}package.json"

        when (projectConfig.getType()) {
            "ts/xml" -> {
                FileUtils.copyDirectory("$project_path${separator}xml",entryPath)
                FileUtils.copyDirectory(node_modules,entryPath+separator+"node_modules")
                FileUtils.copyFile(package_json,entryPath+"${separator}package.json")
                writeFile(project, "${separator}src${separator}main${separator}ts", entryPath, "main.ts", "main.ftl", projectConfig)
                writeFile(project, "${separator}src${separator}main${separator}ts", entryPath, "ui.ts", "ui_xml.ftl", projectConfig)
            }
            "js/xml" -> {
                FileUtils.copyDirectory(project_path+"${separator}xml",entryPath)
                FileUtils.copyDirectory(node_modules,entryPath+separator+"node_modules")
                FileUtils.copyFile(package_json,entryPath+"${separator}package.json")
                writeFile(project, "${separator}src${separator}main${separator}js", entryPath, "main.js", "main.ftl", projectConfig)
                writeFile(project, "${separator}src${separator}main${separator}ts", entryPath, "ui.js", "ui_xml.ftl", projectConfig)
            }
            "ts/vue" -> {
                FileUtils.copyDirectory(project_path+"${separator}vue",entryPath)
                FileUtils.copyDirectory(node_modules,entryPath+separator+"node_modules")
                FileUtils.copyFile(package_json,entryPath+"${separator}package.json")
                writeFile(project, "${separator}src${separator}main${separator}ts", entryPath, "main.ts", "main.ftl", projectConfig)
                writeFile(project, "${separator}src${separator}main${separator}ts", entryPath, "ui.ts", "ui_vue.ftl", projectConfig)
                printToConsole(project,"""
                Done. Now run:
                
                   cd  ${convertPath(entryPath)}${separator}src${separator}main${separator}ui
                   npm install
                   npm run dev
                   
                Done
                    """.trimIndent(), ConsoleViewContentType.SYSTEM_OUTPUT)

            }
            "js/vue"-> {
                ApplicationManager.getApplication().invokeLater {
                    FileUtils.copyDirectory(project_path+"${separator}vue",entryPath)
                    FileUtils.copyDirectory(node_modules,entryPath+separator+"node_modules")
                    FileUtils.copyFile(package_json,entryPath+"${separator}package.json")
                    writeFile(project, "${separator}src${separator}main${separator}js", entryPath, "main.js", "main.ftl", projectConfig)
                    writeFile(project, "${separator}src${separator}main${separator}js", entryPath, "ui.js", "ui_vue.ftl", projectConfig)
                }
                printToConsole(project,"""
                Done. Now run:
                
                   cd  ${convertPath(entryPath)}${separator}src${separator}main${separator}ui
                   npm install
                   npm run dev
                   
                Done
                    """.trimIndent(), ConsoleViewContentType.SYSTEM_OUTPUT)

            }
            else -> println("Unknown type")
        }
    }
}
