package com.dazone.crewchatoff.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.interfaces.Urls;

import java.io.Serializable;

public class Prefs implements Serializable {

    private SharedPreferences prefs;

    private final String SHAREDPREFERENCES_NAME = "oathsharedpreferences";
    private final String ACCESSTOKEN = "accesstoken";
    private final String SERVERSITE = "serversite";
    private final String USER_NAME = "username";
    private final String FULL_NAME = "full_name";
    private final String EMAIL = "email";
    private final String BADGE = "badge";
    private final String COMPANY_NAME = "company_name";
    private final String COMPANY_NO = "company_no";
    private final String USERNO = "user_no";
    private final String INTRO_COUNT = "introcount";
    private final String AVATAR_URL = "avatar_url";
    private final String DDS_SERVER = "dds_server";
    private final String PASSWORD = "password";
    private final String USER_ID = "user_id";
    private final String SERVER_VERSION = "server_version";
    private final String UD_ROOM_NAME = "UD_ROOM_NAME";
    private final String UD_ROOM_ID = "UD_ROOM_ID";
    private final String IMAGE_LIST = "IMAGE_LIST";
    private final String HOST_STATUS = "HOST_STATUS";
    private final String DDS_SERVER_PORT = "DDS_SERVER_PORT";
    private final String FILE_SERVER_PORT = "FILE_SERVER_PORT";
    private final String CrewChatFileServerIP = "CrewChatFileServerIP";
    private final String msgNotSend = "msgNotSend";
    private final String login_install_app = "login_install_app";
    private final String load_data_finish = "load_data_finish";
    private final String moddate = "moddate";
    private final String moddate_deppartment = "moddate_deppartment";

    public void setModdate_deppartment(String key) {
        putStringValue(moddate_deppartment, key);
    }

    public String getModdate_deppartment() {
        return getStringValue(moddate_deppartment, "");
    }

    public void setModDate(String key) {
        putStringValue(moddate, key);
    }

    public String getModDate() {
        return getStringValue(moddate, "");
    }

    public void setDataComplete(boolean key) {
        putBooleanValue(load_data_finish, key);
    }

    public boolean isDataComplete() {
        return getBooleanValue(load_data_finish, false);
    }

    public void set_login_install_app(boolean key) {
        putBooleanValue(login_install_app, key);
    }

    public boolean get_login_install_app() {
        return getBooleanValue(login_install_app, true);
    }

    public void setMsgNotSend(String key) {
        putStringValue(msgNotSend, key);
    }

    public String getMsgNotSend() {
        return getStringValue(msgNotSend, "");
    }


    public void setCrewChatFileServerIP(String key) {
        putStringValue(CrewChatFileServerIP, key);
    }

    public String getCrewChatFileServerIP() {
        return getStringValue(CrewChatFileServerIP, Urls.HOST_STATUS);
    }

    public void setFILE_SERVER_PORT(int key) {
        putIntValue(FILE_SERVER_PORT, key);
    }

    public int getFILE_SERVER_PORT() {
        return getIntValue(FILE_SERVER_PORT, Urls.FILE_SERVER_PORT);
    }

    public void setDDS_SERVER_PORT(int key) {
        putIntValue(DDS_SERVER_PORT, key);
    }

    public int getDDS_SERVER_PORT() {
        return getIntValue(DDS_SERVER_PORT, Urls.DDS_SERVER_PORT);
    }


    public void setHOST_STATUS(String key) {
        putStringValue(HOST_STATUS, key);
    }

    public String getHOST_STATUS() {
        return getStringValue(HOST_STATUS, Urls.HOST_STATUS);
    }

    public void setIMAGE_LIST(String key) {
        putStringValue(IMAGE_LIST, key);
    }

    public String getIMAGE_LIST() {
        return getStringValue(IMAGE_LIST, "");
    }

    public void setSERVER_VERSION(String key) {
        putStringValue(SERVER_VERSION, key);
    }

    public String getSERVER_VERSION() {
        return getStringValue(SERVER_VERSION, "");
    }

    public void putRoomId(int companyNo) {
        putIntValue(UD_ROOM_ID, companyNo);
    }

    public int getRoomId() {
        return getIntValue(UD_ROOM_ID, -1);
    }

    public void setRoomName(String key) {
        putStringValue(UD_ROOM_NAME, key);
    }

    public String getRoomName() {
        return getStringValue(UD_ROOM_NAME, "");
    }

    private static final String PREF_FLAG_GMC_ID = "flag_gmc_id_new";

    public Prefs() {
        prefs = CrewChatApplication.getInstance().getApplicationContext().getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
    }


    public void setDDSServer(String key) {
        putStringValue(DDS_SERVER, key);
    }

