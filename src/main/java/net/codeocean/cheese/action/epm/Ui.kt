package net.codeocean.cheese.action.epm


import coco.cheese.core.sendFile
import coco.cheese.core.sendMessage
import net.codeocean.cheese.Env
import net.codeocean.cheese.console.ConsoleExecutor.Companion.printToConsole


import net.codeocean.cheese.console.ConsoleExecutor.Companion.setProject
import net.codeocean.cheese.data.SettingConfig
import net.codeocean.cheese.utils.*

import net.codeocean.cheese.utils.TerminalUtils.execute
import net.codeocean.cheese.utils.TerminalUtils.executeCommand
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.SystemIndependent
import org.tomlj.Toml
import org.tomlj.TomlParseError
import org.tomlj.TomlParseResult
import java.io.File
import java.io.File.separator
import java.nio.file.Paths
import java.util.function.Consumer
import javax.swing.Icon

class Ui(name: String, description: String,icon: Icon) : AnAction(name, description, icon) {

    override fun actionPerformed(e: AnActionEvent) {
        val baseDir: @SystemIndependent @NonNls String? = e.project!!.basePath
        setProject(e.project)

        if (!Env.cd){
            ToastUtils.error("请先启动心跳，再预览Ui。")
            return
        }
        if (baseDir!=null) {
            val CORE_JAR_PATH=File(File(StorageUtils.getString(SettingConfig.CHEESE_HOME),"lib"),"core.jar")
            val JDK_PATH=File(File(StorageUtils.getString(SettingConfig.CHEESE_HOME),"jbr"),"bin")
            val SDK_PATH= File(StorageUtils.getString(SettingConfig.CHEESE_HOME))

         Thread{
             printToConsole(e.project,"整理和编译Ui并进行压缩。",ConsoleViewContentType.USER_INPUT)
             execute(
                 command = "java -Dfile.encoding=GBK -jar $CORE_JAR_PATH -runUi -baseDir ${baseDir} -sdkPath ${SDK_PATH}",  // 执行的命令
                 options = mapOf("workingDir" to JDK_PATH.absolutePath),
                 onData = { output ->  // 处理输出
                     printToConsole(e.project,output,ConsoleViewContentType.USER_INPUT)
                 },
                 onComplete = { exitCode ->
                     if (exitCode == 0) {
                         printToConsole(e.project,"下发Ui并通知设备预览。",ConsoleViewContentType.USER_INPUT)
                         sendMessage("http|2|${baseDir+separator+"build"+separator+"debug.zip"}")
                     }else {
                         ToastUtils.info("运行代码失败，退出码: ${exitCode}")
                     }

                 }
             )
         }.start()

        }
    }
}