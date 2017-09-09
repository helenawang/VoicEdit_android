package com.example.helena.voicedit_android.utils;

/**
 * Created by helena on 2017/8/18.
 */

public class ErrorEscape {

    //TODO : 业务提的需要优化的提示语
    public static String TRAIN_ERROR_TAIL = "，请重试";
    public static String TRAIN_ERROR_WRONG_NUMBER 	= "本条语音录制失败，录制的语音与屏幕显示数字不符，请您正确读出屏幕显示数字";
    public static String TRAIN_ERROR_NOISE			= "本条语音录制失败，环境噪音太大，请重试";
    public static String TRAIN_ERROR_VOLUME_LOW 	= "本条语音录制失败，您的音量太小，请重试";
    public static String TRAIN_ERROR_VOLUME_HIGH	= "本条语音录制失败，您的音量太大，请重试";
    public static String TRAIN_ERROR_NETWORK 		= "本条语音录制失败，网络中断，请您检查网络后重试";
    public static String TRAIN_ERROR_TRAINING			= "您的声纹正在更新，请稍后再试";
    public static String TRAIN_ERROR_WRONG_ID		= "声纹信息有误，请重试";
    public static String TRAIN_VOICEPRINT_TIMEOUT 		= "声纹预留超时，请重新进行操作";
    public static String TRAIN_VOICEPRINT_LOCKED 		= "同一用户三分钟内只可获取一次建模文本";

    // authd error
    public static String SSIONID_TIMEOUT = "会话超时，请重新进行操作！";
    public static String CONNECTSIMPLERERROR = "连接超时！请检查您的网络。" ;

    public static String errorTrans_train(int errorCode) {
        switch (errorCode) {
            case 2006:
            case 2008:
                return TRAIN_ERROR_VOLUME_HIGH ;
            case 20007:
                return TRAIN_ERROR_VOLUME_LOW ;
            case 20009:
                return TRAIN_ERROR_NOISE ;
            case 20011:
                return TRAIN_VOICEPRINT_LOCKED;
            case 20014:
                return TRAIN_ERROR_WRONG_NUMBER;
            default :
                return null;
        }
    }
}
