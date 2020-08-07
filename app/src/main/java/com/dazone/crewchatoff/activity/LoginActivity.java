package com.dazone.crewchatoff.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.BuildConfig;
import com.dazone.crewchatoff.HTTPs.HttpOauthRequest;
import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.customs.AlertDialogView;
import com.dazone.crewchatoff.customs.IconButton;
import com.dazone.crewchatoff.database.AllUserDBHelper;
import com.dazone.crewchatoff.database.BelongsToDBHelper;
import com.dazone.crewchatoff.database.ChatMessageDBHelper;
import com.dazone.crewchatoff.database.ChatRoomDBHelper;
import com.dazone.crewchatoff.database.DepartmentDBHelper;
import com.dazone.crewchatoff.database.FavoriteGroupDBHelper;
import com.dazone.crewchatoff.database.FavoriteUserDBHelper;
import com.dazone.crewchatoff.database.ServerSiteDBHelper;
import com.dazone.crewchatoff.database.UserDBHelper;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallBack;
import com.dazone.crewchatoff.interfaces.ICheckSSL;
import com.dazone.crewchatoff.interfaces.OnCheckDevice;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import me.leolin.shortcutbadger.ShortcutBadger;


public class LoginActivity extends BaseActivity implements BaseHTTPCallBack, OnCheckDevice {
    String TAG = "LoginActivity";
    private Button btnLogin;
    private EditText edtUserName, edtPassword;
    private AutoCompleteTextView edtServer;
    private ScrollView scrollView;
    private boolean firstLogin = true;
    private Dialog errorDialog;
    private IconButton mBtnSignUp;
    private FrameLayout iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        isDisplayPass = true;
        flag = false;
        attachKeyboardListeners();
        ShortcutBadger.removeCount(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        firstChecking();
    }

    private void firstChecking() {
        if (firstLogin) {
            if (Utils.isNetworkAvailable()) {
                doLogin();
            } else {
                notNetwork();
            }
        }
    }

    public void notNetwork() {
        if (Utils.checkStringValue(prefs.getaccesstoken()) && !prefs.getBooleanValue(Statics.PREFS_KEY_SESSION_ERROR, false)) {
            findViewById(R.id.logo).setVisibility(View.VISIBLE);
            callActivity(MainActivity.class);
            finish();
        } else {
            // Haven't ever login yet --> go to login screen and remind switch on network
            prefs.putBooleanValue(Statics.PREFS_KEY_SESSION_ERROR, false);
            findViewById(R.id.logo).setVisibility(View.GONE);
            firstLogin = false;
            init();
        }
    }

    private void doLogin() {
        if (Utils.checkStringValue(prefs.getaccesstoken()) && !prefs.getBooleanValue(Statics.PREFS_KEY_SESSION_ERROR, false)) {
            new Thread(() -> HttpOauthRequest.getInstance().checkLogin(LoginActivity.this)).start();
        } else {
            prefs.putBooleanValue(Statics.PREFS_KEY_SESSION_ERROR, false);
            findViewById(R.id.logo).setVisibility(View.GONE);
            firstLogin = false;
            init();
        }
    }

    private boolean isAutoLogin = true;

    private void init() {
        Intent intent = new Intent();
        intent.setAction("com.dazone.crewcloud.account.get");
        intent.putExtra("senderPackageName", this.getPackageName());
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(intent);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Statics.BROADCAST_ACTION);
        registerReceiver(accountReceiver, intentFilter);
        flag = true;

        btnLogin = findViewById(R.id.login_btn_login);
        edtUserName = findViewById(R.id.login_edt_username);
        edtPassword = findViewById(R.id.login_edt_passsword);
        edtServer = findViewById(R.id.login_edt_server);
        scrollView = findViewById(R.id.scl_login);
        edtUserName.setText(prefs.getUserID());

