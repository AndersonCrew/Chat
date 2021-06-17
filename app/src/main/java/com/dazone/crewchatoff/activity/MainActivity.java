package com.dazone.crewchatoff.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.dazone.crewchatoff.BuildConfig;
import com.dazone.crewchatoff.HTTPs.GetUserStatus;
import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.base.BasePagerActivity;
import com.dazone.crewchatoff.activity.chatroom.ChattingViewModel;
import com.dazone.crewchatoff.adapter.TabPagerAdapter;
import com.dazone.crewchatoff.constant.Constants;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.database.AllUserDBHelper;
import com.dazone.crewchatoff.database.UserDBHelper;
import com.dazone.crewchatoff.dto.CheckUpdateDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.StatusDto;
import com.dazone.crewchatoff.dto.StatusItemDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.dto.UserInfoDto;
import com.dazone.crewchatoff.eventbus.NotifyAdapterOgr;
import com.dazone.crewchatoff.eventbus.ReloadActivity;
import com.dazone.crewchatoff.fragment.BaseFavoriteFragment;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.fragment.CurrentChatListFragment;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallBackWithString;
import com.dazone.crewchatoff.interfaces.OnClickCallback;
import com.dazone.crewchatoff.interfaces.OnGetStatusCallback;
import com.dazone.crewchatoff.interfaces.OnGetUserInfo;
import com.dazone.crewchatoff.services.SyncStatusService;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.Utils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.dazone.crewchatoff.utils.Utils.compareVersionNames;

public class MainActivity extends BasePagerActivity implements ViewPager.OnPageChangeListener, ServiceConnection {
    String TAG = MainActivity.class.getName();
    private boolean doubleBackToExitPressedOnce = false;
    public static MainActivity instance = null;
    public static long myRoom = -55;
    private ChattingViewModel chattingViewModel;
    @SuppressLint("HandlerLeak")
    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            boolean aResponse = msg.getData().getBoolean("is_update");

