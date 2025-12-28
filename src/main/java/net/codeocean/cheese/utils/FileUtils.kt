package net.codeocean.cheese.utils


import org.codehaus.jettison.json.JSONObject
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
object FileUtils {
    fun isFileSizeLessThan10MB(filePath: String): Boolean {
        val file = File(filePath)

        // 检查文件是否存在
        if (file.exists()) {
            // 获取文件大小（字节）
            val fileSizeInBytes = file.length()

            // 转换为 MB
            val fileSizeInMB = fileSizeInBytes.toDouble() / (1024 * 1024)
            println("fileSizeInMB = $fileSizeInMB")

            // 判断文件大小是否小于 10MB
            return fileSizeInMB > 10
        }
        return false
    }

    fun convertPath(path: String): String {
        val separator = File.separator
        // 替换所有的正斜杠和反斜杠为当前平台的路径分隔符
        return path.replace("/", separator).replace("\\", separator)
    }
    fun copyDirectory(sourceDir: String, targetDir: String) {
        val sourcePath = Paths.get(sourceDir)
        val targetPath = Paths.get(targetDir)
        if (!Files.exists(sourcePath)) {
           return
        }
        // 确保目标目录存在
        if (!Files.exists(targetPath)) {
            Files.createDirectories(targetPath)
        }

        // 遍历源目录下的所有文件和子目录
        Files.walk(sourcePath).forEach { source ->
            val target = targetPath.resolve(sourcePath.relativize(source))
            try {
                if (Files.isDirectory(source)) {
                    // 如果是目录，创建对应的目标目录
                    if (!Files.exists(target)) {
                        Files.createDirectories(target)
                    }
                } else {
                    // 如果是文件，复制到目标目录
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    fun copyFile(sourcePath: String, destinationPath: String) {
        val sourceFile = File(sourcePath)
        val destinationFile = File(destinationPath)

        if (!sourceFile.exists()) {
            throw IOException("Source file does not exist: $sourcePath")
        }

        try {
            Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            println("File copied successfully from $sourcePath to $destinationPath")
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }



}
