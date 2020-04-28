package com.dazone.crewchatoff.interfaces;

import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.UserInfoDto;

import java.util.ArrayList;

/**
 * Created by Dat on 5/4/2016.
 */
public interface OnGetUserInfo {
    void OnSuccess(ArrayList<UserInfoDto> userInfo);
    void OnFail(ErrorDto errorDto);
}
