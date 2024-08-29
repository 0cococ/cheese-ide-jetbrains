package coco.cheese.ide.action.epm


import coco.cheese.ide.InteractionServer.getInternalIPAddresses

import coco.cheese.ide.InteractionServer.startHeartbeatMonitoring
import coco.cheese.ide.console.ConsoleExecutor.Companion.printToConsole
import coco.cheese.ide.console.ConsoleExecutor.Companion.setProject
import coco.cheese.ide.data.SettingConfig
import coco.cheese.ide.module
import coco.cheese.ide.utils.*
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
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
        printToConsole(e.project,"心跳监听地址：${getInternalIPAddresses().joinToString(separator = ", ")}:${StorageUtils.getString(SettingConfig.CHEESE_PORT).toIntOrNull()}", ConsoleViewContentType.NORMAL_OUTPUT)


        startHeartbeatMonitoring()
        Thread{
            StorageUtils.getString(SettingConfig.CHEESE_PORT).toIntOrNull()
                ?.let {
                    embeddedServer(Netty, port = it, module = Application::module).start(wait = false)


                }

        }.start()

    }

}