package cn.butterfly.sqllog.render

import cn.butterfly.sqllog.constant.Constants
import coco.cheese.ide.utils.IconsUtils
import coco.cheese.ide.utils.ToastUtils

import com.intellij.codeInsight.hints.presentation.InputHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseEvent


/**
 * MyBatis SQL 渲染处理
 *
 * @author zjw
 * @date 2024-06-19
 */
class MybatisSQLRenderer(private val editor: Editor, private val offset: Int): EditorCustomElementRenderer, InputHandler {

    /**
     * 用于判断鼠标是否在渲染的图标上停留
     */
    private var hovering = false
    
    override fun mouseMoved(event: MouseEvent, translated: Point) {
        if (hovering) return
        (editor as EditorImpl).setCustomCursor(this, Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
        hovering = true
    }

    override fun mouseExited() {
        hovering = false
        (editor as EditorImpl).setCustomCursor(this, null)
    }

    override fun mouseClicked(event: MouseEvent, translated: Point) {
        // 处理 SQL 日志和参数信息并将拼接结果保存到粘贴板中
        val doc = editor.document
        val sqlLineNumber = doc.getLineNumber(offset)
        val paramsLineNumber = sqlLineNumber + 1
        var sqlStr = getTextByLineNumber(sqlLineNumber)
        val paramsStr = getTextByLineNumber(paramsLineNumber)
        paramsStr.split(",").map {
            val param = it.trim()
            val value = param.substring(0, param.indexOf('('))
            val type = param.substring(param.indexOf('(') + 1, param.indexOf(')'))
            if (type == "String") "'$value'" else value
        }.forEach {
            sqlStr = sqlStr.replaceFirst("?", it)
        }
        val stringSelection = StringSelection(sqlStr)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(stringSelection, null)
        ToastUtils.info("复制成功")
    }

    /**
     * 获取指定行的日志信息
     */
    private fun getTextByLineNumber(lineNumber: Int): String {
        val doc = editor.document
        val start = doc.getLineStartOffset(lineNumber)
        val end = doc.getLineEndOffset(lineNumber)
        return doc.getText(TextRange(start, end)).substring(Constants.PARAMS_PREFIX.length)
    }

    override fun calcWidthInPixels(p0: Inlay<*>): Int = IconsUtils.getImage("mybatis.svg").iconWidth

    override fun calcHeightInPixels(inlay: Inlay<*>) =IconsUtils.getImage("mybatis.svg").iconHeight
    
    override fun paint(inlay: Inlay<*>, g: Graphics, r: Rectangle, textAttributes: TextAttributes) {
        val consoleIcon = IconsUtils.getImage("wifi.svg")
        val curX = r.x + r.width / 2 - consoleIcon.iconWidth / 2
        val curY = r.y + r.height / 2 - consoleIcon.iconHeight / 2
        if (curX >= 0 && curY >= 0) {
            consoleIcon.paintIcon(inlay.editor.component, g, curX, curY)
        }
    }

}