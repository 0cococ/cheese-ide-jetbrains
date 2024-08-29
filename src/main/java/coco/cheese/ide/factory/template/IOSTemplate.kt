package coco.cheese.ide.factory.template


import coco.cheese.ide.module.CModuleBuilder
import coco.cheese.ide.utils.IconsUtils
import com.intellij.ide.util.projectWizard.AbstractModuleBuilder
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.NlsContexts
import com.intellij.platform.ProjectTemplate
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import javax.swing.Icon

class IOSTemplate : ProjectTemplate {

     override fun getName(): @NotNull @NlsContexts.Label String {
         return "IOS Project"
     }

     override fun getDescription(): @Nullable @NlsContexts.DetailedDescription String? {
         return "使用Cheese来开发IOS自动化测试"
     }

     override fun getIcon(): Icon {
         return IconsUtils.getImage("ios.svg") // 返回实际的图标
     }

     override fun createModuleBuilder(): @NotNull AbstractModuleBuilder {
         return CModuleBuilder()
     }

     @Deprecated("unused API", ReplaceWith("null"))
     override fun validateSettings(): ValidationInfo? {
         return null // 如果设置有效，返回 null；否则返回错误信息
     }
 }