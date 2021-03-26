package com.dazone.crewchatoff.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.dazone.crewchatoff.BuildConfig;
import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.*;
import com.dazone.crewchatoff.dto.userfavorites.FavoriteGroupDto;
import com.dazone.crewchatoff.dto.userfavorites.FavoriteUserDto;
import com.dazone.crewchatoff.interfaces.*;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
        handleSSLHandshake();
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
        } else {
            isLoggedIn = false;
        }

        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> handleUncaughtException(thread, e));
    }

    public void handleUncaughtException(Thread thread, Throwable e) {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically
        System.exit(1); // kill off the crashed app
    }

    public void syncData() {
        CrewChatApplication.isLoggedIn = true;
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static synchronized CrewChatApplication getInstance() {
        return _instance;
    }

    private static void init() {
        mPrefs = new Prefs();
        // Check version code, if is update
        int old_version = mPrefs.getIntValue(Statics.VERSION_CODE, 0);
        int versionCode = BuildConfig.VERSION_CODE;

        if (old_version < versionCode) {
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
        Date date = new Date(getTimeLocal() - getPrefs().getLongValue(Statics.TIME_SERVER_MILI, 0));
        return TimeUtils.showTimeWithoutTimeZone(date.getTime(), Statics.yyyy_MM_dd_HH_mm_ss_SSS);
    }

    public long getTimeLocal() {
        return System.currentTimeMillis() - getPrefs().getLongValue(Statics.TIME_LOCAL_MILI, 0);
    }

    /**
     * Enables https connections
     */
    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }
}