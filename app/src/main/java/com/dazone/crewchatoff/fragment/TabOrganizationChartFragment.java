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
        // Required empty public constructor
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
        recyclerView = (RecyclerView) v.findViewById(R.id.rv);
        TabOrganizationChartFragment instance = this;
        mAdapter = new AdapterOrganizationChartFragment(getActivity(), list, true, instance);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
    }

    void initDB() {
//        initWholeOrganization();

        if (CompanyFragment.instance != null) {
            list = CompanyFragment.instance.getSubordinates();
            mAdapter.updateList(list);
        } else {
            Toast.makeText(getActivity(), "Can not get list user, restart app please", Toast.LENGTH_SHORT).show();
        }
    }

    private void initWholeOrganization() {
        // build offline version
        // Get offline data
        mDepartmentList = new ArrayList<>();
        listTemp  = new ArrayList<>();

        if (CompanyFragment.instance != null) {
            listTemp = CompanyFragment.instance.getUser();
            mDepartmentList= CompanyFragment.instance.getDepartments();
        }

        if (mDepartmentList == null) mDepartmentList = new ArrayList<>();
        if (listTemp == null) listTemp = new ArrayList<>();


        if (mDepartmentList != null && mDepartmentList.size() > 0) {
            buildTree(mDepartmentList, false);
            Log.d(TAG, "1");
        } else { // Get department from server
            Log.d(TAG, "2");
            Log.d(TAG, "URL_GET_DEPARTMENT 10");
//            HttpRequest.getInstance().GetListDepart(new IGetListDepart() {
//                @Override
//                public void onGetListDepartSuccess(ArrayList<TreeUserDTO> treeUserDTOs) {
//                    buildTree(treeUserDTOs, true);
//                }
//
//                @Override
//                public void onGetListDepartFail(ErrorDto dto) {
//
//                }
//            });
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

    private void buildTree(final ArrayList<TreeUserDTO> treeUserDTOs, boolean isFromServer) {
        if (treeUserDTOs != null) {
            if (isFromServer) {
                convertData(treeUserDTOs);
            } else {
                temp.clear();
                temp.addAll(treeUserDTOs);
            }

            for (TreeUserDTO treeUserDTO : temp) {
                if (treeUserDTO.getSubordinates() != null && treeUserDTO.getSubordinates().size() > 0) {
                    treeUserDTO.setSubordinates(null);
                }
            }

            // sort data by order
            Collections.sort(temp, new Comparator<TreeUserDTO>() {
                @Override
                public int compare(TreeUserDTO r1, TreeUserDTO r2) {
                    if (r1.getmSortNo() > r2.getmSortNo()) {
                        return 1;
                    } else if (r1.getmSortNo() == r2.getmSortNo()) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });

            for (TreeUserDTOTemp treeUserDTOTemp : listTemp) {
                for (BelongDepartmentDTO belong : treeUserDTOTemp.getBelongs()) {
                    TreeUserDTO treeUserDTO = new TreeUserDTO(
                            treeUserDTOTemp.getName(),
                            treeUserDTOTemp.getNameEN(),
                            treeUserDTOTemp.getCellPhone(),
                            treeUserDTOTemp.getAvatarUrl(),
                            belong.getPositionName(),
                            treeUserDTOTemp.getType(),
                            treeUserDTOTemp.getStatus(),
                            treeUserDTOTemp.getUserNo(),
                            belong.getDepartNo(),
                            treeUserDTOTemp.getUserStatusString(),
                            belong.getPositionSortNo()
                    );

                    for (TreeUserDTO u : mSelectedPersonList) {
                        if (treeUserDTOTemp.getUserNo() == u.getId()) {
                            treeUserDTO.setIsCheck(true);
                            break;
                        }
                    }
                    temp.add(treeUserDTO);
                }
            }

            mPersonList = new ArrayList<>();
            mPersonList.addAll(temp);

            TreeUserDTO dto = null;

            try {
                dto = Org_tree.buildTree(mPersonList);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (dto != null) {
                list = dto.getSubordinates();
                mAdapter.updateList(list);
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
            Log.d(TAG, "onQueryTextChange:" + s);
            mAdapter.actionSearch(s);
        }
    }

    public void updateCurrentList() {
        Log.d(TAG, "updateCurrentList");
        if (lstCurrent != null && lstCurrent.size() > 0) {
            mAdapter.updateListSearch(lstCurrent);
        }
    }
}