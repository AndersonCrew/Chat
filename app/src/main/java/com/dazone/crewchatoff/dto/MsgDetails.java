package com.dazone.crewchatoff.dto;

import java.util.List;

/**
 * Created by maidinh on 24/1/2017.
 */

public class MsgDetails {
    int RoomNo;
    int SubjectUserNo;
    List<Integer> UserNos;

    public int getRoomNo() {
        return RoomNo;
    }

    public void setRoomNo(int roomNo) {
        RoomNo = roomNo;
    }

    public int getSubjectUserNo() {
        return SubjectUserNo;
    }

    public void setSubjectUserNo(int subjectUserNo) {
        SubjectUserNo = subjectUserNo;
    }

    public List<Integer> getUserNos() {
        return UserNos;
    }

    public void setUserNos(List<Integer> userNos) {
        UserNos = userNos;
    }
}
