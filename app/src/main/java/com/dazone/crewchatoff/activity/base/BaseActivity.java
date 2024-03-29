package com.dazone.crewchatoff.activity.base;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.eventbus.CloseScreen;
import com.dazone.crewchatoff.eventbus.ReloadListMessage;
import com.dazone.crewchatoff.eventbus.RotationAction;
import com.dazone.crewchatoff.services.NetworkStateReceiver;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.dazone.crewchatoff.fragment.ChattingFragment.sendComplete;

public abstract class BaseActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {
    public ActionBar actionBar;
    protected Context mContext;
    public static BaseActivity Instance = null;
    public Prefs prefs;
    private ProgressDialog mProgressDialog;
    protected String server_site;
    private NetworkStateReceiver networkStateReceiver;
    public static boolean isDisConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        mContext = this;
        Instance = this;
        prefs = CrewChatApplication.getInstance().getPrefs();
        server_site = prefs.getServerSite();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        rotationSetting();
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Subscribe
    public void rotationActionRc(RotationAction rotationAction) {
        rotationSetting();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Instance = this;
    }

    public void showProgressDialog() {
        if (null == mProgressDialog || !mProgressDialog.isShowing()) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setTitle(getString(R.string.loading_title));
            mProgressDialog.setMessage(getString(R.string.loading_content));
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
    }

    public void dismissProgressDialog() {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void callActivity(Class cls) {
        Intent newIntent = new Intent(this, cls);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(newIntent);
    }

    public void startNewActivity(Class cls) {
        if (cls != null) {
            Intent newIntent = new Intent(this, cls);
            newIntent.putExtra("count_id", 1);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(newIntent);
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public AlertDialog customDialog = null;

    public void showAlertDialog(String title, String content, String positiveTitle,
                                String negativeTitle, View.OnClickListener positiveListener,
                                View.OnClickListener negativeListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customView = LayoutInflater.from(this).inflate(R.layout.dialog_alert, null);
        builder.setView(customView);

        Button btnCancel = (Button) customView.findViewById(R.id.add_btn_cancel);
        Button btnAdd = (Button) customView.findViewById(R.id.add_btn_log_time);
        final TextView textView = (TextView) customView.findViewById(R.id.textView);
        final TextView contentTextView = (TextView) customView.findViewById(R.id.contentTextView);
        btnCancel.setText(getText(R.string.string_ok));
        if (TextUtils.isEmpty(title)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(title);
        }
        if (TextUtils.isEmpty(content)) {
            contentTextView.setVisibility(View.GONE);
        } else {
            contentTextView.setVisibility(View.VISIBLE);
            contentTextView.setText(content);
        }

        if (TextUtils.isEmpty(positiveTitle)) {
            btnAdd.setVisibility(View.GONE);
        } else {
            btnAdd.setVisibility(View.VISIBLE);
            btnAdd.setText(positiveTitle);
            btnAdd.setOnClickListener(positiveListener);
        }
        if (TextUtils.isEmpty(negativeTitle)) {
            btnCancel.setVisibility(View.GONE);
        } else {
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setText(negativeTitle);
            btnCancel.setOnClickListener(negativeListener);
        }
        customDialog = builder.create();
        customDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (customDialog != null && customDialog.isShowing()) {
            customDialog.cancel();
        }
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
    }

    public void showAlertDialog(String content, String positiveTitle,
                                String negativeTitle, View.OnClickListener positiveListener) {
        showAlertDialog(getString(R.string.app_name), content, positiveTitle, negativeTitle,
                positiveListener, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        customDialog.dismiss();

                    }
                });
    }

    public void showAlertDialog(String title, Spannable content, String positiveTitle,
                                String negativeTitle, View.OnClickListener positiveListener,
                                View.OnClickListener negativeListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customView = LayoutInflater.from(this).inflate(R.layout.dialog_alert, null);
        builder.setView(customView);

        Button btnCancel = (Button) customView.findViewById(R.id.add_btn_cancel);
        Button btnAdd = (Button) customView.findViewById(R.id.add_btn_log_time);
        final TextView textView = (TextView) customView.findViewById(R.id.textView);
        final TextView contentTextView = (TextView) customView.findViewById(R.id.contentTextView);
        btnCancel.setText(getText(R.string.string_ok));
        if (TextUtils.isEmpty(title)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(title);
        }
        if (TextUtils.isEmpty(content)) {
            contentTextView.setVisibility(View.GONE);
        } else {
            contentTextView.setVisibility(View.VISIBLE);
            contentTextView.setText(content);
        }

        if (TextUtils.isEmpty(positiveTitle)) {
            btnAdd.setVisibility(View.GONE);
        } else {
            btnAdd.setVisibility(View.VISIBLE);
            btnAdd.setText(positiveTitle);
            btnAdd.setOnClickListener(positiveListener);
        }
        if (TextUtils.isEmpty(negativeTitle)) {
            btnCancel.setVisibility(View.GONE);
        } else {
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setText(negativeTitle);
            btnCancel.setOnClickListener(negativeListener);
        }
        customDialog = builder.create();
        customDialog.show();
    }

    public void showNetworkDialog() {
        if (customDialog == null || !customDialog.isShowing()) {
            if (Utils.isWifiEnable()) {
                showAlertDialog(getString(R.string.app_name), getString(R.string.no_connection_error),
                        getString(R.string.string_ok), null, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                customDialog.dismiss();
                                finish();
                            }
                        }, null);
            } else {
                showAlertDialog(getString(R.string.app_name), getString(R.string.no_wifi_error),
                        getString(R.string.turn_wifi_on), getString(R.string.string_cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent wireLess = new Intent(
                                        Settings.ACTION_WIFI_SETTINGS);
                                startActivity(wireLess);
                                customDialog.dismiss();
                            }
                        }, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                customDialog.dismiss();

                                // don't close app when wifi network is disabled
                                finish();
                            }
                        });
            }
        }
    }

    public void rotationSetting() {
        try {


            int rotation = prefs.getIntValue(Statics.SCREEN_ROTATION, Constant.PORTRAIT);

            switch (rotation) {
                case Constant.AUTOMATIC:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    break;
                case Constant.PORTRAIT:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;
                case Constant.LANSCAPE:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void networkAvailable() {

        if (isDisConnect) {
            sendComplete=false;
            EventBus.getDefault().post(new CloseScreen());
            isDisConnect=false;
        }
    }

    @Override
    public void networkUnavailable() {
        isDisConnect = true;
        // isDelete=false;
        Toast.makeText(getApplicationContext(),R.string.no_connection_error,Toast.LENGTH_SHORT).show();
    }
}