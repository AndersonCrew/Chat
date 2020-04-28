package com.dazone.crewchatoff.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.base.BaseSingleBackActivity;
import com.dazone.crewchatoff.fragment.SettingNotificationFragment;

public class NotificationSettingActivity extends BaseSingleBackActivity {
    private SettingNotificationFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUPToolBar(getString(R.string.settings_notification));
    }

    @Override
    protected void addFragment(Bundle bundle) {
        fragment = new SettingNotificationFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.content_base_single_activity, fragment, fragment.getClass().getSimpleName());
        transaction.commit();
    }
}