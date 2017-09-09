package com.example.helena.voicedit_android.utils;

/**
 * Created by helena on 2017/8/18.
 */

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialogUtils {
    private static ProgressDialog mProgressDialog;

    /**
     * 显示ProgressDialog
     * @param context
     * @param message
     */
    public static void showProgressDialog(Context context, CharSequence message){
        if(mProgressDialog == null){
            mProgressDialog = ProgressDialog.show(context, "", message);
        }else{
            mProgressDialog.show();
        }
    }

    /**
     * 关闭ProgressDialog
     */
    public static void dismissProgressDialog(){
        if(mProgressDialog != null){
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
