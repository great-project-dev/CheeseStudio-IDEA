package net.codeocean.cheese.utils

import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.concurrent.thread

object TerminalUtils {
    /**
     * 执行 Shell 命令并实时获取输出信息。
     * @param command 要执行的命令。
     * @param workingDir 要设置的工作目录。
     * @param onOutput 行输出的处理函数。
     * @return 命令的输出。
     */
    fun executeCommand(command: String, workingDir: File, onOutput: (String) -> Unit): String {
        val os = System.getProperty("os.name").lowercase()
        val isWindows = os.contains("win")

        // 构造命令
        val finalCommand = if (isWindows) {
            arrayOf("cmd.exe", "/c", command)
        } else {
            arrayOf("/bin/sh", "-c", command)
        }

        val executor = Executors.newFixedThreadPool(2) // 使用固定线程池
        return try {
            val processBuilder = ProcessBuilder(*finalCommand).apply {
                redirectErrorStream(true)  // 将错误流合并到标准输出流
                if (workingDir.exists()) {
                    directory(workingDir)  // 设置工作目录
                }
            }

            val process = processBuilder.start()

            // 使用线程池读取流
            val stdoutFuture: Future<*> = executor.submit { readStream(process.inputStream, onOutput) }
            val stderrFuture: Future<*> = executor.submit { readStream(process.errorStream, onOutput) }

            val exitCode = process.waitFor()

            // 确保所有流都被读取
            stdoutFuture.get()
            stderrFuture.get()

            if (exitCode != 0) {
                throw IOException("Command failed with exit code $exitCode.")
            }

            "Command completed successfully."
        } catch (e: IOException) {
            e.printStackTrace()
            "Error: ${e.message}"
        } finally {
            executor.shutdown()
        }
    }

    /**
     * 读取输入流并处理每行输出。
     * @param inputStream 输入流。
     * @param onOutput 行输出的处理函数。
     */
    private fun readStream(inputStream: InputStream, onOutput: (String) -> Unit) {
        inputStream.bufferedReader(StandardCharsets.UTF_8).use { reader ->
            reader.lineSequence().forEach { line ->
                onOutput(line)
            }
        }
    }
    fun execute(
        command: String,
        options: Map<String, String> = emptyMap(),
        onData: (String) -> Unit = {},
        onComplete: (Int?) -> Unit = {}
    ) {
        val isWindows = System.getProperty("os.name").contains("win", true)

        // >>>>> 仅修改这部分关键代码 <<<<<
        val encoding = if (isWindows) Charset.forName("GBK") else StandardCharsets.UTF_8

        val finalCommand = if (isWindows) {
            arrayOf("cmd.exe", "/c", command)
        } else {
            // macOS 关键修复：强制注入 UTF-8 环境变量
            arrayOf("/bin/zsh", "-c", "export LANG=en_US.UTF-8; export LC_ALL=en_US.UTF-8; $command")
        }
        // >>>>> 修改结束 <<<<<

        // 以下保持原样...
        val processBuilder = ProcessBuilder(*finalCommand)

        if (Paths.get(options["workingDir"] ?: "").toFile().exists()) {
            processBuilder.directory(Paths.get(options["workingDir"] ?: "").toFile())
        }

        processBuilder.environment().putAll(System.getenv())

        try {
            val process = processBuilder.start()

            // 保持原有流处理（但使用修正后的 encoding）
            val stdoutReader = BufferedReader(InputStreamReader(process.inputStream, encoding))
            val stderrReader = BufferedReader(InputStreamReader(process.errorStream, encoding))

            thread(start = true) {
                stdoutReader.forEachLine(onData)
            }

            thread(start = true) {
                stderrReader.forEachLine(onData)
            }

            val exitCode = process.waitFor()
            onComplete(exitCode)
        } catch (e: Exception) {
            onData("Command execution error: ${e.message}")
            onComplete(null)
        }
    }


    fun execute1(
        command: String,
        options: Map<String, String> = emptyMap(),
        onData: (String) -> Unit = {},
        onComplete: (Int?) -> Unit = {}
    ): Process? {  // 返回 Process? 类型
        val os = System.getProperty("os.name").lowercase()
        val isWindows = os.contains("win")
        val finalCommand = if (isWindows) {
            arrayOf("cmd.exe", "/c", command)
        } else {
            arrayOf("/bin/sh", "-c", command)
        }

        // 设置编码为 GBK（兼容中文 Windows）
        val encoding = Charset.forName("GBK")

        // 创建进程执行命令
        val processBuilder = ProcessBuilder(*finalCommand)

        // 设置工作目录（如果存在）
        options["workingDir"]?.let { workingDir ->
            if (Paths.get(workingDir).toFile().exists()) {
                processBuilder.directory(Paths.get(workingDir).toFile())
            }
        }

        // 继承当前系统环境变量
        processBuilder.environment().putAll(System.getenv())

        try {
            // 启动进程
            val process = processBuilder.start()

            // 异步读取标准输出流
            thread(start = true) {
                BufferedReader(InputStreamReader(process.inputStream, encoding)).use { reader ->
                    reader.forEachLine(onData)
                }
            }

            // 异步读取错误输出流
            thread(start = true) {
                BufferedReader(InputStreamReader(process.errorStream, encoding)).use { reader ->
                    reader.forEachLine(onData)
                }
            }

            // 异步等待进程结束并触发回调
            thread(start = true) {
                val exitCode = process.waitFor()
                onComplete(exitCode)
            }

            return process  // 返回 Process 对象

        } catch (e: Exception) {
            onData("Command execution error: ${e.message}")
            onComplete(null)
            return null
        }
    }




}