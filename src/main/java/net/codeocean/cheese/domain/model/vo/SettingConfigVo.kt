package net.codeocean.cheese.domain.model.vo


import net.codeocean.cheese.Env.DEFAULT_HOME_PATH
import net.codeocean.cheese.Env.form

import net.codeocean.cheese.data.SettingConfig
import net.codeocean.cheese.ui.ConfigSettingUi
import net.codeocean.cheese.utils.StorageUtils
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.util.NlsContexts
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.awt.Desktop
import java.io.File
import java.net.URI
import javax.swing.*


class SettingConfigVo : SearchableConfigurable {


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
        if (form.home.text.isNullOrEmpty()) {
            form.home.text = DEFAULT_HOME_PATH.absolutePath
            StorageUtils.save(SettingConfig.CHEESE_HOME, form.home.text)
        }
        form.gitHubProxy.text = StorageUtils.getString(SettingConfig.PROXY)
        StorageUtils.save(SettingConfig.PROXY, form.gitHubProxy.text)

        form.port.text = StorageUtils.getString(SettingConfig.CHEESE_PORT)
        if (form.port.text.isNullOrEmpty()) {
            form.port.text = "8080"
            StorageUtils.save(SettingConfig.CHEESE_PORT, form.port.text)
        }



        if (StorageUtils.getString(SettingConfig.BUILD).isNullOrEmpty()) {
            form.build.selectedItem = "关闭"
            StorageUtils.save(SettingConfig.BUILD, form.build.selectedItem)
        } else {
            form.build.selectedItem = StorageUtils.getString(SettingConfig.BUILD)
        }

        return form.component
    }


    override fun isModified(): Boolean {
        return true
    }

    override fun apply() {
        StorageUtils.save(SettingConfig.CHEESE_HOME, form.home.text)
        StorageUtils.save(SettingConfig.PROXY, form.gitHubProxy.text)
        StorageUtils.save(SettingConfig.CHEESE_PORT, form.port.text)
        StorageUtils.save(SettingConfig.BUILD, form.build.selectedItem)
    }
}



