package org.mirai.zhao.dice.web

import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object WebUtil {
    /**
     * @功能 下载临时素材接口
     * @param filePath 文件将要保存的目录
     * @param url 请求的路径
     * @return
     */
    fun saveUrlAs(url: String?, path: String) {
        //System.out.println("fileName---->"+filePath);
        var filePath = path
        val fileOut: FileOutputStream
        val conn: HttpURLConnection
        val inputStream: InputStream
        try {
            //创建不同的文件夹目录
            val file = File(filePath)
            //判断文件夹是否存在
            if (!file.exists()) {
                //如果文件夹不存在，则创建新的的文件夹
                file.createNewFile()
            }
            // 建立链接
            val httpUrl = URL(url)
            conn = httpUrl.openConnection() as HttpURLConnection
            //以Post方式提交表单，默认get方式
            conn.requestMethod = "GET"
            conn.doInput = true
            // post方式不能使用缓存
            conn.useCaches = false
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20100101 Firefox/80.0")
            //连接指定的资源
            conn.connect()
            //获取网络输入流
            inputStream = conn.inputStream
            val bis = BufferedInputStream(inputStream)
            //判断文件的保存路径后面是否以/结尾
            if (!filePath.endsWith("/")) {
                filePath += "/"
            }
            //写入到文件（注意文件保存路径的后面一定要加上文件的名称）
            fileOut = FileOutputStream(filePath)
            val bos = BufferedOutputStream(fileOut)
            val buf = ByteArray(4096)
            var length = bis.read(buf)
            //保存文件
            while (length != -1) {
                bos.write(buf, 0, length)
                length = bis.read(buf)
            }
            bos.close()
            bis.close()
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
            println("抛出异常！！")
        }
    }

    fun getWebinfo(str_url: String?): String? {
        try {
            //1.找水源---创建URL（统一资源定位器）
            val url = URL(str_url)
            //2.开水闸---openCOnnection
            val httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.connectTimeout = 2000
            httpURLConnection.readTimeout = 3000
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20100101 Firefox/80.0")
            //3.建管道---InputStream
            val inputStream = httpURLConnection.inputStream
            //4.建蓄水池---InputStreamReader
            val inputStreamReader = InputStreamReader(inputStream, StandardCharsets.UTF_8)
            //5.水桶盛水——BufferedReader
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuffer = StringBuilder()
            var temp: String?
            //循环做盛水工作---while循环
            while (bufferedReader.readLine().also { temp = it } != null) {
                stringBuffer.append(temp)
            }
            //关闭水池入口，从管道到水桶
            bufferedReader.close()
            inputStreamReader.close()
            inputStream.close()
            //打印日志
            //Log.e("Main",stringBuffer.toString());
            return stringBuffer.toString()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }
}