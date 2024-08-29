package coco.cheese.ide


import coco.cheese.ide.console.ConsoleExecutor
import coco.cheese.ide.infrastructure.DataSetting
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class Application : ProjectActivity {

    override suspend fun execute(project: Project) {
        println("Application started")

        val executor = ConsoleExecutor(project)
        ApplicationManager.getApplication().invokeLater {
            executor.withReturn { runExecutor(project) }
                .withStop({ setRunning(project, false) }) { getRunning(project) }
            executor.run()
        }
    }
    fun runExecutor(project: Project?) {
        if (project == null) {
            return
        }
        val executor = ConsoleExecutor(project)
        // 设置restart和stop
        executor.withReturn { runExecutor(project) }.withStop(
            { setRunning(project, false) },
            { getRunning(project) })
        executor.run()
    }
    fun setRunning(project: Project?, value: Boolean) {
        PropertiesComponent.getInstance(project!!).setValue(Env.RUNNING_KEY, value)
    }

    fun getRunning(project: Project?): Boolean {
        return PropertiesComponent.getInstance(project!!).getBoolean(Env.RUNNING_KEY)
    }
}

