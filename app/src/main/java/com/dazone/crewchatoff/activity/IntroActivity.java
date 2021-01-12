package com.dazone.crewchatoff.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.utils.Utils;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        Utils.hideKeyboard(this);
        startApplication();
    }

    // 어플리케이션을 시작합니다.(LoginActivity)
    private void startApplication() {
        Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}