package cn.byteroute.io.helper

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cn.byteroute.io.R
import cn.byteroute.io.common.Constants
import cn.byteroute.io.ui.MainActivity


object NotificationHelper {
    fun createNotification(
        context: Context?,
        channelId: String?,
        drawableId: Int,
        title: String?,
        text: String?
    ): Notification? {
        val openIntent = Intent(context, MainActivity::class.java)
        openIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingOpenIntent = PendingIntent.getActivity(context, 0, openIntent, 0)
        val builder = NotificationCompat.Builder(
            context!!,
            channelId!!
        )
            .setSmallIcon(drawableId)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingOpenIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setShowWhen(true)
        }
        builder.setWhen(0L)
        return builder.build()
    }

    fun createNotify(
        context: Context?,
        channelId: String?,
        drawableId: Int,
        title: String?,
        text: String?
    ): Notification? {
        val openIntent = Intent(context, MainActivity::class.java)
        openIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingOpenIntent = PendingIntent.getActivity(context, 0, openIntent, 0)
        return NotificationCompat.Builder(context!!, channelId!!)
            .setSmallIcon(drawableId)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingOpenIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setOnlyAlertOnce(true).build()
    }

    fun createNotificationChannel(context: Context, channelId: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(
                NotificationManager::class.java
            ).apply {
                createNotificationChannel(
                    NotificationChannel(
                        channelId,
                        context.getString(R.string.notification_channel_name),
                        NotificationManager.IMPORTANCE_HIGH
                    )
                )
            }
        }
    }

    fun destroyNotificationChannel(context: Context, channelId: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            ).apply {
                deleteNotificationChannel(channelId)
            }
        }
    }

    fun updateNotificationChannel(context: Context, notification: Notification?) {
        context.getSystemService(
            NotificationManager::class.java
        ).apply {
            notify(Constants.NOTIFICATION_ID, notification)
        }
        //NotificationManagerCompat.from(context).notify(Constants.NOTIFICATION_ID, notification!!)
    }

    // areNotificationsEnabled方法的有效性官方只最低支持到API 19，低于19的仍可调用此方法不过只会返回true，即默认为用户已经开启了通知。
    fun isOpen(context: Context) = NotificationManagerCompat.from(context).areNotificationsEnabled()


    fun requestPermission(context: Context) {
        Intent().apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName())
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                action = "android.settings.APP_NOTIFICATION_SETTINGS"
                putExtra("app_package", context.getPackageName());
                putExtra("app_uid", context.getApplicationInfo().uid);
            }
            context.startActivity(this);
        }
    }
}