package com.xiaomi.island

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class IslandNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString()
            ?: sbn.packageName
        val body = extras.getCharSequence("android.text")?.toString()
            ?: "Nueva notificación"

        sendBroadcast(
            Intent(IslandService.ACTION_NOTIFICATION).apply {
                setPackage(packageName)
                putExtra("title", title.take(60))
                putExtra("body", body.take(120))
            }
        )
    }
}
