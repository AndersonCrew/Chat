package com.dazone.crewchatoff.interfaces;

import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ErrorDto;

import java.util.List;

public interface OnGetChatList {
    void OnGetChatListSuccess(List<ChattingDto> list);
    void OnGetChatListFail(ErrorDto errorDto);
}