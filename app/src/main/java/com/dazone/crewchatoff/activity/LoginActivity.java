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
    private String username, password;
    private String subDomain;
    protected int activityNumber = 0;
    private TextView forgot_pass, help_login, have_no_id_login;
    private String msg = "";
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
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getInt("count_id") != 0) {
            activityNumber = bundle.getInt("count_id");
        }

//        Log.d(TAG,"CrewChatApplication.isAddUser:"+CrewChatApplication.isAddUser);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (Utils.isNetworkAvailable()) {
//            Thread thread = new Thread(new UpdateRunnable());
//            thread.setDaemon(true);
//            thread.start();
//        } else {
//            firstChecking();
//        }

        firstChecking();

        //firstChecking(); // for beta version on google play store
    }

    private void firstChecking() {
        Log.d(TAG, "firstChecking");
        if (firstLogin) {
            Log.d(TAG, "firstLogin:" + firstLogin);
            if (Utils.isNetworkAvailable()) {
               /* if (prefs.getintrocount() < 1) {
                    //HttpOauthRequest.getInstance().checkPhoneToken(this);
                    prefs.putintrocount(prefs.getintrocount() + 1);
                } else {
                    doLogin();
                }*/

                doLogin();
                Log.d(TAG, "isNetworkAvailable");
            } else {
                notNetwork();
            }
        }
    }

    public void notNetwork() {
        Log.d(TAG, "not isNetworkAvailable");
        //showNetworkDialog();
        // if user is logged in, let's go to main activity, will update this function on next version
        // has logged in before and session is OK
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
            Log.d(TAG, "checkLogin");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpOauthRequest.getInstance().checkLogin(LoginActivity.this);
                }
            }).start();

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

        Log.d(TAG, "init");
        try {
            ShortcutBadger.applyCount(this, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        btnLogin = (Button) findViewById(R.id.login_btn_login);
        edtUserName = (EditText) findViewById(R.id.login_edt_username);
        edtPassword = (EditText) findViewById(R.id.login_edt_passsword);
        edtServer = (AutoCompleteTextView) findViewById(R.id.login_edt_server);
        scrollView = (ScrollView) findViewById(R.id.scl_login);
        /*forgot_pass = (TextView) findViewById(R.id.forgot_pass);
        help_login = (TextView) findViewById(R.id.help_login);
        have_no_id_login = (TextView) findViewById(R.id.have_no_id_login);*/
        edtUserName.setText(prefs.getUserID());

        String dm = prefs.getDDSServer();
        Log.d(TAG, "domain:" + dm);
        if (dm.contains("crewcloud.net")) {
            String str[] = dm.split("[.]");
            if (str[0] != null)
                dm = str[0];
        } else {
        }
        edtServer.setText(dm);
        edtPassword.setText(prefs.getPass());
        mBtnSignUp = (IconButton) findViewById(R.id.login_btn_signup);
        mBtnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Log.d(TAG, "AllUserDBHelper.getUser():" + AllUserDBHelper.getUser().size());
                username = edtUserName.getText().toString().trim();
                password = edtPassword.getText().toString();
                subDomain = edtServer.getText().toString().trim();

                if (subDomain.contains(".")) {
                    Log.d(TAG, "contains");
                } else {
                    Log.d(TAG, "dont contains");
                    subDomain = subDomain + ".crewcloud.net";
                }

                String error = checkStringValue(subDomain, username, password);

                if (TextUtils.isEmpty(error)) {
                    Log.d(TAG, "subDomain:" + subDomain);
                    // Module URL
                    server_site = getServerSite(subDomain);
                    if (!TextUtils.isEmpty(server_site)) {
                        if (!server_site.toLowerCase().startsWith("http")) {
                            server_site = "http://" + server_site;
                        }
                    }
                    Log.d(TAG, "server_site:" + server_site);
                    // URL to login

                    String loginUrl = getLoginServerSite(subDomain);
                    Log.d(TAG, "loginUrl:" + loginUrl);
                    if (!TextUtils.isEmpty(loginUrl)) {
                        if (!loginUrl.toLowerCase().startsWith("http")) {
                            loginUrl = "http://" + loginUrl;
                        }
                        showProgressDialog();
                        HttpOauthRequest.getInstance().loginV2(LoginActivity.this, username, password, Build.VERSION.RELEASE, subDomain, loginUrl);
                    } else {
                        showAlertDialog(getString(R.string.app_name), getString(R.string.string_wrong_server_site), getString(R.string.string_ok), null, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                customDialog.dismiss();
                            }
                        }, null);
                    }
                } else {
                    showAlertDialog(getString(R.string.app_name), error, getString(R.string.string_ok), null, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            customDialog.dismiss();
                        }
                    }, null);
                }
            }
        });

        iv = (FrameLayout) findViewById(R.id.iv);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPass();
            }
        });

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
        password = "1";
        if (subDomain.contains(".")) {
            Log.d(TAG, "contains");
        } else {
            Log.d(TAG, "dont contains");
            subDomain = subDomain + ".crewcloud.net";
        }

        String error = checkStringValue(subDomain, username, password);

        if (TextUtils.isEmpty(error)) {
            Log.d(TAG, "subDomain:" + subDomain);
            // Module URL
            server_site = getServerSite(subDomain);
            if (!TextUtils.isEmpty(server_site)) {
                if (!server_site.toLowerCase().startsWith("http")) {
                    server_site = "http://" + server_site;
                }
            }
            Log.d(TAG, "server_site:" + server_site);
            // URL to login

            String loginUrl = getLoginServerSite(subDomain);
            Log.d(TAG, "loginUrl:" + loginUrl);
            if (!TextUtils.isEmpty(loginUrl)) {
                if (!loginUrl.toLowerCase().startsWith("http")) {
                    loginUrl = "http://" + loginUrl;
                }
                showProgressDialog();
                HttpOauthRequest.getInstance().autoLogin(LoginActivity.this, username, Build.VERSION.RELEASE, subDomain, loginUrl);
            } else {
                showAlertDialog(getString(R.string.app_name), getString(R.string.string_wrong_server_site), getString(R.string.string_ok), null, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        customDialog.dismiss();
                    }
                }, null);
            }
        } else {
            showAlertDialog(getString(R.string.app_name), error, getString(R.string.string_ok), null, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customDialog.dismiss();
                }
            }, null);
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
            Log.d(TAG, "receiverPackageName:" + receiverPackageName);
            if (LoginActivity.this.getPackageName().equals(receiverPackageName)) {
                //String senderPackageName = intent.getExtras().getString("senderPackageName");
                String companyID = intent.getExtras().getString("companyID");
                String userID = intent.getExtras().getString("userID");
                if (!TextUtils.isEmpty(companyID) && !TextUtils.isEmpty(userID)) {

                    Log.d(TAG, "companyID:" + companyID);
                    Log.d(TAG, "userID:" + userID);
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
        TextView tvCompany = (TextView) alertLayout.findViewById(R.id.tvCompany);
        TextView tvUser = (TextView) alertLayout.findViewById(R.id.tvUser);
        TextView tvTitle = (TextView) alertLayout.findViewById(R.id.tv_title_auto);
        tvCompany.setText(": " + companyID);
        tvUser.setText(": " + UserID);
        tvTitle.setText(getResources().getString(R.string.autoLogin));
       /* ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.title_auto_login));
        String titleText = getResources().getString(R.string.autoLogin);

        // Initialize a new spannable string builder instance
        SpannableStringBuilder ssBuilder = new SpannableStringBuilder(titleText);

        // Apply the text color span
        ssBuilder.setSpan(
                foregroundColorSpan,
                0,
                titleText.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );*/
        final AlertDialog.Builder adb = new AlertDialog.Builder(this);

      //  adb.setTitle(ssBuilder);
        adb.setView(alertLayout);
        final AlertDialog alertDialog = adb.create();
        alertDialog.show();
      /*  TextView textView = (TextView) alertDialog.findViewById(android.R.id.title);
        float spTextSize = 17;
        textView.setTextSize(spTextSize);*/
       /* adb.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                autoLogin(UserID, "", companyID);
            }
        });

        adb.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
*/
        TextView btnYes = (TextView) alertLayout.findViewById(R.id.btn_yes_auto);
        TextView btnNo = (TextView) alertLayout.findViewById(R.id.btn_no_auto);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                autoLogin(UserID, "", companyID);
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

      // adb.create().show();
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

    private String getServerSite(String server_site) {
        String[] domains = server_site.split("[.]");
        String subDomain = "crewcloud";
        if (server_site.equalsIgnoreCase("vn.bizsw.co.kr")) {
            return "vn.bizsw.co.kr:8080";
        }

        if (domains.length <= 1 || subDomain.contains(domains[1])) {
            return domains[0] + ".crewcloud.net";
        } else {
            return server_site;
        }
    }

    private String getLoginServerSite(String server_site) {
        String[] domains = server_site.split("[.]");
        String subDomain = "crewcloud";
        if (server_site.equalsIgnoreCase("vn.bizsw.co.kr")) {
            return "vn.bizsw.co.kr:8080";
        }

        if (domains.length <= 1 || subDomain.contains(domains[1])) {
            return "www.crewcloud.net";
        } else {
            return server_site;
        }
    }

    @Override
    public void onHTTPSuccess() {
	        if (!TextUtils.isEmpty(server_site)) {
            server_site.replace("http://", "");

            if (!prefs.getServerSite().toLowerCase().equals(server_site.toLowerCase())) {

                BelongsToDBHelper.clearBelong();
                AllUserDBHelper.clearUser();
//                Log.d(TAG,"AllUserDBHelper.getUser():"+AllUserDBHelper.getUser().size());
                ChatRoomDBHelper.clearChatRooms();
                ChatMessageDBHelper.clearMessages();
                DepartmentDBHelper.clearDepartment();
                UserDBHelper.clearUser();
                FavoriteGroupDBHelper.clearGroups();
                FavoriteUserDBHelper.clearFavorites();
                CrewChatApplication.resetValue();
            }
            Log.d(TAG, "server_site:" + server_site);
            prefs.putServerSite(server_site);
            prefs.putUserName(username);

            ServerSiteDBHelper.addServerSite(server_site);
        }
        //HttpOauthRequest.getInstance().insertPhoneToken();
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
            Log.d(TAG, "isLogin:" + isLogin);
            if (isLogin) {
                notNetwork();
            } else {
                Log.d(TAG, "firstLogin");
                dismissProgressDialog();
                firstLogin = false;
                findViewById(R.id.logo).setVisibility(View.GONE);
                init();
            }


//            Log.d(TAG,"firstLogin");
//            dismissProgressDialog();
//            firstLogin = false;
//            findViewById(R.id.logo).setVisibility(View.GONE);
//            init();
        } else {
            Log.d(TAG, "not firstLogin");
            dismissProgressDialog();
            String error_msg = "";
            switch (errorDto.code) {
                case 2:
//                    error_msg = getString(R.string.string_error_code_2);
                    error_msg = errorDto.getMessage();
//                    Log.d(TAG,errorDto.getMessage());
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
                Log.d(TAG, "error_msg:" + error_msg);
                Toast.makeText(this, error_msg, Toast.LENGTH_SHORT).show();

//                String first_login = Statics.FIRST_LOGIN;
//                boolean isLogin = new Prefs().getBooleanValue(first_login, false);
//                Log.d(TAG,"isLogin:"+isLogin);
//                if (isLogin && error_msg.equals(getString(R.string.string_error_code_default))) {
//                    notNetwork();
//                } else {
//                    Toast.makeText(this, error_msg, Toast.LENGTH_SHORT).show();
//                }


            } else {

                showAlertDialog(error_msg, getString(R.string.string_ok), "", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        customDialog.dismiss();
                    }
                });
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
                scrollView.post(new Runnable() {

                    @Override
                    public void run() {
                        scrollView.scrollTo(0, Utils.getDimenInPx(R.dimen.scroll_height_login));
                        if (v != null) {
                            v.requestFocus();
                        }
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
        rootLayout = (ViewGroup) findViewById(R.id.root_login);
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
        keyboardListenersAttached = true;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(accountReceiver);
//        accountReceiver = null;

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

    private class UpdateRunnable implements Runnable {
        @Override
        public void run() {
            try {
                String url = Constant.ROOT_URL_UPDATE + "/Android/Version/CrewChat.txt";

                URL txtUrl = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) txtUrl.openConnection();

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String serverVersion = String.valueOf(bufferedReader.readLine().trim());
                Log.d(TAG, "serverVersion:" + serverVersion);
                prefs.setSERVER_VERSION(serverVersion);
                inputStream.close();

                String appVersion = BuildConfig.VERSION_NAME;
                // text file is UTF8 - Change to ASCII   of this server file

                if (appVersion.equals(serverVersion)) {
                    mActivityHandler.sendEmptyMessageDelayed(ACTIVITY_HANDLER_NEXT_ACTIVITY, 1);
                } else {
                    mActivityHandler.sendEmptyMessage(ACTIVITY_HANDLER_START_UPDATE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

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
                                new WebClientAsyncTask(activity).execute();
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

    private final ActivityHandler mActivityHandler = new ActivityHandler(this);

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
                Activity activity = mWeakActivity.get();
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
//                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/CrewChat.apk";
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
//                activity.startActivity(intent);


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
    //    AtomicInteger msgId = new AtomicInteger();
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, LoginActivity.this,
                                PLAY_SERVICES_RESOLUTION_REQUEST);
                        errorDialog.show();
                    }
                });

            }

            // Cheat google play service, return false when app is submit to play store
            // return false;
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
                msg = "Device registered, registration ID=" + regId;
            } catch (IOException ex) {
                msg = "Error :" + ex.getMessage();
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            new Prefs().setGCMregistrationid(regId);
            insertDevice(regId);
        }

    }

    private void insertDevice(final String regId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpRequest.getInstance().InsertDevice(regId, new BaseHTTPCallBack() {
                    @Override
                    public void onHTTPSuccess() {
                    }

                    @Override
                    public void onHTTPFail(ErrorDto errorDto) {
                    }
                });
            }
        }).start();
    }
}