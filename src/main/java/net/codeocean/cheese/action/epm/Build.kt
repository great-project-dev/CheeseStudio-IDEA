package net.codeocean.cheese.action.epm


import net.codeocean.cheese.console.ConsoleExecutor.Companion.printToConsole
import net.codeocean.cheese.console.ConsoleExecutor.Companion.setProject
import net.codeocean.cheese.data.SettingConfig
import net.codeocean.cheese.utils.*
import net.codeocean.cheese.utils.TerminalUtils.execute
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.SystemIndependent
import org.tomlj.TomlParseResult
import org.w3c.dom.Element
import java.io.File
import java.io.File.separator
import java.io.IOException
import java.lang.reflect.Method
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.Icon
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


class Build(name: String, description: String, icon: Icon) : AnAction(name, description, icon) {

    override fun actionPerformed(e: AnActionEvent) {
        val baseDir: @SystemIndependent @NonNls String? = e.project!!.basePath
        if (StorageUtils.getString(SettingConfig.CHEESE_HOME).isNullOrEmpty()) {
            ToastUtils.error("CHEESE_SDK 环境路径未设置 无法构建")
            return
        }
        setProject(e.project)
        if (baseDir != null) {
            val CORE_JAR_PATH = File(File(StorageUtils.getString(SettingConfig.CHEESE_HOME), "lib"), "core.jar")
            val JDK_PATH = File(File(StorageUtils.getString(SettingConfig.CHEESE_HOME), "jbr"), "bin")
            val SDK_PATH = File(StorageUtils.getString(SettingConfig.CHEESE_HOME))


            if (StorageUtils.getString(SettingConfig.BUILD) == "开启") {
                Thread {
                    execute(
                        command = "java -Dfile.encoding=GBK -jar ${CORE_JAR_PATH} -build -baseDir ${baseDir} -sdkPath ${SDK_PATH} -u",  // 执行的命令
                        options = mapOf("workingDir" to JDK_PATH.absolutePath),
                        onData = { output ->  // 处理输出
                            printToConsole(e.project, output, ConsoleViewContentType.USER_INPUT)
                        },
                        onComplete = { exitCode ->
                            if (exitCode == 0) {
                                ToastUtils.info("编译项目成功")
                            } else {
                                ToastUtils.info("编译失败，退出码: ${exitCode}")
                            }

                        }
                    )
                }.start()
            } else {
                Thread {
                    execute(
                        command = "java -Dfile.encoding=GBK -jar ${CORE_JAR_PATH} -build -baseDir ${baseDir} -sdkPath ${SDK_PATH}",  // 执行的命令
                        options = mapOf("workingDir" to JDK_PATH.absolutePath),
                        onData = { output ->  // 处理输出
                            printToConsole(e.project, output, ConsoleViewContentType.USER_INPUT)
                        },
                        onComplete = { exitCode ->
                            if (exitCode == 0) {
                                ToastUtils.info("编译项目成功")
                            } else {
                                ToastUtils.info("编译失败，退出码: ${exitCode}")
                            }

                        }
                    )
                }.start()
            }


        }

    }
}


