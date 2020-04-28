package com.dazone.crewchatoff.fragment;

import android.app.Activity;
import android.os.Bundle;

import com.dazone.crewchatoff.adapter.TransferMsgAdapter;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.CurrentChatDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.interfaces.OnGetCurrentChatCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maidinh on 9/2/2017.
 */

public class TabCurrentChatFragment extends ListFragment<ChattingDto> implements OnGetCurrentChatCallBack {
    public static TabCurrentChatFragment fragment;
    String TAG = "TabCurrentChatFragment";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = this;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    protected void initList() {

    }


    public interface OnContextMenuSelect {
        void onSelect(int type, Bundle bundle);
    }

    private OnContextMenuSelect mOnContextMenuSelect = new OnContextMenuSelect() {
        @Override
        public void onSelect(int type, Bundle bundle) {

        }
    };

    @Override
    protected void initAdapter() {
        if (CurrentChatListFragment.fragment != null) {
            dataSet = CurrentChatListFragment.fragment.getListData();
        }
        if (dataSet == null) {
            dataSet = new ArrayList<>();
        }else{

            ArrayList<TreeUserDTOTemp> lst = null;

            if (CompanyFragment.instance != null) lst = CompanyFragment.instance.getUser();
            if (lst == null) lst = new ArrayList<>();

            for (ChattingDto dto : dataSet) {
                dto.setCbChoose(false);
                if (dto.getListTreeUser() != null && dto.getListTreeUser().size() > 0) {
                    if (dto.getListTreeUser().size() < 2) {
                        int userNo = dto.getListTreeUser().get(0).getUserNo();
                        for (TreeUserDTOTemp obj : lst) {
                            int stt = obj.getStatus();
                            int uN = obj.getUserNo();
                            if (userNo == uN) {
                                dto.setStatus(stt);
                                break;
                            }
                        }
                    }
                }
            }
        }
        adapterList = new TransferMsgAdapter(mContext, dataSet, rvMainList, mOnContextMenuSelect);
    }



    public List<ChattingDto> getData() {
        return adapterList.getData();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onHTTPSuccess(List<CurrentChatDto> dtos) {
    }

    @Override
    public void onHTTPFail(ErrorDto errorDto) {
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }


}