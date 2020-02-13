package com.dazone.crewchatoff.interfaces;

import com.dazone.crewchatoff.dto.ErrorDto;

public interface OnGetMessageUnreadCountCallBack {
    void onHTTPSuccess(String result);
    void onHTTPFail(ErrorDto errorDto);
}