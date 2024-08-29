package coco.cheese.ide.utils

import com.intellij.openapi.util.IconLoader
import java.awt.Image
import javax.swing.Icon
import javax.swing.ImageIcon

class IconsUtils {
    private fun load(path: String): Icon {
        return IconLoader.getIcon(path,  IconsUtils::class.java)
    }


    companion object {
        val ICONS = IconsUtils()
        fun getImage(name:String): Icon {
            return ICONS.load("/icons/${name}")
        }

        fun getImage(name: String, width: Int, height: Int): Icon {
            // 使用 IconLoader.getIcon 获取图标，而不是 ICONS.load
            val originalIcon = IconLoader.getIcon("/icons/${name}", ICONS::class.java)
            // 将 Icon 转换为 Image
            val image = IconLoader.toImage(originalIcon)?.getScaledInstance(width, height, Image.SCALE_SMOOTH)
            // 使用转换后的 Image 创建一个新的 ImageIcon 对象
            println(image)
            return ImageIcon(image)
        }

    }

}