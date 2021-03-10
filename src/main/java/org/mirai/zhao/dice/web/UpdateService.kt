package org.mirai.zhao.dice.web

import org.mirai.zhao.dice.file.FileService
import java.io.File
import java.util.*


object UpdateService {
    private fun strGet(input: String, key: String):String?{
        val start=input.indexOf(key)
        if(start==-1)
            return null;
        val end=input.indexOf("#end#", start)
        return input.substring(start + key.length, end)
    }
    @JvmStatic
    fun autoUpdate(pathFile:File){
        val targetFile=pathFile
        var newUrl:String?="https://gitee.com/zhaodice/appupdate/blob/master/diceAppUpdate.txt"
        var update_str:String?
        do{
            update_str= WebUtil.getWebinfo(newUrl)
            if(update_str==null)
                return
            newUrl=strGet(update_str, "#to#")
        }while (newUrl!=null)
        if(update_str==null)
            return
        val jar_url:String?
        val jar_md5:String?
        if(isAndroid0()) {
            jar_url = strGet(update_str, "#android_m2_jar_url#")
            jar_md5 = strGet(update_str, "#android_m2_jar_md5#")
        }else {
            jar_url = strGet(update_str, "#computer_m2_jar_url#")
            jar_md5 = strGet(update_str, "#computer_m2_jar_md5#")
        }
        if(jar_md5!=null&&jar_url!=null){
            val local_jar_md5= FileService.getFileMD5(pathFile)
            if(local_jar_md5==null||!jar_md5.toLowerCase(Locale.ROOT).equals(local_jar_md5.toLowerCase(Locale.ROOT))){//本地文件不存在或md5不同步
                //下载并保存文件
                val tempFile=pathFile.absolutePath+".tmp"
                val _tempFile=File(tempFile)
                if(_tempFile.exists())
                    _tempFile.delete()
                WebUtil.saveUrlAs(jar_url,tempFile)
                //校验md5
                val local_tmp_jar_md5=FileService.getFileMD5(_tempFile)
                if(local_tmp_jar_md5!=null) {
                    if (jar_md5.toLowerCase(Locale.ROOT).equals(local_tmp_jar_md5.toLowerCase(Locale.ROOT))) {
                        targetFile.delete()
                        _tempFile.renameTo(targetFile)
                        print("ZhaoDice! [Update] 更新成功！\n")
                        return
                    } else {
                        _tempFile.delete()
                    }
                }
            }else{
                print("ZhaoDice! [Update] 文件未改变！\n")
            }
        }
        print("ZhaoDice! [Update] 更新失败！\n")
    }

    //https://github.com/netty/netty/blob/162e59848ad1801ab26e501c3c93ee08e83f5065/common/src/main/java/io/netty/util/internal/PlatformDependent0.java
    private fun isAndroid0(): Boolean {
        // Idea: Sometimes java binaries include Android classes on the classpath, even if it isn't actually Android.
        // Rather than check if certain classes are present, just check the VM, which is tied to the JDK.

        // Optional improvement: check if `android.os.Build.VERSION` is >= 24. On later versions of Android, the
        // OpenJDK is used, which means `Unsafe` will actually work as expected.

        // Android sets this property to Dalvik, regardless of whether it actually is.
        val vmName: String? = System.getProperty("java.vm.name")
        val isAndroid = "Dalvik" == vmName
        return isAndroid
    }
}