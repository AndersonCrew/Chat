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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class CrewChatSettingActivity extends BaseSingleBackActivity implements CrewChatPresenter.RotationInterface {
    @BindView(R.id.toolbar)
    Toolbar mToolBar;
    @BindView(R.id.sw_enter_auto)
    SwitchCompat swEnter;
    @BindView(R.id.sw_enter_v_duty)
    SwitchCompat swEnterVDuty;
    @BindView(R.id.tv_screen_rotation)
    TextView mTvScreenRotation;
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
        ButterKnife.bind(this);
        setupToolBarSingleTitle(getString(R.string.settings_crew_chat), mToolBar);
        mPresenter = new CrewChatPresenter(this, this);
        mPrefs = CrewChatApplication.getInstance().getPrefs();
        initView();

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

   /* @Override
    public void onBackPressed() {
        super.onBackPressed();


        finish();
    }*/

    @Override
    protected void addFragment(Bundle bundle) {

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2

    }

    @OnClick(R.id.sw_enter_auto)
    void autoEnter() {
        mPrefs.putBooleanValue(Statics.IS_ENABLE_ENTER_KEY, swEnter.isChecked());
    }

    @OnClick(R.id.sw_enter_v_duty)
    void autoEnterViewDuty() {
        Log.d("CrewChatSettingActivity", swEnterVDuty.isChecked() + "");
        setEnterVDuty(swEnterVDuty);
    }

    @OnClick({R.id.btn_rotation, R.id.tv_screen_rotation})
    public void rotationAction() {
        mPresenter.showDialog("");
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
