package com.dazone.crewchatoff.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {
    protected Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
}