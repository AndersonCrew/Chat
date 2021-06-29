package com.dazone.crewchatoff.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.TestMultiLevelListview.MultilLevelListviewFragment;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.Tree.Org_tree;
import com.dazone.crewchatoff.adapter.AdapterOrganizationCompanyTab;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.database.AllUserDBHelper;
import com.dazone.crewchatoff.database.DepartmentDBHelper;
import com.dazone.crewchatoff.database.FavoriteGroupDBHelper;
import com.dazone.crewchatoff.database.TinyDB;
import com.dazone.crewchatoff.dto.BelongDepartmentDTO;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.StatusViewDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.dto.userfavorites.FavoriteGroupDto;
import com.dazone.crewchatoff.eventbus.NotifyAdapterOgr;
import com.dazone.crewchatoff.interfaces.IGetListDepart;
import com.dazone.crewchatoff.interfaces.IGetListOrganization;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanyFragment extends Fragment {
    String TAG = CompanyFragment.class.getName();
    @SuppressLint("StaticFieldLeak")
    public static CompanyFragment instance = null;
    private boolean isBuild = false;
    private Activity mContext;
    private RecyclerView listCompany;
    private ArrayList<TreeUserDTOTemp> listTemp;
    private int CODE_BUILD_TREE_OFFLINE = 5;
    private ArrayList<TreeUserDTOTemp> currentListStatusUser = new ArrayList<>();

    public void updateListStatus(ArrayList<TreeUserDTOTemp> lst) {
        currentListStatusUser.clear();
        currentListStatusUser.addAll(lst);
        mAdapter.updateListStatus(lst);
    }
    public void setContext(Activity context) {
        mContext = context;
    }

    private ProgressBar progressBar;
    private TextView tvNoData;

    private List<TreeUserDTO> list = new ArrayList<>();
    private AdapterOrganizationCompanyTab mAdapter;
    private LinearLayoutManager linearLayoutManager;

    private final int GET_USER_COMPLETE = 100;
    private final int GET_DEPARTMENT_COMPLETE = 101;
    private final int CREATE_TREE = 102;
    private final int GET_DATA_OFFLINE_COMPLETE = 103;
    private final int GET_USER_MOD_COMPLETE = 104;
    private final int GET_MOD_DEPARTMENT_COMPLETE = 105;
    private boolean isOnline = false;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        initDB();
    }

    private boolean hasPause = false;
    private boolean hasInitCurrentChatListFragment = false;
    @Override
    public void onPause() {
        super.onPause();
        hasPause = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(hasPause) {
            hasPause = false;
            refreshDataUser();
        }
    }

    private void refreshDataUser() {
        swipeRefreshLayout.setRefreshing(true);
        list = new ArrayList<>();
        mDepartmentList = new ArrayList<>();
        listTemp = new ArrayList<>();
        treeUserDTOsInit = new ArrayList<>();
        temp = new ArrayList<>();
        mPersonList = new ArrayList<>();
        mAdapter = new AdapterOrganizationCompanyTab(getActivity(), list, true, this);
        mAdapter.updateIsSearch(0);
        listCompany.setAdapter(mAdapter);
        getListAllUser();
    }

    public void scrollToEndList(int size) {
        listCompany.smoothScrollToPosition(size);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_company, container, false);

        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        listCompany = rootView.findViewById(R.id.listCompany);

        tvNoData = rootView.findViewById(R.id.tv_no_data);
        progressBar = rootView.findViewById(R.id.progressBar);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        listCompany.setVisibility(View.VISIBLE);
        tvNoData.setVisibility(View.GONE);
        CompanyFragment companyFragment = this;

        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listCompany.setLayoutManager(linearLayoutManager);
        mAdapter = new AdapterOrganizationCompanyTab(getActivity(), list, true, companyFragment);
        listCompany.setAdapter(mAdapter);

        swipeRefreshLayout.setOnRefreshListener(() -> {
           refreshDataUser();
        });

        return rootView;
    }

    public void initDB() {
        getListAllUser_Mod_V2();
    }

    private ArrayList<TreeUserDTO> mDepartmentList;

    public ArrayList<TreeUserDTOTemp> getUser() {
        if (listTemp == null) listTemp = new ArrayList<>();
        return listTemp;
    }

    public ArrayList<TreeUserDTO> getDepartments() {
        if (mDepartmentList == null) mDepartmentList = new ArrayList<>();
        return mDepartmentList;
    }

    private void initWholeOrganization() {
        mDepartmentList = new ArrayList<>();
        listTemp = new ArrayList<>();

        new Thread(() -> {
            swipeRefreshLayout.setRefreshing(false);
            listTemp = AllUserDBHelper.getUser_v2();
            mDepartmentList = DepartmentDBHelper.getDepartments_v2();
            if (listTemp == null) listTemp = new ArrayList<>();
            if (mDepartmentList == null) mDepartmentList = new ArrayList<>();
            isBuild = true;
            mHandler.obtainMessage(GET_DATA_OFFLINE_COMPLETE).sendToTarget();
        }).start();
    }

    private void getListDepartment_Mod() {
        String moddate = new Prefs().getModdate_deppartment();
        if (moddate.length() == 0) {
            Calendar calendar = Calendar.getInstance();
            moddate = TimeUtils.showTimeWithoutTimeZone(calendar.getTimeInMillis(), Statics.yyyy_MM_dd_HH_mm_ss_SSS);
            new Prefs().setModdate_deppartment(moddate);
        }

        HttpRequest.getInstance().GetListDepart_Mod(moddate, new IGetListDepart() {
            @Override
            public void onGetListDepartSuccess(final ArrayList<TreeUserDTO> treeUserDTOs) {
                Log.d(TAG, "getListDepartment_Mod\t\tonGetListDepartSuccess");
                if (treeUserDTOs != null && treeUserDTOs.size() > 0) {
                    Calendar calendar = Calendar.getInstance();
                    new Prefs().setModdate_deppartment(TimeUtils.showTimeWithoutTimeZone(calendar.getTimeInMillis(), Statics.yyyy_MM_dd_HH_mm_ss_SSS));

                    new Thread(() -> {
                        DepartmentDBHelper.addDepartment(treeUserDTOs);
                        mHandler.obtainMessage(GET_MOD_DEPARTMENT_COMPLETE, treeUserDTOs).sendToTarget();
                    }).start();
                } else {
                    if (!isBuild) {
                        initWholeOrganization();
                    }
                }
            }

            @Override
            public void onGetListDepartFail(ErrorDto dto) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "onGetListDepartFail getListDepartment");
            }
        });
    }

    private void getListDepartment() {
        HttpRequest.getInstance().GetListDepart(new IGetListDepart() {
            @Override
            public void onGetListDepartSuccess(final ArrayList<TreeUserDTO> treeUserDTOs) {
                if (treeUserDTOs != null && treeUserDTOs.size() > 0) {
                    Calendar calendar = Calendar.getInstance();
                    String modDate = TimeUtils.showTimeWithoutTimeZone(calendar.getTimeInMillis(), Statics.yyyy_MM_dd_HH_mm_ss_SSS);
                    new Prefs().setModdate_deppartment(modDate);
                    Log.d(TAG, "getListDepartment\t\tonGetListDepartSuccess");

                    new Thread(() -> {
                        DepartmentDBHelper.addDepartment(treeUserDTOs);
                        mHandler.obtainMessage(GET_DEPARTMENT_COMPLETE, treeUserDTOs).sendToTarget();
                    }).start();
                }
            }

            @Override
            public void onGetListDepartFail(ErrorDto dto) {
                progressBar.setVisibility(View.GONE);

            }
        });
    }

    boolean isGetUser = false;
    ArrayList<TreeUserDTOTemp> treeUserDTOsInit = new ArrayList<>();

    private void getListAllUser() {
        HttpRequest.getInstance().GetListOrganize(new IGetListOrganization() {
            @Override
            public void onGetListSuccess(final ArrayList<TreeUserDTOTemp> treeUserDTOs) {
                if (treeUserDTOs != null && treeUserDTOs.size() > 0) {
                    Calendar calendar = Calendar.getInstance();
                    String modDate = TimeUtils.showTimeWithoutTimeZone(calendar.getTimeInMillis(), Statics.yyyy_MM_dd_HH_mm_ss_SSS);
                    new Prefs().setModDate(modDate);
                    isGetUser = true;
                    new Thread(() -> {
                        AllUserDBHelper.addUser(treeUserDTOs);
                        treeUserDTOsInit.addAll(treeUserDTOs);
                        new TinyDB(CrewChatApplication.getInstance()).putListObjectTmp("user", treeUserDTOsInit);
                        mHandler.obtainMessage(GET_USER_COMPLETE, treeUserDTOs).sendToTarget();
                    }).start();
                }
            }

            @Override
            public void onGetListFail(ErrorDto dto) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private boolean isLoad = false;

    public boolean isLoadTreeData() {
        return isLoad;
    }

    public List<TreeUserDTO> getSubordinates() {
        if (list == null) list = new ArrayList<>();
        return list;
    }

    private void getListAllUser_Mod() {
        String moddate = new Prefs().getModDate();
        if (moddate.length() == 0) {
            Calendar calendar = Calendar.getInstance();
            moddate = TimeUtils.showTimeWithoutTimeZone(calendar.getTimeInMillis(), Statics.yyyy_MM_dd_HH_mm_ss_SSS);
            new Prefs().setModDate(moddate);
        }

        HttpRequest.getInstance().GetListOrganize_Mod(moddate, new IGetListOrganization() {
            @Override
            public void onGetListSuccess(final ArrayList<TreeUserDTOTemp> treeUserDTOs) {
                swipeRefreshLayout.setRefreshing(false);
                if (treeUserDTOs != null && treeUserDTOs.size() > 0) {
                    Log.d(TAG, "getListAllUser_Mod onGetListSuccess");
                    Calendar calendar = Calendar.getInstance();
                    new Prefs().setModDate(TimeUtils.showTimeWithoutTimeZone(calendar.getTimeInMillis(), Statics.yyyy_MM_dd_HH_mm_ss_SSS));
                    if (treeUserDTOs == null || treeUserDTOs.size() == 0) {
                        Log.d(TAG, "getListAllUser_Mod null list");
                    } else {
                        Log.d(TAG, "treeUserDTOs:" + treeUserDTOs.size());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                AllUserDBHelper.addUser(treeUserDTOs);
                                mHandler.obtainMessage(GET_USER_MOD_COMPLETE, treeUserDTOs).sendToTarget();
                            }
                        }).start();
                    }
                } else {
                    mHandler.obtainMessage(GET_USER_MOD_COMPLETE, treeUserDTOs).sendToTarget();
                }
            }

            @Override
            public void onGetListFail(ErrorDto dto) {
                swipeRefreshLayout.setRefreshing(false);
                Log.d(TAG, "getListAllUser_Mod ErrorDto");
            }
        });
    }

    @Subscribe
    public void notifyAdapter(NotifyAdapterOgr notifyAdapterOgr) {
        mAdapter.notifyDataSetChanged();
    }

    private void getListAllUser_Mod_V2() {
        Log.d(TAG, "URL_GET_ALL_USER_BE_LONGS_MOD");
        String moddate = new Prefs().getModDate();
        if (moddate.length() == 0) {
            Calendar calendar = Calendar.getInstance();
            moddate = TimeUtils.showTimeWithoutTimeZone(calendar.getTimeInMillis(), Statics.yyyy_MM_dd_HH_mm_ss_SSS);
            new Prefs().setModDate(moddate);
        }
        if (Utils.isNetworkAvailable()) {
            HttpRequest.getInstance().GetListOrganize_Mod(moddate, new IGetListOrganization() {
                @Override
                public void onGetListSuccess(final ArrayList<TreeUserDTOTemp> treeUserDTOs) {
                    swipeRefreshLayout.setRefreshing(false);
                    if (treeUserDTOs != null && treeUserDTOs.size() > 0) {
                        Log.d("treeUserDTOs", treeUserDTOs.toString());
                        Calendar calendar = Calendar.getInstance();
                        new Prefs().setModDate(TimeUtils.showTimeWithoutTimeZone(calendar.getTimeInMillis(), Statics.yyyy_MM_dd_HH_mm_ss_SSS));
                        if (treeUserDTOs.size() == 0) {
                            Log.d(TAG, "getListAllUser_Mod null list");
                            if (!isBuild) {
                                mHandler.obtainMessage(GET_USER_MOD_COMPLETE, treeUserDTOs).sendToTarget();
                            }

                        } else {
                            AllUserDBHelper.addUser(treeUserDTOs);
                            mHandler.obtainMessage(GET_USER_MOD_COMPLETE, treeUserDTOs).sendToTarget();
                        }
                    } else {
                        if (!isBuild) {
                            mHandler.obtainMessage(GET_USER_MOD_COMPLETE, treeUserDTOs).sendToTarget();
                        }
                    }
                }

                @Override
                public void onGetListFail(ErrorDto dto) {
                    swipeRefreshLayout.setRefreshing(false);
                    Log.d(TAG, "getListAllUser_Mod ErrorDto");
                }
            });
        } else {
            if (!isBuild) {
                initWholeOrganization();
            }
        }

    }

    @SuppressLint("HandlerLeak")
    protected final android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
            } else if (msg.what == 2) {
                Bundle args = msg.getData();
                ArrayList<FavoriteGroupDto> groups = args.getParcelableArrayList("groupList");
                if (groups == null) {
                    groups = new ArrayList<>();
                }
                groups.add(0, new FavoriteGroupDto("Favorite", 0));
                createDialog(groups);
            } else if (msg.what == 3) {
                Bundle args = msg.getData();
                ArrayList<FavoriteGroupDto> groups = args.getParcelableArrayList("groupList");
                if (groups != null) {
                    // Just get data from server and store to local data, not show dialog
                    saveDataToLocal(groups);
                }
            } else if (msg.what == 4) { // update status
                Bundle args = msg.getData();
                ArrayList<TreeUserDTOTemp> users = args.getParcelableArrayList("listUsers");
                updateStatus(users);
            } else if (msg.what == CODE_BUILD_TREE_OFFLINE) {
                isOnline = false;
                swipeRefreshLayout.setRefreshing(false);
                buildTree(mDepartmentList, isOnline);
            } else if (msg.what == GET_USER_COMPLETE) {
                listTemp = (ArrayList<TreeUserDTOTemp>) msg.obj;
                getListDepartment();
            } else if (msg.what == GET_DEPARTMENT_COMPLETE) {
                isOnline = true;
                buildTree((ArrayList<TreeUserDTO>) msg.obj, isOnline);
            } else if (msg.what == CREATE_TREE) {
                TreeUserDTO dto = (TreeUserDTO) msg.obj;
                if (dto != null) {
                    list = dto.getSubordinates();
                    mAdapter.updateList(list);
                    isLoad = true;
                    if (CurrentChatListFragment.fragment != null && !hasInitCurrentChatListFragment) {
                        hasInitCurrentChatListFragment = true;
                        CurrentChatListFragment.fragment.init();
                    }


                    if (RecentFavoriteFragment.instance != null)
                        RecentFavoriteFragment.instance.init();

                    if (MultilLevelListviewFragment.instanceNew != null)
                        MultilLevelListviewFragment.instanceNew.initDB();

                }

                getListAllUser_Mod();
                progressBar.setVisibility(View.GONE);

                if(currentListStatusUser.size() > 0) {
                    mAdapter.updateListStatus(currentListStatusUser);
                }
            } else if (msg.what == GET_DATA_OFFLINE_COMPLETE) {
                boolean flag = new Prefs().isDataComplete();
                if (listTemp != null && listTemp.size() > 0 && mDepartmentList != null && mDepartmentList.size() > 0 && flag) {
                    mHandler.obtainMessage(CODE_BUILD_TREE_OFFLINE).sendToTarget();
                } else {
                    getListAllUser();
                }
            } else if (msg.what == GET_USER_MOD_COMPLETE) {
                getListDepartment_Mod();
            } else if (msg.what == GET_MOD_DEPARTMENT_COMPLETE) {
                if (!isBuild) {
                    initWholeOrganization();
                }
            }
        }
    };

    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, ArrayList<StatusViewDto>> statusList = new HashMap<>();

    private void updateStatus(ArrayList<TreeUserDTOTemp> users) {
        // Compare status and update view
        for (TreeUserDTOTemp user : users) {
            for (Map.Entry<Integer, ArrayList<StatusViewDto>> u : statusList.entrySet()) {
                if (user.getUserNo() == u.getKey()) {
                    // set image resource for this view
                    int status = user.getStatus();
                    String status_text = user.getUserStatusString();

                    for (StatusViewDto row : u.getValue()) {
                        if (TextUtils.isEmpty(status_text)) {
                            row.status_text.setVisibility(View.GONE);
                        } else {
                            row.status_text.setText(status_text);

                            if (!row.status_text.isShown()) {
                                row.status_text.setVisibility(View.VISIBLE);
                            }
                        }

                        if (status == Statics.USER_LOGIN) {
                            row.status_icon.setImageResource(R.drawable.home_big_status_01);
                        } else if (status == Statics.USER_AWAY) {
                            row.status_icon.setImageResource(R.drawable.home_big_status_02);
                        } else {
                            row.status_icon.setImageResource(R.drawable.home_big_status_03);
                        }
                    }
                }
            }
        }
    }

    private void saveDataToLocal(final List<FavoriteGroupDto> groups) {
        // Save data to local
        // sync data and store to local database
        new Thread(() -> {
            // just test, not run now
            FavoriteGroupDBHelper.addGroups(groups);
        }).start();
    }

    private void createDialog(final ArrayList<FavoriteGroupDto> groups) {
        String[] AlertDialogItems = new String[groups.size()];

        for (int i = 0; i < groups.size(); i++) {
            AlertDialogItems[i] = groups.get(i).getName();
        }

        AlertDialog popup;
        final ArrayList<FavoriteGroupDto> selectedItems = new ArrayList<>();
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.choose_group));

        builder.setMultiChoiceItems(AlertDialogItems, null,
                (dialog, indexSelected, isChecked) -> {
                    if (isChecked) {
                        selectedItems.add(groups.get(indexSelected));
                    } else if (selectedItems.contains(indexSelected)) {
                        selectedItems.remove(indexSelected);
                    }
                });

        builder.setPositiveButton(R.string.yes, (dialog, id) -> {
            if (selectedItems.size() == 0) {
                String msg = mContext.getResources().getString(R.string.msg_select_item);
                Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
            } else {
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.no, (dialog, id) -> dialog.dismiss());

        popup = builder.create();
        popup.show();
    }

    private ArrayList<TreeUserDTO> temp = new ArrayList<>();

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

    private ArrayList<TreeUserDTO> mPersonList = new ArrayList<>();

    private void buildTree(final ArrayList<TreeUserDTO> treeUserDTOs, final boolean isFromServer) {
        new Thread(() -> {
            if (treeUserDTOs != null) {
                if (isFromServer) {
                    convertData(treeUserDTOs);
                    mDepartmentList.clear();
                    mDepartmentList.addAll(temp);
                    new TinyDB(CrewChatApplication.getInstance()).putListObject("depart", mDepartmentList);
                } else {
                    temp.clear();
                    temp.addAll(treeUserDTOs);
                }

                for (TreeUserDTO treeUserDTO : temp) {
                    if (treeUserDTO.getSubordinates() != null && treeUserDTO.getSubordinates().size() > 0) {
                        treeUserDTO.setSubordinates(null);
                    }
                }

                Collections.sort(temp, (r1, r2) -> {
                    if (r1.getmSortNo() > r2.getmSortNo()) {
                        return 1;
                    } else if (r1.getmSortNo() == r2.getmSortNo()) {
                        return 0;
                    } else {
                        return -1;
                    }
                });


                for (TreeUserDTOTemp treeUserDTOTemp : listTemp) {
                    if (treeUserDTOTemp.getBelongs() != null) {
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

                            treeUserDTO.DutyName = belong.getDutyName();
                            treeUserDTO.setCompanyNumber(treeUserDTOTemp.getCompanyPhone());
                            if (treeUserDTO.isEnabled()) {
                                temp.add(treeUserDTO);
                            }

                        }
                    }
                }
                mPersonList = new ArrayList<>(temp);

                TreeUserDTO dto = null;
                try {
                    dto = Org_tree.buildTree(mPersonList);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mHandler.obtainMessage(CREATE_TREE, dto).sendToTarget();
            }
        }).start();
    }

    List<TreeUserDTO> lstCurrent = new ArrayList<>();

    public void getListCurrent() {
        lstCurrent = mAdapter.getCurrentList();
    }

    public void updateCurrentList() {
        Log.d(TAG, "updateCurrentList");
        if (lstCurrent != null && lstCurrent.size() > 0) {
            mAdapter.updateListSearch(lstCurrent);
        }
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
}

