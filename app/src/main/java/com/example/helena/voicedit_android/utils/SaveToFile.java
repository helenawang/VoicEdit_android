package com.example.helena.voicedit_android.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by helena on 2017/8/20.
 */

public class SaveToFile {
    private static final String TAG = "SaveToFile";
    public static boolean saveToFile(String filename, String json) {
        boolean ret = false;
        if(isExternalStorageWritable() && isExternalStorageReadable()) {
            File file = new File(Environment.getExternalStorageDirectory(), filename);
            try {
                if (!file.exists()) {
                    Log.i(TAG, "saveToFile: create new file");
                    file.createNewFile();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(file, true);//append
                fileOutputStream.write(json.getBytes());
                fileOutputStream.close();
                Log.i(TAG, "saveToFile: 写入外部存储成功");
                ret = true;
                return ret;
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.e(TAG, "saveToFile: 外部存储不可用");
        return ret;
    }

    //来自https://developer.android.google.cn/training/basics/data-storage/files.html
    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
