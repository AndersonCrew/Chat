package com.dazone.crewchatoff.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.Tree.Org_tree;
import com.dazone.crewchatoff.adapter.AdapterOrganizationChartFragment;
import com.dazone.crewchatoff.dto.BelongDepartmentDTO;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by maidinh on 9/2/2017.
 */

public class TabOrganizationChartFragment extends Fragment {
    private String TAG = "NewOrganizationChart";
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private AdapterOrganizationChartFragment mAdapter;
    private List<TreeUserDTO> list = new ArrayList<>();
    private ArrayList<TreeUserDTOTemp> listTemp;
    private ArrayList<TreeUserDTO> mDepartmentList;
    private ArrayList<TreeUserDTO> temp = new ArrayList<>();
    private ArrayList<TreeUserDTO> mPersonList = new ArrayList<>();
    private ArrayList<TreeUserDTO> mSelectedPersonList = new ArrayList<>();
    public static TabOrganizationChartFragment fm;

    public TabOrganizationChartFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.tab_1_layout, container, false);
        fm = this;
        initView(v);
        initDB();

        return v;
    }

    public ArrayList<TreeUserDTO> getListUser() {
        ArrayList<TreeUserDTO> lst = getListDTO(mAdapter.getList());
        if (lst == null) {
            lst = new ArrayList<>();
        }
        return lst;
    }

    ArrayList<TreeUserDTO> getListDTO(List<TreeUserDTO> lst) {
        ArrayList<TreeUserDTO> dtoList = new ArrayList<>();
        for (TreeUserDTO obj : lst) {
            if (obj.isCheck())
                dtoList.add(obj);
        }
        return dtoList;
    }

    public void scrollToEndList(int size) {
        recyclerView.smoothScrollToPosition(size);
    }

    void initView(View v) {
        recyclerView = v.findViewById(R.id.rv);
        TabOrganizationChartFragment instance = this;
        mAdapter = new AdapterOrganizationChartFragment(getActivity(), list, true, instance);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
    }

    void initDB() {
        if (CompanyFragment.instance != null) {
            list = CompanyFragment.instance.getSubordinates();
            mAdapter.updateList(list);
        } else {
            Toast.makeText(getActivity(), "Can not get list user, restart app please", Toast.LENGTH_SHORT).show();
        }
    }

    public void convertData(List<TreeUserDTO> treeUserDTOs) {
        if (treeUserDTOs != null && treeUserDTOs.size() != 0) {
            for (TreeUserDTO dto : treeUserDTOs) {
                if (dto.getSubordinates() != null && dto.getSubordinates().size() > 0) {
                    temp.add(dto);
                    convertData(dto.getSubordinates());
                } else {
                    temp.add(dto);
                }
            }
        }
    }

    List<TreeUserDTO> lstCurrent = new ArrayList<>();

    public void touchSearch() {
        lstCurrent = mAdapter.getCurrentList();
    }

    public void updateSearch(String s) {
        if (s.length() == 0) {
            mAdapter.updateIsSearch(0);
            updateCurrentList();
        } else {
            mAdapter.updateIsSearch(1);
            mAdapter.actionSearch(s);
        }
    }

    public void updateCurrentList() {
        if (lstCurrent != null && lstCurrent.size() > 0) {
            mAdapter.updateListSearch(lstCurrent);
        }
    }
}