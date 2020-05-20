package com.dazone.crewchatoff.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dazone.crewchatoff.HTTPs.GetUserStatus;
import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.activity.MainActivity;
import com.dazone.crewchatoff.activity.RenameRoomActivity;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.adapter.CurrentChatAdapter;
import com.dazone.crewchatoff.utils.Statics;
import com.dazone.crewchatoff.database.ChatRoomDBHelper;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.CurrentChatDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.StatusDto;
import com.dazone.crewchatoff.dto.StatusItemDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallBack;
import com.dazone.crewchatoff.interfaces.OnGetChatList;
import com.dazone.crewchatoff.interfaces.OnGetCurrentChatCallBack;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CurrentChatListFragment extends ListFragment<ChattingDto> implements OnGetCurrentChatCallBack {
    public boolean isUpdate = false;
    public static CurrentChatListFragment fragment;
    private List<TreeUserDTOTemp> treeUserDTOTempList;
    public boolean isActive = false;
    private int myId;
    private ArrayList<TreeUserDTOTemp> listOfUsers = null;

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Statics.ACTION_SHOW_SEARCH_INPUT_IN_CURRENT_CHAT);
        filter.addAction(Statics.ACTION_HIDE_SEARCH_INPUT_IN_CURRENT_CHAT);
        getActivity().registerReceiver(mReceiverShowSearchInput, filter);
    }

    private BroadcastReceiver mReceiverShowSearchInput = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Statics.ACTION_SHOW_SEARCH_INPUT_IN_CURRENT_CHAT)) {
                showSearchInput();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = this;
        registerReceiver();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            getActivity().unregisterReceiver(mReceiverShowSearchInput);
        }
    }


    public void init() {
        myId = new Prefs().getUserNo();
        if (CompanyFragment.instance != null) listOfUsers = CompanyFragment.instance.getUser();
        if (listOfUsers == null) listOfUsers = new ArrayList<>();

        // If list user is not null, load data from client at first
        if (listOfUsers != null && listOfUsers.size() > 0) {
            treeUserDTOTempList = listOfUsers;
            getDataFromClient();
        }

    }

    void showLoading() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
    }

    void hideLoading() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void initList() {
        showLoading();
    }


    private void getDataFromClient() {
        dataSet.clear();
        if (Utils.isNetworkAvailable()) {
            getDataFromServer();
        } else {
            hideLoading();
            Toast.makeText(getContext(), "Please check your internet connection!", Toast.LENGTH_SHORT).show();
        }
    }

    public interface OnContextMenuSelect {
        void onSelect(int type, Bundle bundle);
    }

    private OnContextMenuSelect mOnContextMenuSelect = new OnContextMenuSelect() {
        @Override
        public void onSelect(int type, Bundle bundle) {
            Intent intent;
            final long roomNo = bundle.getInt(Statics.ROOM_NO, 0);

            switch (type) {
                case Statics.ROOM_RENAME:
                    intent = new Intent(getActivity(), RenameRoomActivity.class);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, Statics.RENAME_ROOM);

                    break;

                case Statics.ROOM_OPEN:
                    intent = new Intent(BaseActivity.Instance, ChattingActivity.class);
                    ChattingDto dto = (ChattingDto) bundle.getSerializable(Constant.KEY_INTENT_ROOM_DTO);

                    Bundle args = new Bundle();
                    args.putLong(Constant.KEY_INTENT_ROOM_NO, roomNo);
                    args.putSerializable(Constant.KEY_INTENT_CHATTING_DTO, dto);

                    intent.putExtras(args);
                    startActivity(intent);
                    break;

                case Statics.ROOM_ADD_TO_FAVORITE:
                    break;

                case Statics.ROOM_LEFT:
                    HttpRequest.getInstance().DeleteChatRoomUser(roomNo, myId, new BaseHTTPCallBack() {
                        @Override
                        public void onHTTPSuccess() {
                            try {
                                for (int i = 0; i < dataSet.size(); i++) {
                                    if (dataSet.get(i).getRoomNo() == roomNo) {
                                        if (RecentFavoriteFragment.instance != null) {
                                            if (dataSet.get(i).isFavorite()) {
                                                RecentFavoriteFragment.instance.removeFavorite(roomNo);
                                            }
                                        }

                                        dataSet.remove(i);

                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ChatRoomDBHelper.deleteChatRoom(roomNo);
                                            }
                                        }).start();

                                        adapterList.notifyDataSetChanged();
                                        break;
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

    @Override
    protected void initAdapter() {
        adapterList = new CurrentChatAdapter(mContext, dataSet, rvMainList, mOnContextMenuSelect);
    }

    public List<ChattingDto> getListData() {
        if (dataSet == null)
            dataSet = new ArrayList<>();
        return dataSet;
    }

    public void updateRenameRoom(int roomNo, String roomTitle) {
        for (ChattingDto a : dataSet) {
            if (roomNo == a.getRoomNo()) {
                a.setRoomTitle(roomTitle);
                adapterList.notifyDataSetChanged();
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

                        for (ChattingDto a : dataSet) {
                            if (roomNo == a.getRoomNo()) {
                                a.setRoomTitle(roomTitle);
                                adapterList.notifyDataSetChanged();
                                break;
                            }

                        }

                        if (RecentFavoriteFragment.instance != null) {
                            RecentFavoriteFragment.instance.updateRenameRoom(roomNo, roomTitle);
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

    @Override
    public void onHTTPSuccess(List<CurrentChatDto> dtos) {

    }

    @Override
    public void onHTTPFail(ErrorDto errorDto) {
        hideLoading();
    }

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

    public void updateDataSet(ChattingDto dto) {
        boolean isContains = false;
        for (ChattingDto chattingDto : dataSet) {
            if (chattingDto.getRoomNo() == dto.getRoomNo()) {
                chattingDto.setLastedMsg(dto.getMessage());
                chattingDto.setLastedMsgType(dto.getLastedMsgType());
                chattingDto.setLastedMsgAttachType(dto.getLastedMsgAttachType());
                chattingDto.setLastedMsgDate(dto.getLastedMsgDate());
                chattingDto.setRegDate(dto.getRegDate());

                chattingDto.setUnReadCount(dto.getUnreadTotalCount());
                chattingDto.setWriterUserNo(dto.getWriterUserNo());

                chattingDto.setRoomNo(dto.getRoomNo());
                chattingDto.setMessage(dto.getMessage());
                chattingDto.setMessageNo(dto.getMessageNo());

                chattingDto.setAttachNo(dto.getAttachNo());
                chattingDto.setAttachFileName(dto.getAttachFileName());
                chattingDto.setAttachFileType(dto.getAttachFileType());
                chattingDto.setAttachFilePath(dto.getAttachFilePath());
                chattingDto.setAttachFileSize(dto.getAttachFileSize());

                if (dto.getLastedMsgDate() != null) {
                    String time = TimeUtils.convertTimeDeviceToTimeServerDefault(dto.getLastedMsgDate());
                    chattingDto.setLastedMsgDate(time);
                } else if (dto.getRegDate() != null) {
                    String time = TimeUtils.convertTimeDeviceToTimeServerDefault(dto.getRegDate());
                    chattingDto.setLastedMsgDate(time);
                }

                isContains = true;

                break;
            }
        }

        if (!isContains) {
            HttpRequest.getInstance().GetChatList(new OnGetChatList() {
                @Override
                public void OnGetChatListSuccess(List<ChattingDto> list) {
                    dataSet.clear();
                    List<TreeUserDTOTemp> list1;
                    TreeUserDTOTemp treeUserDTOTemp1;
                    Collections.sort(list, new Comparator<ChattingDto>() {
                        public int compare(ChattingDto chattingDto1, ChattingDto chattingDto2) {
                            if (chattingDto1.getLastedMsgDate() == null || chattingDto2.getLastedMsgDate() == null) {
                                return -1;
                            }

                            return chattingDto2.getLastedMsgDate().compareToIgnoreCase(chattingDto1.getLastedMsgDate());
                        }
                    });

                    for (ChattingDto chattingDto : list) {
                        if (!Utils.checkChatId198(chattingDto)) {
                            list1 = new ArrayList<>();

                            for (int id : chattingDto.getUserNos()) {
                                if ((myId != id) || (chattingDto.getUserNos().size() == 1 && chattingDto.getUserNos().get(0) == myId && chattingDto.getRoomType() == 1)) {
                                    treeUserDTOTemp1 = Utils.GetUserFromDatabase(treeUserDTOTempList, id);
                                    if (treeUserDTOTemp1 != null) {
                                        list1.add(treeUserDTOTemp1);
                                    }
                                }
                            }

                            chattingDto.setListTreeUser(list1);
                            if (Constant.isAddChattingDto(chattingDto))
                                dataSet.add(chattingDto);
                        }
                    }
                    adapterList.notifyDataSetChanged();
                }

                @Override
                public void OnGetChatListFail(ErrorDto errorDto) {
                }
            });
        } else {
            // Sort by date
            Collections.sort(dataSet, new Comparator<ChattingDto>() {
                public int compare(ChattingDto chattingDto1, ChattingDto chattingDto2) {
                    if (chattingDto1.getLastedMsgDate() == null || chattingDto2.getLastedMsgDate() == null) {
                        return -1;
                    }

                    return chattingDto2.getLastedMsgDate().compareToIgnoreCase(chattingDto1.getLastedMsgDate());
                }
            });

            adapterList.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isActive = true;
        registerGCMReceiver();
        if (isUpdate) {
            isUpdate = false;
            adapterList.updateData(dataSet);
        }

        Prefs prefs = CrewChatApplication.getInstance().getPrefs();
        int roomNo = prefs.getRoomId();
        String roomTitle = prefs.getRoomName();
        if (roomNo > -1) {
            prefs.putRoomId(-1);
            for (ChattingDto a : dataSet) {
                if (roomNo == a.getRoomNo()) {
                    a.setRoomTitle(roomTitle);
                    adapterList.notifyDataSetChanged();
                    break;
                }
            }
            if (RecentFavoriteFragment.instance != null) {
                RecentFavoriteFragment.instance.updateRenameRoom(roomNo, roomTitle);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isActive = false;
        getActivity().unregisterReceiver(mReceiverNewAssignTask);
    }

    private void registerGCMReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Statics.ACTION_RECEIVER_NOTIFICATION);
        filter.addAction(Constant.INTENT_FILTER_GET_MESSAGE_UNREAD_COUNT);
        filter.addAction(Constant.INTENT_FILTER_ADD_USER);
        filter.addAction(Constant.INTENT_FILTER_NOTIFY_ADAPTER);
        filter.addAction(Constant.INTENT_FILTER_UPDATE_ROOM_NAME);
        getActivity().registerReceiver(mReceiverNewAssignTask, filter);
    }

    private BroadcastReceiver mReceiverNewAssignTask = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Statics.ACTION_RECEIVER_NOTIFICATION)) {
                handleDataNotification(intent);
            } else if (intent.getAction().equals(Constant.INTENT_FILTER_ADD_USER)) {
                getDataFromServer();
            } else if (intent.getAction().equals(Constant.INTENT_FILTER_NOTIFY_ADAPTER)) {
                long roomNo = intent.getLongExtra("roomNo", 0);
                int type = intent.getIntExtra("type", 0);
                int pos = 0;
                for (ChattingDto chat : dataSet) {
                    if (chat.getRoomNo() == roomNo) {
                        if (type == Constant.TYPE_ACTION_ALARM_ON) {
                            chat.setNotification(true);
                        } else if (type == Constant.TYPE_ACTION_ALARM_OFF) {
                            chat.setNotification(false);
                        } else if (type == Constant.TYPE_ACTION_FAVORITE) {
                            chat.setFavorite(false);
                        }

                        // Notify database
                        final int finalPos = pos;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (adapterList != null) {
                                    adapterList.notifyItemChanged(finalPos);
                                }
                            }
                        });

                        break;
                    }

                    // increase position
                    pos++;
                }
            } else if (intent.getAction().equals(Constant.INTENT_FILTER_UPDATE_ROOM_NAME)) {
                updateRoomRename(intent);
            }
        }
    };

    void updateRoomRename(Intent data) {
        if (data != null) {
            long roomNo = data.getLongExtra(Statics.ROOM_NO, 0);
            String roomTitle = data.getStringExtra(Statics.ROOM_TITLE);
            for (ChattingDto a : dataSet) {
                if (roomNo == a.getRoomNo()) {
                    a.setRoomTitle(roomTitle);
                    adapterList.notifyDataSetChanged();
                    break;
                }
            }

            if (RecentFavoriteFragment.instance != null) {
                RecentFavoriteFragment.instance.updateRenameRoom((int) roomNo, roomTitle);
            }
        }
    }

    public void getDataFromServer() {
        getChatList(listOfUsers);
    }

    private void getChatList(final List<TreeUserDTOTemp> listOfUsers) {
        HttpRequest.getInstance().GetChatList(new OnGetChatList() {
            @Override
            public void OnGetChatListSuccess(List<ChattingDto> list) {
                hideLoading();
                Collections.sort(list, new Comparator<ChattingDto>() {
                    public int compare(ChattingDto chattingDto1, ChattingDto chattingDto2) {
                        return chattingDto2.getLastedMsgDate().compareToIgnoreCase(chattingDto1.getLastedMsgDate());
                    }
                });

                dataSet.clear();
                List<TreeUserDTOTemp> list1;
                TreeUserDTOTemp treeUserDTOTemp1;
                // Sort before display it
                for (ChattingDto chattingDto : list) {
                    if (!Utils.checkChatId198(chattingDto)) {
                        list1 = new ArrayList<>();
                        ArrayList<Integer> cloneArr = new ArrayList<>(chattingDto.getUserNos());
                        Utils.removeArrayDuplicate(cloneArr);
                        for (int id : cloneArr) {
                            if ((myId != id) || (cloneArr.size() == 1 && cloneArr.get(0) == myId && chattingDto.getRoomType() == 1)) {
                                treeUserDTOTemp1 = Utils.GetUserFromDatabase(listOfUsers, id);
                                if (treeUserDTOTemp1 != null) {
                                    list1.add(treeUserDTOTemp1);
                                }
                            }
                        }

                        chattingDto.setListTreeUser(list1);
                        if (Constant.isAddChattingDto(chattingDto) && chattingDto.getListTreeUser() != null && chattingDto.getListTreeUser().size() > 0)
                            dataSet.add(chattingDto);
                    }
                }

                adapterList.notifyDataSetChanged();
                updateFavoriteList();
                countDataFromServer();
                updateStatus();
            }

            @Override
            public void OnGetChatListFail(ErrorDto errorDto) {
                hideLoading();
                countDataFromServer();
                updateStatus();
            }
        });
    }


    public void updateStatus() {
        if (MainActivity.instance != null) {
            int a = MainActivity.instance.currentItem();
            if (a == 0) {
                MainActivity.instance.showPAB();
            }
        }

        new getStatus(new onStatus() {
            @Override
            public void finishStatus() {
                if (dataSet != null && dataSet.size() > 0) {
                    for (ChattingDto dto : dataSet) {
                        if (dto.getListTreeUser() != null && dto.getListTreeUser().size() > 0 && dto.getListTreeUser().size() < 2) {
                            int userNo = dto.getListTreeUser().get(0).getUserNo();
                            for (TreeUserDTOTemp obj : listOfUsers) {
                                int stt = obj.getStatus();
                                int uN = obj.getUserNo();
                                if (userNo == uN) {
                                    dto.setStatus(stt);
                                    break;
                                }
                            }
                        }
                    }
                    adapterList.notifyDataSetChanged();
                    if (RecentFavoriteFragment.instance != null) {
                        RecentFavoriteFragment.instance.updateSTT(listOfUsers);
                    }
                }
                if (CompanyFragment.instance != null) {
                    CompanyFragment.instance.updateListStatus(listOfUsers);
                }
            }
        }).execute();
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
        StatusDto status = new GetUserStatus().getStatusOfUsers(new Prefs().getHOST_STATUS(), new Prefs().getCompanyNo());
        if (status != null) {
            for (TreeUserDTOTemp u : listOfUsers) {
                for (StatusItemDto sItem : status.getItems()) {
                    if (sItem.getUserID().equals(u.getUserID())) {
                        u.setStatus(sItem.getStatus());
                        break;
                    }
                }
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
                adapterList.notifyItemChanged(dataSet.indexOf(chattingDto));
                break;
            }
        }
    }

    private void countDataFromServer() {
        if (dataSet != null && dataSet.size() > 0) {
            rvMainList.setVisibility(View.VISIBLE);
            no_item_found.setVisibility(View.GONE);
        } else {
            rvMainList.setVisibility(View.GONE);
            no_item_found.setVisibility(View.VISIBLE);
            no_item_found.setText(getResources().getString(R.string.no_data));
        }
    }

    public void updateData(ChattingDto dto, boolean isAddUnread) {
        boolean isContains = false;
        for (ChattingDto chattingDto : dataSet) {
            if (chattingDto.getRoomNo() == dto.getRoomNo()) {
                chattingDto.setLastedMsg(dto.getMessage());
                chattingDto.setLastedMsgType(dto.getLastedMsgType());
                chattingDto.setLastedMsgAttachType(dto.getLastedMsgAttachType());
                chattingDto.setAttachFileName(dto.getAttachFileName());
                chattingDto.setLastedMsgDate(dto.getRegDate());
                isContains = true;
                break;
            }
        }

        if (!isContains) {
            HttpRequest.getInstance().GetChatList(new OnGetChatList() {
                @Override
                public void OnGetChatListSuccess(List<ChattingDto> list) {
                    dataSet.clear();
                    List<TreeUserDTOTemp> list1;
                    TreeUserDTOTemp treeUserDTOTemp1;
                    for (ChattingDto chattingDto : list) {
                        if (!Utils.checkChatId198(chattingDto)) {
                            list1 = new ArrayList<>();

                            for (int id : chattingDto.getUserNos()) {
                                if ((myId != id) || (chattingDto.getUserNos().size() == 1 && chattingDto.getUserNos().get(0) == myId && chattingDto.getRoomType() == 1)) {
                                    treeUserDTOTemp1 = Utils.GetUserFromDatabase(treeUserDTOTempList, id);

                                    if (treeUserDTOTemp1 != null) {
                                        list1.add(treeUserDTOTemp1);
                                    }
                                }
                            }

                            chattingDto.setListTreeUser(list1);
                            if (Constant.isAddChattingDto(chattingDto))
                                dataSet.add(chattingDto);
                        }
                    }

                    if (dataSet != null && dataSet.size() > 0) {
                        adapterList.updateData(dataSet);
                    }

                    countDataFromServer();
                }

                @Override
                public void OnGetChatListFail(ErrorDto errorDto) {
                    countDataFromServer();
                }
            });
        } else {
            Collections.sort(dataSet, new Comparator<ChattingDto>() {
                public int compare(ChattingDto chattingDto1, ChattingDto chattingDto2) {
                    if (chattingDto1.getLastedMsgDate() == null || chattingDto2.getLastedMsgDate() == null) {
                        return -1;
                    }

                    return chattingDto2.getLastedMsgDate().compareToIgnoreCase(chattingDto1.getLastedMsgDate());
                }
            });


            adapterList.updateData(dataSet);

            updateFavoriteList();

        }
    }

    public void updateFavoriteList() {
        if (RecentFavoriteFragment.instance != null && dataSet != null && dataSet.size() > 0) {
            List<ChattingDto> lst = new ArrayList<>();
            for (ChattingDto obj : dataSet) {
                if (obj.isFavorite()) {
                    try {
                        lst.add((ChattingDto) obj.clone());
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
            }

            RecentFavoriteFragment.instance.getData(lst);
        }
    }

    private void handleDataNotification(Intent intent) {
        try {
            ChattingDto dto = new Gson().fromJson(intent.getStringExtra(Statics.GCM_DATA_NOTIFICATOON), ChattingDto.class);
            updateDataSet(dto);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}