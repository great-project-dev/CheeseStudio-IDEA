package net.codeocean.cheese.action.epm

import coco.cheese.core.sendMessage
import net.codeocean.cheese.Env
import net.codeocean.cheese.console.ConsoleExecutor.Companion.printToConsole
import net.codeocean.cheese.data.SettingConfig
import net.codeocean.cheese.utils.StorageUtils
import net.codeocean.cheese.utils.TerminalUtils.execute
import net.codeocean.cheese.utils.TerminalUtils.execute1
import net.codeocean.cheese.utils.ToastUtils
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import java.io.File
import javax.swing.Icon
import kotlin.concurrent.thread

class Tools (name: String, description: String,icon: Icon) : AnAction(name, description, icon) {

    override fun actionPerformed(e: AnActionEvent) {

        if (!Env.cd){
            ToastUtils.error("请先启动心跳，再启动抓抓。")
            return
        }
        sendMessage("工具")


    }
}