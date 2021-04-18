package org.mirai.zhao.dice

import android.app.Application
import android.content.Context
import android.os.Environment.getExternalStorageDirectory
import androidx.multidex.MultiDex
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.mirai.zhao.dice.console.ConsoleService
import java.io.File
import java.security.Security


class AppContext : Application() {
    val privateStorage: String? = null
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
    override fun onCreate() {
        //dataStorage = getExternalFilesDir(null).toString()
        dataStorage = getExternalStorageDirectory().path
        //privateStorage = getExternalStorageDirectory().path
        miraiDir = "$dataStorage/MiraiDice"
        pluginsDir = getDir("plugins", 0).absolutePath;//"$miraiDir/plugins"
        zhaoDice = "$miraiDir/data/ZhaoDice"
        zhaoDiceData = "$zhaoDice/cocdata/data"
        zhaoDiceDeviceInfo = "$zhaoDiceData/deviceInfo"
        autoLoginFile = "$zhaoDiceData/autoLogin.txt"
        val f=File(miraiDir)
        if(!f.exists())
            f.mkdir()
        System.setProperty("zhao.dice.plugins.dir",pluginsDir)
        System.setProperty("mirai.slider.captcha.supported", "1")
        context = this
        try {
            val bouncyCastleProvider=BouncyCastleProvider()
            // Remove the OS provided bouncy castle provider
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
            // Add the bouncy castle provider from the added library
            //Security.addProvider(BouncyCastleProvider()) //坑死我了，这个代码在安卓5.0上会造成闪退！！
            Security.insertProviderAt(bouncyCastleProvider, 1)
        }catch (e:Throwable){
            e.printStackTrace()
        }
        super.onCreate()
    }

    val consoleService: ConsoleService?
        get() = Companion.consoleService

    companion object {
        lateinit var dataStorage: String
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