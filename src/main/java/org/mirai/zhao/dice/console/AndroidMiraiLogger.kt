package org.mirai.zhao.dice.console

import net.mamoe.mirai.utils.MiraiLoggerPlatformBase
import org.mirai.zhao.dice.console.AndroidMiraiLogger.LogBuilder.Companion.LEVEL_DEBUG
import org.mirai.zhao.dice.console.AndroidMiraiLogger.LogBuilder.Companion.LEVEL_ERROR
import org.mirai.zhao.dice.console.AndroidMiraiLogger.LogBuilder.Companion.LEVEL_INFO
import org.mirai.zhao.dice.console.AndroidMiraiLogger.LogBuilder.Companion.LEVEL_WARNING
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AndroidMiraiLogger(override val identity: String?) : MiraiLoggerPlatformBase() {

    @JvmField
    val logStorage=LogBuilder().limit(500)
    class LogBuilder {
        private var limitLineNum=-1
        companion object{
            const val LEVEL_INFO=3
            const val LEVEL_DEBUG=2
            const val LEVEL_WARNING=1
            const val LEVEL_ERROR=0
        }

        class Log(
                private val level: Int,
                private val text:String,
        ){
            private val time=System.currentTimeMillis()
            override fun toString():String{
                val res: String
                val simpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
                val date = Date(time)
                res = simpleDateFormat.format(date)
                var levelStr="[UNKNOWN]"
                when(level){
                    LEVEL_DEBUG ->
                        levelStr="<font color=\"#006699\">[DEBUG]</font>"
                    LEVEL_INFO ->
                        levelStr="<font color=\"#009900\">[INFO]</font>"
                    LEVEL_ERROR ->
                        levelStr="<font color=\"#990000\">[ERROR]</font>"
                    LEVEL_WARNING ->
                        levelStr="<font color=\"#999933\">[WARNING]</font>"
                }
                return String.format("<font color=\"#333366\">[%s]</font> %s %s ",res,levelStr,text)
            }
        }
        private val list=ArrayList<Log>()
        fun append(level: Int, text: String): LogBuilder {
            synchronized(this){
                val log=Log(level, text)
                list.add(log)
                if(list.size>limitLineNum){
                    list.removeAt(0)
                }
                ConsoleService.onLogChangedListener?.logChanged(log.toString())
                return this
            }
        }
        fun build():String{
            synchronized(this) {
                val sb = StringBuilder()
                for (log in list) {
                    sb.append(log.toString()).append("<br/>")
                }
                return sb.toString()
            }
        }
        fun clear(): LogBuilder {
            synchronized(this) {
                list.clear()
                return this
            }
        }
        fun limit(lineNum:Int): LogBuilder {
            limitLineNum=lineNum
            return this
        }
    }
    override fun debug0(message: String?, e: Throwable?) {
        if (message != null) {
            logStorage.append(LEVEL_DEBUG,message)
        }
    }

    override fun error0(message: String?, e: Throwable?) {
        if (message != null) {
            logStorage.append(LEVEL_ERROR,message)
        }
    }

    override fun info0(message: String?, e: Throwable?) {
        if (message != null) {
            logStorage.append(LEVEL_INFO,message)
        }
    }

    override fun verbose0(message: String?, e: Throwable?) {
        if (message != null) {
            logStorage.append(LEVEL_INFO,message)
        }
    }

    override fun warning0(message: String?, e: Throwable?) {
        if (message != null) {
            logStorage.append(LEVEL_WARNING,message)
        }
    }
}