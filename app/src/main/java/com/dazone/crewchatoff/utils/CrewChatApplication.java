package com.dazone.crewchatoff.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.dazone.crewchatoff.BuildConfig;
import com.dazone.crewchatoff.HTTPs.GetUserStatus;
import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.database.*;
import com.dazone.crewchatoff.dto.*;
import com.dazone.crewchatoff.dto.userfavorites.FavoriteGroupDto;
import com.dazone.crewchatoff.dto.userfavorites.FavoriteUserDto;
import com.dazone.crewchatoff.interfaces.*;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.*;

public class CrewChatApplication extends MultiDexApplication {
    private static final String TAG = "EmcorApplication";
    public static boolean isAddUser = true;
    private static CrewChatApplication _instance;
    private RequestQueue mRequestQueue;
    private static Prefs mPrefs;
    private HashMap<Object, Object> _data = new HashMap<>();
    public ImageLoader imageLoader = ImageLoader.getInstance();

    // Define some static var here
    public static ArrayList<TreeUserDTOTemp> listUsers = null;
    public static ArrayList<TreeUserDTO> listDeparts = null;
    public static int currentId = 0;
    public static boolean CrewChatLocalDatabase = false;
//    public static String currentName = "";

    public static ArrayList<FavoriteGroupDto> listFavoriteGroup = null;
    public static ArrayList<FavoriteUserDto> listFavoriteTop = null;
    public static UserDto currentUser = null;
    private int companyNo;

    public static boolean isLoggedIn = false;

    @Override
    public void onCreate() {
        super.onCreate();
        isAddUser = true;
        _instance = this;
        init();
        imageLoader.init(new ImageLoaderConfiguration.Builder(getApplicationContext())
                .threadPoolSize(5)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new WeakMemoryCache())
                .build());

        companyNo = mPrefs.getCompanyNo();

        // Sync data from server
        // Check session here
        if (Utils.checkStringValue(mPrefs.getaccesstoken()) && !mPrefs.getBooleanValue(Statics.PREFS_KEY_SESSION_ERROR, false)) {
            isLoggedIn = true;

            syncData();

            // Thread to Load data from local
            loadStaticLocalData();
            // Thread to Update status from server
            if (listUsers != null) {
                updateAllAppInfo();
            }
        } else {
            isLoggedIn = false;
        }

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(thread, e);
            }
        });
    }

    public void handleUncaughtException(Thread thread, Throwable e) {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically
        System.exit(1); // kill off the crashed app
    }

    public void syncData() {
        CrewChatApplication.isLoggedIn = true;
    }

    private ArrayList<TreeUserDTO> temp = new ArrayList<>();

    public void convertData(List<TreeUserDTO> treeUserDTOs) {
        if (treeUserDTOs != null && treeUserDTOs.size() != 0) {
            for (TreeUserDTO dto : treeUserDTOs) {
                if (dto.getSubordinates() != null && dto.getSubordinates().size() > 0) {
                    temp.add(dto);
                    convertData(dto.getSubordinates());
                } else {
                    temp.add(dto);
                }
            }
        }
    }

    public void loadStaticLocalData() {

    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static synchronized CrewChatApplication getInstance() {
        return _instance;
    }

    public void updateAllAppInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    private static void init() {
        mPrefs = new Prefs();

        // Check version code, if is update
        int old_version = mPrefs.getIntValue(Statics.VERSION_CODE, 0);
        int versionCode = BuildConfig.VERSION_CODE;

        if (old_version < versionCode) {
            // upgrade
            mPrefs.putIntValue(Statics.VERSION_CODE, versionCode);
        }

        // init default value to enable/ disable notification settings
        if (!mPrefs.isContainKey(Statics.ENABLE_NOTIFICATION)) {
            mPrefs.putBooleanValue(Statics.ENABLE_NOTIFICATION, true);

            Map<String, Object> params = new HashMap<>();
            params.put("enabled", true);
            params.put("sound", true);
            params.put("vibrate", true);
            params.put("notitime", true);
            params.put("starttime", Statics.DEFAULT_START_NOTIFICATION_TIME + ":00");
            params.put("endtime", Statics.DEFAULT_END_NOTIFICATION_TIME + ":00");
            params.put("confirmonline", true);

            HttpRequest.getInstance().setNotification(Urls.URL_INSERT_DEVICE,
                    mPrefs.getGCMregistrationid(),
                    params,
                    new OnSetNotification() {
                        @Override
                        public void OnSuccess() {
                        }

                        @Override
                        public void OnFail(ErrorDto errorDto) {
                        }
                    }
            );

        }

        if (!mPrefs.isContainKey(Statics.ENABLE_SOUND)) {
            mPrefs.putBooleanValue(Statics.ENABLE_SOUND, true);
        }

        if (!mPrefs.isContainKey(Statics.ENABLE_VIBRATE)) {
            mPrefs.putBooleanValue(Statics.ENABLE_VIBRATE, true);
        }

        if (!mPrefs.isContainKey(Statics.ENABLE_TIME)) {
            mPrefs.putBooleanValue(Statics.ENABLE_TIME, false);
        }

        if (!mPrefs.isContainKey(Statics.ENABLE_NOTIFICATION_WHEN_USING_PC_VERSION)) {
            mPrefs.putBooleanValue(Statics.ENABLE_NOTIFICATION_WHEN_USING_PC_VERSION, true);
        }

        // Default value for notification time
        if (!mPrefs.isContainKey(Statics.START_NOTIFICATION_HOUR)) {
            mPrefs.putIntValue(Statics.START_NOTIFICATION_HOUR, Statics.DEFAULT_START_NOTIFICATION_TIME);
        }

        if (!mPrefs.isContainKey(Statics.START_NOTIFICATION_MINUTES)) {
            mPrefs.putIntValue(Statics.START_NOTIFICATION_MINUTES, 0);
        }

        if (!mPrefs.isContainKey(Statics.END_NOTIFICATION_HOUR)) {
            mPrefs.putIntValue(Statics.END_NOTIFICATION_HOUR, Statics.DEFAULT_END_NOTIFICATION_TIME);
        }

        if (!mPrefs.isContainKey(Statics.END_NOTIFICATION_MINUTES)) {
            mPrefs.putIntValue(Statics.END_NOTIFICATION_MINUTES, 0);
        }
    }

    public Prefs getPrefs() {
        return mPrefs;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setRetryPolicy(new DefaultRetryPolicy(Statics.REQUEST_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public Object getData(Object key) {
        return _data.get(key);
    }

    public static long currentRoomNo = 0;
    public static long currentNotification = 0;

    public static void resetValue() {
        if (listUsers != null) {
            listUsers.clear();
            listUsers = null;
        }

        if (listDeparts != null) {
            listDeparts.clear();
            listDeparts = null;
        }

        currentId = 0;

        if (listFavoriteGroup != null) {
            listFavoriteGroup.clear();
            listFavoriteGroup = null;
        }

        if (listFavoriteTop != null) {
            listFavoriteTop.clear();
            listFavoriteTop = null;
        }
    }

    public String getTimeServer() {
        Date date = new Date(System.currentTimeMillis() - getPrefs().getLongValue(Statics.TIME_SERVER_MILI, 0));
        return TimeUtils.showTimeWithoutTimeZone(date.getTime(), Statics.yyyy_MM_dd_HH_mm_ss_SSS);
    }


}