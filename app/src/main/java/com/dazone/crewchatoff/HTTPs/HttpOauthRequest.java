package com.dazone.crewchatoff.HTTPs;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.dazone.crewchatoff.constant.Constants;
import com.dazone.crewchatoff.database.ChatMessageDBHelper;
import com.dazone.crewchatoff.database.ChatRoomDBHelper;
import com.dazone.crewchatoff.database.UserDBHelper;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.UserDto;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallBack;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallbackWithJson;
import com.dazone.crewchatoff.interfaces.IF_GET_IP;
import com.dazone.crewchatoff.interfaces.IF_UpdatePass;
import com.dazone.crewchatoff.interfaces.OAUTHUrls;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HttpOauthRequest {
    public static String TAG = "HttpOauthRequest";
    private static HttpOauthRequest mInstance;
    private static String root_link;

    public static HttpOauthRequest getInstance() {
        if (null == mInstance) {
            mInstance = new HttpOauthRequest();
        }

        root_link = CrewChatApplication.getInstance().getPrefs().getServerSite();
        return mInstance;
    }


    public class getIpFromDomain extends AsyncTask<String, String, String> {
        IF_GET_IP callback;
        String DOMAIN;

        public getIpFromDomain(String DOMAIN, IF_GET_IP callback) {
            this.DOMAIN = DOMAIN;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... strings) {
            String ip = "";
            InetAddress address = null;
            try {
                address = InetAddress.getByName(new URL("http://" + DOMAIN).getHost());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (address != null) {
                ip = address.getHostAddress();
            }
            return ip;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            callback.onSuccess(s);

        }
    }

    public class onUpdate {
        boolean success;
        String newSessionID;

        public onUpdate() {
        }

        public onUpdate(boolean success, String newSessionID) {
            this.success = success;
            this.newSessionID = newSessionID;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getNewSessionID() {
            return newSessionID;
        }

        public void setNewSessionID(String newSessionID) {
            this.newSessionID = newSessionID;
        }
    }

    public void updatePassword(String originalPassword, String newPassword, final IF_UpdatePass callback) {
        final String url = root_link + OAUTHUrls.URL_GET_UPDATE_PASSWORD;
        Log.d(TAG, "updatePassword url:" + url);
        Map<String, String> params = new HashMap<>();
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("originalPassword", originalPassword);
        params.put("newPassword", newPassword);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                if (response.length() > 0 && response.startsWith("{")) {
                    try {
                        Log.d(TAG, "updatePassword response:" + response);
                        onUpdate obj = new Gson().fromJson(response, onUpdate.class);
                        if (obj != null) {
                            boolean success = obj.isSuccess();
                            String newSessionID = obj.getNewSessionID();
                            if (success && newSessionID.length() > 0) {
                                callback.onSuccess(newSessionID);
                            } else {
                                callback.onFail();
                            }
                        } else {
                            callback.onFail();
                        }
                    } catch (Exception e) {
                        callback.onFail();
                        e.printStackTrace();
                    }
                } else {
                    callback.onFail();
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                Log.d(TAG, "updatePassword error");
                callback.onFail();
            }//
        });
    }

    // Login function V2
    public void loginV2(final BaseHTTPCallBack baseHTTPCallBack, final String userID, final String password, String mobileOSVersion) {
        final String url = CrewChatApplication.getInstance().getPrefs().getStringValue(Constants.DOMAIN, "") + OAUTHUrls.URL_GET_LOGIN_NEW_API;
        Map<String, String> params = new HashMap<>();
        params.put("companyDomain", CrewChatApplication.getInstance().getPrefs().getStringValue(Constants.COMPANY_NAME, ""));
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        params.put("userID", userID);
        params.put("password", password);
        params.put("mobileOSVersion", mobileOSVersion);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "loginV2 response:" + response);
                Gson gson = new Gson();
                final UserDto userDto = gson.fromJson(response, UserDto.class);

                final String CrewDDSServerIP = userDto.getCrewDDSServerIP();
                final int CrewDDSServerPort = userDto.getCrewDDSServerPort();
                final String CrewChatFileServerIP = userDto.getCrewChatFileServerIP();
                final int CrewChatFileServerPort = userDto.getCrewChatFileServerPort();
                final boolean CrewChatLocalDatabase = userDto.isCrewChatLocalDatabase();

                if (!Constant.isIp(CrewDDSServerIP) || !Constant.isIp(CrewChatFileServerIP)) {
                    if (!Constant.isIp(CrewDDSServerIP)) {
                        new getIpFromDomain(CrewDDSServerIP, ip -> {
                            if (ip.length() > 0) {
                                if (!Constant.isIp(CrewChatFileServerIP)) {
                                    FileServerNotIP(userDto, CrewChatApplication.getInstance().getPrefs().getStringValue(Constants.COMPANY_NAME, ""), password, userID, ip, CrewDDSServerPort,
                                            CrewChatFileServerPort, CrewChatFileServerIP, baseHTTPCallBack, CrewChatLocalDatabase);
                                } else {
                                    _httpSuccess(userDto, CrewChatApplication.getInstance().getPrefs().getStringValue(Constants.COMPANY_NAME, ""), password, userID, ip, CrewDDSServerPort,
                                            CrewChatFileServerPort, CrewChatFileServerIP, baseHTTPCallBack, CrewChatLocalDatabase);
                                }
                            }
                        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        FileServerNotIP(userDto, CrewChatApplication.getInstance().getPrefs().getStringValue(Constants.COMPANY_NAME, ""), password, userID, CrewDDSServerIP, CrewDDSServerPort,
                                CrewChatFileServerPort, CrewChatFileServerIP, baseHTTPCallBack, CrewChatLocalDatabase);
                    }
                } else {
                    _httpSuccess(userDto, CrewChatApplication.getInstance().getPrefs().getStringValue(Constants.COMPANY_NAME, ""), password, userID, CrewDDSServerIP, CrewDDSServerPort,
                            CrewChatFileServerPort, CrewChatFileServerIP, baseHTTPCallBack, CrewChatLocalDatabase);
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                baseHTTPCallBack.onHTTPFail(error);
            }
        });
    }


    public void FileServerNotIP(final UserDto userDto, final String subDomain, final String password, final String userID, final String CrewDDSServerIP, final int CrewDDSServerPort,
                                final int CrewChatFileServerPort, final String CrewChatFileServerIP, final BaseHTTPCallBack baseHTTPCallBack, final boolean CrewChatLocalDatabase) {
        Log.d(TAG, "CrewChatFileServerIP not IP+" + CrewChatFileServerIP);
        new getIpFromDomain(CrewChatFileServerIP, new IF_GET_IP() {
            @Override
            public void onSuccess(String ip) {
                Log.d(TAG, CrewChatFileServerIP + " -> " + ip);
                if (ip.length() > 0) {
                    _httpSuccess(userDto, subDomain, password, userID, CrewDDSServerIP,
                            CrewDDSServerPort, CrewChatFileServerPort, ip, baseHTTPCallBack, CrewChatLocalDatabase);
                } else {
                    Toast.makeText(CrewChatApplication.getInstance().getApplicationContext(), "Can not get ip, try again", Toast.LENGTH_SHORT).show();
                }
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void _httpSuccess(UserDto userDto, String subDomain, String password, String userID, String CrewDDSServerIP, int CrewDDSServerPort,
                             int CrewChatFileServerPort, String CrewChatFileServerIP, BaseHTTPCallBack baseHTTPCallBack, boolean CrewChatLocalDatabase) {
        userDto.prefs.putaccesstoken(userDto.session);
        userDto.prefs.putUserNo(userDto.Id);
        userDto.prefs.putUserName(userDto.userID);

        userDto.prefs.setAvatarUrl(userDto.avatar);
        userDto.prefs.setFullName(userDto.getFullName());
        userDto.prefs.putCompanyNo(userDto.getCompanyNo());
        userDto.prefs.setDDSServer(subDomain);
        userDto.prefs.setPass(password);
        userDto.prefs.putUserID(userID);
        userDto.prefs.putEmail(userDto.getMailAddress());
        UserDBHelper.addUser(userDto);
        // Set static current user Id
        CrewChatApplication.currentId = userDto.Id;
        if (!CrewChatLocalDatabase) {
            ChatMessageDBHelper.clearMessages();
            ChatRoomDBHelper.clearChatRooms();
        }
        CrewChatApplication.CrewChatLocalDatabase = CrewChatLocalDatabase;

        new Prefs().setHOST_STATUS(CrewDDSServerIP);
        new Prefs().setDDS_SERVER_PORT(CrewDDSServerPort);
        new Prefs().setFILE_SERVER_PORT(CrewChatFileServerPort);
        new Prefs().setCrewChatFileServerIP(CrewChatFileServerIP);
        baseHTTPCallBack.onHTTPSuccess();
    }

    public void getUser(final BaseHTTPCallbackWithJson baseHTTPCallBack, String userNo, String languageCode, String timeZoneOffset, String server_link) {
        final String url = server_link + OAUTHUrls.URL_GET_USER_DETAIL;
        Map<String, String> params = new HashMap<>();
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("userNo", userNo);
        params.put("timeZoneOffset", timeZoneOffset);
        params.put("languageCode", languageCode);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                baseHTTPCallBack.onHTTPSuccess(response);
            }

            @Override
            public void onFailure(ErrorDto error) {
                baseHTTPCallBack.onHTTPFail(error);
            }
        });

    }

    public void checkLogin(final BaseHTTPCallBack baseHTTPCallBack) {
        final String url = root_link + OAUTHUrls.URL_CHECK_SESSION;
        Map<String, String> params = new HashMap<>();
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());

        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "checkLogin response:" + response);
                Gson gson = new Gson();
                final UserDto userDto = gson.fromJson(response, UserDto.class);

                final String CrewDDSServerIP = userDto.getCrewDDSServerIP();
                final int CrewDDSServerPort = userDto.getCrewDDSServerPort();
                final String CrewChatFileServerIP = userDto.getCrewChatFileServerIP();
                final int CrewChatFileServerPort = userDto.getCrewChatFileServerPort();
                final boolean CrewChatLocalDatabase = userDto.isCrewChatLocalDatabase();

                if (!Constant.isIp(CrewDDSServerIP) || !Constant.isIp(CrewChatFileServerIP)) {
                    if (!Constant.isIp(CrewDDSServerIP)) {
                        // get ip CrewDDSServerIP
                        new getIpFromDomain(CrewDDSServerIP, ip -> {
                            Log.d(TAG, CrewDDSServerIP + " -> " + ip);
                            if (ip.length() > 0) {
                                if (!Constant.isIp(CrewChatFileServerIP)) {
                                    // get ip CrewChatFileServerIP
                                    SessionFileServerNotIP(userDto, ip, CrewDDSServerPort, CrewChatFileServerIP, CrewChatFileServerPort, baseHTTPCallBack, CrewChatLocalDatabase);
                                } else {
                                    // finish
                                    _checkSessionSuccess(userDto, ip, CrewDDSServerPort, CrewChatFileServerIP, CrewChatFileServerPort, baseHTTPCallBack, CrewChatLocalDatabase);
                                }
                            } else {
                                Toast.makeText(CrewChatApplication.getInstance().getApplicationContext(), "Can not get ip, try again", Toast.LENGTH_SHORT).show();
                            }
                        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        SessionFileServerNotIP(userDto, CrewDDSServerIP, CrewDDSServerPort, CrewChatFileServerIP, CrewChatFileServerPort, baseHTTPCallBack, CrewChatLocalDatabase);
                    }
                } else {
                    Log.d(TAG, "ALL IS IP");
                    _checkSessionSuccess(userDto, CrewDDSServerIP, CrewDDSServerPort, CrewChatFileServerIP, CrewChatFileServerPort, baseHTTPCallBack, CrewChatLocalDatabase);
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPFail(error);
                }
            }
        });
    }


    public void SessionFileServerNotIP(final UserDto userDto, final String CrewDDSServerIP, final int CrewDDSServerPort, final String CrewChatFileServerIP,
                                       final int CrewChatFileServerPort, final BaseHTTPCallBack baseHTTPCallBack, final boolean CrewChatLocalDatabase) {
        Log.d(TAG, "CrewChatFileServerIP not IP");
        new getIpFromDomain(CrewChatFileServerIP, ip -> {
            Log.d(TAG, CrewChatFileServerIP + " -> " + ip);
            if (ip.length() > 0) {
                _checkSessionSuccess(userDto, CrewDDSServerIP, CrewDDSServerPort, ip, CrewChatFileServerPort, baseHTTPCallBack, CrewChatLocalDatabase);
            } else {
                Toast.makeText(CrewChatApplication.getInstance().getApplicationContext(), "Can not get ip, try again", Toast.LENGTH_SHORT).show();
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void _checkSessionSuccess(UserDto userDto, String CrewDDSServerIP, int CrewDDSServerPort, String CrewChatFileServerIP,
                                     int CrewChatFileServerPort, BaseHTTPCallBack baseHTTPCallBack, boolean CrewChatLocalDatabase) {


        userDto.prefs.putaccesstoken(userDto.session);
        userDto.prefs.putUserNo(userDto.Id);
        userDto.prefs.putUserName(userDto.userID);
        userDto.prefs.setAvatarUrl(userDto.avatar);
        userDto.prefs.setFullName(userDto.getFullName());
        userDto.prefs.putCompanyNo(userDto.getCompanyNo());
        userDto.prefs.putEmail(userDto.getMailAddress());
        UserDBHelper.addUser(userDto);
        // Set static current user Id`
        CrewChatApplication.currentId = userDto.Id;
        if (!CrewChatLocalDatabase) {
            ChatMessageDBHelper.clearMessages();
            ChatRoomDBHelper.clearChatRooms();
        }
        CrewChatApplication.CrewChatLocalDatabase = CrewChatLocalDatabase;
        new Prefs().setHOST_STATUS(CrewDDSServerIP);
        new Prefs().setDDS_SERVER_PORT(CrewDDSServerPort);
        new Prefs().setFILE_SERVER_PORT(CrewChatFileServerPort);
        new Prefs().setCrewChatFileServerIP(CrewChatFileServerIP);

        if (baseHTTPCallBack != null) {
            baseHTTPCallBack.onHTTPSuccess();
        }
    }

    public void logout(final BaseHTTPCallBack baseHTTPCallBack) {
        final String url = root_link + OAUTHUrls.URL_LOG_OUT;
        Map<String, String> params = new HashMap<>();
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                baseHTTPCallBack.onHTTPSuccess();
            }

            @Override
            public void onFailure(ErrorDto error) {
                baseHTTPCallBack.onHTTPFail(error);
            }
        });
    }

    public void autoLogin(final BaseHTTPCallBack baseHTTPCallBack, final String userID, String mobileOSVersion) {
        final String url = CrewChatApplication.getInstance().getPrefs().getStringValue(Constants.DOMAIN, "") + OAUTHUrls.AutoLogin;
        Map<String, String> params = new HashMap<>();
        params.put("companyDomain", CrewChatApplication.getInstance().getPrefs().getStringValue(Constants.COMPANY_NAME, ""));
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        params.put("userID", userID);
        params.put("mobileOSVersion", mobileOSVersion);


        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                Gson gson = new Gson();
                final UserDto userDto = gson.fromJson(response, UserDto.class);
                final String CrewDDSServerIP = userDto.getCrewDDSServerIP();
                final int CrewDDSServerPort = userDto.getCrewDDSServerPort();
                final String CrewChatFileServerIP = userDto.getCrewChatFileServerIP();
                final int CrewChatFileServerPort = userDto.getCrewChatFileServerPort();
                final boolean CrewChatLocalDatabase = userDto.isCrewChatLocalDatabase();
                if (!Constant.isIp(CrewDDSServerIP) || !Constant.isIp(CrewChatFileServerIP)) {
                    if (!Constant.isIp(CrewDDSServerIP)) {
                        // get ip CrewDDSServerIP
                        new getIpFromDomain(CrewDDSServerIP, ip -> {
                            if (ip.length() > 0) {
                                if (!Constant.isIp(CrewChatFileServerIP)) {
                                    // get ip CrewChatFileServerIP
                                    FileServerNotIP(userDto, CrewChatApplication.getInstance().getPrefs().getStringValue(Constants.COMPANY_NAME, ""), "", userID, ip, CrewDDSServerPort,
                                            CrewChatFileServerPort, CrewChatFileServerIP, baseHTTPCallBack, CrewChatLocalDatabase);
                                } else {
                                    // finish
                                    _httpSuccess(userDto, CrewChatApplication.getInstance().getPrefs().getStringValue(Constants.COMPANY_NAME, ""), "", userID, ip, CrewDDSServerPort,
                                            CrewChatFileServerPort, CrewChatFileServerIP, baseHTTPCallBack, CrewChatLocalDatabase);
                                }
                            } else {
                                Toast.makeText(CrewChatApplication.getInstance().getApplicationContext(), "Can not get ip, try again", Toast.LENGTH_SHORT).show();
                            }
                        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        FileServerNotIP(userDto, CrewChatApplication.getInstance().getPrefs().getStringValue(Constants.COMPANY_NAME, ""), "", userID, CrewDDSServerIP, CrewDDSServerPort,
                                CrewChatFileServerPort, CrewChatFileServerIP, baseHTTPCallBack, CrewChatLocalDatabase);
                    }
                } else {
                    _httpSuccess(userDto, CrewChatApplication.getInstance().getPrefs().getStringValue(Constants.COMPANY_NAME, ""), "", userID, CrewDDSServerIP, CrewDDSServerPort,
                            CrewChatFileServerPort, CrewChatFileServerIP, baseHTTPCallBack, CrewChatLocalDatabase);
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                baseHTTPCallBack.onHTTPFail(error);
            }
        });
    }
}