package com.dazone.crewchatoff.activity.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.dazone.crewchatoff.HTTPs.HttpOauthRequest;
import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.TestMultiLevelListview.MultilLevelListviewFragment;
import com.dazone.crewchatoff.activity.LoginActivity;
import com.dazone.crewchatoff.activity.MainActivity;
import com.dazone.crewchatoff.adapter.TabPagerAdapter;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.fragment.BaseFavoriteFragment;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.fragment.CurrentChatListFragment;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallBack;
import com.dazone.crewchatoff.interfaces.OnClickCallback;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import static java.lang.Integer.MAX_VALUE;

public abstract class BasePagerActivity extends BaseActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    protected TabPagerAdapter tabAdapter;
    String TAG = "BasePagerActivity";
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    protected ViewPager mViewPager;
    public TabLayout tabLayout;
    protected FloatingActionButton fab;

    /**
     * MENU ITEM
     */
    protected MenuItem menuItemSearch;
    protected MenuItem menuItemMore;
    protected SearchView searchView;

    protected FrameLayout ivSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_base_pager);
        CrewChatApplication.isAddUser = true;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        tabAdapter = new TabPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(tabAdapter);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        ivSearch = (FrameLayout) findViewById(R.id.iv_search);
        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.CURRENT_TAB == 0) {
                    if (CurrentChatListFragment.fragment != null) {
                        CurrentChatListFragment.fragment.searchAction(1);
//                        actionSearchForFab();
                    }
                } else if (MainActivity.CURRENT_TAB == 1) {

                } else if (MainActivity.CURRENT_TAB == 2) {
                    if (BaseFavoriteFragment.CURRENT_TAB == 0) {
                        BaseFavoriteFragment.instance.Favorite_left();
                    } else {
                        BaseFavoriteFragment.instance.Favorite_Right();
//                        actionSearchForFab();
                    }
                }

            }
        });


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (MainActivity.CURRENT_TAB == 0) {
                    if (MainActivity.instance != null) {
                        MainActivity.instance.gotoOrganizationChart();
                    }
                } else if (MainActivity.CURRENT_TAB == 2) {
                    if (BaseFavoriteFragment.CURRENT_TAB == 0) {
//                        MainActivity.instance.gotoOrganizationChart();
                    } else {
                        if (MultilLevelListviewFragment.instanceNew != null
                                && MultilLevelListviewFragment.instanceNew.isLoadDB()) {
                            MultilLevelListviewFragment.instanceNew.addFavorite();
                        }
                    }


                }
            }
        });
//        hidePAB();
       /* fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                *//*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*//*
                Intent intent = new Intent(BasePagerActivity.this, OrganizationActivity.class);
                startActivity(intent);
            }
        });*/
        init();
        inItShare();
    }


    // Show topmenubar saerch icon
    // 탑 메뉴바의 검색 아이콘을 표시
    public void showSearchIcon(final OnClickCallback callback) {
        if (ivSearch != null) {
            ivSearch.setVisibility(View.VISIBLE);
            ivSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onClick();
                }
            });
        }
    }


    // Hide topmenubar search icon(default)
    // 탑 메뉴바의 검색 아이콘을 숨김(기본)
    public void showIcon() {
        if (ivSearch != null) {
            if (!ivSearch.isShown())
                ivSearch.setVisibility(View.VISIBLE);
        }
    }

    public void hideSearchIcon() {

        if (ivSearch != null) {
            ivSearch.setVisibility(View.GONE);

        } else {

        }
    }

    public void hideSearchView() {

        if (menuItemSearch != null) {
            menuItemSearch.setVisible(false);
            } else {

        }
    }

    // FloatingActionButton Show
    // 하단 플로팅 액션 버튼을 보이게 설정
//    public void showPAB(final OnClickCallback callback) {
//        if (fab != null) {
//            fab.setVisibility(View.VISIBLE);
//            fab.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Log.d(TAG, "fab Click");
//                    callback.onClick();
//                }
//            });
//        }
//    }
    public void showPAB() {
        if (fab != null) {
//            fab.setVisibility(View.VISIBLE);
            fab.show();
        }
    }

    // FloatingActionButton Hide
    // 하단 플로팅 액션 버튼을 안보이게 설정
    public void hidePAB() {
        if (fab != null) {
//            fab.setVisibility(View.GONE);
            fab.hide();
        }
    }

    protected abstract void init();
    protected abstract void inItShare();
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_base_pager, menu);
        Log.d("MainActivity", "onCreateOptionsMenu");
        menuItemSearch = menu.findItem(R.id.action_search);
        menuItemMore = menu.findItem(R.id.action_status);
        searchView = (SearchView) menuItemSearch.getActionView();
        searchView.setMaxWidth(MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                Intent intentFinish = new Intent(Constant.INTENT_FILTER_SEARCH);
//                intentFinish.putExtra(Constant.KEY_INTENT_TEXT_SEARCH, query);
//                BasePagerActivity.this.sendBroadcast(intentFinish);
                Log.d(TAG, "onQueryTextSubmit");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                Intent intentFinish = new Intent(Constant.INTENT_FILTER_SEARCH);
//                intentFinish.putExtra(Constant.KEY_INTENT_TEXT_SEARCH, newText);
//                BasePagerActivity.this.sendBroadcast(intentFinish);
                if (CompanyFragment.instance != null)
                    CompanyFragment.instance.updateSearch(newText);
                Log.d(TAG, "onQueryTextChange");
                return false;
            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_logout) {
            String ids = new Prefs().getGCMregistrationid();
            if (!TextUtils.isEmpty(ids)) {
                HttpRequest.getInstance().DeleteDevice(ids, new BaseHTTPCallBack() {
                    @Override
                    public void onHTTPSuccess() {
                        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getBaseContext());
                        try {

                            gcm.unregister();
                        } catch (IOException e) {
                            System.out.println("Error Message: " + e.getMessage());
                        }
                        new Prefs().setGCMregistrationid("");
                        HttpOauthRequest.getInstance().logout(new BaseHTTPCallBack() {
                            @Override
                            public void onHTTPSuccess() {
                                Intent intent = new Intent(BasePagerActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }

                            @Override
                            public void onHTTPFail(ErrorDto errorDto) {

                            }
                        });
                    }

                    @Override
                    public void onHTTPFail(ErrorDto errorDto) {

                    }
                });
            } else {
                HttpOauthRequest.getInstance().logout(new BaseHTTPCallBack() {
                    @Override
                    public void onHTTPSuccess() {
                        Intent intent = new Intent(BasePagerActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }

                    @Override
                    public void onHTTPFail(ErrorDto errorDto) {

                    }
                });
            }
            return true;
        } else if (id == R.id.action_search) {
            if (CompanyFragment.instance != null) CompanyFragment.instance.getListCurrent();
            Log.d(TAG, "action_search");
        }

        return super.onOptionsItemSelected(item);
    }

    public void destroyFragment() {
        tabAdapter.destroyItem(mViewPager, 0, tabAdapter.getItem(0));
        tabAdapter.destroyItem(mViewPager, 1, tabAdapter.getItem(1));
        tabAdapter.destroyItem(mViewPager, 2, tabAdapter.getItem(2));
        tabAdapter.destroyItem(mViewPager, 3, tabAdapter.getItem(3));
    }
}
