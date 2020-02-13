package com.dazone.crewchatoff.TestMultiLevelListview;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.activity.MainActivity;
import com.dazone.crewchatoff.activity.base.OrganizationFavoriteActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.customs.AlertDialogView;
import com.dazone.crewchatoff.database.AllUserDBHelper;
import com.dazone.crewchatoff.database.FavoriteGroupDBHelper;
import com.dazone.crewchatoff.database.FavoriteUserDBHelper;
import com.dazone.crewchatoff.dto.BelongDepartmentDTO;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.dto.userfavorites.FavoriteGroupDto;
import com.dazone.crewchatoff.dto.userfavorites.FavoriteUserDto;
import com.dazone.crewchatoff.eventbus.NotifyAdapterOgr;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.fragment.RecentFavoriteFragment;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallbackWithJson;
import com.dazone.crewchatoff.interfaces.OnGetStatusCallback;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MultilLevelListviewFragment extends Fragment {
    public static MultilLevelListviewFragment instance;
    public static MultilLevelListviewFragment instanceNew = null;
    String TAG = "MultilLevelList";
    private List<NLevelItem> lstFavorite = new CopyOnWriteArrayList<>();
    private View rootView;
    private Context mContext;
    public static int idFolder = 0;
    private RecyclerView rvMain;
    public RecyclerView.LayoutManager layoutManager;
    private TreeUserDTOTemp tempUser = null;
    private int myId;
    private boolean isCreated = false;
    private NLevelRecycleAdapter mAdapter = null;
    private EditText mInputSearch;

    // Local variable
    // Define some static var here
    private ArrayList<TreeUserDTOTemp> mListUsers = null;
    private ArrayList<TreeUserDTO> mListDeparts = null;
    private ArrayList<FavoriteGroupDto> mListFavoriteGroup = null;
    private ArrayList<FavoriteUserDto> mListFavoriteTop = null;
    private NLevelItem favoriteRoot = null;
    private final int FAVORITE_BUILD = 1;

    // Addition search favorite user
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Statics.ACTION_SHOW_SEARCH_FAVORITE_INPUT);
        filter.addAction(Statics.ACTION_HIDE_SEARCH_FAVORITE_INPUT);
        getActivity().registerReceiver(mReceiverShowSearchInput, filter);
    }

    private void unregisterReceiver() {
        getActivity().unregisterReceiver(mReceiverShowSearchInput);
    }

    private BroadcastReceiver mReceiverShowSearchInput = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Statics.ACTION_SHOW_SEARCH_FAVORITE_INPUT)) {
                if (mInputSearch != null) {
                    showSearchInput();
                }
            } else if (intent.getAction().equals(Statics.ACTION_HIDE_SEARCH_FAVORITE_INPUT)) {
                hideSearchInput();
            }
        }
    };

    protected void showSearchInput() {
        if (mInputSearch != null) {
            mInputSearch.setVisibility(View.VISIBLE);
            mInputSearch.post(new Runnable() {
                @Override
                public void run() {
                    mInputSearch.requestFocus();
                    InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imgr.showSoftInput(mInputSearch, InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }
    }

    protected void hideSearchInput() {
        if (mInputSearch != null) {
            mInputSearch.setText("");
            mInputSearch.setVisibility(View.GONE);
            if (getActivity() != null) {
                Utils.hideKeyboard(getActivity());
            }
        }
    }

    // End - Addition favorite search

    /*IntentFilter intentFilterSearch;
    BroadcastReceiver receiverSearch = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String textSearch = intent.getStringExtra(Constant.KEY_INTENT_TEXT_SEARCH);
                if (mAdapter != null){
                    mAdapter.getFilter().filterUser(textSearch);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };*/

    public void showFAB() {
        if (getActivity() != null && getActivity() instanceof MainActivity) {
//            ((MainActivity)getActivity()).showPAB(mAddFavoriteGroup);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public static MultilLevelListviewFragment newInstance() {
        // Required empty public constructor
        if (instance == null) {
            instance = new MultilLevelListviewFragment();
        }
        return instance;
    }

    protected final android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == FAVORITE_BUILD) {

                if (mAdapter != null) {

                    mAdapter.reloadData();
                }
            }

        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        //intentFilterSearch = new IntentFilter(Constant.INTENT_FILTER_SEARCH);
    }

    @Override
    public void onStart() {
        super.onStart();
        /*if (getActivity() != null){
            getActivity().registerReceiver(receiverSearch, intentFilterSearch);
        }*/
        registerReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        /*if (getActivity() != null){
            getActivity().unregisterReceiver(receiverSearch);
        }*/
        unregisterReceiver();
    }

    private boolean isShowSearchIcon = false;

    @Override
    public void onResume() {
        super.onResume();
//        if (getActivity() != null) {
//            if (((MainActivity) getActivity()).getCurrentTab() == 2) {
//                ((MainActivity) getActivity()).showSearchIcon(new OnClickCallback() {
//                    @Override
//                    public void onClick() {
//                        if (!isShowSearchIcon) {
//                            Intent intent = new Intent(Statics.ACTION_SHOW_SEARCH_FAVORITE_INPUT);
//                            getActivity().sendBroadcast(intent);
//                            isShowSearchIcon = true;
//                        } else {
//                            isShowSearchIcon = false;
//                            Intent intent = new Intent(Statics.ACTION_HIDE_SEARCH_FAVORITE_INPUT);
//                            getActivity().sendBroadcast(intent);
//                        }
//                    }
//                });
//
//            }
//        }
    }

    @Subscribe
    public void notifyAdapter(NotifyAdapterOgr notifyAdapterOgr) {
        /* Toast.makeText(getContext(), "OK", Toast.LENGTH_SHORT).show();*/
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && favoriteRoot != null) {
            Log.d(TAG, "getFavoriteGroup 1");
            getFavoriteGroup(favoriteRoot);
        }

        if (isVisibleToUser) {
            if (getActivity() != null && getActivity() instanceof MainActivity) {
//                ((MainActivity) getActivity()).showPAB(mAddFavoriteGroup);
            }
        }
    }

    public void addNewFavorite(FavoriteUserDto userDto) {
        // find parent
        Iterator<NLevelItem> iter = lstFavorite.iterator();
        int index = -1;

        while (iter.hasNext()) {
            NLevelItem item = iter.next();
            TreeUserDTO temp = item.getObject();
            index++;
            Log.d(TAG, "index:" + index);
            if (temp.getId() == userDto.getGroupNo() && temp.getType() != Statics.TYPE_USER) { // Got parent == item
                // process to get an user from data base
                TreeUserDTOTemp tempU = AllUserDBHelper.getAUser(userDto.getUserNo());
               /* TreeUserDTOTemp userDtoBelong = new TreeUserDTOTemp();
                userDtoBelong.setBelongs(BelongsToDBHelper.getBelongs(userDto.getUserNo()));*/
                TreeUserDTO user = new TreeUserDTO(tempU.getName(), tempU.getNameEN(), tempU.getCellPhone(), tempU.getAvatarUrl()
                        , getPositionName(tempU), 2, 1, userDto.getUserNo(), userDto.getGroupNo());
                try {
                    user.setEnabled(tempU.getEnabled());
                } catch (Exception e) {
                    user.setEnabled(true);
                    e.printStackTrace();
                }
                user.setStatus(tempU.getStatus());
                user.setStatusString(tempU.getUserStatusString());
//                Log.d(TAG,tempU.getCellPhone()+":"+tempU.getCompanyPhone());
                user.setPhoneNumber(tempU.getCellPhone());
                user.setDutyName(getDutyName(
                        tempU));
                user.setCompanyNumber(tempU.getCompanyPhone());
                NLevelItem childItem = new NLevelItem(user, item, item.getLevel() + 1);
                Log.d(TAG, "lstFavorite.add 3");
                if (isAdd(childItem))
                    lstFavorite.add(index + 1, childItem);
                if (mAdapter != null) {
                    mAdapter.reloadData();
                }
                break;
            } else {
//                Log.d(TAG, "###" + temp.getId() + "--" + userDto.getGroupNo() + "--" + temp.getType());
            }
        }
    }

//    public void removeFavoriteUser(int userNo) {
//        // remove favorite from current dataset
//        Iterator<NLevelItem> iter = lstFavorite.iterator();
//
//        while (iter.hasNext()) {
//            NLevelItem item = iter.next();
//            TreeUserDTO temp = item.getObject();
//
//            if (temp.getId() == userNo && temp.getType() == Statics.TYPE_USER) {
//                iter.remove();
//
//                if (mAdapter != null) {
//
//                    mAdapter.reloadData();
//                }
//            }
//        }
//    }

    private OnGetStatusCallback mStatusCallback = new OnGetStatusCallback() {
        @Override
        public void onGetStatusFinish() {

            if (!rvMain.isComputingLayout()) {
                // Need to update status
                if (mListUsers != null) {
                    for (TreeUserDTOTemp user : mListUsers) {
                        for (NLevelItem item : lstFavorite) {
                            TreeUserDTO dto = item.getObject();

                            if (dto.getType() == Statics.TYPE_USER) {
                                if (user.getUserNo() == dto.getId()) {
                                    dto.setStatus(user.getStatus());
                                    dto.setStatusString(user.getUserStatusString());
                                }
                            }
                        }
                    }

                    // Notify data set after set status string
                    if (mAdapter != null) {

                        mAdapter.reloadData();
                    }
                }
            }

            if (RecentFavoriteFragment.instance != null) {
                RecentFavoriteFragment.instance.updateSTTOffline();
            }

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        instanceNew = this;
        mListFavoriteGroup = null;
        rootView = inflater.inflate(R.layout.fragment_multil_level_listview, container, false);
        rvMain = (RecyclerView) rootView.findViewById(R.id.rv_main);

        // Addition - Search favorite
        mInputSearch = (EditText) rootView.findViewById(R.id.inputSearch);
        mInputSearch.setImeOptions(mInputSearch.getImeOptions() | EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);
        mInputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mAdapter != null) {
                    mAdapter.getFilter().filterUser(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // End addition - favorite search
        rvMain.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        rvMain.setLayoutManager(layoutManager);
        lstFavorite = new ArrayList<>();


        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setGetStatusCallbackFavorite(mStatusCallback);
            //((MainActivity)getActivity()).showPAB(mAddFavoriteGroup);
        }

        return rootView;
    }

    private boolean isLoad = false;

    public boolean isLoadDB() {
        return isLoad;
    }


    public void initDB() {
        if (CompanyFragment.instance != null) {
            mListUsers = CompanyFragment.instance.getUser();
            mListDeparts = CompanyFragment.instance.getDepartments();

            if (mListUsers == null) mListUsers = new ArrayList<>();
            if (mListDeparts == null) mListDeparts = new ArrayList<>();

            Log.d(TAG, "mListUsers:" + mListUsers.size());
            Log.d(TAG, "mListDeparts:" + mListDeparts.size());

            HttpRequest.getInstance().getFavotiteGroupAndData(new BaseHTTPCallbackWithJson() {
                @Override
                public void onHTTPSuccess(String json) {
                    Log.d(TAG, "getGroupFromServer\t\tonHTTPSuccess");
                    Type listType = new TypeToken<ArrayList<FavoriteGroupDto>>() {
                    }.getType();
                    ArrayList<FavoriteGroupDto> list = new Gson().fromJson(json, listType);
                    if (list != null) {
                        FavoriteGroupDBHelper.addGroups(list);
                        buildTree();
                    }
                }

                @Override
                public void onHTTPFail(ErrorDto errorDto) {
                    Log.d(TAG, "getGroupFromServer onHTTPFail");
                }
            });

        } else {
            Toast.makeText(getActivity(), "Can not get list user, restart app please", Toast.LENGTH_SHORT).show();
        }
    }

    public void addFavorite() {
        Resources res = getResources();
        String groupName = res.getString(R.string.group_name_add_favorite);
        String confirm = res.getString(R.string.confirm_add_favorite);
        String cancel = res.getString(R.string.cancel);
        String hintText = res.getString(R.string.group_name);

        AlertDialogView.alertDialogConfirmWithEditText(mContext, groupName, hintText, "", confirm, cancel, new AlertDialogView.onAlertDialogViewClickEventData() {
            @Override
            public void onOkClick(String groupName) {
                // Call API to add group
                addFavoriteGroup(groupName);
            }

            @Override
            public void onCancelClick() {
                // Dismiss dialog
            }
        });
    }

//    OnClickCallback mAddFavoriteGroup = new OnClickCallback() {
//        @Override
//        public void onClick() {
//            Resources res = getResources();
//            String groupName = res.getString(R.string.group_name_add_favorite);
//            String confirm = res.getString(R.string.confirm_add_favorite);
//            String cancel = res.getString(R.string.cancel);
//            String hintText = res.getString(R.string.group_name);
//
//            AlertDialogView.alertDialogConfirmWithEditText(mContext, groupName, hintText, "", confirm, cancel, new AlertDialogView.onAlertDialogViewClickEventData() {
//                @Override
//                public void onOkClick(String groupName) {
//                    // Call API to add group
//                    addFavoriteGroup(groupName);
//                }
//
//                @Override
//                public void onCancelClick() {
//                    // Dismiss dialog
//                }
//            });
//        }
//    };


    ArrayList<FavoriteGroupDto> temFavorite = new ArrayList<>();

    private void getFavoriteGroup(final NLevelItem parent) {

        // Get main favorite group and data
        HttpRequest.getInstance().getFavotiteGroupAndData(new BaseHTTPCallbackWithJson() {
            @Override
            public void onHTTPSuccess(String json) {
                Type listType = new TypeToken<ArrayList<FavoriteGroupDto>>() {
                }.getType();
                // Add data from local before get all from local database --> it may perform slow
                final ArrayList<FavoriteGroupDto> listFromServer = new Gson().fromJson(json, listType);


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FavoriteGroupDBHelper.addGroups(listFromServer);
                    }
                }).start();

                // foreach to compare
                if (mListFavoriteGroup != null) {

                    if (mListFavoriteGroup.size() == 0 && CrewChatApplication.listFavoriteGroup != null && CrewChatApplication.listFavoriteGroup.size() > 0) {
                        mListFavoriteGroup = CrewChatApplication.listFavoriteGroup;

                    }

                    // Loop and add favorite to current list, sync data
                    // Thread to sync data
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            temFavorite.clear();

                            for (FavoriteGroupDto serverDto : listFromServer) {
                                boolean isExist = false;

                                for (FavoriteGroupDto clientDto : mListFavoriteGroup) {
                                    if (serverDto.getGroupNo() == clientDto.getGroupNo()) {

                                        isExist = true;
                                        ArrayList<FavoriteUserDto> listUsersServer = serverDto.getUserList();

                                        if (listUsersServer != null) {
                                            for (FavoriteUserDto userServer : listUsersServer) {
                                                boolean isChildExist = false;
                                                ArrayList<FavoriteUserDto> listUsersClient = clientDto.getUserList();

                                                for (FavoriteUserDto userClient : listUsersClient) {
                                                    if (userServer.getUserNo() == userClient.getUserNo()) {
                                                        isChildExist = true;
                                                        break;
                                                    }
                                                }

                                                // Add child to current list
                                                if (!isChildExist) {
                                                    listUsersClient.add(userServer);
                                                    // Find parent
                                                    int i = 0;
                                                    for (NLevelItem item : lstFavorite) {
                                                        i++;

                                                        if (item.getObject().getId() == userServer.getGroupNo()) {
                                                            TreeUserDTOTemp tempU = AllUserDBHelper.getAUser(userServer.getUserNo());

                                                            if (tempU != null) {
                                                                TreeUserDTO user = new TreeUserDTO(tempU.getName(), tempU.getNameEN(), tempU.getCellPhone(), tempU.getAvatarUrl(), tempU.getPosition(), 2, 1, userServer.getUserNo(), userServer.getGroupNo());
                                                                NLevelItem newItem = new NLevelItem(user, item, item.getLevel() + 1);
                                                                Log.d(TAG, "lstFavorite.add 4");
                                                                if (isAdd(newItem))
                                                                    lstFavorite.add(i, newItem);

                                                                Message message = Message.obtain();
                                                                message.what = FAVORITE_BUILD;
                                                                mHandler.sendMessage(message);
                                                            }
                                                            break;
                                                        }
                                                    }
                                                    // Create new view
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }

                                if (!isExist) {
                                    temFavorite.add(serverDto);
                                }
                            }

                            // build favorite group
                            if (temFavorite.size() > 0) {
                                reBuildFavoriteGroup();
                                Log.d(TAG, "temFavorite.size() > 0: " + temFavorite.size());
                            } else {
                                Log.d(TAG, " temFavorite.size() <= 0");
                            }
                        }
                    }).start();
                } else {

                    CrewChatApplication.listFavoriteGroup = listFromServer;
                    Log.d(TAG, "init mListFavoriteGroup 1");
                    mListFavoriteGroup = CrewChatApplication.listFavoriteGroup;
                    buildFavoriteGroup(parent);
                    if (mAdapter != null) {

                        mAdapter.reloadData();
                    }
                }
            }

            @Override
            public void onHTTPFail(ErrorDto errorDto) {
            }
        });
    }

//    private void getFavoriteGroup() {
//        // Get main favorite group and data
//        HttpRequest.getInstance().getFavotiteGroupAndData(new BaseHTTPCallbackWithJson() {
//            @Override
//            public void onHTTPSuccess(String json) {
//                Type listType = new TypeToken<ArrayList<FavoriteGroupDto>>() {
//                }.getType();
//                // Add data from local before get all from local database --> it may perform slow
//                final ArrayList<FavoriteGroupDto> listFromServer = new Gson().fromJson(json, listType);
//
////                for(FavoriteGroupDto obj:listFromServer)
////                {
////                    Log.d(TAG,"getFavoriteGroup:"+new Gson().toJson(obj));
////
////                }
//
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        FavoriteGroupDBHelper.addGroups(listFromServer);
//                    }
//                }).start();
//
//                CrewChatApplication.listFavoriteGroup = listFromServer;
//                Log.d(TAG, "listFromServer 2");
//                mListFavoriteGroup = listFromServer;
//
//                buildTree();
//            }
//
//            @Override
//            public void onHTTPFail(ErrorDto errorDto) {
//            }
//        });
//    }

//    private void getListUserFromServer() {
//        HttpRequest.getInstance().GetListOrganize(new IGetListOrganization() {
//            @Override
//            public void onGetListSuccess(final ArrayList<TreeUserDTOTemp> treeUserDTOs) {
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        AllUserDBHelper.addUser(treeUserDTOs);
//                        Log.d(TAG, "addUser 6");
//                    }
//                }).start();
//
//                mListUsers = treeUserDTOs;
//                CrewChatApplication.listUsers = treeUserDTOs;
//
//                getFavoriteGroup();
//            }
//
//            @Override
//            public void onGetListFail(ErrorDto dto) {
//
//            }
//        });
//    }

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

//    private void getDepartmentFromServer() {
//
//        HttpRequest.getInstance().GetListDepart(new IGetListDepart() {
//            @Override
//            public void onGetListDepartSuccess(final ArrayList<TreeUserDTO> treeUserDTOs) {
//                // Thread to store database to local
//
//                // Get department
//                temp.clear();
//                convertData(treeUserDTOs);
//
//                /*// sort data by order
//                Collections.sort(temp, new Comparator<TreeUserDTO>() {
//                    @Override
//                    public int compare(TreeUserDTO r1, TreeUserDTO r2) {
//                        if (r1.getmSortNo() > r2.getmSortNo()) {
//                            return 1;
//                        } else if (r1.getmSortNo() == r2.getmSortNo()) {
//                            return 0;
//                        } else {
//                            return -1;
//                        }
//                    }
//                });*/
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        DepartmentDBHelper.addDepartment(treeUserDTOs);
//                    }
//                }).start();
//
//                CrewChatApplication.listDeparts = temp;
//                mListDeparts = CrewChatApplication.listDeparts;
//
//                getListUserFromServer();
//
//            }
//
//            @Override
//            public void onGetListDepartFail(ErrorDto dto) {
//
//            }
//        });
//    }

    private void buildTree() {
        int mCurrentId = new Prefs().getUserNo();
        for (TreeUserDTOTemp temp : mListUsers) {
            if (temp.getUserNo() == mCurrentId) {
                tempUser = temp;
                break;
            }
        }

        // perform add user to list department that's formatted above
        // The first time , level is 0, level will increase with each level
        lstFavorite.clear();
        // convertDataV2(null);

        // Need to get favorite group android user to
        TreeUserDTO favorite = new TreeUserDTO(Constant.Favorites, Constant.Favorites, "", "", "", 1, 1, 0, 0);
        favorite.setIsHide(1);
        favoriteRoot = new NLevelItem(favorite, null, 0);
        Log.d(TAG, "lstFavorite.add 5");
        if (isAdd(favoriteRoot)) lstFavorite.add(favoriteRoot);

        mListFavoriteTop = FavoriteUserDBHelper.getFavoriteTop();
        if (mListFavoriteTop != null && mListFavoriteTop.size() > 0) { // Local data is null, get from server
            buildFavoriteTop(mListFavoriteTop, favoriteRoot);
            buildFavoriteGroup(favoriteRoot);

            if (mAdapter != null) mAdapter.reloadData();

        } else { // get from local data and build it

            getTopFavorite(favoriteRoot);
        }
//        }

        // build favorite group
        // Init adapter after get all user
        init();
        isLoad = true;
    }

    private void getTopFavorite(final NLevelItem favoriteRoot) {

        HttpRequest.getInstance().getTopFavotiteGroupAndData(new BaseHTTPCallbackWithJson() {
            @Override
            public void onHTTPSuccess(String jsonData) {
                Type listType = new TypeToken<ArrayList<FavoriteUserDto>>() {
                }.getType();
                // Add data from local before get all from local database --> it may perform slow
                final ArrayList<FavoriteUserDto> listTop = new Gson().fromJson(jsonData, listType);

                for (FavoriteUserDto dto : listTop) {
                    dto.setIsTop(1);
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FavoriteUserDBHelper.addUsers(listTop);
                    }
                }).start();

                CrewChatApplication.listFavoriteTop = listTop;

                buildFavoriteTop(listTop, favoriteRoot);

                if (mListFavoriteGroup != null && mListFavoriteGroup.size() > 0) {
                    buildFavoriteGroup(favoriteRoot);
                    if (mAdapter != null) {

                        mAdapter.reloadData();
                    }
                } else {
                    Log.d(TAG, "getFavoriteGroup 2");
                    getFavoriteGroup(favoriteRoot);
                }


            }

            @Override
            public void onHTTPFail(ErrorDto errorDto) {
                Log.d(TAG, "onHTTPFail");
                if (mListFavoriteGroup != null && mListFavoriteGroup.size() > 0) {
                    buildFavoriteGroup(favoriteRoot);
                    if (mAdapter != null) {

                        mAdapter.reloadData();
                    }
                } else {
                    Log.d(TAG, "getFavoriteGroup 3");
                    getFavoriteGroup(favoriteRoot);
                }

            }
        });
    }

    private void buildFavoriteTop(ArrayList<FavoriteUserDto> listTop, NLevelItem parent) {
        for (FavoriteUserDto u : listTop) {
//            Log.d(TAG, "FavoriteUserDto:" + new Gson().toJson(u));
            TreeUserDTOTemp tempU = AllUserDBHelper.getAUser(u.getUserNo());

            if (tempU != null) {
                String position = "";
                String dutyName = "";
                ArrayList<BelongDepartmentDTO> belongs = tempU.getBelongs();
                if (belongs != null) {
                    for (BelongDepartmentDTO belong : belongs) {
                        if (TextUtils.isEmpty(position)) {
                            position += belong.getPositionName();
                        } else {
                            position = "";
                            position += belong.getPositionName();
                        }
                        if (TextUtils.isEmpty(dutyName)) {
                            dutyName += belong.getDutyName();
                        } else {
                            dutyName = "";
                            dutyName += belong.getDutyName();
                        }
                    }
                }

                TreeUserDTO user = new TreeUserDTO(dutyName, tempU.getName(), tempU.getNameEN(), tempU.getCellPhone(), tempU.getAvatarUrl(), position, 2, 1, u.getUserNo(), u.getGroupNo());
                user.setStatus(tempU.getStatus());
                user.setStatusString(tempU.getUserStatusString());
//                Log.d(TAG, new Gson().toJson(user));
                user.setPhoneNumber(tempU.getCellPhone());
                user.setCompanyNumber(tempU.getCompanyPhone());
                user.setEnabled(tempU.getEnabled());
                /* user.setDutyName(tempU.DutyName);
                 */
                NLevelItem childItem = new NLevelItem(user, favoriteRoot, favoriteRoot.getLevel() + 1);
                Log.d(TAG, "lstFavorite.add 6");
                if (isAdd(childItem))
                    lstFavorite.add(childItem);
            }
        }
    }

    public void buildFavoriteGroup(NLevelItem parent) {

//        if (mListFavoriteGroup == null) {
//            mListFavoriteGroup = CrewChatApplication.listFavoriteGroup;
//
//        }

        mListFavoriteGroup = FavoriteGroupDBHelper.getFavoriteGroup();

        if (mListFavoriteGroup != null && mListFavoriteGroup.size() > 0) {
            // sort data by order

            for (FavoriteGroupDto fa : mListFavoriteGroup) {
                TreeUserDTO folder = new TreeUserDTO(fa.getName(), fa.getName(), "", "", "", 1, 1, fa.getGroupNo(), favoriteRoot.getObject().getId());
                NLevelItem folderItem = new NLevelItem(folder, parent, parent.getLevel() + 1);
                Log.d(TAG, "lstFavorite.add 7");
                if (isAdd(folderItem))
                    lstFavorite.add(folderItem);
                // for folder
                if (fa.getUserList() != null && fa.getUserList().size() > 0) {
                    Log.d(TAG, "fa.getUserList()" + fa.getUserList().size());
                    // Sort favorite user
                    Collections.sort(fa.getUserList(), new Comparator<FavoriteUserDto>() {
                        @Override
                        public int compare(FavoriteUserDto r1, FavoriteUserDto r2) {
                            TreeUserDTOTemp name1 = AllUserDBHelper.getAUser(r1.getUserNo());
                            TreeUserDTOTemp name2 = AllUserDBHelper.getAUser(r2.getUserNo());
                            if (name1 != null && name2 != null) {
                                return name1.getName().compareToIgnoreCase(name2.getName());
                            }
                            return -1;
                        }
                    });


                    for (FavoriteUserDto u : fa.getUserList()) {
                        // Get list user and build tree
                        TreeUserDTOTemp tempU = AllUserDBHelper.getAUser(u.getUserNo());
                        if (tempU != null) {
                            Log.d("tempU!null", "tempU!null");
                            String position = "";
                            String dutyName = "";
                            ArrayList<BelongDepartmentDTO> belongs = tempU.getBelongs();
                            if (belongs != null) {
                                for (BelongDepartmentDTO belong : belongs) {
                                    if (TextUtils.isEmpty(position)) {
                                        position += belong.getPositionName();
                                    } else {
                                        position = "";
                                        position += belong.getPositionName();
                                    }
                                    if (TextUtils.isEmpty(dutyName)) {
                                        dutyName += belong.getDutyName();
                                    } else {
                                        dutyName = "";
                                        dutyName += belong.getDutyName();
                                    }
                                   /* if (TextUtils.isEmpty(position) && !TextUtils.isEmpty(belong.getDutyName())) {
                                        position += belong.getDutyName();
                                    } else if (TextUtils.isEmpty(position) && TextUtils.isEmpty(belong.getDutyName())) {
                                        position += belong.getPositionName();
                                    } else if (TextUtils.isEmpty(position) && !TextUtils.isEmpty(belong.getDutyName())) {
                                        position += "," + belong.getDutyName();
                                    } else if (TextUtils.isEmpty(position) && TextUtils.isEmpty(belong.getDutyName())) {
                                        position += "," + belong.getPositionName();
                                    }*/
                                }
                            }

                            TreeUserDTO user = new TreeUserDTO(dutyName,tempU.getName(), tempU.getNameEN(), tempU.getCellPhone(), tempU.getAvatarUrl(), position, 2, 1, u.getUserNo(), u.getGroupNo());
                            user.setStatus(tempU.getStatus());
                            Log.d(TAG, "setStatus");
                            user.setStatusString(tempU.getUserStatusString());
                            user.mIsFavoriteUser = true;
                            Log.d(TAG, "mIsFavoriteUser");
                            user.setPhoneNumber(tempU.getCellPhone());
                            user.setCompanyNumber(tempU.getCompanyPhone());
                            Log.d(TAG, "getCompanyPhone");
                            //user.DutyName = tempU.DutyName;
                            Log.d(TAG, "DutyName");
                            NLevelItem childItem = new NLevelItem(user, folderItem, folderItem.getLevel() + 1);
                            Log.d(TAG, " new NLevelItem");
                            Log.d(TAG, "lstFavorite.add 8");
                            if (isAdd(childItem))
                                Log.d(TAG, "isAdd(childItem)");
                            lstFavorite.add(childItem);
                        }
                    }
                }
            }

        }
    }

    void checkRemove() {

    }

    boolean isAdd(NLevelItem item) {
        TreeUserDTO obj_2 = item.getObject();

        if (lstFavorite.size() == 0) {
            Log.d(TAG, "lstFavorite.size() == 0");
            return true;
        } else {
            Log.d(TAG, "lstFavorite.size() != 0");
            for (NLevelItem obj : lstFavorite) {
                TreeUserDTO obj_1 = obj.getObject();
                if (obj_1.getParent() == obj_2.getParent() && obj_1.getId() == obj_2.getId()) {
                    Log.d(TAG, "duplicate: " + obj_2.getName());
                    return false;
                }
            }
            return true;
        }

    }

    public void reBuildFavoriteGroup() {
        Log.d(TAG, "reBuildFavoriteGroup");
        if (temFavorite.size() > 0) {
            for (FavoriteGroupDto fa : temFavorite) {
                TreeUserDTO folder = new TreeUserDTO(fa.getName(), fa.getName(), "", "", "", 1, 1, fa.getGroupNo(), favoriteRoot.getObject().getId());
                NLevelItem folderItem = new NLevelItem(folder, favoriteRoot, favoriteRoot.getLevel() + 1);
                Log.d(TAG, "lstFavorite.add 9");
                if (isAdd(folderItem))
                    lstFavorite.add(folderItem);


                // for folder
                if (fa.getUserList() != null && fa.getUserList().size() > 0) {

                    for (FavoriteUserDto u : fa.getUserList()) {
                        // Get list user and build tree
                        TreeUserDTOTemp tempU = AllUserDBHelper.getAUser(u.getUserNo());
                        if (tempU != null) {

                            String position = "";
                            String dutyName = "";
                            ArrayList<BelongDepartmentDTO> belongs = tempU.getBelongs();
                            if (belongs != null) {
                                for (BelongDepartmentDTO belong : belongs) {
                                   /* if (TextUtils.isEmpty(position)) {
                                        position += belong.getPositionName();
                                    } else {
                                        position += "," + belong.getPositionName();
                                    }*/
                                    if (TextUtils.isEmpty(position)) {
                                        position += belong.getPositionName();
                                    } else {
                                        position = "";
                                        position += belong.getPositionName();
                                    }
                                    if (TextUtils.isEmpty(dutyName)) {
                                        dutyName += belong.getDutyName();
                                    } else {
                                        dutyName = "";
                                        dutyName += belong.getDutyName();
                                    }
                                }
                            }

                            TreeUserDTO user = new TreeUserDTO(dutyName,tempU.getName(), tempU.getNameEN(), tempU.getCellPhone(), tempU.getAvatarUrl(), position, 2, 1, u.getUserNo(), u.getGroupNo());
                            user.setStatus(tempU.getStatus());
                            user.setStatusString(tempU.getUserStatusString());
                            NLevelItem childItem = new NLevelItem(user, folderItem, folderItem.getLevel() + 1);
                            Log.d(TAG, "lstFavorite.add 10");
                            if (isAdd(childItem))
                                lstFavorite.add(childItem);

//                            for (NLevelItem obj : lstFavorite) {
//                                TreeUserDTO treeUserDTO = obj.getObject();
//                                Log.d(TAG, obj.getLevel() + "--" + new Gson().toJson(treeUserDTO));
//                            }
                        }
                    }
                }
            }

            Message message = Message.obtain();
            message.what = FAVORITE_BUILD;
            mHandler.sendMessage(message);
        }
    }

    // Convert data for favorite list
    public void convertDataV2(NLevelItem parent) {
        if (mListDeparts == null) return;
        // Sort by Parent id
        if (parent == null) {
            TreeUserDTO root = new TreeUserDTO("Dazone", "Dazone", "", "", "", 1, 1, 0, 0);
            NLevelItem item = new NLevelItem(root, null, 0);
            convertDataV2(item);
        } else {
            for (TreeUserDTO dto : mListDeparts) {
                if (dto.getParent() == parent.getObject().getId()) {
                    NLevelItem item = new NLevelItem(dto, parent, parent.getLevel());
                    convertDataV2(item);
                    // Show all current user departments
                    if (tempUser != null) {
                        ArrayList<BelongDepartmentDTO> belongs = tempUser.getBelongs();
                        if (belongs != null) {
                            for (BelongDepartmentDTO belong : belongs) {
                                if (dto.getId() == belong.getDepartNo()) {
                                    Log.d(TAG, "lstFavorite.add 11");
                                    if (isAdd(item)) lstFavorite.add(item);
                                    convertUser(item, parent.getLevel() + 1, dto);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Build list tree for department
    public void convertDataV2Backup(NLevelItem parent) {
        // Sort by Parent id
        if (parent == null) {
            TreeUserDTO root = new TreeUserDTO("Dazone", "Dazone", "", "", "", 1, 1, 0, 0);
            NLevelItem item = new NLevelItem(root, null, -1);
            convertDataV2(item);
        } else {

            for (TreeUserDTO dto : CrewChatApplication.listDeparts) {
                if (dto.getParent() == parent.getObject().getId()) {

                    NLevelItem item = new NLevelItem(dto, parent, parent.getLevel() + 1);
                    Log.d(TAG, "lstFavorite.add 12");
                    if (isAdd(item))
                        lstFavorite.add(item);
                    convertUser(item, parent.getLevel() + 1, dto);
                    convertDataV2(item);
                }
            }
        }
    }

    private void convertUser(NLevelItem parent, int level, TreeUserDTO sub) {
        for (TreeUserDTOTemp user : mListUsers) {
            ArrayList<BelongDepartmentDTO> belongs = user.getBelongs();

            if (belongs != null) {
                for (BelongDepartmentDTO belong : belongs) {
                    if (belong.getDepartNo() == sub.getId()) {
                        TreeUserDTO treeUserDTO = new TreeUserDTO(
                                user.getName(),
                                user.getNameEN(),
                                user.getCellPhone(),
                                user.getAvatarUrl(),
                                belong.getPositionName(),
//                                belong.getDutyName(),
                                user.getType(),
                                user.getStatus(),
                                user.getUserNo(),
                                belong.getDepartNo(),
                                user.getUserStatusString()

                        );
                        try {
                            treeUserDTO.setEnabled(user.getEnabled());
                        } catch (Exception e) {
                            treeUserDTO.setEnabled(true);
                            e.printStackTrace();
                        }
                        treeUserDTO.DutyName = belong.getDutyName();
                        treeUserDTO.setCompanyNumber(user.getCompanyPhone());
                        treeUserDTO.setPhoneNumber(user.getCellPhone());
                        treeUserDTO.setDutyName(belong.getDutyName());
                        // treeUserDTO.setCompanyNumber(user.getCompanyPhone());
                        NLevelItem item = new NLevelItem(treeUserDTO, parent, level);
                        Log.d(TAG, "lstFavorite.add 13");
                        if (isAdd(item))
                            lstFavorite.add(item);
                    }
                }
            }
        }
    }

    private void init() {
        int left20dp = Utils.getDimenInPx(R.dimen.dimen_20_40);
//        Log.d(TAG, "lstFavorite:" + lstFavorite.size());
        mAdapter = new NLevelRecycleAdapter(getActivity(), lstFavorite, left20dp, mOnshowCallback);
        rvMain.setAdapter(mAdapter);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("nLevelItem", mCurrentItemContext);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            //probably orientation change
            mCurrentItemContext = (NLevelItem) savedInstanceState.getSerializable("nLevelItem");
        }
    }

    // 유저 추가 화면을 띄웁니다.(Show AddUser Activity)
    private OnGroupShowContextMenu mOnshowCallback = new OnGroupShowContextMenu() {
        @Override
        public void onShow(NLevelItem item, ArrayList<Integer> uNos) {
            mCurrentItemContext = item;

            TreeUserDTO dto = item.getObject();
            final Intent intent = new Intent(getActivity(), OrganizationFavoriteActivity.class);
            intent.putExtra(Constant.KEY_INTENT_GROUP_NO, (long) dto.getId());
            intent.putIntegerArrayListExtra(Constant.KEY_INTENT_COUNT_MEMBER, uNos);
            startActivityForResult(intent, Statics.ADD_USER_TO_FAVORITE);
        }
    };

    private NLevelItem mCurrentItemContext = null;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Statics.ADD_USER_TO_FAVORITE) {
                Log.d(TAG, "ADD_USER_TO_FAVORITE");
                Bundle args = data.getExtras();

                long groupNo = args.getLong(Constant.KEY_INTENT_GROUP_NO, 0);
                ArrayList<TreeUserDTO> selectedArr = null;

                try {
                    selectedArr = (ArrayList<TreeUserDTO>) args.getSerializable(Constant.KEY_INTENT_SELECT_USER_RESULT);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (selectedArr != null && selectedArr.size() > 0) {
                    for (TreeUserDTO u : selectedArr) {
                        insertFavoriteUser(groupNo, u.getId());
                    }

                }
            }
        }
    }

    private void insertFavoriteUser(final long groupNo, final long userNo) {
        if (mCurrentItemContext != null) {
            final TreeUserDTO dto = mCurrentItemContext.getObject();
            HttpRequest.getInstance().insertFavoriteUser(dto.getId(), userNo, new BaseHTTPCallbackWithJson() {
                @Override
                public void onHTTPSuccess(String jsonData) {
                    Log.d(TAG, "insertFavoriteUser onHTTPSuccess");
                    Type listType = new TypeToken<FavoriteUserDto>() {
                    }.getType();
                    final FavoriteUserDto userDto = new Gson().fromJson(jsonData, listType);

                    // add to local data
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (userDto.getGroupNo() == Statics.ID_GROUP) {
                                userDto.setIsTop(1);
                            }
                            FavoriteUserDBHelper.addFavoriteUser(userDto);
                        }
                    }).start();

                    // create new item to add, by find user in local data
                    TreeUserDTOTemp tempU = AllUserDBHelper.getAUser(userNo);
                  /*  TreeUserDTOTemp userDtoBelong = new TreeUserDTOTemp();
                    userDtoBelong.setBelongs(BelongsToDBHelper.getBelongs(userNo))*/
                    ;
                    if (tempU != null) {
                        TreeUserDTO newUser = new TreeUserDTO(tempU.getName(), tempU.getNameEN(), tempU.getCellPhone(), tempU.getAvatarUrl(),
                                getPositionName(tempU), 2, 1, tempU.getUserNo(), idFolder);
                        //---->Son edit
                        newUser.setCompanyNumber(tempU.getCompanyPhone());
                        newUser.setDutyName(getDutyName(tempU));
                        try {

                            newUser.setEnabled(tempU.getEnabled());
                        } catch (Exception e) {
                            newUser.setEnabled(true);
                            e.printStackTrace();
                        }
                        NLevelItem childItem = new NLevelItem(newUser, mCurrentItemContext, mCurrentItemContext.getLevel() + 1);
                        //--->end Son edit
                        int indexOf = lstFavorite.indexOf(mCurrentItemContext);
                        if (indexOf != -1) {
                            lstFavorite.add(indexOf + 1, childItem);
                            mAdapter.reloadData();
                        }
                    }
                }

                @Override
                public void onHTTPFail(ErrorDto errorDto) {
                    Log.d(TAG, "insertFavoriteUser onHTTPFail");
                }
            });
        }
    }

    private String getPositionName(TreeUserDTOTemp tempU) {
        String position = "";
        ArrayList<BelongDepartmentDTO> belongs = tempU.getBelongs();
        if (belongs != null) {
            for (BelongDepartmentDTO belong : belongs) {
                if (TextUtils.isEmpty(position)) {
                    position += belong.getPositionName();
                } else {
                    position += "," + belong.getPositionName();
                }
            }
        }
        return position;
    }

    private String getDutyName(TreeUserDTOTemp tempU) {
        String position = "";
        ArrayList<BelongDepartmentDTO> belongs = tempU.getBelongs();
        if (belongs != null) {
            for (BelongDepartmentDTO belong : belongs) {
                if (TextUtils.isEmpty(position)) {
                    position += belong.getDutyName();
                } else {
                    position += "," + belong.getDutyName();
                }
            }
        }
        return position;
    }

    // Addition
    /* Function request to server to add new favorite group */
    private void addFavoriteGroup(String groupName) {

        if (Utils.isNetworkAvailable()) {
            HttpRequest.getInstance().insertFavoriteGroup(groupName, new BaseHTTPCallbackWithJson() {
                @Override
                public void onHTTPSuccess(String jsonData) {
                    Type listType = new TypeToken<FavoriteGroupDto>() {
                    }.getType();
                    FavoriteGroupDto group = new Gson().fromJson(jsonData, listType);
                    // Add favorite group to local database
                    FavoriteGroupDBHelper.addGroup(group); // Store to data base
                    // Need to update current data set

                    // 1. Find Parent
                    // 2. Build node
                    if (favoriteRoot != null) {
                        TreeUserDTO newGroup = new TreeUserDTO(group.getName(), group.getName(), "", "", "", 1, 1, group.getGroupNo(), favoriteRoot.getObject().getId());
                        NLevelItem newItem = new NLevelItem(newGroup, favoriteRoot, favoriteRoot.getLevel() + 1);
                        Log.d(TAG, "lstFavorite.add 2");
                        if (isAdd(newItem))
                            lstFavorite.add(newItem);

                        mAdapter.reloadData();
                    }
                }

                @Override
                public void onHTTPFail(ErrorDto errorDto) {
                    // If add a new group is failed, nothing to do
                    Toast.makeText(CrewChatApplication.getInstance(), getResources().getString(R.string.insert_fail), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(CrewChatApplication.getInstance(), getResources().getString(R.string.no_connection_error), Toast.LENGTH_SHORT).show();
        }

    }
}