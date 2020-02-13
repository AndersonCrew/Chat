package com.dazone.crewchatoff.adapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.dazone.crewchatoff.fragment.BaseFavoriteFragment;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.fragment.CurrentChatListFragment;
import com.dazone.crewchatoff.fragment.SettingFragment;

public class TabPagerAdapter extends FragmentPagerAdapter {
    private int count = 4;
    private Activity mContext;
    public TabPagerAdapter(FragmentManager fm, int count, Activity context) {
        super(fm);
        this.count = count;
        mContext = context;
    }

    public TabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    // 해당 위치의 탭 정보( Fragment)
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: // 채팅 리스트 탭
                return new CurrentChatListFragment();

            case 1: // 조직도 탭
                CompanyFragment companyFragment = new CompanyFragment();
                companyFragment.setContext(mContext);
                return companyFragment;

            case 2: // 즐겨찾기 탭
                BaseFavoriteFragment fragment = new BaseFavoriteFragment();
                fragment.setContext(mContext);
                return fragment;

            case 3: // 환경설정 탭
                return new SettingFragment();

            default: // 기본(채팅리스트)
                return new CurrentChatListFragment();
        }
    }

/*
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
        // save the appropriate reference depending on position
        switch (position) {
            case 0:
                currentChatListFragment = (CurrentChatListFragment) createdFragment;
                break;
            case 1:
                companyFragment = (CompanyFragment) createdFragment;
                break;
            case 2:
                baseFavoriteFragment =(BaseFavoriteFragment)createdFragment;
                break;
            case 3:
                settingFragment=(SettingFragment)createdFragment;
                break;

        }
        return createdFragment;
    }
*/



    // 해당 탭을 데이터 삭제
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    // 탭의 전체 갯수
    @Override
    public int getCount() {
        return count;
    }

    // 사용하지 않음
    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }
}