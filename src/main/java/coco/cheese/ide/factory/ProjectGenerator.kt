package coco.cheese.ide.factory

import coco.cheese.ide.utils.IconsUtils
import com.intellij.facet.ui.ValidationResult
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.platform.ProjectGeneratorPeer
import com.intellij.platform.ProjectTemplatesFactory
import java.awt.GridLayout
import javax.swing.*
data class MySettings(
    val projectName: String,
    val language: String
)

class ProjectGenerator : DirectoryProjectGenerator<MySettings> {

    override fun getName(): @NlsContexts.Label String {
        return "Cheese项目"
    }

    override fun getLogo(): Icon {
        return IconsUtils.getImage("cheese.svg") // 提供一个图标
    }

    override fun validate(baseDirPath: String): ValidationResult {
        return ValidationResult.OK // 你可以根据需要验证 baseDirPath
    }

    override fun createPeer(): ProjectGeneratorPeer<MySettings> {
        return object : ProjectGeneratorPeer<MySettings> {
            private val projectNameField = JTextField()
            private val languageComboBox = JComboBox(arrayOf("Kotlin", "Java", "Python"))
            override fun getComponent(): JComponent {
                val panel = JPanel(GridLayout(2, 2))
                panel.add(JLabel("Project Name:"))
                panel.add(projectNameField)
                panel.add(JLabel("Language:"))
                panel.add(languageComboBox)
                return panel
            }
            override fun buildUI(settingsStep: SettingsStep) {
                settingsStep.addSettingsComponent(getComponent())
            }
            override fun getSettings(): MySettings {
                return MySettings(
                    projectName = projectNameField.text,
                    language = languageComboBox.selectedItem as String
                )
            }
            override fun validate(): ValidationInfo? {
                return if (projectNameField.text.isBlank()) {
                    ValidationInfo("Project name cannot be empty", projectNameField)
                } else null
            }

            override fun isBackgroundJobRunning(): Boolean {
                return false
            }
        }
    }

    override fun generateProject(
        project: Project,
        baseDir: VirtualFile,
        settings: MySettings,
        module: Module
    ) {
        val projectName = settings.projectName
        val language = settings.language

        // 根据 settings 生成项目的逻辑，例如创建文件、目录、配置等
        baseDir.createChildDirectory(this, projectName)
        // 根据需要创建文件和配置
    }
}