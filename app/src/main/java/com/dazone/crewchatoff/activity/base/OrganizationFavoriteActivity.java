package com.dazone.crewchatoff.activity.base;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.test.OrganizationFragment;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;

public class OrganizationFavoriteActivity extends BaseSingleActivity {
    private OrganizationFragment fragment;
    private long groupNo = -1;
    private ArrayList<Integer> userNos;
    String TAG = "OrganizationFavoriteActivity";
    private Handler mHandler = new Handler();
    private String mKeySearch = "";

    static {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    protected void addFragment(Bundle bundle) {
        Intent intent = getIntent();
        if (intent != null) {
            try {
                groupNo = intent.getLongExtra(Constant.KEY_INTENT_GROUP_NO, 0);
                userNos = intent.getIntegerArrayListExtra(Constant.KEY_INTENT_COUNT_MEMBER);
            } catch (Exception e) {
                groupNo = -1;
                e.printStackTrace();
            }
        }

        fragment = OrganizationFragment.newInstance(userNos, true);
        if (intent != null) {
            Utils.addFragmentToActivity(getSupportFragmentManager(), fragment, R.id.content_base_single_activity, false);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showSave();
        HiddenTitle();
        ivMore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "ivMore");
                if (fragment != null) {
                    if (groupNo == -1) {
                        finish();
                    } else {
                        ArrayList<TreeUserDTO> list = fragment.getListUser();
                        if (list != null && list.size() > 0) {
                            Iterator<TreeUserDTO> iter = list.iterator();
                            while (iter.hasNext()) {
                                TreeUserDTO tree = iter.next();
                                for (Integer userId : userNos) {
                                    if (tree.getId() == userId) {
                                        iter.remove();
                                    }
                                }
                            }

                            Intent intent = new Intent();

                            Bundle args = new Bundle();
                            args.putLong(Constant.KEY_INTENT_GROUP_NO, groupNo);
                            args.putSerializable(Constant.KEY_INTENT_SELECT_USER_RESULT, list);
                            intent.putExtras(args);

                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                }
            }
        });
        setSearchBar();
    }

    Runnable mFilterTask = new Runnable() {
        @Override
        public void run() {
            fragment.search(mKeySearch);
        }
    };

    private void setSearchBar() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (fragment != null) {

                    mHandler.removeCallbacks(mFilterTask);
                    mKeySearch = s;
                    mHandler.postDelayed(mFilterTask, 300);
                }
                return false;
            }
        });

        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (fragment != null) {
                    fragment.closeSearch();
                }
                return false;
            }
        });
    }

}