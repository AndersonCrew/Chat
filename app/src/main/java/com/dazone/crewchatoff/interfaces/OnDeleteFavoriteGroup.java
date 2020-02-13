package com.dazone.crewchatoff.interfaces;

import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;

import java.util.ArrayList;

public interface OnDeleteFavoriteGroup {
    void onDelete(long groupNo);
    void onEdit(long groupNo, String groupName);
    void onAdd(long groupNo, ArrayList<TreeUserDTO> list);
}