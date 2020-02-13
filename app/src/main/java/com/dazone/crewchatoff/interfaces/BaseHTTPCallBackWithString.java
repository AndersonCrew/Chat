package com.dazone.crewchatoff.interfaces;


import com.dazone.crewchatoff.dto.ErrorDto;

public interface BaseHTTPCallBackWithString {
    void onHTTPSuccess(String message);
    void onHTTPFail(ErrorDto errorDto);
}
