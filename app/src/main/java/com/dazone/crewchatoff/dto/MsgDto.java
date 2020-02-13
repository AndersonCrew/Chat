package com.dazone.crewchatoff.dto;

/**
 * Created by maidinh on 24/1/2017.
 */

public class MsgDto {
    int Code;
    MsgDetails Data;

    public int getCode() {
        return Code;
    }

    public void setCode(int code) {
        Code = code;
    }

    public MsgDetails getData() {
        return Data;
    }

    public void setData(MsgDetails data) {
        Data = data;
    }
}
