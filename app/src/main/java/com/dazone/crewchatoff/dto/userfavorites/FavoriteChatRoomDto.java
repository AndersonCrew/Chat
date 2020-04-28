package com.dazone.crewchatoff.dto.userfavorites;

/**
 * Created by Admin on 7/1/2016.
 */
public class FavoriteChatRoomDto {
    private long FavoriteChatRoomNo;
    private long RegUserNo;
    private long RoomNo;
    private String ModDate;

    @Override
    public String toString() {
        return "FavoriteChatRoomDto{" +
                "FavoriteChatRoomNo=" + FavoriteChatRoomNo +
                ", RegUserNo=" + RegUserNo +
                ", RoomNo=" + RoomNo +
                ", ModDate='" + ModDate + '\'' +
                '}';
    }

    public long getRoomNo() {
        return RoomNo;
    }

    public void setRoomNo(long roomNo) {
        RoomNo = roomNo;
    }
}
