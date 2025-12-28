package net.codeocean.cheese.action.epm

import coco.cheese.core.sendMessage
import net.codeocean.cheese.Env
import net.codeocean.cheese.console.ConsoleExecutor.Companion.getConsoleView
import net.codeocean.cheese.console.ConsoleExecutor.Companion.printToConsole
import net.codeocean.cheese.console.ConsoleExecutor.Companion.setProject
import net.codeocean.cheese.data.SettingConfig
import net.codeocean.cheese.utils.StorageUtils
import net.codeocean.cheese.utils.TerminalUtils.execute
import net.codeocean.cheese.utils.ToastUtils
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.SystemIndependent
import java.io.File
import java.io.File.separator
import javax.swing.Icon

class HOT(name: String, description: String,icon: Icon)  :  AnAction(name, description, icon){

    override fun actionPerformed(e: AnActionEvent) {
        val baseDir: @SystemIndependent @NonNls String? = e.project!!.basePath
//        val virtualFile: VirtualFile? = e.getData(CommonDataKeys.VIRTUAL_FILE)
        setProject(e.project)
        getConsoleView(e.project).clear()


//        if (!Env.cd){
//            ToastUtils.error("请先启动心跳，再运行代码。")
//            return
//        }

        if (baseDir!=null) {
            val CORE_JAR_PATH= File(File(StorageUtils.getString(SettingConfig.CHEESE_HOME),"lib"),"core.jar")
            val JDK_PATH= File(File(StorageUtils.getString(SettingConfig.CHEESE_HOME),"jbr"),"bin")
            val SDK_PATH= File(StorageUtils.getString(SettingConfig.CHEESE_HOME))
            Thread{
                printToConsole(e.project,"整理和编译代码并进行压缩。", ConsoleViewContentType.USER_INPUT)
                execute(
                    command = "java -Dfile.encoding=GBK -jar $CORE_JAR_PATH -hot -baseDir ${baseDir} -sdkPath ${SDK_PATH}",  // 执行的命令
                    options = mapOf("workingDir" to JDK_PATH.absolutePath),
                    onData = { output ->  // 处理输出
                        println(output)
                    },
                    onComplete = { exitCode ->
                        if (exitCode == 0) {
                            printToConsole(e.project,"生成热更新包成功："+baseDir+separator+"build"+separator+"cheese.hot", ConsoleViewContentType.USER_INPUT)

                        }else {
                            ToastUtils.info("生成热更新包失败，退出码: ${exitCode}")
                        }

                    }
                )
            }.start()

        }

    }
}