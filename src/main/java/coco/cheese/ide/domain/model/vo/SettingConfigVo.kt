package coco.cheese.ide.domain.model.vo

import coco.cheese.ide.data.SettingConfig
import coco.cheese.ide.swing.gui.ConfigSettingUi
import coco.cheese.ide.utils.StorageUtils
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.util.NlsContexts
import com.intellij.platform.diagnostic.telemetry.Storage
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import javax.swing.JComponent

class SettingConfigVo: SearchableConfigurable {
    private val form = ConfigSettingUi()

    @NotNull
    @NonNls
    override fun getId(): String {
        return SettingConfig.SETTING_ID
    }

    @NlsContexts.ConfigurableName
    override fun getDisplayName(): String {
        return SettingConfig.SETTING_NAME
    }

    @Nullable
    override fun createComponent(): JComponent? {
        form.home.text = StorageUtils.getString(SettingConfig.CHEESE_HOME)
        form.esp.text = StorageUtils.getString(SettingConfig.CHEESE_ESP)
        form.port.text = StorageUtils.getString(SettingConfig.CHEESE_PORT)
        form.sdk.text = StorageUtils.getString(SettingConfig.ANDROID_SDK)

        if (StorageUtils.getString(SettingConfig.BUILD).isNullOrEmpty()){
            form.build.selectedItem="关闭"
        }else{
            form.build.selectedItem = StorageUtils.getString(SettingConfig.BUILD)
        }

        return form.component
    }

    override fun isModified(): Boolean {
        return true
    }

    override fun apply() {
        StorageUtils.save(SettingConfig.CHEESE_HOME, form.home.text)
        StorageUtils.save(SettingConfig.CHEESE_ESP, form.esp.text)
        StorageUtils.save(SettingConfig.CHEESE_PORT, form.port.text)
        StorageUtils.save(SettingConfig.ANDROID_SDK, form.sdk.text)
        StorageUtils.save(SettingConfig.BUILD, form.build.selectedItem)
    }
}