package coco.cheese.core


import net.codeocean.cheese.console.ConsoleExecutor.Companion.printToConsole
import com.intellij.execution.ui.ConsoleViewContentType
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.io.File
import java.io.FileInputStream
import java.net.URI
import java.nio.ByteBuffer
import java.nio.charset.Charset

class WebSocketClientExample(serverUri: URI) : WebSocketClient(serverUri) {

    // 连接成功时调用
    override fun onOpen(handshakedata: ServerHandshake) {
        println("Connection established with server.")
        send("ide")  // 连接成功后发送设备 ID
    }

    // 接收到消息时调用
    override fun onMessage(message: String) {
        val processedLog = message.trim()
        printToConsole(processedLog, ConsoleViewContentType.NORMAL_OUTPUT)
        println("Message from server: $message")
    }

    // 连接关闭时调用
    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        println("Connection closed. Reason: $reason")
    }

    // 发生错误时调用
    override fun onError(ex: Exception?) {
        println("An error occurred: ${ex?.message}")
        ex?.printStackTrace()
    }

    // 发送文件的函数
    fun sendFile(cmd: String, filePath: String) {
        try {
            // 读取文件内容
            val file = File(filePath)
            val fileName = file.name

            // 将命令转换为字节数组
            val cmdBytes = cmd.toByteArray(Charset.forName("UTF-8"))

            // 将文件名转换为字节数组
            val fileNameBytes = fileName.toByteArray(Charset.forName("UTF-8"))

            // 读取文件内容为字节数组
            val fileBytes = FileInputStream(file).readBytes()

            // 使用分隔符 (例如 "|")
            val separator = "|".toByteArray(Charset.forName("UTF-8"))

            // 合并命令、文件名、分隔符和文件内容
            val combinedBuffer = ByteBuffer.allocate(cmdBytes.size + separator.size + fileNameBytes.size + separator.size + fileBytes.size)
            combinedBuffer.put(cmdBytes)
            combinedBuffer.put(separator)
            combinedBuffer.put(fileNameBytes)
            combinedBuffer.put(separator)
            combinedBuffer.put(fileBytes)

            // 发送数据
            send(combinedBuffer.array())
            println("File '$fileName' sent successfully")
        } catch (e: Exception) {
            println("Error sending file: ${e.message}")
        }
    }

    // 发送普通消息的函数
    fun sendMessage(message: String) {
        send(message)
        println("Sent message: $message")
    }
}
var webSocketClient:WebSocketClientExample?=null
fun connectWebSocket(port: Int?): WebSocketClientExample {
    val serverUri = URI("ws://localhost:$port/chat")
     webSocketClient = WebSocketClientExample(serverUri)
     webSocketClient!!.connect()
    return webSocketClient!!
}


fun sendFile(cmd: String, filePath: String) {
    webSocketClient?.sendFile(cmd, filePath)
}

fun sendMessage(cmd: String) {
    webSocketClient?.sendMessage(cmd)
}