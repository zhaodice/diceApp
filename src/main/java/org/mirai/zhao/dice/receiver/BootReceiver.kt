package org.mirai.zhao.dice.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import org.mirai.zhao.dice.console.ConsoleService

class BootReceiver : BroadcastReceiver() {
    private val action = "android.intent.action.BOOT_COMPLETED"
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == action) {
            val shareData: SharedPreferences = context.getSharedPreferences("app", Activity.MODE_PRIVATE)
            if(shareData.getBoolean("bootStart",true))
                ConsoleService.startControlService(context)
        }
    }
}