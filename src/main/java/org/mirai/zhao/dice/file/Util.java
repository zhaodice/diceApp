package org.mirai.zhao.dice.file;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Util {
    public static boolean CopyAssets(Context context, String dir, String fileName){
        //String[] files;
        File mWorkingPath = new File(dir);
        if (!mWorkingPath.exists()) {
            if (!mWorkingPath.mkdirs()) {
                System.out.println("cannot create plugins dir");
            }
        }
        try {
            File outFile = new File(mWorkingPath, fileName);
            if(outFile.exists()) {//文件存在不写入
                return true;
            }
            InputStream in = context.getAssets().open(fileName);
            OutputStream out = new FileOutputStream(outFile);
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            return true;
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return false;
    }
}