            if (aResponse) {
                int tab = msg.getData().getInt("tab");

                if (mGetStatusCallbackCompany != null && tab == TAB_COMPANY) {
                    mGetStatusCallbackCompany.onGetStatusFinish();
                    Log.d(TAG, "mGetStatusCallbackCompany");

                }

                if (mGetStatusCallbackFavorite != null && tab == TAB_FAVORITE) {
                    mGetStatusCallbackFavorite.onGetStatusFinish();
                }
            }
        }
    };

    private final int MY_PERMISSIONS_REQUEST_CODE = 1;
    private final ActivityHandler2 mActivityHandler2 = new ActivityHandler2(this);
    static boolean active = false;
    public static String urlDownload = "";
    public static int TAB_CHAT = 0;
    public static int TAB_COMPANY = 1;
    public static int TAB_FAVORITE = 2;
    public static int TAB_SETTING = 3;
    private int currentUserNo = 0;
    public static Uri imageUri;
    private SyncStatusService syncStatusService = null;
    private boolean isBound = false;
    private int companyNo;
    public static String type;
    public static ArrayList<String> mSelectedImage = new ArrayList<>();
    private OnGetStatusCallback mGetStatusCallbackCompany, mGetStatusCallbackFavorite;

    public void setGetStatusCallbackFavorite(OnGetStatusCallback mGetStatusCallback) {
        this.mGetStatusCallbackFavorite = mGetStatusCallback;
    }

    @Override
    protected void init() {
        File dir = new File(Environment.getExternalStorageDirectory() + Constant.pathDownload_no);
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }

        instance = this;
        myRoom = Statics.MYROOM_DEFAULT;
        CURRENT_TAB = 0;
        currentUserNo = Utils.getCurrentId();
        companyNo = new Prefs().getCompanyNo();

        active = true;
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        if (!CrewChatApplication.isLoggedIn) {
            CrewChatApplication.getInstance().syncData();
            CrewChatApplication.isLoggedIn = true;
            CrewChatApplication.currentId = currentUserNo;
        }

        if (BuildConfig.FLAVOR.equals("serverVersion")) {
            checkVersion();
        }

        chattingViewModel = ViewModelProviders.of(this).get(ChattingViewModel.class);
        chattingViewModel.checkHasCallUnreadCount();
    }

    public void setPermissionsReadExternalStorage() {
        String[] requestPermission;
        requestPermission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,};
        ActivityCompat.requestPermissions(this, requestPermission, 0);
    }

    @Override
    protected void inItShare() {
        if (Utils.checkStringValue(prefs.getaccesstoken()) && !prefs.getBooleanValue(Statics.PREFS_KEY_SESSION_ERROR, false)) {
            final Intent intent = getIntent();
            final String action = intent.getAction();
            type = intent.getType();
            if (type != null) {
                setPermissionsReadExternalStorage();
                tabAdapter = new TabPagerAdapter(getSupportFragmentManager(), 2, this);
                mViewPager.setAdapter(tabAdapter);
                mViewPager.setOffscreenPageLimit(1);
                tabLayout.setupWithViewPager(mViewPager);
                setupTabTwo();

            } else {
                tabAdapter = new TabPagerAdapter(getSupportFragmentManager(), 4, this);
                mViewPager.setAdapter(tabAdapter);
                mViewPager.setOffscreenPageLimit(3);
                tabLayout.setupWithViewPager(mViewPager);
                setupTab();
            }

            setupViewPager();
            mHandler.postDelayed(() -> {
                if (Intent.ACTION_SEND.equals(action) && type != null) {
                    if ("text/plain".equals(type)) {
                        // handleSendText(intent);
                    } else if (type.startsWith("video/")) {
                        handleSendVideo(intent);
                    } else if (type.startsWith("audio/")) {
                        handleSendAudio(intent);
                    } else if (type.startsWith("text/")) {
                        handleSendContact(intent);
                    } else if (type.startsWith("image/")) {
                        handlSendImage(intent);
                    } else {
                        handleSendFile(intent);
                    }
                } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
                    handleSendMultipleFile(intent);
                } else {
                    // Handle other intents, such as being started from the home screen
                }
            }, 1500);
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

    }

    protected void setupTabTwo() {
        if (tabLayout == null) {
            return;
        }
        View view = LayoutInflater.from(this).inflate(R.layout.custom_tab_view, null);
        tabLayout.getTabAt(0).setCustomView(view);
        tabLayout.getTabAt(1).setIcon(R.drawable.tabbar_group_ic);
    }

    void handleSendContact(Intent intent) {
        imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {


        }
    }

    void handleSendVideo(Intent intent) {
        imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            mSelectedImage.add(Utils.getPathFromURI(imageUri, getApplicationContext()));
        }
    }

    void handleSendAudio(Intent intent) {
        imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            //fab.performClick();
        }
    }

    void handleSendFile(Intent intent) {
        imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        mSelectedImage.clear();
        if (imageUri != null) {
            mSelectedImage.add(Utils.getPathFromURI(imageUri, getApplicationContext()));
        }
    }

    void handlSendImage(Intent intent) {
        imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        mSelectedImage.clear();
        if (imageUri != null) {
            mSelectedImage.add(Utils.getPathFromURI(imageUri, getApplicationContext()));
        }
    }

    void handleSendMultipleFile(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        mSelectedImage.clear();
        if (imageUris != null) {
            for (Uri path : imageUris) {
                if (mSelectedImage != null) {
                    mSelectedImage.add(Utils.getPathFromURI(path, getApplicationContext()));
                }

            }
        }
    }

    private void checkVersion() {
        HttpRequest.getInstance().checkVersionUpdate(new BaseHTTPCallBackWithString() {
            @Override
            public void onHTTPSuccess(String response) {
                Gson gson = new Gson();
                CheckUpdateDto checkUpdateDto = gson.fromJson(response, CheckUpdateDto.class);
                urlDownload = checkUpdateDto.getPackageUrl();
                Thread thread = new Thread(new UpdateRunnable(checkUpdateDto.getVersion()));
                thread.setDaemon(true);
                thread.start();
            }

            @Override
            public void onHTTPFail(ErrorDto errorDto) {

            }
        });


    }

    private class UpdateRunnable implements Runnable {
        String version = "";

        public UpdateRunnable(String version) {
            this.version = version;
        }

        @Override
        public void run() {
            try {
                String appVersion = BuildConfig.VERSION_NAME;
                if (compareVersionNames(appVersion, version) == -1) {
                    mActivityHandler2.sendEmptyMessage(Constant.ACTIVITY_HANDLER_START_UPDATE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class ActivityHandler2 extends Handler {
        private final WeakReference<MainActivity> mWeakActivity;

        public ActivityHandler2(MainActivity activity) {
            mWeakActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final MainActivity activity = mWeakActivity.get();

            if (activity != null) {
                if (msg.what == Constant.ACTIVITY_HANDLER_NEXT_ACTIVITY) {
                } else if (msg.what == Constant.ACTIVITY_HANDLER_START_UPDATE) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(R.string.string_update_content);
                    builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                        new Async_DownloadApkFile(MainActivity.this, "CrewChat").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        dialog.dismiss();
                    });
                    AlertDialog dialog = builder.create();
                    dialog.setCancelable(false);
                    dialog.show();
                }
            }
        }
    }

    @Subscribe
    public void reloadActivity(ReloadActivity reloadActivity) {
        finish();
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for (int i = 0; i < procInfos.size(); i++) {
            if (procInfos.get(i).processName.equals("com.crewcloud.apps.crewchat")) {
                startActivity(getIntent());
            }
        }
    }

    private class Async_DownloadApkFile extends AsyncTask<Void, Void, Void> {
        private String mApkFileName;
        private final WeakReference<MainActivity> mWeakActivity;
        private ProgressDialog mProgressDialog = null;

        public Async_DownloadApkFile(MainActivity activity, String apkFileName) {
            mWeakActivity = new WeakReference<>(activity);
            mApkFileName = apkFileName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            MainActivity activity = mWeakActivity.get();

            if (activity != null) {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage(getString(R.string.mailActivity_message_download_apk));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            BufferedInputStream bufferedInputStream = null;
            FileOutputStream fileOutputStream = null;

            try {
                URL apkUrl = new URL(urlDownload);
                urlConnection = (HttpURLConnection) apkUrl.openConnection();
                inputStream = urlConnection.getInputStream();
                bufferedInputStream = new BufferedInputStream(inputStream);

                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/" + mApkFileName + ".apk";
                fileOutputStream = new FileOutputStream(filePath);

                byte[] buffer = new byte[4096];
                int readCount;

                while (true) {
                    readCount = bufferedInputStream.read(buffer);
                    if (readCount == -1) {
                        break;
                    }

                    fileOutputStream.write(buffer, 0, readCount);
                    fileOutputStream.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (urlConnection != null) {
                    try {
                        urlConnection.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            MainActivity activity = mWeakActivity.get();
            if (activity != null) {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/" + mApkFileName + ".apk";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (!checkPermissions()) {
                        setPermissions();
                    } else {
                        Uri apkUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", new File(filePath));
                        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                        intent.setData(apkUri);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        activity.startActivity(intent);
                    }
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

            }

            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        }
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else
            return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    private void setPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_WIFI_STATE
        }, MY_PERMISSIONS_REQUEST_CODE);
    }

    //--->end Check version
    public int currentItem() {
        return mViewPager.getCurrentItem();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewPager.setCurrentItem(new Prefs().getIntValue("PAGE", 0));
        /*if(CompanyFragment.instance != null) {
            CompanyFragment.instance.initDB();
        }*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind service to sync status data
        Intent objIntent = new Intent(this, SyncStatusService.class);
        bindService(objIntent, this, Context.BIND_AUTO_CREATE);
    }

    protected void setupViewPager() {
        mViewPager.addOnPageChangeListener(this);
    }

    protected void setupTab() {
        if (tabLayout == null) {
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.custom_tab_view, null);
        tabLayout.getTabAt(0).setCustomView(view);
        tabLayout.getTabAt(1).setIcon(R.drawable.tabbar_group_ic);
        tabLayout.getTabAt(2).setIcon(R.drawable.nav_favorite_ic);
        tabLayout.getTabAt(3).setIcon(R.drawable.nav_mnu_hol_ic);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;

        Toast.makeText(this, getResources().getString(R.string.press_again_to_exit_message), Toast.LENGTH_SHORT).show();
        mHandler.postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    public void hideMenuSearch() {
        Log.d(TAG, "hideMenuSearch");
        if (menuItemSearch != null) {
            searchView.setIconified(true);
            searchView.setVisibility(View.GONE);
            menuItemMore.setVisible(false);
            menuItemSearch.collapseActionView();
            menuItemSearch.setVisible(false);
        }
    }

    public void gotoOrganizationChart() {
        if (CompanyFragment.instance != null
                && CompanyFragment.instance.getSubordinates().size() > 0
                && CompanyFragment.instance.isLoadTreeData()) {
            Intent intent = new Intent(MainActivity.this, NewOrganizationChart.class);
            intent.putExtra(Statics.IS_NEW_CHAT, true);
            intent.putExtra(Constants.LIST_MEMBER, (Serializable) CompanyFragment.instance.getSubordinates());

            if(MainActivity.type != null && MainActivity.mSelectedImage.size() > 0) {
                intent.putExtra(Constants.TYPE_SHARE, MainActivity.type);
                intent.putExtra(Constants.LIST_FILE_PATH_SHARE, MainActivity.mSelectedImage);
            }

            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "wait get list user finish", Toast.LENGTH_SHORT).show();
        }


    }


    // 탭을 직접 선택 했을 경우 이벤트 처리
    public static int CURRENT_TAB = 0;

    @Override
    public void onPageSelected(final int position) {
        if (position != TAB_CHAT) {
            if (CurrentChatListFragment.fragment != null) {
                CurrentChatListFragment.fragment.justHide();
            }
        }

        MainActivity.CURRENT_TAB = position;
        new Prefs().putIntValue("PAGE", position);
        if (position == TAB_CHAT || position == TAB_FAVORITE) {
            showPAB();
            hideSearchView();
            hideMenuSearch();
            showIcon();
            if (position == TAB_FAVORITE) {
                if (BaseFavoriteFragment.CURRENT_TAB == 0) {
                    hidePAB();
                }
            }
        } else if (position == TAB_COMPANY) {
            hideSearchIcon();
            hidePAB();
        } else if (position == TAB_SETTING) {
            hideSearchIcon();
            hidePAB();
            hideSearchView();
        }
        if (position == TAB_FAVORITE || position == TAB_COMPANY) {
            if (position == TAB_COMPANY || position == TAB_FAVORITE) {
                final ArrayList<TreeUserDTOTemp> users = AllUserDBHelper.getUser();
                boolean isStaticList = false;
                if (CrewChatApplication.listUsers != null && CrewChatApplication.listUsers.size() > 0) {
                    isStaticList = true;
                }
                final boolean finalIsStaticList = isStaticList;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final StatusDto status = new GetUserStatus().getStatusOfUsers(new Prefs().getHOST_STATUS(), companyNo);

                        if (status != null) {

                            for (final TreeUserDTOTemp u : users) {
                                boolean isUpdate = false;

                                for (final StatusItemDto sItem : status.getItems()) {

                                    if (sItem.getUserID().equals(u.getUserID())) {
                                        Log.d(TAG, "updateStatus 3:" + u.getName() + " # " + u.getDBId() + " # " + sItem.getUserID() + " # " + sItem.getStatus());
                                        AllUserDBHelper.updateStatus(u.getDBId(), sItem.getStatus());

                                        if (finalIsStaticList) {
                                            boolean xUpdate = false;
                                            TreeUserDTOTemp temp = null;

                                            for (TreeUserDTOTemp uu : CrewChatApplication.listUsers) {
                                                temp = uu;

                                                if (sItem.getUserID().equals(uu.getUserID())) {
                                                    uu.setStatus(sItem.getStatus());
                                                    xUpdate = true;
                                                    break;
                                                }
                                            }

                                            if (!xUpdate) {
                                                if (temp != null) {
                                                    temp.setStatus(Statics.USER_LOGOUT);
                                                }
                                            }
                                        }

                                        isUpdate = true;
                                        break;
                                    }
                                }

                                if (!isUpdate) {
                                    if (u.getUserNo() == currentUserNo) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                // 내 상태값을 바꾼다.
                                                UserDBHelper.updateStatus(u.getUserNo(), Statics.USER_LOGOUT);
                                            }
                                        }).start();
                                    }
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // 나 이외의 사용자 상태값을 바꾼다.
                                            AllUserDBHelper.updateStatus(u.getDBId(), Statics.USER_LOGOUT);
                                        }
                                    }).start();
                                }
                            }

                            // Send message to main thread after update status
                            Message msgObj = mHandler.obtainMessage();
                            Bundle b = new Bundle();
                            b.putBoolean("is_update", true);
                            if (position == TAB_COMPANY) {
                                b.putInt("tab", TAB_COMPANY);
                            } else {
                                b.putInt("tab", TAB_FAVORITE);
                            }

                            msgObj.setData(b);
                            Log.d(TAG, "mHandler 1");
                            mHandler.sendMessage(msgObj);
                        }

                    }
                }).start();

                // 유저 상태메시지 처리
                HttpRequest.getInstance().getAllUserInfo(new OnGetUserInfo() {
                    @Override
                    public void OnSuccess(final ArrayList<UserInfoDto> userInfo) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // If list user is #null then update user status string
                                if (users == null) {
                                    ArrayList<TreeUserDTOTemp> tempUsers = AllUserDBHelper.getUser();
                                    for (TreeUserDTOTemp sItem : tempUsers) {
                                        for (UserInfoDto u : userInfo) {
                                            if (sItem.getUserNo() == u.getUserNo()) {
                                                AllUserDBHelper.updateStatusString(sItem.getDBId(), u.getStateMessage());
                                            }
                                        }
                                    }
                                } else {
                                    for (TreeUserDTOTemp sItem : users) {
                                        for (UserInfoDto u : userInfo) {
                                            if (sItem.getUserNo() == u.getUserNo()) {
                                                AllUserDBHelper.updateStatusString(sItem.getDBId(), u.getStateMessage());
                                                sItem.setUserStatusString(u.getStateMessage());
                                            }
                                        }

                                    }
                                }

                                // Send message to main thread after update status
                                Message msgObj = mHandler.obtainMessage();
                                Bundle b = new Bundle();
                                b.putBoolean("is_update", true);
                                if (position == TAB_COMPANY) {
                                    b.putInt("tab", TAB_COMPANY);
                                } else {
                                    b.putInt("tab", TAB_FAVORITE);
                                }

                                msgObj.setData(b);
                                mHandler.sendMessage(msgObj);

                            }
                        }).start();
                    }

                    @Override
                    public void OnFail(ErrorDto errorDto) {
                    }
                });
            }

            Utils.hideKeyboard(this);
            Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + mViewPager.getCurrentItem());
            new Prefs().putIntValue("PAGE", position);

            // based on the current position you can then cast the page to the correct
            // class and call the method:
            // 탭 화면이 스크롤 되어 질 때에 이벤트 처리(좌우), 화면에 보이는 검색 아이콘등을 설정

            if (position != TAB_COMPANY) {
                hideMenuSearch();
            } else {
                if (menuItemSearch != null && searchView != null) {
                    menuItemSearch.setVisible(true);
                    searchView.setVisibility(View.VISIBLE);
                } else {
                    Log.d(TAG, "menuItemSearch null");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (menuItemSearch != null && searchView != null) {
                                menuItemSearch.setVisible(true);
                                searchView.setVisibility(View.VISIBLE);
                            } else {
                                Log.d(TAG, "still null");
                            }
                        }
                    }, 2000);
                }
            }

            if (mViewPager.getCurrentItem() == TAB_CHAT && page != null) {
                if (menuItemSearch != null) {
                    searchView.setIconified(true);
                    searchView.setVisibility(View.GONE);
                    menuItemMore.setVisible(false);
                    menuItemSearch.collapseActionView();
                    menuItemSearch.setVisible(false);
                }
            } else if (mViewPager.getCurrentItem() == TAB_COMPANY && page != null) {
            } else if (mViewPager.getCurrentItem() == TAB_FAVORITE && page != null) {
                if (menuItemSearch != null && menuItemMore != null) {
                    menuItemSearch.collapseActionView();
                    menuItemSearch.setVisible(false);
                }
            } else {
                if (menuItemSearch != null && menuItemMore != null) {
                    searchView.setIconified(true);
                    searchView.setVisibility(View.GONE);
                    menuItemMore.setVisible(false);
                    menuItemSearch.collapseActionView();
                    menuItemSearch.setVisible(false);
                }
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public static void cancelAllNotification(Context ctx) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancelAll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        myRoom = Statics.MYROOM_DEFAULT;
        cancelAllNotification(CrewChatApplication.getInstance());

        if (isBound) {
            unbindService(this);
        }

        active = false;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        SyncStatusService.Binder binder = (SyncStatusService.Binder) service;
        syncStatusService = binder.getMyService();

        new Thread(new Runnable() {
            @Override
            public void run() {
                syncStatusService.syncData();
                syncStatusService.syncStatusString();
            }
        }).start();

        isBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        isBound = false;
    }
}