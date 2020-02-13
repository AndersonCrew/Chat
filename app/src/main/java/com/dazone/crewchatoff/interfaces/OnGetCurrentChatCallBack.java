package com.dazone.crewchatoff.interfaces;

import com.dazone.crewchatoff.dto.CurrentChatDto;
import com.dazone.crewchatoff.dto.ErrorDto;

import java.util.List;

public interface OnGetCurrentChatCallBack {
    void onHTTPSuccess(List<CurrentChatDto> dtos);
    void onHTTPFail(ErrorDto errorDto);
}