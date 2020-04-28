package com.dazone.crewchatoff.test;

import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;

/**
 * Created by Sherry on 12/31/15.
 */
public interface OnOrganizationSelectedEvent {
    void onOrganizationCheck(boolean isCheck, TreeUserDTO personData);
}
