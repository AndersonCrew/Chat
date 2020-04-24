package com.dazone.crewchatoff.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dazone.crewchatoff.HTTPs.HttpOauthRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.interfaces.IF_UpdatePass;
import com.dazone.crewchatoff.utils.CrewChatApplication;

/**
 * Created by maidinh on 4/3/2017.
 */

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText edOldPass, edNewPass, edConfirmPass;
    private Button btnChange;

    void initView() {
        edOldPass = findViewById(R.id.edOldPass);
        edNewPass = findViewById(R.id.edNewPass);
        edConfirmPass = findViewById(R.id.edConfirmPass);
        btnChange = findViewById(R.id.btnChange);

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionChange();
            }
        });
    }

    void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    void actionChange() {
        String oldPass = edOldPass.getText().toString().trim();
        final String newPass = edNewPass.getText().toString().trim();
        String cfPass = edConfirmPass.getText().toString().trim();
        if (oldPass.length() == 0 || newPass.length() == 0 || cfPass.length() == 0) {
            if (oldPass.length() == 0) showMsg("missing input Old password");
            else if (newPass.length() == 0) showMsg("missing input New password");
            else showMsg("missing input Confirm password");
        } else {
            if (!newPass.equals(cfPass)) {
                showMsg("Confirm pass incorrect");
            } else {
                HttpOauthRequest.getInstance().updatePassword(oldPass, newPass, new IF_UpdatePass() {
                    @Override
                    public void onSuccess(String newSessionID) {
                        showMsg("Success");
                        CrewChatApplication.getInstance().getPrefs().putaccesstoken(newSessionID);
                        CrewChatApplication.getInstance().getPrefs().setPass(newPass);
                        finish();
                    }

                    @Override
                    public void onFail() {
                        showMsg("There was an error changing your password, try again");
                    }
                });
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password_layout);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

        }
        return false;
    }
}
