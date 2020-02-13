package com.dazone.crewchatoff.interfaces;

import com.dazone.crewchatoff.dto.ChatRoomDTO;
import com.dazone.crewchatoff.dto.ErrorDto;

import java.util.List;

public interface OnGetChatListV2 {
    void OnGetChatListSuccess(List<ChatRoomDTO> list);
    void OnGetChatListFail(ErrorDto errorDto);
}