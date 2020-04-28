package com.dazone.crewchatoff.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.adapter.ViewPagerAdapter;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.fragment.TabCurrentChatFragment;
import com.dazone.crewchatoff.fragment.TabOrganizationChartFragment;
import com.dazone.crewchatoff.interfaces.IF_Relay;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maidinh on 9/2/2017.
 */

public class RelayActivity extends AppCompatActivity {
    String TAG = "RelayActivity";
    private TabLayout tabLayout;
    private ViewPager viewPager;
    long MessageNo = -99;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.relay_layout);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MessageNo = getIntent().getLongExtra(Statics.MessageNo, -99);
        initView();
    }

    void initView() {
        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) showSearch();
                else hideSearch();

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new TabOrganizationChartFragment(), getResources().getString(R.string.tab_1));
        adapter.addFragment(new TabCurrentChatFragment(), getResources().getString(R.string.tab_2));
        viewPager.setAdapter(adapter);
    }


    void showSearch() {
        if (myActionMenuItem != null) myActionMenuItem.setVisible(true);
    }

    void hideSearch() {
        if (myActionMenuItem != null) {
            myActionMenuItem.collapseActionView();
            myActionMenuItem.setVisible(false);
        }

    }

    MenuItem myActionMenuItem;
    SearchView searchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_relay, menu);

        myActionMenuItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s == null) s = "";
                if (TabOrganizationChartFragment.fm != null)
                    TabOrganizationChartFragment.fm.updateSearch(s);
                return false;
            }
        });
        return true;
    }

    void sendMsgRoom() {
        if (TabCurrentChatFragment.fragment != null) {
            List<ChattingDto> lst = TabCurrentChatFragment.fragment.getData();
            if (lst == null) {
                lst = new ArrayList<>();
            }
            List<String> lstRoom = new ArrayList<>();
            for (ChattingDto obj : lst) {
                if (obj.isCbChoose()) {
                    lstRoom.add("" + obj.getRoomNo());
                }
            }
            if (lstRoom.size() > 0 && MessageNo != -99) {
                HttpRequest.getInstance().ForwardChatMsgChatRoom(MessageNo, lstRoom, new IF_Relay() {
                    @Override
                    public void onSuccess() {
                        finish();
                    }

                    @Override
                    public void onFail() {
                        finish();
                        Toast.makeText(getApplicationContext(), "Send Msg to room Fail", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.d(TAG, "dont have room choose");
                finish();
            }

        } else {
            finish();
            Toast.makeText(getApplicationContext(), "Can not get list room", Toast.LENGTH_SHORT).show();
        }
    }

    void actionSearch() {
        if (TabOrganizationChartFragment.fm != null)
            TabOrganizationChartFragment.fm.touchSearch();
        Log.d(TAG, "actionSearch");
    }

    void sendMsgUser() {
        if (TabOrganizationChartFragment.fm != null) {
            ArrayList<TreeUserDTO> lst = TabOrganizationChartFragment.fm.getListUser();
            if (lst == null) {
                lst = new ArrayList<>();
            }
            List<String> lstUser = new ArrayList<>();
            for (TreeUserDTO obj : lst) {
                lstUser.add("" + obj.getId());
            }
            if (lstUser.size() > 0 && MessageNo != -99) {
                HttpRequest.getInstance().ForwardChatMsgUser(MessageNo, lstUser, new IF_Relay() {
                    @Override
                    public void onSuccess() {
                        sendMsgRoom();
                    }

                    @Override
                    public void onFail() {
                        sendMsgRoom();
                        Toast.makeText(getApplicationContext(), "Send Msg to User Fail", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.d(TAG, "dont have user choose");
                sendMsgRoom();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Can not get list user", Toast.LENGTH_SHORT).show();
            sendMsgRoom();
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_add:
                sendMsgUser();
                break;
            case R.id.menu_search:
                actionSearch();
                break;
        }
        return false;
    }
}
