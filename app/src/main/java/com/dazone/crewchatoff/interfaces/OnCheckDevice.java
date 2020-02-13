package com.dazone.crewchatoff.interfaces;

import com.dazone.crewchatoff.dto.ErrorDto;

public interface OnCheckDevice {
    void onDeviceSuccess();
    void onHTTPFail(ErrorDto errorDto);
}