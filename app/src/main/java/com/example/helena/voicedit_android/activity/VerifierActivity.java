package com.example.helena.voicedit_android.activity;

import com.dear.smb.android.api.CallBackResult;
import com.dear.smb.android.api.NameValuePair;
import com.dear.smb.android.api.SMBAPIConstant;
import com.dear.smb.android.api.VerifierCallBack;
import com.dear.voice.IRecordListener;
import com.dear.voice.VoiceFormat;
import com.dear.voice.VoiceRecorder;
import com.example.helena.voicedit_android.R;
import com.example.helena.voicedit_android.utils.Constant;
import com.example.helena.voicedit_android.utils.ErrorEscape;
import com.example.helena.voicedit_android.utils.ProgressDialogUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class VerifierActivity extends Activity {
    private VoiceRecorder voiceRecorder;//声明录音 工具类对象
    private TextView mNumberTV, mVervif_prompt;
    private Button mMicBtn;
    private byte[] recordData ;
    private String SessionId ;
    public final static int RESULT_CODE=1;
    private NameValuePair nameValuePair = new NameValuePair() ;
    private VerifierCallBack verifierCallBack ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifier);

        initView() ;

        initSmbController() ;
        ProgressDialogUtils.showProgressDialog(this, "获取身份验证文本...");
        nameValuePair.setVerifiyType(SMBAPIConstant.TEXT_TYPE_VERIFY_8_5);
        nameValuePair.setVoiceprintId(Constant.HttpCode.USERNAME);
        verifierCallBack.executeGetText(nameValuePair);
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
        // TODO Auto-generated method stub
        verifierCallBack = new VerifierCallBack(this) {
            @Override
            public void onSuccess(CallBackResult callBackResult) {
                super.onSuccess(callBackResult);

                switch (callBackResult.getTag()) {
                    case 1:
                        //getTextSuccess
                        ProgressDialogUtils.dismissProgressDialog();
                        String temp = callBackResult.getVerifierTextResult().getText() ;
                        SessionId = callBackResult.getVerifierTextResult().getSessionId() ;
                        int interval = callBackResult.getVerifierTextResult().getInterval() ;
                        mNumberTV.setText(temp);
                        break;
                    case 2:
                        //VerifierSuccess
                        ProgressDialogUtils.dismissProgressDialog();
                        double score = callBackResult.getVerifierResult().getScore() ;
                        //传递参数给上一个activity
                        Intent intent=new Intent();
                        if(callBackResult.getVerifierResult().getPassed()){
                            showToast(getApplicationContext(), "验证通过！");
                            intent.putExtra("verify_result", "1");
                        }else{
                            showToast(getApplicationContext(), "验证失败！");
                            intent.putExtra("verify_result", "-1");
                        }
                        setResult(RESULT_CODE, intent);
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
                System.out.println("失败中的失败"+exceptionMsg);
                if(ErrorEscape.errorTrans_train(exceptionMsg)!=null){
                    showToast(getApplicationContext(), ErrorEscape.errorTrans_train(exceptionMsg));
                    ProgressDialogUtils.showProgressDialog(VerifierActivity.this, "获取身份验证文本...");
                    nameValuePair.setVerifiyType(SMBAPIConstant.TEXT_TYPE_VERIFY_8_5);
                    nameValuePair.setVoiceprintId(Constant.HttpCode.USERNAME);
                    verifierCallBack.executeGetText(nameValuePair);
                }else{
                    showToast(getApplicationContext(), "服务端异常！("+exceptionMsg+")");
                    finish();
                }
            }
        };
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

                    ProgressDialogUtils.showProgressDialog(VerifierActivity.this, "身份验证中...");
                    nameValuePair.setSessionId(SessionId);
                    nameValuePair.setDate(recordData);
                    verifierCallBack.executeVerifier(nameValuePair);

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