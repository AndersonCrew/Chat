package com.dazone.crewchatoff.interfaces;

import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ErrorDto;

/**
 * Created by THANHTUNG on 17/02/2016.
 */
public interface ICreateOneUserChatRom {
    void onICreateOneUserChatRomSuccess(ChattingDto chattingDto);
    void onICreateOneUserChatRomFail(ErrorDto errorDto);
}