    public String getDDSServer() {
        return getStringValue(DDS_SERVER, "");
    }

    public void setPass(String key) {
        putStringValue(PASSWORD, key);
    }

    public String getPass() {
        return getStringValue(PASSWORD, "");
    }

    public boolean isContainKey(String key) {
        return prefs.contains(key);
    }

    public void putServerSite(String serversite) {
        putStringValue(SERVERSITE, serversite);
    }

    public String getServerSite() {
        return getStringValue(SERVERSITE, "");
    }

    public void putUserName(String username) {
        putStringValue(USER_NAME, username);
    }

    public String getUserID() {
        return getStringValue(USER_ID, "");
    }

    public void putUserID(String userId) {
        putStringValue(USER_ID, userId);
    }

    public void setAvatarUrl(String url) {
        putStringValue(AVATAR_URL, url);
    }

    public String getAvatarUrl() {
        return getStringValue(AVATAR_URL, "");
    }

    public void putCompanyName(String companyName) {
        putStringValue(COMPANY_NAME, companyName);
    }

    public String getCompanyName() {
        return getStringValue(COMPANY_NAME, "");
    }

    public void putCompanyNo(int companyNo) {
        putIntValue(COMPANY_NO, companyNo);
    }

    public int getCompanyNo() {
        return getIntValue(COMPANY_NO, 1);
    }

    public void setFullName(String fullName) {
        putStringValue(FULL_NAME, fullName);
    }

    public String getFullName() {
        return getStringValue(FULL_NAME, "");
    }

    public String getUserName() {
        return getStringValue(USER_NAME, "");
    }

    public void putaccesstoken(String accesstoken) {
        putStringValue(ACCESSTOKEN, accesstoken);
    }

    public void putUserNo(int userNo) {
        putIntValue(USERNO, userNo);
    }

    public int getUserNo() {
        return getIntValue(USERNO, -1);
    }

    public void putScaleImageMode(int mode) {
        putIntValue(Statics.IMAGE_SIZE_MODE, mode);
    }

    public int getScaleImageMode() {
        return getIntValue(Statics.IMAGE_SIZE_MODE, Statics.MODE_ORIGINAL);
    }
    public void putIdMessage(long idMess) {
        putLongValue(Statics.ID_MESS, idMess);
    }

    public long getIdMessage() {
        return getLongValue(Statics.ID_MESS, Statics.MODE_ORIGINAL);
    }
    public String getaccesstoken() {
        return getStringValue(ACCESSTOKEN, "");
    }

    public void putBooleanValue(String KEY, boolean value) {
        prefs.edit().putBoolean(KEY, value).apply();
    }

    public boolean getBooleanValue(String KEY, boolean defvalue) {
        return prefs.getBoolean(KEY, defvalue);
    }

    public void putStringValue(String KEY, String value) {
        prefs.edit().putString(KEY, value).apply();
    }

    public String getStringValue(String KEY, String defvalue) {
        return prefs.getString(KEY, defvalue);
    }

    public void putIntValue(String KEY, int value) {
        prefs.edit().putInt(KEY, value).apply();
    }

    public int getIntValue(String KEY, int defvalue) {
        return prefs.getInt(KEY, defvalue);
    }

    public void putLongValue(String KEY, long value) {
        prefs.edit().putLong(KEY, value).apply();
    }

    public long getLongValue(String KEY, long defvalue) {
        return prefs.getLong(KEY, defvalue);
    }

    public void putFloatValue(String KEY, float value) {
        prefs.edit().putFloat(KEY, value).apply();
    }

    public void putintrocount(int introcount) {
        putIntValue(INTRO_COUNT, introcount);
    }

    public int getintrocount() {
        return getIntValue(INTRO_COUNT, 0);
    }

    public float getFloatValue(String KEY, float defvalue) {
        return prefs.getFloat(KEY, defvalue);
    }

    public void removeValue(String KEY) {
        prefs.edit().remove(KEY).apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }

    public void clearLogin() {
        prefs.edit().remove(ACCESSTOKEN).apply();
    }

    //gmc
    public String getGCMregistrationid() {
        return getStringValue(PREF_FLAG_GMC_ID, "");
    }

    public void setGCMregistrationid(String value) {
        putStringValue(PREF_FLAG_GMC_ID, value);
    }

    public void putEmail(String mailAddress) {
        putStringValue(EMAIL, mailAddress);
    }

    public String getEmail() {
        return getStringValue(EMAIL, "");
    }

    public void setCountBadge(int count) {
        putIntValue(BADGE, count);

    }

    public int getCountBadge() {
        return getIntValue(BADGE, 0);
    }
}
