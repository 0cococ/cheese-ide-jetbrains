package coco.cheese.ide.fileicon

import coco.cheese.ide.utils.IconsUtils
import com.intellij.ide.FileIconProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

class TomlIcon : FileIconProvider {
    override fun getIcon(file: VirtualFile, flags: Int, project: Project?): Icon? = when (file.name) {
        "cheese.toml" ->IconsUtils.getImage("cheeses.svg")
        else -> null
    }
}
