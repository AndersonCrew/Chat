package com.dazone.crewchatoff.dto;

import com.dazone.crewchatoff.constant.Statics;

import java.util.List;

public class GroupDto extends ChattingDto {
    private List<UserDto> group;

    public GroupDto(List<UserDto> group) {
        this.group = group;
        setmType(Statics.CHATTING_VIEW_TYPE_GROUP);
    }

    @Override
    public String toString() {
        return "GroupDto{" +
                "group=" + group +
                '}';
    }

    @Override
    public String getName() {
        if (group == null || group.size() == 0)
            return "";

        String temp = "";

        for (UserDto dto : group) {
            temp = temp + dto.FullName + ",";
        }

        return temp.substring(0, temp.length() - 1);
    }

    public List<UserDto> getGroup() {
        return group;
    }

    public void setGroup(List<UserDto> group) {
        this.group = group;
    }
}