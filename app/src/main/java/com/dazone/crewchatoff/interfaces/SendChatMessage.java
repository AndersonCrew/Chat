package com.dazone.crewchatoff.interfaces;

import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ErrorDto;

public interface SendChatMessage {
    void onSendChatMessageSuccess(ChattingDto chattingDto);
    void onSendChatMessageFail(ErrorDto errorDto, String url);
}
