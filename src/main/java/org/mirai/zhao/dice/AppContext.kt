package org.mirai.zhao.dice

import android.app.Application
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import org.mirai.zhao.dice.console.ConsoleService.Companion.startControlService
import org.mirai.zhao.dice.crash.CrashHandler
import org.mirai.zhao.dice.console.ConsoleService
import org.mirai.zhao.dice.file.FileService
import org.mirai.zhao.dice.file.Util
import org.mirai.zhao.dice.web.UpdateService
import java.io.File

class AppContext : Application() {
    var dataStorage: String? = null
    val privateStorage: String? = null
    override fun onCreate() {
        System.setProperty("mirai.slider.captcha.supported","1")
        val crashHandler = CrashHandler.getInstance()
        crashHandler.init()
        //dataStorage = getExternalFilesDir(null).toString()
        dataStorage = getExternalStorageDirectory().path
        //privateStorage = getExternalStorageDirectory().path
        miraiDir = "$dataStorage/MiraiDice"
        pluginsDir = "$miraiDir/plugins"
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
        var pluginsDir: String = ""
        var zhaoDice: String = ""
        @JvmField
        var zhaoDiceData: String = ""
        var zhaoDiceDeviceInfo: String = ""
        var autoLoginFile: String = ""
    }
}