package com.dazone.crewchatoff.interfaces;

import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.userfavorites.FavoriteChatRoomDto;

import java.util.List;

public interface OnGetFavoriteChatRoom {
    void OnGetChatRoomSuccess(List<FavoriteChatRoomDto> list);
    void OnGetChatRoomFail(ErrorDto errorDto);
}