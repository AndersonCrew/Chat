package com.dazone.crewchatoff.interfaces;

import com.dazone.crewchatoff.dto.AttachImageList;

import java.util.List;

/**
 * Created by maidinh on 6/2/2017.
 */

public interface GetIvFileBox {
    void onSuccess(List<AttachImageList> lst);
    void onFail();
}
