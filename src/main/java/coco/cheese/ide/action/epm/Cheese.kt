package coco.cheese.ide.action.epm

import coco.cheese.ide.action.epm.yolo.Yolo
import coco.cheese.ide.utils.IconsUtils

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup

class Cheese : DefaultActionGroup("Cheese", "Cheese工具集合", IconsUtils.getImage("cheese.svg")) {

    init {
        addAction(Yolo())
        addSeparator()
    }

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        return arrayOf(
            Wifi("启动心跳", "", IconsUtils.getImage("wifi.svg")),
            Run("运行", "", IconsUtils.getImage("run.svg")),
            Runx("运行选中", "", IconsUtils.getImage("runx.svg")),
            Ui("预览Ui", "", IconsUtils.getImage("ui.svg")),
            Stop("停止", "", IconsUtils.getImage("stop.svg")),
            Build("构建", "", IconsUtils.getImage("build.svg")),
            Docs("文档", "", IconsUtils.getImage("doc.svg")),
            UpdateProject("更新项目", "", IconsUtils.getImage("up.svg")),
            Yolo()
        )
    }


}