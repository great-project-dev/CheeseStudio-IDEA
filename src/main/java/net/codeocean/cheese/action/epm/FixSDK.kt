package net.codeocean.cheese.action.epm

import net.codeocean.cheese.console.ConsoleExecutor.Companion.setProject
import net.codeocean.cheese.data.SettingConfig
import net.codeocean.cheese.manager.SDKManager
import net.codeocean.cheese.utils.StorageUtils
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import java.io.File
import javax.swing.Icon

class FixSDK (name: String, description: String,icon: Icon)  :  AnAction(name, description, icon){


    override fun actionPerformed(e: AnActionEvent) {
        setProject(e.project)
        val CORE_JAR_PATH = File(File(StorageUtils.getString(SettingConfig.CHEESE_HOME), "lib"), "core.jar")
        val JDK_PATH = File(File(StorageUtils.getString(SettingConfig.CHEESE_HOME), "jbr"), "bin")
        val SDK_PATH = File(StorageUtils.getString(SettingConfig.CHEESE_HOME))

        SDKManager(e.project!!).update(true,SDK_PATH.absolutePath){}


    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        // 指定在 EDT（事件调度线程）上执行
        return ActionUpdateThread.EDT
    }

}



