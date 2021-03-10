package org.mirai.zhao.dice.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.util.Log;

/**
 * 文件保存与读取功能实现类
 * @author Administrator
 *
 * 2010-6-28 下午08:15:18
 */
public class FileService {
    public static final String TAG = "FileService";
    public static void copy(String path, String copyPath) throws IOException{
        File filePath = new File(path);
        DataInputStream read ;
        DataOutputStream write;
        if(filePath.isDirectory()){
            File[] list = filePath.listFiles();
            for (File file : list) {
                String newPath = path + File.separator + file.getName();
                String newCopyPath = copyPath + File.separator + file.getName();
                File newFile = new File(copyPath);
                if (!newFile.exists()) {
                    newFile.mkdir();
                }
                copy(newPath, newCopyPath);
            }
        }else if(filePath.isFile()){
            read = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(path)));
            write = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(copyPath)));
            byte [] buf = new byte[1024*512];
            int length;
            while((length=read.read(buf)) != -1){
                write.write(buf,0,length);
            }
            read.close();
            write.close();
        }else{
            System.out.println("请输入正确的文件名或路径名");
        }
    }
    public static String fillZero(String s,int len){
        StringBuilder filled=new StringBuilder();
        int needRepeat=len-s.length();
        if(needRepeat<=0)
            return s;
        while(needRepeat-->0){
            filled.append("0");
        }
        filled.append(s);
        return filled.toString();
    }
    /*获取文件MD5*/
    public static String getFileMD5(InputStream is) {
        BigInteger bi = null;
        try {
            byte[] buffer = new byte[8192];
            int len = 0;
            MessageDigest md = MessageDigest.getInstance("MD5");
            while ((len = is.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            is.close();
            byte[] b = md.digest();
            bi = new BigInteger(1, b);
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        if(bi==null)
            return null;
        return fillZero(bi.toString(16),32);
    }
    /*获取文件MD5*/
    public static String getFileMD5(File path) {
        try {
            return getFileMD5(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
