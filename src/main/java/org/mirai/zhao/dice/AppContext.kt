package org.mirai.zhao.dice

import android.app.Application
import android.content.Context
import android.os.Environment.getExternalStorageDirectory
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.mirai.zhao.dice.crash.CrashHandler
import org.mirai.zhao.dice.console.ConsoleService
import java.io.File
import java.security.Security

class AppContext : Application() {
    var dataStorage: String? = null
    val privateStorage: String? = null
    override fun onCreate() {
        // Remove the OS provided bouncy castle provider
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        // Add the bouncy castle provider from the added library
        Security.addProvider(BouncyCastleProvider())

        System.setProperty("mirai.slider.captcha.supported","1")
        val crashHandler = CrashHandler.getInstance()
        crashHandler.init()
        //dataStorage = getExternalFilesDir(null).toString()
        dataStorage = getExternalStorageDirectory().path
        //privateStorage = getExternalStorageDirectory().path
        miraiDir = "$dataStorage/MiraiDice"
        pluginsDir = getDir("plugins",0).absolutePath;//"$miraiDir/plugins"
        zhaoDice = "$miraiDir/data/ZhaoDice"
        zhaoDiceData = "$zhaoDice/cocdata/data"
        zhaoDiceDeviceInfo = "$zhaoDiceData/deviceInfo"
        autoLoginFile = "$zhaoDiceData/autoLogin.txt"
        val f=File(miraiDir)
        if(!f.exists())
            f.mkdir()
        /*
        if (Companion.consoleService == null) {
            val oldStorage=File("${getExternalStorageDirectory().getPath()}/miraiDice/plugins/ZhaoDice")
            val oldStorageRename=File("${getExternalStorageDirectory().getPath()}/miraiDice/plugins/ZhaoDice_")
            if(oldStorage.exists()){
                FileService.copy(oldStorage.absolutePath,zhaoDice)
                oldStorage.renameTo(oldStorageRename)
            }
            object : Thread() {
                override fun run() {
                    //插件只初始化一次
                    val newjar = File(pluginsDir, "mirai-zhao.jar")
                    UpdateService.autoUpdate(newjar)
                    if(newjar.exists())
                        println("自动更新mirai 赵骰插件成功！")
                    else {
                        Util.CopyAssets(this@AppContext, pluginsDir, newjar.name)
                        println("Wrote mirai-zhao.jar -> $pluginsDir")
                    }
                    startControlService(this@AppContext)
                    super.run()
                }
            }.start()
        }*/
        super.onCreate()
    }

    val consoleService: ConsoleService?
        get() = Companion.consoleService

    companion object {
        @JvmField
        var context: AppContext? = null
        var consoleService: ConsoleService? = null
        var miraiDir: String = ""
        @JvmField
        var pluginsDir: String = ""
        var zhaoDice: String = ""
        @JvmField
        var zhaoDiceData: String = ""
        var zhaoDiceDeviceInfo: String = ""
        var autoLoginFile: String = ""
    }
}