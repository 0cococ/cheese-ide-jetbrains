package coco.cheese.ide.action.epm.yolo

import coco.cheese.ide.action.epm.Run
import coco.cheese.ide.action.epm.Wifi
import coco.cheese.ide.utils.IconsUtils
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent


class Yolo : ActionGroup("Yolo工具", "Yolo工具合集", IconsUtils.getImage("yolo.svg")) {
    init {
        isPopup = true
    }

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {

        return arrayOf(
            Training("训练数据集","", IconsUtils.getImage("yolo/training.svg")),

        )
    }


}

