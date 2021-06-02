package org.mirai.zhao.dice

import android.app.Application
import android.content.Context
import android.os.Environment.getExternalStorageDirectory
import androidx.multidex.MultiDex
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.mirai.zhao.dice.console.ConsoleService
import java.io.File
import java.security.Provider
import java.security.Security


class AppContext : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
    override fun onCreate() {
        context = this
        dataStorage = getExternalStorageDirectory().path
        val f=File(miraiDir)
        if(!f.exists())
            f.mkdir()
        System.setProperty("zhao.dice.plugins.dir", pluginsDir)
        System.setProperty("mirai.slider.captcha.supported", "1")

        try {
            val provider = classLoader.loadClass("org.bouncycastle.jce.provider.BouncyCastleProvider").newInstance() as Provider
            // Remove the OS provided bouncy castle provider
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
            // Add the bouncy castle provider from the added library
            //Security.addProvider(BouncyCastleProvider()) //坑死我了，这个代码在安卓5.0上会造成闪退！！
            Security.insertProviderAt(provider, 1)
        }catch (e: Throwable){
            e.printStackTrace()
        }
        try {
            val clz: Class<*> = Class.forName("net.mamoe.mirai.console.internal.MiraiConsoleBuildConstants")
            val versionConstField = clz.getDeclaredField("version")
            versionConstField.isAccessible = true
            val clz2: Class<*> = Class.forName("net.mamoe.mirai.console.util.SemVersion")
            val m=clz2.getMethod("parse", String::class.java)
            versionConstField.set(null, m.invoke(null, miraiCoreVersion))
        }catch (e: Throwable){
            e.printStackTrace()
        }
        super.onCreate()
    }

    val consoleService: ConsoleService?
        get() = Companion.consoleService

    companion object {
        var dataStorage = ""
        var context: AppContext? = null
        var consoleService: ConsoleService? = null
        val pluginsDir get() = if(context!=null)
            context!!.getDir("plugins", 0).absolutePath
        else
            ""
        val miraiDir get() = "$dataStorage/MiraiDice"
        val zhaoDice get() = "$miraiDir/data/ZhaoDice"
        val zhaoDiceRootData get() = "$zhaoDice/cocdata/data"
        val zhaoDiceData: String
            get(){
                if(currentEditQQ!=null) {
                    val subDir = "$zhaoDice/cocdata_$currentEditQQ/data"
                    if (File(subDir).isDirectory) {
                        return subDir
                    }
                }
                return zhaoDiceRootData
            }
        val zhaoDiceDeviceInfo get()= "$zhaoDiceRootData/deviceInfo"
        val autoLoginFile get()= "$zhaoDiceRootData/autoLogin.txt"
        var currentEditQQ:String? = null
        private const val miraiCoreVersion = "2.6.5-dalvik"
    }
}