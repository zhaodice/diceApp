package org.mirai.zhao.dice.console

import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancel
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.LoginSolver
import org.mirai.zhao.dice.AppContext
import org.mirai.zhao.dice.activity.CaptchaActivity
import org.mirai.zhao.dice.activity.UnsafeLoginActivity


class AndroidLoginSolver : LoginSolver() {

    lateinit var verificationResult: CompletableDeferred<String>
    lateinit var captchaData: ByteArray
    lateinit var url: String
    var context:ConsoleService?=AppContext.consoleService

    companion object {
        const val CAPTCHA_NOTIFICATION_ID = 2
        val INSTANCE:AndroidLoginSolver=AndroidLoginSolver()
    }

    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String {
        if(context==null){
            bot.cancel()
            return ""
        }
        verificationResult = CompletableDeferred()
        captchaData = data
        val notifyIntent = Intent(context, CaptchaActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK// or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context!!.startActivity(notifyIntent)
        val c=verificationResult.await()
        Log.d("zdice!", "验证码输入：$c")
        return c
    }

    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String {
        if(context==null){
            bot.cancel()
            return ""
        }
        verificationResult = CompletableDeferred()
        this.url = url
        sendVerifyNotification()
        return verificationResult.await()
    }

    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String {
        if(context==null){
            bot.cancel()
            return ""
        }
        verificationResult = CompletableDeferred()
        this.url = url
        sendVerifyNotification()
        return verificationResult.await()
    }

    private fun sendVerifyNotification() {
        if(AppContext.context!=null) {
            Log.d("zdice!", "陌生设备验证！$url")
            Log.d("zdice!", "已自动启动activity")
            val notifyIntent = Intent(AppContext.context, UnsafeLoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK// or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            notifyIntent.putExtra("url", url)
            AppContext.context!!.startActivity(notifyIntent)
        }
    }
}

