package com.example.helena.voicedit_android.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.dear.smb.android.api.CallBackResult;
import com.dear.smb.android.api.NameValuePair;
import com.dear.smb.android.api.SMBAPIConstant;
import com.dear.smb.android.api.TrainerCallBack;
import com.dear.voice.IRecordListener;
import com.dear.voice.VoiceFormat;
import com.dear.voice.VoiceRecorder;
import com.example.helena.voicedit_android.R;
import com.example.helena.voicedit_android.utils.Constant;
import com.example.helena.voicedit_android.utils.ErrorEscape;
import com.example.helena.voicedit_android.utils.ProgressDialogUtils;
import com.example.helena.voicedit_android.utils.SharedPreferencesUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class TrainerActivity extends Activity {

    private VoiceRecorder voiceRecorder;//声明录音 工具类对象
    private TextView mNumberTV, mVervif_prompt;
    private Button mMicBtn;
    private byte[] recordData ;

    private String SessionId ;
    private int Index = 0 ;
    private String[] traintext = new String[5] ;

    private NameValuePair nameValuePair = new NameValuePair() ;
    private TrainerCallBack trainerCallBack ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer);


        initView() ;

        initSmbController() ;

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        initRecordTools() ;
    }

    protected void showToast(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL,
                0, 0);
        toast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceRecorder != null) {
            voiceRecorder.releaseRecord();//释放录音资源
            voiceRecorder = null ;
        }
    }

    private void initSmbController() {

        trainerCallBack = new TrainerCallBack(this) {
            @Override
            public void onSuccess(CallBackResult callBackResult) {
                super.onSuccess(callBackResult);

                switch (callBackResult.getTag()) {
                    case 1:
                        //getTextSuccess
                        ProgressDialogUtils.dismissProgressDialog();
                        String temp = callBackResult.getTrainerTextResult().getText() ;
                        int interval = callBackResult.getTrainerTextResult().getInterval() ;
                        SessionId = callBackResult.getTrainerTextResult().getSessionId() ;
                        JsonArray object = new JsonParser().parse(temp).getAsJsonArray();
                        traintext = new String[5];
                        for (int i = 0; i < 5; i++) {
                            traintext[i] =  object.get(i).toString().replaceAll("\"", "");
                        }
                        mNumberTV.setText(traintext[Index]);
                        break;
                    case 2:
                        //UploadVoiceSuccess
                        ProgressDialogUtils.dismissProgressDialog();
                        int index = callBackResult.getTrainerUploadVoiceResult().getIndex();
                        showToast(getApplicationContext(), "第"+index+"条语音上传成功！");
                        SessionId = callBackResult.getTrainerUploadVoiceResult().getSessionId() ;
                        if(index == 5 ){
                            ProgressDialogUtils.showProgressDialog(TrainerActivity.this , "建模中...");
                            nameValuePair.setSessionId(SessionId);
                            nameValuePair.setVoiceprintId(Constant.HttpCode.USERNAME);
                            trainerCallBack.executeTrainer(nameValuePair);
                            return ;
                        }
                        Index++ ;
                        mNumberTV.setText(traintext[Index]);
                        break;
                    case 3:
                        //TrainerSucccess
                        ProgressDialogUtils.dismissProgressDialog();
                        String VoiceprintID = callBackResult.getTrainerResult().getVoiceprintId() ;
                        showToast(getApplicationContext(), "建模成功！ID:"+VoiceprintID);
                        System.out.println("VoiceprintID:"+VoiceprintID);
                        SharedPreferencesUtils.setParam(getApplicationContext(), "voiceprintID", VoiceprintID);
                        finish();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFailure(int exceptionMsg) {
                // TODO Auto-generated method stub
                super.onFailure(exceptionMsg);
                ProgressDialogUtils.dismissProgressDialog();
                if(ErrorEscape.errorTrans_train(exceptionMsg)!=null){
                    showToast(getApplicationContext(), ErrorEscape.errorTrans_train(exceptionMsg));
                }else{
                    showToast(getApplicationContext(), "服务端异常！("+exceptionMsg+")");
                    finish();
                }

            }
        };

        ProgressDialogUtils.showProgressDialog(this, "获取建模文本...");
        nameValuePair.setTrainType(SMBAPIConstant.TEXT_TYPE_TRAINING_8_5);
        nameValuePair.setVoiceprintId(Constant.HttpCode.USERNAME);
        nameValuePair.setDeviceInfo("");
        trainerCallBack.executeGetText(nameValuePair);

    }


    OnTouchListener touchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int btn_id = v.getId();
            if (btn_id == R.id.btn_mic) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    voiceRecorder.startRecord();//开始录音
                    mVervif_prompt.setText(getString(R.string.loosen_end));
                    mMicBtn.setBackgroundResource(R.drawable.mic_down);
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    mVervif_prompt.setText(getString(R.string.pass_say));
                    mMicBtn.setBackgroundResource(R.drawable.mic_up);
                    voiceRecorder.stopRecord();//停止录音
                    recordData = voiceRecorder.getRecordData() ;//获取录音数据,只可get一次，第二次为null
                    ProgressDialogUtils.showProgressDialog(TrainerActivity.this, "上传语音中...");
                    nameValuePair.setSessionId(SessionId);
                    nameValuePair.setIndex(Index);
                    nameValuePair.setDate(recordData);
                    trainerCallBack.executeUploadVoice(nameValuePair);
                    return true;
                }
            }
            return false;
        }
    };


    private void initRecordTools() {
        // TODO Auto-generated method stub
        mMicBtn.setOnTouchListener(touchListener);
        //初始化录音工具类
        if (voiceRecorder == null) {
            // 16K 16比特 单声道 pcm编码
            voiceRecorder = new VoiceRecorder(getApplicationContext(), VoiceFormat.AUDIOFORMAT_16K16BIT_MONO);
        }
        voiceRecorder.setRecordListener(recordListener,15L);//设置监听器

    }

    private void initView() {
        // TODO Auto-generated method stub
        mNumberTV = (TextView) findViewById(R.id.tv_verify_number);
        mVervif_prompt = (TextView) findViewById(R.id.vervif_prompt);
        mMicBtn = (Button) findViewById(R.id.btn_mic);
    }

    // 监听器
    IRecordListener recordListener = new IRecordListener() {
        private long interval = 0L;
        @Override
        public void onStopRecord() {

        }

        @Override
        public void onStartRecord() {

        }

        @Override
        public void onRecording(byte[] data, double decibelValue) {
        }

        @Override
        public void setInterval(long interval) {
            this.interval = interval;
        }

        @Override
        public long getInterval() {
            return this.interval;
        }

    };

}
