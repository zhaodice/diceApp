package org.mirai.zhao.dice.console

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.DeviceInfo
import org.mirai.zhao.dice.AppContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.security.MessageDigest
import java.util.*
import kotlin.random.Random
import kotlin.random.nextInt


@Suppress("MISSING_DEPENDENCY_CLASS")
object AndroidDevice {

    private val defaultRanges: Array<CharRange> = arrayOf('a'..'z', 'A'..'Z', '0'..'9')
    private val intCharRanges: Array<CharRange> = arrayOf('0'..'9')
    private val linuxVersions=floatArrayOf(3.18f, 4.4f, 4.10f, 4.4f)
    private val products = arrayOf("vivo", "xiaomi", "huawei", "5G")
    fun getLinuxKernalInfoEx(): String {
        var result = ""
        var line: String?
        val cmd = arrayOf("/system/bin/cat", "/proc/version")
        val workdirectory = "/system/bin/"
        try {
            val bulider = ProcessBuilder(*cmd)
            bulider.directory(File(workdirectory))
            bulider.redirectErrorStream(true)
            val process = bulider.start()
            val input: InputStream = process.inputStream
            val isrout = InputStreamReader(input)
            val brout = BufferedReader(isrout, 8 * 1024)
            while (brout.readLine().also { line = it } != null) {
                result += line
                // result += "\n";
            }
            input.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
    @SuppressLint("PrivateApi")
    fun getBaseBand():String{
        return try {
            val cl = Class.forName("android.os.SystemProperties")
            val invoker = cl.newInstance()
            val m = cl.getMethod("get", String::class.java, String::class.java)
            val result: Any? = m.invoke(invoker, arrayOf<Any>("gsm.version.baseband", "no message"))
            result as String
        } catch (e: Throwable) {
            ""
        }
    }
    @SuppressLint("HardwareIds")
    private fun getIMEI(): String {
        if(AppContext.context==null)
            return getRandomIntString(15)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getRandomIntString(15)
        } else try {
            val telephonyManager = AppContext.context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephonyManager.deviceId
        } catch (e: java.lang.Exception) {
            getRandomIntString(15)
        }
    }
    @SuppressLint("HardwareIds")
    private fun getIMSIMd5():ByteArray{
        if(AppContext.context==null)
            return getRandomByteArray(16).md5()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getRandomByteArray(16).md5()
        } else try {
            val telephonyManager = AppContext.context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephonyManager.subscriberId.toByteArray().md5()
        } catch (e: java.lang.Exception) {
            getRandomByteArray(16).md5()
        }

    }
    private fun random(): DeviceInfo {
        val product = products.random()
        return DeviceInfo(
                display = "${product.toUpperCase(Locale.ROOT)}.${getRandomString(6, '0'..'9')}.001".toByteArray(),
                product = product.toByteArray(),
                device = product.toByteArray(),
                board = product.toByteArray(),
                brand = product.toByteArray(),
                model = product.toByteArray(),
                bootloader = "unknown".toByteArray(),
                fingerprint = "$product/${getRandomString(6).toUpperCase(Locale.ROOT)}/${getRandomString(6).toUpperCase(Locale.ROOT)}:10/${getRandomString(6).toUpperCase(Locale.ROOT)}.${getRandomIntString(6)}.${getRandomIntString(3)}/${getRandomIntString(7)}:user/release-keys".toByteArray(),
                bootId = generateUUID(getRandomByteArray(16).md5()).toByteArray(),
                procVersion = "Linux version ${linuxVersions.random()}-${getRandomString(8)} (android-build@${getRandomString(16)})".toByteArray(),
                baseBand = byteArrayOf(),
                version = DeviceInfo.Version(),
                simInfo = "T-Mobile".toByteArray(),
                osType = "android".toByteArray(),
                macAddress = "02:00:00:00:00:00".toByteArray(),
                wifiBSSID = "02:00:00:00:00:00".toByteArray(),
                wifiSSID = "<unknown ssid>".toByteArray(),
                imsiMd5 = getRandomByteArray(16).md5(),
                imei = getRandomIntString(15),
                apn = "wifi".toByteArray()
        )
    }
    private fun real(): DeviceInfo {
        return DeviceInfo(
                display = Build.DISPLAY.toByteArray(),
                product = Build.PRODUCT.toByteArray(),
                device = Build.DEVICE.toByteArray(),
                board = Build.BOARD.toByteArray(),
                brand = Build.BRAND.toByteArray(),
                model = Build.MODEL.toByteArray(),
                bootloader = Build.BOOTLOADER.toByteArray(),
                fingerprint = Build.FINGERPRINT.toByteArray(),
                bootId = generateUUID(getRandomByteArray(16).md5()).toByteArray(),
                procVersion = getLinuxKernalInfoEx().toByteArray(),
                baseBand = getBaseBand().toByteArray(),
                version = DeviceInfo.Version(),
                simInfo = "T-Mobile".toByteArray(),
                osType = "android".toByteArray(),
                macAddress = "02:00:00:00:00:00".toByteArray(),
                wifiBSSID = "02:00:00:00:00:00".toByteArray(),
                wifiSSID = "<unknown ssid>".toByteArray(),
                imsiMd5 = getIMSIMd5().md5(),
                imei = getIMEI(),
                apn = "wifi".toByteArray()
        )
    }
    private fun generateUUID(md5: ByteArray): String {
        return "${md5[0, 3]}-${md5[4, 5]}-${md5[6, 7]}-${md5[8, 9]}-${md5[10, 15]}"
    }
    operator fun ByteArray.get(rangeStart: Int, rangeEnd: Int): String = buildString {
        for (it in rangeStart..rangeEnd) {
            append(this@get[it].fixToString())
        }
    }
    private fun Byte.fixToString(): String {
        return when (val b = this.toInt() and 0xff) {
            in 0..15 -> "0${this.toString(16).toUpperCase(Locale.ROOT)}"
            else -> b.toString(16).toUpperCase(Locale.ROOT)
        }
    }
    /**
     * 生成长度为 [length], 元素为随机 `0..255` 的 [ByteArray]
     */
    private fun getRandomByteArray(length: Int): ByteArray = ByteArray(length) { Random.nextInt(0..255).toByte() }
    private fun ByteArray.md5(offset: Int = 0, length: Int = size - offset): ByteArray {
        checkOffsetAndLength(offset, length)
        return MessageDigest.getInstance("MD5").apply { update(this@md5, offset, length) }.digest()
    }
    private fun ByteArray.checkOffsetAndLength(offset: Int, length: Int) {
        require(offset >= 0) { "offset shouldn't be negative: $offset" }
        require(length >= 0) { "length shouldn't be negative: $length" }
        require(offset + length <= this.size) { "offset ($offset) + length ($length) > array.size (${this.size})" }
    }
    /**
     * 根据所给 [charRange] 随机生成长度为 [length] 的 [String].
     */
    private fun getRandomString(length: Int, charRange: CharRange): String =
            CharArray(length) { charRange.random() }.concatToString()

    /**
     * 根据所给 [charRanges] 随机生成长度为 [length] 的 [String].
     */
    private fun getRandomString(length: Int, vararg charRanges: CharRange): String =
            CharArray(length) { charRanges[Random.Default.nextInt(0..charRanges.lastIndex)].random() }.concatToString()


    private fun getRandomIntString(length: Int): String =
            getRandomString(length, *intCharRanges)
    private fun getRandomString(length: Int): String =
            getRandomString(length, *defaultRanges)

    fun getFileBasedDeviceInfoSupplier(file:File,realDevice:Boolean): (Bot) -> DeviceInfo {
        return {
            if (!file.exists() || file.length() == 0L) {
                if(realDevice){
                    real()
                }else{
                    random()
                }.also {
                    file.writeText(Json.encodeToString(serializer(), it))
                }
            }
            Json.decodeFromString(serializer(), file.readText())
        }
    }
}