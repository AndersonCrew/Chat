package com.dazone.crewchatoff.interfaces;

import com.dazone.crewchatoff.dto.ChatRoomDTO;
import com.dazone.crewchatoff.dto.ErrorDto;

public interface OnGetChatRoom {
    void OnGetChatRoomSuccess(ChatRoomDTO chatRoomDTO);
    void OnGetChatRoomFail(ErrorDto errorDto);
}