        String dm = prefs.getDDSServer();
        if (dm.contains("crewcloud.net")) {
            String[] str = dm.split("[.]");
            if (str[0] != null)
                dm = str[0];
        } else {
        }
        edtServer.setText(dm);
        edtPassword.setText(prefs.getPass());
        mBtnSignUp = findViewById(R.id.login_btn_signup);
        mBtnSignUp.setOnClickListener(v -> {
            Intent intent1 = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent1);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        btnLogin.setOnClickListener(v -> {
            String mUsername = edtUserName.getText().toString().trim();
            String mPassword = edtPassword.getText().toString();
            String domain = edtServer.getText().toString().trim();

            Utils.setServerSite(domain);
            if (TextUtils.isEmpty(checkStringValue(domain, mUsername, mPassword))) {
                showProgressDialog();
                HttpRequest.getInstance().checkSSL(new ICheckSSL() {
                    @Override
                    public void hasSSL(boolean hasSSL) {
                        HttpOauthRequest.getInstance().loginV2(LoginActivity.this, mUsername, mPassword, Build.VERSION.RELEASE);
                    }

                    @Override
                    public void checkSSLError(ErrorDto errorData) {
                        dismissProgressDialog();
                        Toast.makeText(LoginActivity.this, "Cannot check ssl this domain!", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                showAlertDialog(getString(R.string.app_name), checkStringValue(domain, mUsername, mPassword), getString(R.string.string_ok), null, v1 -> customDialog.dismiss(), null);
            }
        });

        iv = findViewById(R.id.iv);
        iv.setOnClickListener(v -> displayPass());
    }

    boolean isDisplayPass = true;

    void displayPass() {
        if (isDisplayPass) {
            edtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            isDisplayPass = false;
        } else {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            isDisplayPass = true;
        }
    }

    void autoLogin(String username, String password, String subDomain) {
        Utils.setServerSite(subDomain);
        String error = checkStringValue(subDomain, username, password);

        if (TextUtils.isEmpty(error)) {
            showProgressDialog();
            HttpRequest.getInstance().checkSSL(new ICheckSSL() {
                @Override
                public void hasSSL(boolean hasSSL) {
                    HttpOauthRequest.getInstance().autoLogin(LoginActivity.this, username, Build.VERSION.RELEASE);
                }

                @Override
                public void checkSSLError(ErrorDto errorData) {
                    dismissProgressDialog();
                    Toast.makeText(LoginActivity.this, "Cannot check ssl this domain!", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            showAlertDialog(getString(R.string.app_name), error, getString(R.string.string_ok), null, v -> customDialog.dismiss(), null);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (flag) unregisterReceiver(accountReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean flag = false;
    private BroadcastReceiver accountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String receiverPackageName = intent.getExtras().getString("receiverPackageName");
            if (LoginActivity.this.getPackageName().equals(receiverPackageName)) {
                String companyID = intent.getExtras().getString("companyID");
                String userID = intent.getExtras().getString("userID");
                if (!TextUtils.isEmpty(companyID) && !TextUtils.isEmpty(userID)) {
                    if (isAutoLogin) {
                        isAutoLogin = false;
                        showDialogAutoLogin(companyID, userID);
                    }
                }
            }
        }
    };

    public void showDialogAutoLogin(final String companyID, final String UserID) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.auto_login, null);
        TextView tvCompany = alertLayout.findViewById(R.id.tvCompany);
        TextView tvUser = alertLayout.findViewById(R.id.tvUser);
        TextView tvTitle = alertLayout.findViewById(R.id.tv_title_auto);
        tvCompany.setText(": " + companyID);
        tvUser.setText(": " + UserID);
        tvTitle.setText(getResources().getString(R.string.autoLogin));
        final AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setView(alertLayout);
        final AlertDialog alertDialog = adb.create();
        alertDialog.show();

        TextView btnYes = alertLayout.findViewById(R.id.btn_yes_auto);
        TextView btnNo = alertLayout.findViewById(R.id.btn_no_auto);
        btnYes.setOnClickListener(view -> {
            alertDialog.dismiss();
            autoLogin(UserID, "", companyID);
        });

        btnNo.setOnClickListener(view -> alertDialog.dismiss());
    }

    private String checkStringValue(String server_site, String username, String password) {
        String result = "";
        if (TextUtils.isEmpty(server_site)) {
            if (TextUtils.isEmpty(result)) {
                result += getString(R.string.string_server_site);
            } else {
                result += ", " + getString(R.string.string_server_site);
            }
        }
        if (TextUtils.isEmpty(username)) {
            if (TextUtils.isEmpty(result)) {
                result += getString(R.string.login_username);
            } else {
                result += ", " + getString(R.string.login_username);
            }
        }
        if (TextUtils.isEmpty(password)) {
            if (TextUtils.isEmpty(result)) {
                result += getString(R.string.login_password);
            } else {
                result += ", " + getString(R.string.login_password);
            }
        }
        if (TextUtils.isEmpty(result)) {
            return result;
        } else {
            return result += " " + getString(R.string.login_empty_input);
        }
    }

    @Override
    public void onHTTPSuccess() {
        ServerSiteDBHelper.addServerSite(CrewChatApplication.getInstance().getPrefs().getServerSite());

        createGMC();
        loginSuccess();
    }

    private void loginSuccess() {
        dismissProgressDialog();
        callActivity(MainActivity.class);
        finish();
    }

    @Override
    public void onDeviceSuccess() {
        doLogin();
    }

    @Override
    public void onHTTPFail(ErrorDto errorDto) {
        Utils.hideKeyboard(this);
        if (firstLogin) {

            String first_login = Statics.FIRST_LOGIN;
            boolean isLogin = new Prefs().getBooleanValue(first_login, false);
            if (isLogin) {
                notNetwork();
            } else {
                dismissProgressDialog();
                firstLogin = false;
                findViewById(R.id.logo).setVisibility(View.GONE);
                init();
            }
        } else {
            dismissProgressDialog();
            String error_msg = "";
            switch (errorDto.code) {
                case 2:
                    error_msg = errorDto.getMessage();
                    break;
                case 3:
                    error_msg = getString(R.string.string_error_code_3);
                    break;
                case 4:
                    error_msg = getString(R.string.string_error_code_4);
                    break;
                case 5:
                    error_msg = getString(R.string.string_error_code_5);
                    break;
                case 9:
                    error_msg = getString(R.string.string_error_code_9);
                    break;
                default:
                    error_msg = getString(R.string.string_error_code_default);
                    break;
            }
            if (errorDto.getCode() == 1) {
                Toast.makeText(this, error_msg, Toast.LENGTH_SHORT).show();
            } else {
                showAlertDialog(error_msg, getString(R.string.string_ok), "", v -> customDialog.dismiss());
            }
        }
    }

    View v;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int heightDiff = rootLayout.getRootView().getHeight() - rootLayout.getHeight();
            int contentViewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();

            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(LoginActivity.this);

            if (heightDiff <= 100) {
                onHideKeyboard();

                v = getCurrentFocus();
                Intent intent = new Intent("KeyboardWillHide");
                broadcastManager.sendBroadcast(intent);
            } else {
                int keyboardHeight = heightDiff - contentViewTop;
                onShowKeyboard();
                v = getCurrentFocus();
                Intent intent = new Intent("KeyboardWillShow");
                intent.putExtra("KeyboardHeight", keyboardHeight);
                broadcastManager.sendBroadcast(intent);
            }
        }
    };

    private boolean keyboardListenersAttached = false;
    private ViewGroup rootLayout;

    protected void onShowKeyboard() {
        if (!hasScroll) {
            if (scrollView != null) {
                scrollView.post(() -> {
                    scrollView.scrollTo(0, Utils.getDimenInPx(R.dimen.scroll_height_login));
                    if (v != null) {
                        v.requestFocus();
                    }
                });
            }
            hasScroll = true;
        }
    }

    boolean hasScroll = false;

    protected void onHideKeyboard() {
        hasScroll = false;
    }

    protected void attachKeyboardListeners() {
        if (keyboardListenersAttached) {
            return;
        }
        rootLayout = findViewById(R.id.root_login);
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
        keyboardListenersAttached = true;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void onDestroy() {
        super.onDestroy();
        if (keyboardListenersAttached) {
            try {
                rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardLayoutListener);
            } catch (NoSuchMethodError x) {
                rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(keyboardLayoutListener);
            }
        }
        // DisMiss error dialog
        if (errorDialog != null && errorDialog.isShowing()) {
            errorDialog.cancel();
        }
    }

    private static final int ACTIVITY_HANDLER_NEXT_ACTIVITY = 1111;
    private static final int ACTIVITY_HANDLER_START_UPDATE = 1112;

    private static class ActivityHandler extends Handler {
        private final WeakReference<LoginActivity> mWeakActivity;

        public ActivityHandler(LoginActivity activity) {
            mWeakActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final LoginActivity activity = mWeakActivity.get();

            if (activity != null) {
                if (msg.what == ACTIVITY_HANDLER_NEXT_ACTIVITY) {

                    activity.firstChecking();
                } else if (msg.what == ACTIVITY_HANDLER_START_UPDATE) {
                    if (!activity.isFinishing()) {
                        AlertDialogView.normalAlertDialogWithCancelWhite(activity, null, Utils.getString(R.string.string_update_content_new), Utils.getString(R.string.no), Utils.getString(R.string.yes), new AlertDialogView.OnAlertDialogViewClickEvent() {
                            @Override
                            public void onOkClick(DialogInterface alertDialog) {
                                new WebClientAsyncTask(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }

                            @Override
                            public void onCancelClick() {

                                activity.firstChecking();
                            }
                        });
                    }


                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isAutoLogin = false;
    }


    // ----------------------------------------------------------------------------------------------

    private static class WebClientAsyncTask extends AsyncTask<Void, Void, Void> {
        private final WeakReference<LoginActivity> mWeakActivity;
        private ProgressDialog mProgressDialog = null;

        public WebClientAsyncTask(LoginActivity activity) {
            mWeakActivity = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            LoginActivity activity = mWeakActivity.get();

            if (activity != null) {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage(activity.getString(R.string.wating_app_download));
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
                URL apkUrl = new URL(Constant.ROOT_URL_UPDATE + "/Android/Package/CrewChat.apk");
                urlConnection = (HttpURLConnection) apkUrl.openConnection();
                inputStream = urlConnection.getInputStream();
                bufferedInputStream = new BufferedInputStream(inputStream);

                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/CrewChat.apk";
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

            LoginActivity activity = mWeakActivity.get();

            if (activity != null) {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/CrewChat.apk";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    File toInstall = new File(filePath);
                    Uri apkUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", toInstall);
                    Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    intent.setData(apkUri);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    activity.startActivity(intent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
                    activity.startActivity(intent);
                }
            }

            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        }
    }

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private GoogleCloudMessaging gcm;
    private Context context;
    private String regId;

    private void createGMC() {
        context = getApplicationContext();
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regId = new Prefs().getGCMregistrationid();
            if (regId.isEmpty()) {
                registerInBackground();
            } else {
                insertDevice(regId);
            }
        } else {
            dismissProgressDialog();
            callActivity(MainActivity.class);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    private boolean checkPlayServices() {
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                runOnUiThread(() -> {
                    errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, LoginActivity.this,
                            PLAY_SERVICES_RESOLUTION_REQUEST);
                    errorDialog.show();
                });

            }

            return true;
        }
        return true;
    }

    private void registerInBackground() {
        new register().execute("");
    }

    public class register extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                if (gcm == null) {
                    return null;
                }
                regId = gcm.register(Statics.GOOGLE_SENDER_ID);
            } catch (IOException ex) {
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            new Prefs().setGCMregistrationid(regId);
            insertDevice(regId);
        }
    }

    private void insertDevice(final String regId) {
        new Thread(() -> HttpRequest.getInstance().InsertDevice(regId, new BaseHTTPCallBack() {
            @Override
            public void onHTTPSuccess() {
            }

            @Override
            public void onHTTPFail(ErrorDto errorDto) {
            }
        })).start();
    }
}