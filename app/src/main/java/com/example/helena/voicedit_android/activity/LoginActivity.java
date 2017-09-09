package com.example.helena.voicedit_android.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dear.smb.android.api.CallBackResult;
import com.dear.smb.android.api.NameValuePair;
import com.dear.smb.android.api.TrainerCallBack;
import com.dear.smb.android.controller.SmbController;
import com.example.helena.voicedit_android.R;
import com.example.helena.voicedit_android.utils.Constant;
import com.example.helena.voicedit_android.utils.ErrorEscape;
import com.example.helena.voicedit_android.utils.ProgressDialogUtils;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mSigninButton;
    private Button mSignupButton;
    private TextView mLoginTextView;
    private TextView mUserExistTextView;
    boolean isExit = false;//此用户已录过声纹
    private AlertDialog.Builder builder;
    private TrainerCallBack trainerCallBack;
    private NameValuePair nameValuePair = new NameValuePair();
    private final static int REQUEST_CODE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init_smb();
        init_view();
        init_controller();
    }

    private void init_smb() {
        SmbController.initAddress(Constant.HttpCode.DEFAULHTTP,
                Constant.HttpCode.DEFAULTPORT);
        SmbController.initConfigParam(Constant.HttpCode.app_id,Constant.HttpCode.app_secret);

    }
    private void init_view() {
        mSigninButton = (Button) findViewById(R.id.signin_button);
        mSignupButton = (Button) findViewById(R.id.signup_button);
        mLoginTextView = (TextView) findViewById(R.id.tv_login);
        mUserExistTextView = (TextView)findViewById(R.id.tv_toast);
        mLoginTextView.setText("当前用户：" + Constant.HttpCode.USERNAME);
        mLoginTextView.append("\n更改用户请按返回键");
    }
    private void init_controller() {
        builder = new AlertDialog.Builder(this);
        mSigninButton.setOnClickListener(this);
        mSignupButton.setOnClickListener(this);

        trainerCallBack = new TrainerCallBack(this) {
            @Override
            public void onSuccess(CallBackResult callBackResult) {
                super.onSuccess(callBackResult);
                switch (callBackResult.getTag()) {
                    case 4:
                        ProgressDialogUtils.dismissProgressDialog();
                        isExit = callBackResult.getVoiceIdExisResult().isExisted();
                        if (!isExit) {
                            mUserExistTextView.setVisibility(View.VISIBLE);
                        }else{
                            mUserExistTextView.setVisibility(View.INVISIBLE);
                        }
                        break;
                    case 5:
                        //remove VoiceprintID SUCCESS
                        ProgressDialogUtils.dismissProgressDialog();
                        boolean isRemoved = callBackResult.getReVoiceIdResult().isRemoved();
                        if(isRemoved){
                            Intent intent = new Intent(LoginActivity.this, TrainerActivity.class);
                            startActivity(intent);
                        }else{
                            showToast(getApplicationContext(), "该声纹ID不存在");

                        }
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
                }

            }
        };
    }
    private  void checkmodle(){
        ProgressDialogUtils.showProgressDialog(this, "判断声纹ID是否存在。。。");
        nameValuePair.setVoiceprintId(Constant.HttpCode.USERNAME);
        trainerCallBack.executeVoiceIdExisResult(nameValuePair);
    }

    protected void showToast(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL,
                0, 0);
        toast.show();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        checkmodle();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissAlertDialog();
    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.signup_button:
                if(isExit){
                    showAlertDialog("提示", "当前用户已有声纹模型，是否删除重新预留？");
                }else{
                    Intent intent = new Intent(LoginActivity.this, TrainerActivity.class);
                    startActivity(intent);
                }

                break;
            case R.id.signin_button:
                if(isExit){
                    Intent intent2 = new Intent(LoginActivity.this,
                            VerifierActivity.class);
                    startActivityForResult(intent2, REQUEST_CODE);
                }else{
                    showToast(getApplicationContext(), "请先进行声纹预留！");
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if(resultCode == VerifierActivity.RESULT_CODE) {
                String verify_result = data.getExtras().getSerializable("verify_result").toString();
                if(verify_result.equals("1")) {
//                    showToast(getApplicationContext(), "验证已通过，进入语音填表界面");
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }else if(verify_result.equals("-1")) {
                    showToast(getApplicationContext(), "验证未通过，请重新验证或更改用户");
                }else {
                    showToast(getApplicationContext(), "验证结果未正确获得");
                }
            }
        }
    }

    public void showAlertDialog(String title, String msg){
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("确定",
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ProgressDialogUtils.showProgressDialog(LoginActivity.this, "删除原有的模型ID...");
                        nameValuePair.setVoiceprintId(Constant.HttpCode.USERNAME);
                        trainerCallBack.executeReVoiceIdResult(nameValuePair);
                    }
                });
        builder.setNeutralButton("取消",
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismissAlertDialog();
                    }
                });
        builder.show();
    }
    public void dismissAlertDialog(){
        if(builder!=null){
            builder = null ;
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
