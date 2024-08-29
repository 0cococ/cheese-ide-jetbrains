package coco.cheese.ide.module






import coco.cheese.ide.Env.getDataSetting
import coco.cheese.ide.data.SettingConfig
import coco.cheese.ide.factory.CModuleType
import coco.cheese.ide.infrastructure.DataSetting
import coco.cheese.ide.project.IProjectGenerator
import coco.cheese.ide.project.ProjectGeneratorImpl
import coco.cheese.ide.utils.StorageUtils
import coco.cheese.ide.utils.ToastUtils
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.DumbAwareRunnable
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.util.DisposeAwareRunnable
import java.io.File


class CModuleBuilder : ModuleBuilder() {
    private val projectGenerator: IProjectGenerator =
        ProjectGeneratorImpl()


//    override fun getNodeIcon(): Icon {
//        return ICONS.SPRING_BOOT
//    }

    override fun getModuleType(): ModuleType<out ModuleBuilder> {
        return CModuleType.getInstance()
    }

//    override fun getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep {
//        return CModuleConfigStep()
//    }

    override fun getBuilderId(): String? {
        return javaClass.name
    }

    override fun modifySettingsStep(settingsStep: SettingsStep): ModuleWizardStep? {
        val moduleNameLocationSettings = settingsStep.moduleNameLocationSettings
        val artifactId = getDataSetting()!!.getInstance().getProjectConfig().getProjectname()
        if (null != moduleNameLocationSettings && !StringUtil.isEmptyOrSpaces(artifactId)) {
            moduleNameLocationSettings.moduleName = artifactId!!
        }
        return super.modifySettingsStep(settingsStep)
    }
    override fun setupRootModel(model: ModifiableRootModel) {
            println(StorageUtils.getString(SettingConfig.CHEESE_HOME))
        if(StorageUtils.getString(SettingConfig.CHEESE_HOME).isNullOrEmpty()){
            ToastUtils.error("CHEESE_HOME 环境路径未设置 无法创建项目")
            return
        }

        if (null != this.myJdk) {
            model.sdk = this.myJdk
        } else {
            model.inheritSdk()
        }
        // 生成工程路径
        val path = FileUtil.toSystemIndependentName(contentEntryPath!!)
        File(path).mkdirs()
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(path)
        model.addContentEntry(virtualFile!!)
        val project: Project = model.getProject()
        val r: Runnable = DumbAwareRunnable {
            WriteCommandAction.runWriteCommandAction(
                project
            ) {
                try {
                    projectGenerator.doGenerator(
                        project,
                        contentEntryPath,
                        getDataSetting()!!.getInstance().getProjectConfig()
                    )
                } catch (throwable: Throwable) {
                    throwable.printStackTrace()
                }
            }
        }


        if (ApplicationManager.getApplication().isUnitTestMode
            || ApplicationManager.getApplication().isHeadlessEnvironment
        ) {
            r.run()
            return
        }

        if (!project.isInitialized) {
            StartupManager.getInstance(project).registerPostStartupActivity(DisposeAwareRunnable.create(r, project))
            return
        }

        if (DumbService.isDumbAware(r)) {
            r.run()
        } else {
            DumbService.getInstance(project).runWhenSmart(DisposeAwareRunnable.create(r, project))
        }


    }

    override fun createWizardSteps(
        wizardContext: WizardContext,
        modulesProvider: ModulesProvider
    ): Array<ModuleWizardStep> {
        // 添加工程配置步骤，可以自己定义需要的步骤，如果有多个可以依次添加


        return arrayOf(CModuleConfigStep())
    }

}