package com.dazone.crewchatoff.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.base.BaseSingleBackActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.eventbus.NotifyAdapterOgr;
import com.dazone.crewchatoff.eventbus.RotationAction;
import com.dazone.crewchatoff.presenter.CrewChatPresenter;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;

import org.greenrobot.eventbus.EventBus;


public class CrewChatSettingActivity extends BaseSingleBackActivity implements CrewChatPresenter.RotationInterface {
    private Toolbar mToolBar;
    private SwitchCompat swEnter;
    private SwitchCompat swEnterVDuty;
    private TextView mTvScreenRotation;
    private Prefs mPrefs;

    private CrewChatPresenter mPresenter;

    public static void toActivity(Context context) {
        Intent intent = new Intent(context, CrewChatSettingActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_setting);

        mToolBar = findViewById(R.id.toolbar);
        swEnter = findViewById(R.id.sw_enter_auto);
        swEnterVDuty = findViewById(R.id.sw_enter_v_duty);
        mTvScreenRotation = findViewById(R.id.tv_screen_rotation);

        setupToolBarSingleTitle(getString(R.string.settings_crew_chat), mToolBar);
        mPresenter = new CrewChatPresenter(this, this);
        mPrefs = CrewChatApplication.getInstance().getPrefs();
        initView();
        initControls();
    }

    private void initControls() {
        swEnter.setOnClickListener(view -> mPrefs.putBooleanValue(Statics.IS_ENABLE_ENTER_KEY, swEnter.isChecked()));
        swEnterVDuty.setOnClickListener(view -> {
            Log.d("CrewChatSettingActivity", swEnterVDuty.isChecked() + "");
            setEnterVDuty(swEnterVDuty);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }

    @Override
    protected void addFragment(Bundle bundle) {

    }

    private void initView() {
        isEnterAuto();
        inItEnterVDuty();
        mPresenter.rotationSettingValue(mTvScreenRotation);
    }

    private boolean isEnterAuto() {
        boolean isEnable = false;
        isEnable = mPrefs.getBooleanValue(Statics.IS_ENABLE_ENTER_KEY, isEnable);
        swEnter.setChecked(isEnable);
        return isEnable;
    }

    private boolean inItEnterVDuty() {
        boolean isEnable = false;
        isEnable = mPrefs.getBooleanValue(Statics.IS_ENABLE_ENTER_VIEW_DUTY_KEY, isEnable);
        swEnterVDuty.setChecked(isEnable);
        return isEnable;
    }

    private void setEnterVDuty(SwitchCompat swEnterVDuty) {
        boolean isEnable = swEnterVDuty.isChecked();
        mPrefs.putBooleanValue(Statics.IS_ENABLE_ENTER_VIEW_DUTY_KEY, isEnable);
        EventBus.getDefault().post(new NotifyAdapterOgr());
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void rotationScreen() {
        int rotation = mPrefs.getIntValue(Statics.SCREEN_ROTATION, Constant.PORTRAIT);

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
        mPresenter.rotationSettingValue(mTvScreenRotation);
        EventBus.getDefault().post(new RotationAction());
    }
}
