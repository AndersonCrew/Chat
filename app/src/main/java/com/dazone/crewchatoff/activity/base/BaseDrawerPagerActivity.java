package com.dazone.crewchatoff.activity.base;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.dazone.crewchatoff.adapter.TabPagerAdapter;
import com.dazone.crewchatoff.R;

public abstract class BaseDrawerPagerActivity extends BaseDrawerActivity {
    protected ViewPager mViewPager;
    protected TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content_main.setVisibility(View.GONE);
        mViewPager = findViewById(R.id.container);
        mViewPager.setVisibility(View.VISIBLE);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setVisibility(View.VISIBLE);
        init();
    }

    protected abstract void init();
}