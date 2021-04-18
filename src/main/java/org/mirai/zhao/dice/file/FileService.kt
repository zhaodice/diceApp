package org.mirai.zhao.dice.file

import java.io.*
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * 文件保存与读取功能实现类
 * @author Administrator
 *
 * 2010-6-28 下午08:15:18
 */
object FileService {
    const val TAG = "FileService"
    @Throws(IOException::class)
    fun copy(path: String, copyPath: String) {
        val filePath = File(path)
        val read: DataInputStream
        val write: DataOutputStream
        if (filePath.isDirectory) {
            val list = filePath.listFiles() as Array<File>
            for (file in list) {
                val newPath = path + File.separator + file.name
                val newCopyPath = copyPath + File.separator + file.name
                val newFile = File(copyPath)
                if (!newFile.exists()) {
                    newFile.mkdir()
                }
                copy(newPath, newCopyPath)
            }
        } else if (filePath.isFile) {
            read = DataInputStream(
                    BufferedInputStream(FileInputStream(path)))
            write = DataOutputStream(
                    BufferedOutputStream(FileOutputStream(copyPath)))
            val buf = ByteArray(1024 * 512)
            var length: Int
            while (read.read(buf).also { length = it } != -1) {
                write.write(buf, 0, length)
            }
            read.close()
            write.close()
        } else {
            println("请输入正确的文件名或路径名")
        }
    }

    fun fillZero(s: String, len: Int): String {
        val filled = StringBuilder()
        var needRepeat = len - s.length
        if (needRepeat <= 0) return s
        while (needRepeat-- > 0) {
            filled.append("0")
        }
        filled.append(s)
        return filled.toString()
    }

    /*获取文件MD5*/
    fun getFileMD5(`is`: InputStream): String? {
        var bi: BigInteger? = null
        try {
            val buffer = ByteArray(8192)
            var len:Int
            val md = MessageDigest.getInstance("MD5")
            while (`is`.read(buffer).also { len = it } != -1) {
                md.update(buffer, 0, len)
            }
            `is`.close()
            val b = md.digest()
            bi = BigInteger(1, b)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return if (bi == null) null else fillZero(bi.toString(16), 32)
    }

    /*获取文件MD5*/
    fun getFileMD5(path: File?): String? {
        return try {
            getFileMD5(FileInputStream(path))
        } catch (e: FileNotFoundException) {
            null
        }
    }
}