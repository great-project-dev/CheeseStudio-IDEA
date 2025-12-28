package net.codeocean.cheese.action.epm


import coco.cheese.core.connectWebSocket
import net.codeocean.cheese.Env


import net.codeocean.cheese.console.ConsoleExecutor.Companion.setProject
import net.codeocean.cheese.data.SettingConfig
import net.codeocean.cheese.utils.*
import net.codeocean.cheese.utils.TerminalUtils.execute
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import java.io.File
import java.net.NetworkInterface
import java.net.SocketException
import javax.swing.Icon

class Wifi(name: String, description: String,icon: Icon) : AnAction(name, description, icon) {
    override fun actionPerformed(e: AnActionEvent) {
        if(StorageUtils.getString(SettingConfig.CHEESE_PORT).isNullOrEmpty()){
            ToastUtils.error("CHEESE_PORT 端口号未设置 无法启动心跳")
            return
        }
        setProject(e.project)
        Thread{
            val CORE_JAR_PATH=File(File(StorageUtils.getString(SettingConfig.CHEESE_HOME),"lib"),"core.jar")
            val JDK_PATH=File(File(StorageUtils.getString(SettingConfig.CHEESE_HOME),"jbr"),"bin")
            val SDK_PATH= File(StorageUtils.getString(SettingConfig.CHEESE_HOME))
           if(  Env.cd){
               ToastUtils.info("监听IP: ${getInternalIPAddresses().joinToString(separator = ", ")}:${
                   StorageUtils.getString(
                       SettingConfig.CHEESE_PORT
                   ).toIntOrNull()
               }")
           }else{
               try {
                   execute(
                       command = "java -Dfile.encoding=GBK -jar $CORE_JAR_PATH -server -sdkPath ${SDK_PATH} -port ${StorageUtils.getString(SettingConfig.CHEESE_PORT).toIntOrNull()}",  // 执行的命令
                       options = mapOf("workingDir" to JDK_PATH.absolutePath),
                       onData = { output ->  // 处理输出
                           println(output)
                           if(output.contains("Start Ok")){
                               connectWebSocket(StorageUtils.getString(SettingConfig.CHEESE_PORT).toIntOrNull())

                           }else if(output.contains("设备连接: device")){
                               ToastUtils.info("手机设备连接成功")
                           }else if(output.contains("设备连接: ide")){
                               ToastUtils.info("监听IP: ${getInternalIPAddresses().joinToString(separator = ", ")}:${
                                   StorageUtils.getString(
                                       SettingConfig.CHEESE_PORT
                                   ).toIntOrNull()
                               }")
                               Env.cd=true
                           }else if(output.contains("设备 device 已断开连接")){
                               ToastUtils.info("手机设备已断开连接")
                           }

                       },
                       onComplete = { exitCode ->  // 处理完成
                           if (exitCode === 0) {
                           } else {
                               ToastUtils.error("启动服务执行失败或服务器被关闭，退出码: ${exitCode}");
                           }
                           Env.cd=false
                           println("Process completed with exit code: $exitCode")
                       }
                   )
               } catch (e: Exception) {
                   Env.cd=false
                   ToastUtils.error(e.message.toString());
               }
           }



        }.start()

    }
    fun getInternalIPAddresses(): List<String> {
        val internalIPs = mutableListOf<String>()

        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()

            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                val inetAddresses = networkInterface.inetAddresses

                while (inetAddresses.hasMoreElements()) {
                    val inetAddress = inetAddresses.nextElement()

                    if (!inetAddress.isLoopbackAddress && !inetAddress.isLinkLocalAddress && inetAddress is java.net.Inet4Address) {
                        val address = inetAddress.hostAddress
                        if (address != "127.0.0.1" && !address.startsWith("169.")) {
                            internalIPs.add(address)
                        }
                    }
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }

        return internalIPs
    }

}