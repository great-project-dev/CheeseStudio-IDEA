package net.codeocean.cheese.action.epm



import coco.cheese.core.sendFile
import net.codeocean.cheese.Env
import net.codeocean.cheese.data.SettingConfig
import net.codeocean.cheese.utils.FileUtils
import net.codeocean.cheese.utils.StorageUtils
import net.codeocean.cheese.utils.TerminalUtils.execute
import net.codeocean.cheese.utils.ToastUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.SystemIndependent
import java.io.File
import java.io.File.separator
import javax.swing.Icon

class UpdateProject(name: String, description: String,icon: Icon) : AnAction(name, description, icon) {
    override fun actionPerformed(e: AnActionEvent) {
        if(StorageUtils.getString(SettingConfig.CHEESE_HOME).isNullOrEmpty()){
            ToastUtils.error("CHEESE_SDK 环境路径未设置 无法更新项目.")
            return
        }
        val baseDir: @SystemIndependent @NonNls String? = e.project!!.basePath
        baseDir?.let {
            val CORE_JAR_PATH=File(File(StorageUtils.getString(SettingConfig.CHEESE_HOME),"lib"),"core.jar")
            val JDK_PATH=File(File(StorageUtils.getString(SettingConfig.CHEESE_HOME),"jbr"),"bin")
            val SDK_PATH= File(StorageUtils.getString(SettingConfig.CHEESE_HOME))
            execute(
                command = "java -Dfile.encoding=GBK -jar $CORE_JAR_PATH -updateProject -sdkPath ${SDK_PATH} -baseDir ${baseDir}",  // 执行的命令
                options = mapOf("workingDir" to JDK_PATH.absolutePath),
                onData = { output ->  // 处理输出
                    println(">>>$output")
                },
                onComplete = { exitCode ->
                    if (exitCode == 0) {
                        ToastUtils.info("更新项目完毕")
                    }else {
                        ToastUtils.info("更新项目失败，退出码: ${exitCode}")
                    }
                }
            )

        }


    }
}