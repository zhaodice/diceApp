package org.mirai.zhao.dice.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WebUtil {
    /**
     * @功能 下载临时素材接口
     * @param filePath 文件将要保存的目录
     * @param url 请求的路径
     * @return
     */

    public static void saveUrlAs(String url, String filePath){
        //System.out.println("fileName---->"+filePath);

        FileOutputStream fileOut = null;
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        try
        {
            //创建不同的文件夹目录
            File file=new File(filePath);
            //判断文件夹是否存在
            if (!file.exists())
            {
                //如果文件夹不存在，则创建新的的文件夹
                file.createNewFile();
            }
            // 建立链接
            URL httpUrl=new URL(url);
            conn=(HttpURLConnection) httpUrl.openConnection();
            //以Post方式提交表单，默认get方式
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // post方式不能使用缓存
            conn.setUseCaches(false);
            conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20100101 Firefox/80.0");
            //连接指定的资源
            conn.connect();
            //获取网络输入流
            inputStream=conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            //判断文件的保存路径后面是否以/结尾
            if (!filePath.endsWith("/")) {

                filePath += "/";

            }
            //写入到文件（注意文件保存路径的后面一定要加上文件的名称）
            fileOut = new FileOutputStream(filePath);
            BufferedOutputStream bos = new BufferedOutputStream(fileOut);

            byte[] buf = new byte[4096];
            int length = bis.read(buf);
            //保存文件
            while(length != -1)
            {
                bos.write(buf, 0, length);
                length = bis.read(buf);
            }
            bos.close();
            bis.close();
            conn.disconnect();
        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("抛出异常！！");
        }
    }
    public static String getWebinfo(String str_url) {
        try {
            //1.找水源---创建URL（统一资源定位器）
            URL url=new URL(str_url);
            //2.开水闸---openCOnnection
            HttpURLConnection httpURLConnection= (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20100101 Firefox/80.0");
            //3.建管道---InputStream
            InputStream inputStream=httpURLConnection.getInputStream();
            //4.建蓄水池---InputStreamReader
            InputStreamReader inputStreamReader=new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            //5.水桶盛水——BufferedReader
            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
            StringBuilder stringBuffer=new StringBuilder();
            String temp;
            //循环做盛水工作---while循环
            while ((temp=bufferedReader.readLine())!=null){
                stringBuffer.append(temp);
            }
            //关闭水池入口，从管道到水桶
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            //打印日志
            //Log.e("Main",stringBuffer.toString());
            return stringBuffer.toString();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
