package com.dazone.crewchatoff.interfaces;

import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.ProfileUserDTO;

public interface OnGetUserCallBack {
    void onHTTPSuccess(ProfileUserDTO result);
    void onHTTPFail(ErrorDto errorDto);
}