package com.example.helena.voicedit_android.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.helena.voicedit_android.R;

public class MainActivity extends AppCompatActivity {
    private Button mSimpleFormButton;
    private Button mBgFormButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init_view();
        init_controller();
    }

    private void init_view() {
        mSimpleFormButton = (Button) findViewById(R.id.simple_form_button);
        mBgFormButton = (Button) findViewById(R.id.bg_form_button);
    }

    private void init_controller() {
        mSimpleFormButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, VoiceEditActivity.class);
                startActivity(intent);
            }
        });
        mBgFormButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BloodGlucoseRecordActivity.class);
                startActivity(intent);
            }
        });
    }
}
