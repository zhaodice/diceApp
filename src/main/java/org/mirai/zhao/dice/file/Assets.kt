package org.mirai.zhao.dice.file

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

object Assets {
    fun copyAssets(context: Context, dir: String, fileName: String): Boolean {
        //String[] files;
        val mWorkingPath = File(dir)
        if (!mWorkingPath.exists()) {
            if (!mWorkingPath.mkdirs()) {
                println("cannot create plugins dir")
            }
        }
        try {
            val outFile = File(mWorkingPath, fileName)
            if (outFile.exists()) { //文件存在不写入
                return true
            }
            val `in` = context.assets.open(fileName)
            val out: OutputStream = FileOutputStream(outFile)
            // Transfer bytes from in to out
            val buf = ByteArray(1024)
            var len: Int
            while (`in`.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
            `in`.close()
            out.close()
            return true
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        return false
    }
}