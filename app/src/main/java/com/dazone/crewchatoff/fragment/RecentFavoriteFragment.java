package com.dazone.crewchatoff.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.HTTPs.GetUserStatus;
import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.activity.MainActivity;
import com.dazone.crewchatoff.activity.RenameRoomActivity;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.adapter.RecentFavoriteAdapter;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.database.AllUserDBHelper;
import com.dazone.crewchatoff.database.ChatRoomDBHelper;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.CurrentChatDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.StatusDto;
import com.dazone.crewchatoff.dto.StatusItemDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallBack;
import com.dazone.crewchatoff.interfaces.OnGetCurrentChatCallBack;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RecentFavoriteFragment extends ListFragment<ChattingDto> implements OnGetCurrentChatCallBack {
    String TAG = "RecentFavoriteFragment";
    private View rootView;
    public boolean isUpdate = false;
    TextView tvNodata;
    public static RecentFavoriteFragment instance;

    private List<TreeUserDTOTemp> treeUserDTOTempList;
    private EditText etInputSearch;
    public boolean isActive = false;

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Statics.ACTION_SHOW_SEARCH_INPUT);
        filter.addAction(Statics.ACTION_HIDE_SEARCH_INPUT);
        getActivity().registerReceiver(mReceiverShowSearchInput, filter);
    }

    private void unregisterReceiver() {
        getActivity().unregisterReceiver(mReceiverShowSearchInput);
    }

    private BroadcastReceiver mReceiverShowSearchInput = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Statics.ACTION_SHOW_SEARCH_INPUT)) {
                if (etInputSearch != null) {
                    etInputSearch.setVisibility(View.VISIBLE);
                    etInputSearch.post(new Runnable() {
                        @Override
                        public void run() {
                            etInputSearch.requestFocus();
                            InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imgr.showSoftInput(etInputSearch, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
            } else if (intent.getAction().equals(Statics.ACTION_HIDE_SEARCH_INPUT)) {
                etInputSearch.setText("");
                etInputSearch.setVisibility(View.GONE);

                if (getActivity() != null) {
                    Utils.hideKeyboard(getActivity());
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHttpRequest = HttpRequest.getInstance();
        dataSet = new ArrayList<>();
        instance = this;
        registerReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (getActivity() != null) {
//            ((MainActivity) getActivity()).hideSearchIcon();
//            ((MainActivity) getActivity()).hideMenuSearch();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterReceiver();
    }

    public void removeFavorite(long roomNo) {
        for (ChattingDto chat : dataSet) {
            if (chat.getRoomNo() == roomNo) {
                dataSet.remove(chat);
                adapterList.notifyDataSetChanged();
                Log.d(TAG, "adapterList.notifyDataSetChanged 1");
                break;
            }
        }
    }

    public void addFavorite(ChattingDto dto) {
        boolean isExist = false;

        for (ChattingDto chat : dataSet) {
            if (chat.getRoomNo() == dto.getRoomNo()) {
                chat.setFavorite(true);
                adapterList.notifyDataSetChanged();
                Log.d(TAG, "adapterList.notifyDataSetChanged 2");
                isExist = true;
                break;
            }
        }

        if (!isExist) {
            if (Constant.isAddChattingDto(dto)) {
                Log.d(TAG, "add 3 ");
                dataSet.add(dto);
                rvMainList.setVisibility(View.VISIBLE);

            }

            if (dataSet.size() > 1) {
                Collections.sort(dataSet, new Comparator<ChattingDto>() {
                    public int compare(ChattingDto chattingDto1, ChattingDto chattingDto2) {
                        if (chattingDto1.getLastedMsgDate() == null || chattingDto2.getLastedMsgDate() == null) {
                            return -1;
                        }

                        return chattingDto2.getLastedMsgDate().compareToIgnoreCase(chattingDto1.getLastedMsgDate());
                    }
                });
            }
            adapterList.notifyDataSetChanged();
            Log.d(TAG, "adapterList.notifyDataSetChanged 3");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_recent_favorite, container, false);

        progressBar = (LinearLayout) rootView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "VISIBLE 1");
        rvMainList = (RecyclerView) rootView.findViewById(R.id.rv_main);

        tvNodata = (TextView) rootView.findViewById(R.id.tvNodata);
        etInputSearch = (EditText) rootView.findViewById(R.id.inputSearch);
        etInputSearch.setImeOptions(etInputSearch.getImeOptions() | EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


//        etInputSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    Log.d(TAG, "hasFocus 2");
//                } else {
//                    Log.d(TAG, "not hasFocus 2");
//                }
//            }
//        });
        etInputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged");
                if (adapterList != null) {
                    adapterList.filterRecentFavorite(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        rvMainList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvMainList.setLayoutManager(layoutManager);
        initAdapter();
        rvMainList.setAdapter(adapterList);

        initList();

        return rootView;
    }

    public void updateUnRead(int finalPos) {

        adapterList.notifyItemChanged(finalPos);
    }

    // update data after read msg
    public void updateRoomUnread(long roomNo) {

        for (ChattingDto chattingDto : dataSet) {
            if (chattingDto.getRoomNo() == roomNo) {
                chattingDto.setUnReadCount(0);
                adapterList.updateData(dataSet, dataSet.indexOf(chattingDto));
                break;
            }
        }
    }

    public void updateRoomUnread(long roomNo, int count) {

        for (ChattingDto chattingDto : dataSet) {
            if (chattingDto.getRoomNo() == roomNo) {
                chattingDto.setUnReadCount(count);
                adapterList.updateData(dataSet, dataSet.indexOf(chattingDto));
                break;
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && !isActive) {
            isActive = true;

        }

        if (isVisibleToUser) {
            if (getActivity() != null && getActivity() instanceof MainActivity) {
//                ((MainActivity) getActivity()).hidePAB();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void initAdapter() {
        adapterList = new RecentFavoriteAdapter(mContext, dataSet, rvMainList, mOnContextMenuSelect);
    }

    void showLoading(){
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
    }
    void hideLoading(){
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }
    @Override
    protected void initList() {
        showLoading();
    }

    ArrayList<TreeUserDTOTemp> listOfUsers = null;

    public void init() {
        if (CompanyFragment.instance != null) listOfUsers = CompanyFragment.instance.getUser();
        if (listOfUsers == null) listOfUsers = new ArrayList<>();
        hideLoading();
    }

    public void getData(final List<ChattingDto> lst) {
        rvMainList.setVisibility(View.VISIBLE);
        hideLoading();
        dataSet = lst;
        Log.d(TAG, "updateList from list chat:" + dataSet.size());
        adapterList.updateData(dataSet);
        if (tvNodata != null) {
            tvNodata.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onHTTPSuccess(List<CurrentChatDto> dtos) {
    }

    @Override
    public void onHTTPFail(ErrorDto errorDto) {
    }

    public interface OnContextMenuSelect {
        void onSelect(int type, Bundle bundle);
    }

    public void updateRenameRoom(int roomNo, String roomTitle) {
        for (ChattingDto a : dataSet) {
            if (roomNo == a.getRoomNo()) {
                a.setRoomTitle(roomTitle);
                adapterList.notifyDataSetChanged();
                Log.d(TAG, "adapterList.notifyDataSetChanged 4");
                Log.d(TAG, "RENAME_ROOM");
                break;
            }

        }
    }

    public void updateWhenRemoveUser(long roomNo, List<Integer> userNosRemove) {
        for (ChattingDto chattingDto : dataSet) {
            if (chattingDto.getRoomNo() == roomNo) {
                ArrayList<Integer> chattingDTOUserNos = chattingDto.getUserNos();

                for (int i = 0; i < userNosRemove.size(); i++) {
                    int value = userNosRemove.get(i);
                    for (int j = 0; j < chattingDTOUserNos.size(); j++) {
                        int value2 = chattingDTOUserNos.get(j);
                        if (value == value2) {
                            chattingDTOUserNos.remove(j);
                            break;
                        }
                    }
                }

                chattingDto.setUserNos(chattingDTOUserNos);
                Log.d(TAG, "insert 3");
                adapterList.notifyItemChanged(dataSet.indexOf(chattingDto));
                break;
            }
        }
    }

    public void updateWhenAddUser(long roomNo, ArrayList<Integer> userNosAdd) {
        for (ChattingDto chattingDto : dataSet) {
            if (chattingDto.getRoomNo() == roomNo) {
                ArrayList<Integer> chattingDTOUserNos = chattingDto.getUserNos();

                for (int i : userNosAdd) {
                    if (Constant.isAddUser(chattingDTOUserNos, i)) {
                        chattingDTOUserNos.add(i);
                    }


                    for (TreeUserDTOTemp treeUserDTOTemp : treeUserDTOTempList) {
                        if (i == treeUserDTOTemp.getUserNo()) {
                            chattingDto.getListTreeUser().add(treeUserDTOTemp);
                            break;
                        }
                    }
                }

                chattingDto.setUserNos(chattingDTOUserNos);
                Log.d(TAG, "insert 3");
                adapterList.notifyItemChanged(dataSet.indexOf(chattingDto));
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Statics.RENAME_ROOM:

                    if (data != null) {
                        final int roomNo = data.getIntExtra(Statics.ROOM_NO, 0);
                        final String roomTitle = data.getStringExtra(Statics.ROOM_TITLE);
                        // Update current chat list

                        for (ChattingDto a : dataSet) {
                            if (roomNo == a.getRoomNo()) {
                                a.setRoomTitle(roomTitle);
                                adapterList.notifyDataSetChanged();
                                Log.d(TAG, "adapterList.notifyDataSetChanged 5");
                                Log.d(TAG, "RENAME_ROOM");
                                break;
                            }

                        }
                        if (CurrentChatListFragment.fragment != null) {
                            CurrentChatListFragment.fragment.updateRenameRoom(roomNo, roomTitle);
                        }
                        // Start new thread to update local database
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ChatRoomDBHelper.updateChatRoom(roomNo, roomTitle);
                            }
                        }).start();
                    }
                    break;

            }
        }
    }

    private OnContextMenuSelect mOnContextMenuSelect = new OnContextMenuSelect() {
        @Override
        public void onSelect(int type, Bundle bundle) {
            Intent intent;
            final long roomNo = bundle.getLong(Statics.ROOM_NO, 0);

            switch (type) {
                case Statics.ROOM_RENAME:
                    intent = new Intent(getActivity(), RenameRoomActivity.class);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, Statics.RENAME_ROOM);
                    break;

                case Statics.ROOM_OPEN:
                    intent = new Intent(BaseActivity.Instance, ChattingActivity.class);
                    intent.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo);
                    startActivity(intent);
                    break;

                case Statics.ROOM_REMOVE_FROM_FAVORITE:
                    for (ChattingDto chat : dataSet) {
                        if (chat.getRoomNo() == roomNo) {
                            dataSet.remove(chat);
                            adapterList.notifyDataSetChanged();
                            Log.d(TAG, "adapterList.notifyDataSetChanged 6");
                            break;
                        }
                    }
                    break;

                case Statics.ROOM_ADD_TO_FAVORITE:
                    final Resources res = mContext.getResources();
                    HttpRequest.getInstance().addRoomToFavorite(roomNo, new BaseHTTPCallBack() {
                        @Override
                        public void onHTTPSuccess() {
                            Toast.makeText(mContext, res.getString(R.string.favorite_add_success), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onHTTPFail(ErrorDto errorDto) {
                            Toast.makeText(mContext, res.getString(R.string.favorite_add_success), Toast.LENGTH_LONG).show();
                        }
                    });
                    break;

                case Statics.ROOM_LEFT:
                    int myId = Utils.getCurrentId();
                    HttpRequest.getInstance().DeleteChatRoomUser(roomNo, myId, new BaseHTTPCallBack() {
                        @Override
                        public void onHTTPSuccess() {
                            try {
                                for (int i = 0; i < dataSet.size(); i++) {
                                    if (dataSet.get(i).getRoomNo() == roomNo) {
                                        dataSet.remove(i);
                                        adapterList.notifyDataSetChanged();
                                        Log.d(TAG, "adapterList.notifyDataSetChanged 7");
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onHTTPFail(ErrorDto errorDto) {
                        }
                    });
                    break;
            }
        }
    };

    public void updateSTTOffline() {
        Log.d(TAG, "updateSTTOffline");
        if (dataSet != null) {
            if (dataSet.size() > 0) {
                ArrayList<TreeUserDTOTemp> lst = listOfUsers;
                for (ChattingDto dto : dataSet) {
                    if (dto.getListTreeUser() != null && dto.getListTreeUser().size() > 0) {
                        if (dto.getListTreeUser().size() < 2) {
                            int userNo = dto.getListTreeUser().get(0).getUserNo();
                            String userName = dto.getListTreeUser().get(0).getName();
                            for (TreeUserDTOTemp obj : lst) {
                                int stt = obj.getStatus();
                                int uN = obj.getUserNo();
                                if (userNo == uN) {
                                    Log.d(TAG, "userNo:" + userNo + " userName:" + userName + " stt:" + stt);
                                    dto.setStatus(stt);
                                    break;
                                }
                            }
                        }
                    }
                }
                adapterList.notifyDataSetChanged();
            }
        }
    }

    interface onStatus {
        void finishStatus();
    }

    class getStatus extends AsyncTask<String, String, String> {
        onStatus callback;

        public getStatus(onStatus callback) {
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            getStatusPersonal();
            return null;
        }

        @Override
        protected void onPostExecute(String status) {
            super.onPostExecute(status);
            callback.finishStatus();

        }
    }

    void getStatusPersonal() {
        ArrayList<TreeUserDTOTemp> users = listOfUsers;
        StatusDto status = new GetUserStatus().getStatusOfUsers(new Prefs().getHOST_STATUS(), new Prefs().getCompanyNo());
        if (status != null) {
            for (TreeUserDTOTemp u : users) {
                for (StatusItemDto sItem : status.getItems()) {
                    if (sItem.getUserID().equals(u.getUserID())) {
                        AllUserDBHelper.updateStatus(u.getDBId(), sItem.getStatus());
                        break;
                    }
                }
            }
        }
    }

    public void updateSTT(ArrayList<TreeUserDTOTemp> lst) {
        if (dataSet != null) {
            if (dataSet.size() > 0) {
                for (ChattingDto dto : dataSet) {
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
                adapterList.notifyDataSetChanged();
                Log.d(TAG, "adapterList.notifyDataSetChanged 11");
            }
        }
    }
}