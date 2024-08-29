package coco.cheese.ide.utils

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications

class ToastUtils {

    companion object {
        private var manager: NotificationGroupManager = NotificationGroupManager.getInstance()

        private var balloon: NotificationGroup = manager.getNotificationGroup("cheese_toast")

        fun info(msg: String) {
            val notification = balloon.createNotification(msg!!, NotificationType.INFORMATION)
            Notifications.Bus.notify(notification)
        }

        fun warning(msg: String) {
            val notification = balloon.createNotification(msg!!, NotificationType.WARNING)
            Notifications.Bus.notify(notification)
        }

        fun error(msg: String) {
            val notification = balloon.createNotification(msg!!, NotificationType.ERROR)
            Notifications.Bus.notify(notification)
        }
    }


}