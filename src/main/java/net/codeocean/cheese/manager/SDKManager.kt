package net.codeocean.cheese.manager

import net.codeocean.cheese.Env.form
import net.codeocean.cheese.console.ConsoleExecutor.Companion.printToConsole
import net.codeocean.cheese.data.SettingConfig
import net.codeocean.cheese.utils.FileUtils.isFileSizeLessThan10MB
import net.codeocean.cheese.utils.StorageUtils
import net.codeocean.cheese.utils.TerminalUtils.executeCommand
import net.codeocean.cheese.utils.ToastUtils
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.Project
import org.codehaus.jettison.json.JSONObject
import java.io.*
import java.io.File.separator
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
fun createIfNotExists(pathStr: String) {
    val path: Path = Paths.get(pathStr)
    if (!Files.exists(path)) {
        try {
            Files.createDirectories(path)
        } catch (e: Exception) {
            println("Failed to create directory: ${e.message}")
        }
    }
}

class SDKManager(val project: Project) {

    // 主入口函数，处理SDK更新逻辑
    fun update(fix: Boolean, sdkPath: String, callback: (Boolean) -> Unit) {
        createIfNotExists(sdkPath)
        Thread {
            try {
                if (fix || compareVersions(sdkPath)) {
                    if (fix){
                        ToastUtils.info("开始修复SDK,即将开始下载。")
                    }else{
                        ToastUtils.info("检测到新版SDK,即将开始下载。")
                    }
                    printToConsole(project,"清理相应资源，为下载做准备。",ConsoleViewContentType.USER_INPUT)
                    closeProcess(sdkPath)
                    printToConsole(project,"寻找最快节点进行下载。",ConsoleViewContentType.USER_INPUT)
                    form.gitHubProxy.text = StorageUtils.getString(SettingConfig.PROXY)
//                    val url= getFastestDownloadUrl("http://cheese.codeocean.net/sdk_proxy.json")
//                    println("最快速度："+url)
                    val isWindows = System.getProperty("os.name").contains("win", true)
                    if (isWindows) {
                        form.gitHubProxy.text= "https://pan.codeocean.net/d/pan/sdk/cheese-sdk-win-x64.zip"
                    }else{
                        form.gitHubProxy.text= "https://pan.codeocean.net/d/pan/sdk/cheese-sdk-mac-x64.zip"
                    }
                    StorageUtils.save(SettingConfig.PROXY,  form.gitHubProxy.text)
                    val proxyUrl = StorageUtils.getString(SettingConfig.PROXY)
                    val downloadUrl = proxyUrl
                    printToConsole(project,"SDK下载节点："+downloadUrl,ConsoleViewContentType.USER_INPUT)
                    val zipFilePath = sdkPath + separator + "cheese-sdk.zip"
                    val zipFile = File(zipFilePath)
                    try {
                        if (zipFile.exists()) {
                            File(zipFilePath).delete()
                        }
                        if (curl(downloadUrl, zipFilePath)) {
                            ToastUtils.info("SDK下载成功。")

                            if (unzip(zipFilePath, sdkPath)){
                                callback(true)
                            }else{
                                callback(false)
                            }

                        } else {
                            ToastUtils.info("SDK下载失败。")
                            callback(false)
                        }
                    }catch (e:Exception){
                        ToastUtils.error("SDK下载失败："+e.message+"。")
                        callback(false)
                    }

                } else {
                    ToastUtils.info("SDK已经是最新版。")
                    callback(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtils.error("更新SDK时发生错误: ${e.message}。")
                callback(false)
            }
        }.start()
    }
    fun getProcessIdsByPath(processPath: String): List<Int> {
        val processIds = mutableListOf<Int>()  // 存储所有匹配的进程 ID
        try {
            // 执行 wmic 命令来获取进程的详细信息，包括路径
            val process = ProcessBuilder("wmic", "process", "get", "Caption,ProcessId,ExecutablePath")
                .start()

            // 获取命令输出的结果
            val reader = BufferedReader(InputStreamReader(process.inputStream, Charset.forName("GBK")))  // 设置编码为 GBK
            var line: String? = reader.readLine()

            // 跳过列标题行
            reader.readLine()

            // 将反斜杠转义为双反斜杠，并避免路径中的空格被误解
            val escapedProcessPath = processPath.replace("\\", "\\\\").replace(" ", "\\ ")

            // 定义正则表达式来匹配包含指定进程路径的行
            val regex = ".*$escapedProcessPath.*".toRegex()

            // 遍历所有进程并筛选包含指定路径的行
            while (line != null) {
                if (regex.matches(line)) {
                    // 使用正则表达式提取最后一个出现的数字（进程 ID）
                    val processIdRegex = "(\\d+)(?!.*\\d)".toRegex()  // 匹配最后一个数字
                    val matchResult = processIdRegex.find(line)
                    matchResult?.let {
                        println("找到匹配的进程: $line")  // 输出匹配的行
                        processIds.add(it.value.toInt())  // 将进程 ID 添加到列表中
                    }
                }
                line = reader.readLine()
            }

            process.waitFor()  // 等待命令执行完成
        } catch (e: Exception) {
            println("无法获取进程列表: ${e.message}")
            e.printStackTrace()  // 打印堆栈跟踪，便于调试
        }

        return processIds  // 返回所有匹配的进程 ID 列表
    }



    fun closeProcess(processPath:String) {
        // 获取所有匹配路径的进程 ID
        val processIds = getProcessIdsByPath(processPath)

        if (processIds.isNotEmpty()) {
            println("找到以下进程 ID: $processIds")
            // 尝试关闭所有匹配的进程
            processIds.forEach { processId ->
                try {
                    val killCommand = "taskkill /PID $processId /F"
                    val killProcess = Runtime.getRuntime().exec(killCommand)
                    killProcess.waitFor() // 等待进程终止
                    println("成功关闭进程 ID: $processId")
                }catch (e:Exception){
                    println("关闭进程失败 ID: $processId")
                }
            }
        } else {
            println("未找到匹配的进程")
        }

    }

    // curl下载函数
    fun curl(url: String, path: String): Boolean {
        val command = """
            curl -L -k -C - "$url" -o "$path"
        """.trimIndent()

        return try {
            executeCommand(command, File("")) { output ->
                printToConsole(project, output, ConsoleViewContentType.USER_INPUT)
            }
            println("Downloaded to: $path")
            isFileSizeLessThan10MB(path)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.error("下载失败: ${e.message}")
            false
        }
    }

    // 解压ZIP文件
    fun unzip(zipFilePath: String, destDirectory: String):Boolean {
        val destDir = File(destDirectory)
        if (!destDir.exists()) {
            destDir.mkdirs()
        }

        try {
            FileInputStream(zipFilePath).use { fis ->
                ZipInputStream(fis).use { zis ->
                    var entry: ZipEntry?
                    while (zis.nextEntry.also { entry = it } != null) {
                        val filePath = destDirectory + File.separator + entry!!.name
                        val newFile = File(filePath)

                        if (entry!!.isDirectory) {
                            newFile.mkdirs()
                        } else {
                            newFile.parentFile.mkdirs()
                            FileOutputStream(newFile).use { fos ->
                                val buffer = ByteArray(1024)
                                var length: Int
                                while (zis.read(buffer).also { length = it } > 0) {
                                    fos.write(buffer, 0, length)
                                }
                            }
                        }
                        zis.closeEntry()
                    }
                }
            }
            println("SDK解压成功。")
            ToastUtils.info("SDK解压成功。")
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            ToastUtils.error("解压失败: ${e.message}。")
            return false
        }
    }

    // 获取远程版本
    fun getRemoteVersion(url: String): String {
        return try {
            val uri = URI(url)
            val connection = uri.toURL().openConnection() as HttpURLConnection
//            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            connection.inputStream.bufferedReader().use {
                val response = it.readText()
                val jsonObject = JSONObject(response)
                jsonObject.getString("version") ?: "0.0.0"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "0.0.0" // 如果发生异常，返回默认版本号
        }
    }

    // 获取本地版本
    fun getLocalVersion(filePath: String): String {
        return try {
            val jsonString = File(filePath).readText()
            val jsonObject = JSONObject(jsonString)
            jsonObject.optString("version", "0.0.0")
        } catch (e: Exception) {
            e.printStackTrace()
            "0.0.0"
        }
    }

    // 比较远程版本和本地版本
    fun compareVersions(sdkPath: String): Boolean {
        val remoteVersion = getRemoteVersion("https://cheese.codeocean.net/version.json")
        val localVersion = getLocalVersion("${sdkPath}${separator}config.json")

        return compareVersionStrings(remoteVersion, localVersion)
    }

    // 比较版本号字符串
    fun compareVersionStrings(remoteVersion: String, localVersion: String): Boolean {
        val remoteVersionParts = remoteVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val localVersionParts = localVersion.split(".").map { it.toIntOrNull() ?: 0 }

        for (i in 0 until minOf(remoteVersionParts.size, localVersionParts.size)) {
            if (remoteVersionParts[i] > localVersionParts[i]) {
                return true
            } else if (remoteVersionParts[i] < localVersionParts[i]) {
                return false
            }
        }
        return remoteVersionParts.size > localVersionParts.size
    }

    fun getSDKProxyUrl(url: String): List<String>{
        return try {
            val uri = URI(url)
            val connection = uri.toURL().openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.inputStream.bufferedReader().use {
                val response = it.readText()
                val jsonObject = JSONObject(response)
                val urls = jsonObject.getJSONArray("urls")
                if (urls.length() == 0) {
                    listOf("https://github.com/topcoco/cheese-sdk/releases/download/cheese-sdk/cheese-sdk.zip")
                } else {
                    List(urls.length()) { index -> urls.getString(index) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            listOf("https://github.com/topcoco/cheese-sdk/releases/download/cheese-sdk/cheese-sdk.zip")
        }

    }

    fun getSpeed(url: String): Double {
        var speed = 0.0  // 将 speed 改为可变变量
        val command = """
    curl -L -k -C - "$url" -o NUL -w "%{speed_download}" -s -r 0-1048575
      """.trimIndent()

        return try {
            executeCommand(command, File("")) { output ->
                // 将 output 转换为 Double，如果转换失败，返回 0.0
                speed = output.trim().toDoubleOrNull() ?: run {
                    0.0
                }
            }
            speed
        } catch (e: Exception) {
            e.printStackTrace()
            speed
        }
    }

    fun getFastestDownloadUrl(url: String): String {
        // 获取 SDK 代理 URL 列表
        val urls = getSDKProxyUrl(url)

        // 获取每个 URL 的下载速度
        val speeds = urls.map {
            println("检测节点："+it)
            getSpeed(it)
        }
        printToConsole(project,"节点下载速度排行："+speeds.joinToString { it.toString() },ConsoleViewContentType.USER_INPUT)
        val fastestUrl = urls[ speeds.indexOf(speeds.maxOrNull() ?: 0.0) ]
        return fastestUrl
    }

}
