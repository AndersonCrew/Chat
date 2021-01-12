package com.dazone.crewchatoff.interfaces;

public interface OAUTHUrls {
    String URL_ROOT = "/UI/WebService/WebServiceCenter.asmx/";
    String URL_GET_LOGIN_V3 = URL_ROOT + "Login_v5";
    String URL_GET_LOGIN_NEW_API = URL_ROOT + "Login_CrewChat";
    String URL_GET_UPDATE_PASSWORD = URL_ROOT + "UpdatePassword";
    String URL_CHECK_SESSION = URL_ROOT + "CheckSessionUser_v5";
    String URL_LOG_OUT = URL_ROOT + "LogOutUser";
    String URL_GET_USER_DETAIL = URL_ROOT + "GetUser";
    String AutoLogin = URL_ROOT + "AutoLogin_v5";
}