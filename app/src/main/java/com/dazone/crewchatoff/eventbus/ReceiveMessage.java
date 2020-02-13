package com.dazone.crewchatoff.eventbus;

import com.dazone.crewchatoff.dto.ChattingDto;

/**
 * Created by maidinh on 21-Nov-17.
 */

public class ReceiveMessage {
    private ChattingDto chattingDto;
    public ReceiveMessage(ChattingDto message){
        this.chattingDto = message;
    }

    public ChattingDto getChattingDto() {
        return chattingDto;
    }

    public void setChattingDto(ChattingDto chattingDto) {
        this.chattingDto = chattingDto;
    }
}
