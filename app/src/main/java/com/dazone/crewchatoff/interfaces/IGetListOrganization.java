package com.dazone.crewchatoff.interfaces;

import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;

import java.util.ArrayList;

/**
 * Created by maidinh on 8/19/2015.
 */
public interface IGetListOrganization {
    void onGetListSuccess(ArrayList<TreeUserDTOTemp> treeUserDTOs);
    void onGetListFail(ErrorDto dto);
}
