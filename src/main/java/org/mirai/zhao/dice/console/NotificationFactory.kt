package org.mirai.zhao.dice.console

import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.mirai.zhao.dice.AppContext
import org.mirai.zhao.dice.BuildConfig
import org.mirai.zhao.dice.MainActivity
import org.mirai.zhao.dice.R

object NotificationFactory {

    const val SERVICE_NOTIFICATION = "service"
    const val CAPTCHA_NOTIFICATION = "captcha"
    const val OFFLINE_NOTIFICATION = "offline"

    val context by lazy {
        AppContext.consoleService
    }

    private val notifyIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    private val launchMainActivity = PendingIntent.getActivity(
            context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
    )
}