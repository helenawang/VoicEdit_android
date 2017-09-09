package com.example.helena.voicedit_android.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.helena.voicedit_android.R;
import com.example.helena.voicedit_android.utils.Constant;

public class UsernameActivity extends AppCompatActivity {
    private EditText mUsernameEditText;
    private Button mEnterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);

        mUsernameEditText = (EditText) findViewById(R.id.username_edit_text);
        mEnterButton = (Button) findViewById(R.id.enter_button);
        mEnterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //捕获用户名，进入登录界面
                Constant.HttpCode.USERNAME = mUsernameEditText.getText().toString();
                Intent intent = new Intent(UsernameActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
