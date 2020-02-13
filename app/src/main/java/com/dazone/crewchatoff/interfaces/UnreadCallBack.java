package com.dazone.crewchatoff.interfaces;

import com.dazone.crewchatoff.dto.UnreadDto;

import java.util.List;

/**
 * Created by maidinh on 31-Aug-17.
 */

public interface UnreadCallBack {
    void onSuccess(List<UnreadDto> list);
    void onFail();
}
