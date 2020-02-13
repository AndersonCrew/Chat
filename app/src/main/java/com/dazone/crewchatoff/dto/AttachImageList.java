package com.dazone.crewchatoff.dto;

/**
 * Created by maidinh on 6/2/2017.
 */

public class AttachImageList {
    int MessageNo;
    int RoomNo;
    int UserNo;
    int AttachNo;
    String FileName;
    String FullPath;
    String RegDate;
    int Size;
    int Type;

    public int getMessageNo() {
        return MessageNo;
    }

    public void setMessageNo(int messageNo) {
        MessageNo = messageNo;
    }

    public int getRoomNo() {
        return RoomNo;
    }

    public void setRoomNo(int roomNo) {
        RoomNo = roomNo;
    }

    public int getUserNo() {
        return UserNo;
    }

    public void setUserNo(int userNo) {
        UserNo = userNo;
    }

    public int getAttachNo() {
        return AttachNo;
    }

    public void setAttachNo(int attachNo) {
        AttachNo = attachNo;
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }

    public String getFullPath() {
        return FullPath;
    }

    public void setFullPath(String fullPath) {
        FullPath = fullPath;
    }

    public String getRegDate() {
        return RegDate;
    }

    public void setRegDate(String regDate) {
        RegDate = regDate;
    }

    public int getSize() {
        return Size;
    }

    public void setSize(int size) {
        Size = size;
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }
}
