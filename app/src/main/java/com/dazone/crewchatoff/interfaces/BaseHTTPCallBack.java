package com.dazone.crewchatoff.interfaces;

import com.dazone.crewchatoff.dto.ErrorDto;

public interface BaseHTTPCallBack {
    void onHTTPSuccess();
    void onHTTPFail(ErrorDto errorDto);
}
