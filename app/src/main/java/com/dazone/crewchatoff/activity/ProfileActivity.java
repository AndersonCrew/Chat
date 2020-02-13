package com.dazone.crewchatoff.activity;

import android.os.Bundle;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.base.BaseSingleBackActivity;
import com.dazone.crewchatoff.fragment.ProfileFragment;
import com.dazone.crewchatoff.interfaces.OnBackCallBack;
import com.dazone.crewchatoff.utils.Utils;

public class ProfileActivity extends BaseSingleBackActivity implements OnBackCallBack {
    private ProfileFragment fragment;

    @Override
    protected void addFragment(Bundle bundle) {
        setTitle("");
        fragment = new ProfileFragment();
        fragment.setCallback(this);
        /** ADD FRAGMENT TO ACTIVITY */
        Utils.addFragmentNotSupportV4ToActivity(getFragmentManager(), fragment, R.id.content_base_single_activity, false, fragment.getClass().getSimpleName());
    }

    @Override
    public void onBack() {
        finish();
    }
}