package com.example.helena.voicedit_android.activity;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.helena.voicedit_android.R;
import com.example.helena.voicedit_android.utils.JsonParser;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class VoiceEditActivity extends AppCompatActivity {
    private static final String TAG = "VoiceEditActivity";
    static String[] fieldNames = {"姓名", "年龄", "性别" };//这个具体属性的数据最好能进一步与程序解耦，不要硬编码
    int[] fieldIds = new int[4];
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    //这个结果是json格式的
    private ArrayList<RecognizerResult> resultJsonArray = new ArrayList<>();
    private TextView mResultText;
    private EditText mResultJson;
    private Toast mToast;
    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };
    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
            resultJsonArray.add(results);
            if(isLast) {
                parseResult();
            }
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            if(error.getErrorCode() == 14002) {
                showTip( error.getPlainDescription(true)+"\n请确认是否已开通翻译功能" );
            } else {
                showTip(error.getPlainDescription(true));
            }
        }

    };
    private void parseResult() {
        //一句一句解析
        for(RecognizerResult result: resultJsonArray) {
//            mResultJson.append(result.getResultString());

            Log.i(TAG, "parseResult: 开始解析" + result.getResultString());
            ArrayList<String> words = JsonParser.parseIatToArray(result.getResultString());
            if(words.size() == 0) continue;//这句是空的

            //需要对每个field检查是否在输入语音中出现
//            boolean flag = false;//for silence situation
            for(int i = 0; i < fieldNames.length; i++) {
                String field = fieldNames[i];
                Log.i(TAG, "parseResult: field " + field);
                for(int j=0; j < words.size(); j++) {//我之所以不用增强for循环，是因为取不到下一个，而迭代器又跳过下一个了，所以还是index最实在
                    if(field.equals(words.get(j))) {//域的名字在语音识别结果中出现了
//                        flag = true;
                        Log.i(TAG, "parseResult: 匹配上了 [" + field + "]");
                        EditText nameField = (EditText) findViewById(fieldIds[i]);//把id存到一个数组里了
                        Log.i(TAG, "parseResult: 找到对应的空" + nameField.getId());
                        //认为field的名字后面的值就是value, 如：姓名 张三。但姓名可能被拆分为若干个字，于是碰到姓名，往后找2-3个字
                        String value = words.get(j+1);
                        Log.i(TAG, "parseResult: value" + value);
                        if(field.equals("姓名")) {
                                System.out.println(words.size());
                                value += words.get(j+2);
                                if(words.size() > 3) {
                                    System.out.println("三个字的名字");
                                    value += words.get(j+3);
                                }
                                System.out.println("识别出的姓名:" + value);
                        }
                        nameField.setText(value);
                        nameField.hasFocus();
                    }
                }
            }
//                //silence situation
//                if(flag == false) {
//                    EditText silenceField = (EditText) findViewById(fieldIds[fieldNames.length-1]);//最后一项设为silence项
//                    silenceField.setText("未捕获到声音");
//                    silenceField.hasFocus();
//                }

        }
    }
    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        mResultText.setText(resultBuffer.toString());
    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }
    private Button mStartButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_edit);
        SpeechUtility.createUtility(VoiceEditActivity.this, "appid=" + getString(R.string.app_id));

        init_view();
        Log.i(TAG, "onCreate: 初始化表单成功");
        init_msc();
        init_controller();
    }
    private void init_controller() {
        fieldIds[0] = R.id.etName_1;
        fieldIds[1] = R.id.etName_2;
        fieldIds[2] = R.id.etName_3;
//        fieldIds[3] = R.id.etName_4;

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTip("开始听写");
//                mResultJson.setText(null);
                mResultText.setText(null);// 清空显示内容
                mIatResults.clear();
                resultJsonArray.clear();//清空结果数组
                mIatDialog.setListener(mRecognizerDialogListener);
                mIatDialog.show();
                showTip(getString(R.string.text_begin));
            }
        });
    }
    private void init_view() {
        for(int i=0; i<fieldNames.length; i++) {//动态设置表单的域
            LayoutInflater inflater = LayoutInflater.from(VoiceEditActivity.this);
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.activity_voice_edit_layout);
            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.form, null).findViewById(R.id.form_layout);
            TextView label = layout.findViewById(R.id.form_label);
            label.setText(fieldNames[i]);

            EditText editText = layout.findViewById(R.id.form_blank);
            switch (i) {//这里不能字符串拼接啊
                case 0: editText.setId(R.id.etName_1); break;
                case 1: editText.setId(R.id.etName_2); break;
                case 2: editText.setId(R.id.etName_3); break;
                default:
            }
            linearLayout.addView(layout);
        }
        mStartButton = (Button) findViewById(R.id.iat_recognize);
        mResultText = (TextView) findViewById(R.id.iat_text);
//        mResultJson = (EditText) findViewById(R.id.json_result);
    }

    private void init_msc() {
        mIatDialog = new RecognizerDialog(VoiceEditActivity.this, mInitListener);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        setParam();
    }
    public void setParam() {
        // 清空参数
        mIatDialog.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎，云端
        mIatDialog.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);

        //设置语言
        mIatDialog.setParameter(SpeechConstant.DOMAIN, "iat");
        mIatDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIatDialog.setParameter(SpeechConstant.ACCENT, "mandarin ");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIatDialog.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIatDialog.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIatDialog.setParameter(SpeechConstant.ASR_PTT, "0");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIatDialog.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mIatDialog.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
    }
}
