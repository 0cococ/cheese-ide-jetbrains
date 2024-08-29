package coco.cheese.ide.module


import coco.cheese.ide.Env.getDataSetting
import coco.cheese.ide.domain.model.vo.ProjectConfigVO
import coco.cheese.ide.infrastructure.DataSetting
import coco.cheese.ide.swing.gui.ProjectConfigUI

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import javax.swing.JComponent

class CModuleConfigStep: ModuleWizardStep() {
    private var projectConfigUI: ProjectConfigUI

    init {
        // 在构造方法中初始化一些东西
        this.projectConfigUI = ProjectConfigUI()
    }

    override fun getComponent(): JComponent {
        return  projectConfigUI.component;
    }

    override fun updateDataModel() {
        // todo: 根据 UI 更新模型
    }


    override fun validate(): Boolean {
        // 获取配置信息，写入到 DataSetting
        val projectConfig: ProjectConfigVO = getDataSetting()!!.getInstance().getProjectConfig()
        projectConfig.setProjectname(projectConfigUI.name.text)
        projectConfig.setPkg(projectConfigUI.pkg.text)
        projectConfig.setType(projectConfigUI.type.selectedItem as String)

      val b= projectConfig.getType()?.split("/")
        projectConfig.setTs(b?.get(0).equals("ts"))
        projectConfig.setUi(b?.get(1))
        projectConfig.setEnd()

        return super.validate()
    }
}