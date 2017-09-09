package com.example.helena.voicedit_android.activity;

import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonWriter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.helena.voicedit_android.R;
import com.example.helena.voicedit_android.model.BloodGlucoseRecord;
import com.example.helena.voicedit_android.utils.JsonParser;
import com.example.helena.voicedit_android.utils.SaveToFile;
import com.google.gson.Gson;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class BloodGlucoseRecordActivity extends AppCompatActivity {
    private static final String TAG = "BloodGlucoseRecordActiv";
    private TimePicker mTimePicker;
    private EditText mInsulinValue;
    private EditText mBgValue;
    private Spinner mSpinner;//自动填充当前时间
    private Button mInsulinButton;
    private Button mBgButton;
    private Button mSaveButton;

    Gson gson = new Gson();

    private BloodGlucoseRecord recordEntry;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    //这个结果是json格式的
    private ArrayList<RecognizerResult> resultJsonArray = new ArrayList<>();
    private EditText mResultText;
    private EditText mResultJson;
    private Toast mToast;

    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 默认发音人
    private String voicer = "xiaoyan";

    private String[] mCloudVoicersEntries;
    private String[] mCloudVoicersValue ;

    // 缓冲进度
    private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;
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
    private RecognizerDialogListener mRecognizerDialogListener_insulin = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results, mInsulinValue);
            resultJsonArray.add(results);
            if(isLast) {

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
    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener_bg = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results, mBgValue);
            resultJsonArray.add(results);
            if(isLast) {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_glucose_record);
        SpeechUtility.createUtility(BloodGlucoseRecordActivity.this, "appid=" + getString(R.string.app_id));

        init_view();
        init_msc();
        init_controller();
    }

    private void init_view() {
        mTimePicker = (TimePicker) findViewById(R.id.timepicker);
        mSpinner = (Spinner) findViewById(R.id.spinner);
        mInsulinValue = (EditText) findViewById(R.id.insulin_value);
        mBgValue = (EditText) findViewById(R.id.bg_value);
        mBgButton = (Button) findViewById(R.id.bg_button);
        mInsulinButton = (Button) findViewById(R.id.insulin_button);
        mSaveButton = (Button) findViewById(R.id.save_button);

        recordEntry = new BloodGlucoseRecord();
        recordEntry.setDate(new Date());//暂存当前时间
    }

    private void init_controller() {
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double insulin = Double.parseDouble(mInsulinValue.getText().toString());
                double bg = Double.parseDouble(mBgValue.getText().toString());
                recordEntry.setInsulin(insulin);
                recordEntry.setBlood_glucose(bg);
                String json = gson.toJson(recordEntry);
                SaveToFile.saveToFile("blood_glucose_record.txt", json);
                //9.7 在此增加播报（语音合成）功能

                showTip("已保存：" +
                        "\ninsulin: " + insulin +
                        "\nbg: " +bg +
                        "\nstate: " + recordEntry.getState() +
                        "\ndate&time: " + recordEntry.getDate()
                );
            }
        });
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] mItems = getResources().getStringArray(R.array.states);
                recordEntry.setState(mItems[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                showTip("请选择状态");
            }
        });
        mInsulinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mResultJson.setText(null);
                mInsulinValue.setText(null);// 清空显示内容
                mIatResults.clear();
                resultJsonArray.clear();//清空结果数组
                mIatDialog.setListener(mRecognizerDialogListener_insulin);
                mIatDialog.show();
//                showTip(getString(R.string.text_begin));
            }
        });
        mBgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBgValue.setText(null);
                mIatResults.clear();
                resultJsonArray.clear();
                mIatDialog.setListener(mRecognizerDialogListener_bg);
                mIatDialog.show();
            }
        });
        mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hour, int minute) {
                Date date = new Date();
                date.setHours(hour);
                date.setMinutes(minute);
                recordEntry.setDate(date);
            }
        });
    }
    private void printResult(RecognizerResult results, EditText editText) {
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

        editText.setText(resultBuffer.toString());
        editText.setSelection(editText.length());
    }
    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }
    private void init_msc() {
        mIatDialog = new RecognizerDialog(BloodGlucoseRecordActivity.this, mInitListener);
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
