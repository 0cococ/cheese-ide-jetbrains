package coco.cheese.ide.project

import coco.cheese.ide.Env.OS.separator
import coco.cheese.ide.domain.model.vo.ProjectConfigVO

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import freemarker.template.TemplateException

import java.io.File
import java.io.IOException
import java.io.StringWriter


abstract class AbstractProjectGenerator : FreemarkerConfiguration(), IProjectGenerator {

    override fun doGenerator(project: Project, entryPath: String, projectConfig: ProjectConfigVO) {
        generateProjectConfig(project, entryPath, projectConfig)
        generateProjectCode(project, entryPath, projectConfig)
    }

    protected abstract fun generateProjectConfig(project: Project, entryPath: String, projectConfig: ProjectConfigVO)
    protected abstract fun generateProjectCode(project: Project, entryPath: String, projectConfig: ProjectConfigVO)
    fun writeFile(project: Project, packageName: String, entryPath: String, name: String, ftl: String, dataModel: Any) {
        var virtualFile: VirtualFile? = null
        try {
            virtualFile = createPackageDir(packageName, entryPath).createChildData(project, name)
            val stringWriter = StringWriter()
            val template = super.getTemplate(ftl)
            template.process(dataModel, stringWriter)
            virtualFile.setBinaryContent(stringWriter.toString().toByteArray(charset("UTF-8")))
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: TemplateException) {
            e.printStackTrace()
        }
    }

    private fun createPackageDir(packageName: String, entryPath: String): VirtualFile {
        val path = FileUtil.toSystemIndependentName("$entryPath${separator}${StringUtil.replace(packageName, ".", separator)}")
        File(path).mkdirs()
        return LocalFileSystem.getInstance().refreshAndFindFileByPath(path)!!
    }
}
