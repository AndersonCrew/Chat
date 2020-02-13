package com.dazone.crewchatoff.interfaces;

import com.dazone.crewchatoff.dto.ErrorDto;

public interface OnSetNotification {
    void OnSuccess();
    void OnFail(ErrorDto errorDto);
}