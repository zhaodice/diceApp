package org.mirai.zhao.dice.console

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiInternalApi
import org.mirai.zhao.dice.AppContext
import org.mirai.zhao.dice.R
import org.mirai.zhao.dice.activity.MiraiConsoleActivity
import org.mirai.zhao.dice.file.FileService
import org.mirai.zhao.dice.file.Util
import org.mirai.zhao.dice.web.UpdateService
import terminal.ConsoleTerminalExperimentalApi
import terminal.ConsoleTerminalSettings
import terminal.MiraiConsoleTerminalLoader
import terminal.mainLogger
import java.io.File


class ConsoleService : Service() {
    private var start = false
    private val zhaoNotifyId="zhao_notification_id"
    private val zhaoNotifyName="zhao_notification_name"

    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    companion object {
        @JvmField
        var androidMiraiLogger: AndroidMiraiLogger?=null
        @JvmField
        var onLogChangedListener: OnLogChangedListener?=null
        //const val START_SERVICE = 0
        //const val STOP_SERVICE = 1
        //const val NOTIFICATION_ID = 1
        //const val OFFLINE_NOTIFICATION_ID = 3
        //const val TAG = "BOT_SERVICE"
        /*
        @JvmField
        val miraiDir = "$sdcard/MiraiDice"
        @JvmField
        val pluginsDir = "$miraiDir/plugins"
        @JvmField
        val zhaoDice = "$pluginsDir/ZhaoDice/cocdata"
        @JvmField
        val zhaoDiceData = "$zhaoDice/data"
        @JvmField
        val zhaoDiceDeviceInfo= "$zhaoDiceData/deviceInfo"
        @JvmField
        val autoLoginFile= "$zhaoDiceData/autoLogin.txt"

        init {
            var f= File(zhaoDice)
            if(!f.exists()){
                val k=f.mkdirs()
                println(k)
            }
            f= File(pluginsDir)
            if(!f.exists()){
                f.mkdirs()
            }
        }
*/
        fun newPluginFile():File{
            return File(AppContext.pluginsDir, "mirai-zhao-m2.jar")
        }
        private fun oldPluginFile():File{
            return File(AppContext.pluginsDir, "mirai-zhao.jar")
        }
        fun initPlugin(context: Context):File{
            //删除旧插件
            oldPluginFile().delete()
            //插件只初始化一次
            val newjar = newPluginFile()
            val shareData = context.getSharedPreferences("app", MODE_PRIVATE)
            val jarMd5=shareData.getString("jar_md5", "")
            val assetsMd5=FileService.getFileMD5(context.assets.open(newjar.name))
            if(!newjar.exists()||!jarMd5.equals(assetsMd5, true)){
                Util.CopyAssets(context, AppContext.pluginsDir, newjar.name)
                shareData.edit().putString("jar_md5", assetsMd5).apply()
                println("Wrote dice plugin -> ${AppContext.pluginsDir}")
            }
            return newjar
        }
        @JvmStatic
        fun startControlService(context: Context){
            if(AppContext.consoleService==null) {
                val intent = Intent(context, ConsoleService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }
        @JvmStatic
        fun autoLogin(qq: String, password: String, setProtocol: BotConfiguration.MiraiProtocol, loginResult: CompletableDeferred<String>):Bot {
            val zhaoDiceDeviceInfo= File(AppContext.zhaoDiceDeviceInfo)
            if(!zhaoDiceDeviceInfo.exists())
                zhaoDiceDeviceInfo.mkdirs()
            val bot= BotFactory.newBot(qq.toLong(), password) {
                //protocol= BotConfiguration.MiraiProtocol.ANDROID_WATCH
                protocol= setProtocol
                fileBasedDeviceInfo("${AppContext.zhaoDiceDeviceInfo}/$qq.json")
                loginSolver=AndroidLoginSolver.INSTANCE
            }
            //System.out.println("使用设备文件："+"${AppContext.zhaoDiceDeviceInfo}/$qq.json")
            GlobalScope.launch(CoroutineExceptionHandler { _, loginFailedException ->
                if (loginFailedException.message == null) {
                    loginResult.complete("未知错误")
                } else {
                    loginResult.complete("来自TX服务器的消息：" + loginFailedException.message)
                }
                //File("${AppContext.zhaoDiceDeviceInfo}/$qq.json").delete()
            }) {
                bot.login()
                loginResult.complete("")
            }
            return bot
        }
    }

    @MiraiInternalApi
    @ConsoleTerminalExperimentalApi
    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate() {
        AppContext.consoleService = this
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BotWakeLock")
        Thread{
            Thread.sleep(1000)
            try {
                wakeLock.acquire()
            } catch (e: Exception) {
                Log.e("wakeLockError", e.message ?: "null")
            }
        }.start()
        startConsole()
        super.onCreate()
    }
    @MiraiInternalApi
    @ConsoleTerminalExperimentalApi
    private fun startConsole() {
        if (!start) {
            Thread{
                try {
                    initPlugin(this)
                    UpdateService.autoUpdate(newPluginFile())
                    ConsoleTerminalSettings.noConsole=true
                    ConsoleTerminalSettings.noAnsi=true
                    MiraiConsoleTerminalLoader.startAsDaemon().apply {
                        androidMiraiLogger=mainLogger
                    }
                }catch (e: Throwable){
                    e.printStackTrace()
                }
            }.start()
            //val f=File("/data/user/0/org.mirai.zhao.dice/files/MiraiDice/plugins").list()
            //for(k in f)
            //    println(k)

            //frontEnd = MiraiConsole.INSTANCE
            //val result= CompletableDeferred<String>()
            //autoLogin("3403310837", "*****",result)
            start = true
        }
    }
    /*
    private fun stopConsole() {
        if (!start) return
        Log.e(TAG, "停止服务")
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        start=false
        //stopForeground(true)
        //stopSelf()
        //exitProcess(0)
    }*/


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //Log.d(TAG, "onStartCommand()");
        // 在API11之后构建Notification的方式
        val builder = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Notification.Builder(this.applicationContext, zhaoNotifyId)
        else
            Notification.Builder(this.applicationContext)//获取一个Notification构造器
        val nfIntent = Intent(this, MiraiConsoleActivity::class.java)
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(this.resources,
                        R.mipmap.ic_launcher)) // 设置下拉列表中的图标(大图标)
                .setContentTitle("Dice! 赵骰核心正在挂后台") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentText("miraiConsoleService服务已启动") // 设置上下文内容
                .setWhen(System.currentTimeMillis()) // 设置该通知发生的时间
        // 【适配Android8.0】设置Notification的Channel_ID,否则不能正常显示
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(zhaoNotifyId)
        }
        //构建NotificationChannel实例
        val notification = builder.build() // 获取构建好的Notification
        //notification.defaults = Notification.DEFAULT_SOUND //设置为默认的声音

        // 额外添加：
        // 【适配Android8.0】给NotificationManager对象设置NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(zhaoNotifyId, zhaoNotifyName, NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }
        // 参数一：唯一的通知标识；参数二：通知消息。
        startForeground(110, notification) // 开始前台服务
        return START_STICKY
    }

}