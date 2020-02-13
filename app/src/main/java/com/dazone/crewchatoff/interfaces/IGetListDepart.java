package com.dazone.crewchatoff.interfaces;

import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;

import java.util.ArrayList;

public interface IGetListDepart {
    void onGetListDepartSuccess(ArrayList<TreeUserDTO> treeUserDTOs);
    void onGetListDepartFail(ErrorDto dto);
}
