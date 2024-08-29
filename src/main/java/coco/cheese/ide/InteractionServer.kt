package coco.cheese.ide


import coco.cheese.ide.Env.OS.separator
import coco.cheese.ide.InteractionServer.handleDownload
import coco.cheese.ide.InteractionServer.handleLogReceiver
import coco.cheese.ide.InteractionServer.instruction
import coco.cheese.ide.InteractionServer.receiveHeartbeatRequest
import coco.cheese.ide.console.ConsoleExecutor.Companion.printToConsole
import coco.cheese.ide.data.SettingConfig
import coco.cheese.ide.swing.code.NodeUi
import coco.cheese.ide.swing.code.PaintUi
import coco.cheese.ide.utils.FileUtils.createDirectoryIfNotExists
import coco.cheese.ide.utils.StorageUtils
import com.google.api.RoutingProto.routing
import com.intellij.execution.ui.ConsoleViewContentType
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import java.io.File
import java.lang.Runnable
import java.net.NetworkInterface
import java.net.SocketException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
fun io.ktor.server.application.Application.module() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }

    // 配置路由
    routing {
        route("/") {
            get {
                call.respondText(instruction.toString(), ContentType.Text.Plain, HttpStatusCode.OK)
                instruction =0
            }
        }
        route("/heartbeat") {
            get {
                receiveHeartbeatRequest()
                call.respondText(instruction.toString(), ContentType.Text.Plain, HttpStatusCode.OK)
            }
        }
        route("/download") {
            get {
                handleDownload(this.call)
            }
        }
        route("/log_receiver") {
            post {
                handleLogReceiver(this.call)
            }
        }
        route("/upload") {
            post {
                createDirectoryIfNotExists("${StorageUtils.getString(SettingConfig.CHEESE_HOME)}${separator}work${separator}device")
                val uploadDir =  "${StorageUtils.getString(SettingConfig.CHEESE_HOME)}${separator}work${separator}device"
                val multipart = call.receiveMultipart()
                var fileDescription = ""
                var _data:String?=null
                var _file:String?=null
                multipart.forEachPart { part ->

                    if (part is PartData.FileItem) {

                        val name = part.originalFileName!!
                        val uniqueFileName = UUID.randomUUID().toString() + "_" + name
                        val file = File(uploadDir, uniqueFileName)

                        try {
                            withContext(Dispatchers.IO) {
                                part.streamProvider().use { input ->
                                    Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
                                }
                            }
                            fileDescription = "File uploaded successfully! Saved as: $uniqueFileName"
                            if (_file=="0"){
                                if (_data=="0"){
                                    PaintUi.imagePanel?.loadImage("$uploadDir${separator}$uniqueFileName")
                                }else if (_data=="1"){
                                    NodeUi.imagePanel1?.loadImage("$uploadDir${separator}$uniqueFileName")

                                }
                                File("$uploadDir${separator}$uniqueFileName").delete()
                            }else if (_file=="1"){
                                println("加载XML:"+"$uploadDir${separator}$uniqueFileName")
                                NodeUi.loadXML("$uploadDir${separator}$uniqueFileName")
                                File("$uploadDir${separator}$uniqueFileName").delete()
                            }

                        } catch (e: Exception) {
                            fileDescription = "File upload failed: ${e.message}"
                        }
                    }else if(part is PartData.FormItem){
                        val keyValue = part.value.split("&")
                            .mapNotNull {
                                val (key, value) = it.split("=", limit = 2)
                                if (key.isNotEmpty() && value.isNotEmpty()) {
                                    key to value
                                } else {
                                    null
                                }
                            }
                            .toMap()
                        _data=keyValue["data"]
                        _file=keyValue["file"]
                    }

                    part.dispose()
                }
//                println("测试:"+da)
//                if(da=="0"){
//
//                }else if(da=="1") {
//                    multipart.forEachPart { part ->
//                        if (part is PartData.FileItem) {
//                            val name = part.originalFileName!!
//                            val uniqueFileName = UUID.randomUUID().toString() + "_" + name
//                            val file = File(uploadDir, uniqueFileName)
//
//                            try {
//                                withContext(Dispatchers.IO) {
//                                    part.streamProvider().use { input ->
//                                        Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
//                                    }
//                                }
//                                fileDescription = "File uploaded successfully! Saved as: $uniqueFileName"
//
//                                nodeui.loadXML("$uploadDir${separator}$uniqueFileName")
////                                imagePanel1?.loadImage("$uploadDir${separator}$uniqueFileName")
//                            } catch (e: Exception) {
//                                fileDescription = "File upload failed: ${e.message}"
//                            }
//                        }
//                        part.dispose()
//                    }
//                }

                call.respondText(fileDescription, ContentType.Text.Plain)
            }
        }
    }
}
object InteractionServer {

    var instruction = 0
    var glDownload=""



    suspend fun handleLogReceiver(call: ApplicationCall) {
        val data = call.receiveText()
        val log = data.decodeURLQueryComponent()

        // 在这里可以处理接收到的日志数据，例如写入日志文件、发送邮件等
        println()

        val processedLog = log.trim()
        printToConsole(processedLog, ConsoleViewContentType.NORMAL_OUTPUT)
        call.respond(HttpStatusCode.OK, "OK")
    }

    suspend fun handleDownload(call: ApplicationCall) {
        // 文件路径/.magichands
        val filePath = glDownload // 替换为实际的文件路径
        val file = File(filePath)
        if (!file.exists()) {
            call.respond(HttpStatusCode.NotFound, "File not found")
            return
        }

        // 获取文件名
        val fileName = file.name
        // 使用 respondFile 发送文件，并设置响应头
//    call.respondFile(file) {
//        headers {
//            append(HttpHeaders.ContentDisposition, ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, fileName).toString())
//            append(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
//        }
//    }

        call.response.header(
            HttpHeaders.ContentDisposition, ContentDisposition.Attachment.withParameter(
                ContentDisposition.Parameters.FileName, fileName).toString())
        call.response.header(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
        call.respondFile(file)

    }

//fun setInstruction(value: Int) {
//    instruction = value
//}

    // 定义全局变量
    private val isHeartbeatNormal = AtomicBoolean(false)
    private val lastHeartbeatTime = AtomicReference<Date?>(null)

    // 创建一个调度线程池
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

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
    fun startHeartbeatMonitoring() {
        val task = Runnable {
            try {
                val currentTime = System.currentTimeMillis()
                val lastTime = lastHeartbeatTime.get()?.time ?: 0
                val timeDiff = (currentTime - lastTime) / 1000

                if (timeDiff <= 3) {
                    if (!isHeartbeatNormal.get()) {
                        printToConsole("心跳接通", ConsoleViewContentType.NORMAL_OUTPUT)
                        isHeartbeatNormal.set(true)
                    }
                } else {
                    if (isHeartbeatNormal.get()) {
                        printToConsole("心跳丢失", ConsoleViewContentType.NORMAL_OUTPUT)
                        isHeartbeatNormal.set(false)
                    }
                }
            } catch (e: Exception) {
                // 处理异常
                println("Error in heartbeat monitoring: ${e.message}")
            }
        }

        // 定时每 3 秒执行一次任务
        scheduler.scheduleAtFixedRate(task, 0, 3, TimeUnit.SECONDS)
    }


    // 更新心跳时间
    fun receiveHeartbeatRequest() {
        lastHeartbeatTime.set(Date())
    }


}