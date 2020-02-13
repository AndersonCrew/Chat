package com.dazone.crewchatoff.fragment;

import android.os.Bundle;
import android.view.View;

import com.dazone.crewchatoff.adapter.GroupListAdapter;
import com.dazone.crewchatoff.database.UserDBHelper;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class GroupListFragment extends ListFragment<TreeUserDTOTemp> implements View.OnClickListener {
    //ChattingDto chattingDto;
    private ArrayList<Integer> userNos;

    public GroupListFragment instance(ArrayList<Integer> userNos) {
        GroupListFragment groupListFragment = new GroupListFragment();
        Bundle bundle = new Bundle();
        bundle.putIntegerArrayList(Constant.KEY_INTENT_USER_NO_ARRAY, userNos);
        //bundle.putSerializable(Statics.CHATTING_DTO_FOR_GROUP_LIST, chattingDto);
        groupListFragment.setArguments(bundle);
        return groupListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receiveBundle();
    }

    public void receiveBundle() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            userNos = bundle.getIntegerArrayList(Constant.KEY_INTENT_USER_NO_ARRAY);
            //chattingDto = (ChattingDto) bundle.getSerializable(Statics.CHATTING_DTO_FOR_GROUP_LIST);
        }
    }

    @Override
    protected void initAdapter() {
        adapterList = new GroupListAdapter(mContext, dataSet, rvMainList);
        enableLoadingMore();

    }

    @Override
    protected void reloadContentPage() {
        dataSet.add(null);
        adapterList.notifyItemInserted(dataSet.size() - 1);
    }

    @Override
    protected void initList() {
        List<TreeUserDTOTemp> allUser = null;
        if (CompanyFragment.instance != null) allUser = CompanyFragment.instance.getUser();
        if (allUser == null) allUser = new ArrayList<>();

        if (userNos != null && userNos.size() > 0) {
            for (int i : userNos) {
                if (i != UserDBHelper.getUser().Id) {
                    TreeUserDTOTemp treeUserDTOTemp = Utils.GetUserFromDatabase(allUser, i);
                    if (treeUserDTOTemp != null) {
                        dataSet.add(treeUserDTOTemp);
                    }
                    adapterList.notifyItemChanged(userNos.indexOf(i));
                }
            }
        }
    }

   /* @Override
    protected void initSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }*/

    @Override
    public void onClick(View v) {
    }
}