package com.dazone.crewchatoff.interfaces;

import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ErrorDto;

import java.util.List;

public interface OnGetChatMessage {
    void OnGetChatMessageSuccess(List<ChattingDto> list);
    void OnGetChatMessageFail(ErrorDto errorDto);
}