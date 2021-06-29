package com.dazone.crewchatoff.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import com.dazone.crewchatoff.HTTPs.GetUserStatus;
import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.activity.MainActivity;
import com.dazone.crewchatoff.activity.RenameRoomActivity;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.adapter.CurrentChatAdapter;
import com.dazone.crewchatoff.constant.Constants;
import com.dazone.crewchatoff.constant.Statics;
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
import java.util.Date;
import java.util.List;


public class CurrentChatListFragment extends ListFragment<ChattingDto> implements OnGetCurrentChatCallBack {
    public boolean isUpdate = false;
    public static CurrentChatListFragment fragment;
    private List<TreeUserDTOTemp> treeUserDTOTempList;
    public boolean isActive = false;
    private int myId;
    public boolean isFirstTime = true;

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Statics.ACTION_SHOW_SEARCH_INPUT_IN_CURRENT_CHAT);
        filter.addAction(Statics.ACTION_HIDE_SEARCH_INPUT_IN_CURRENT_CHAT);

        IntentFilter filterScreen = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        getActivity().registerReceiver(mScreenReceiver, filterScreen);

        getActivity().registerReceiver(mReceiverShowSearchInput, filter);
    }

    private boolean isScreenOff;
    private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                isScreenOff = true;
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                isScreenOff = false;
            }
        }
    };

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
            getActivity().unregisterReceiver(mScreenReceiver);
        }
    }

    ArrayList<TreeUserDTOTemp> listOfUsers = null;

    public void init() {
        myId = new Prefs().getUserNo();
        if (CompanyFragment.instance != null) listOfUsers = CompanyFragment.instance.getUser();
        if (listOfUsers == null) listOfUsers = new ArrayList<>();

        if (listOfUsers != null && listOfUsers.size() > 0) {
            treeUserDTOTempList = listOfUsers;
            getDataFromClient(listOfUsers);
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

    private void getDataFromClient(List<TreeUserDTOTemp> listOfUsers) {
        dataSet.clear();
        List<TreeUserDTOTemp> list1;
        TreeUserDTOTemp treeUserDTOTemp1;
        ArrayList<ChattingDto> listChat = ChatRoomDBHelper.getChatRooms();
        Collections.sort(listChat, (o1, o2) -> {
            Date date1 = new Date(TimeUtils.getTime(o1.getLastedMsgDate()));
            Date date2 = new Date(TimeUtils.getTime(o2.getLastedMsgDate()));
            return date2.compareTo(date1);
        });

        for (ChattingDto chattingDto : listChat) {
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

        if (dataSet != null && dataSet.size() > 0) {
            countDataFromServer(true);
            adapterList.notifyDataSetChanged();
            updateFavoriteList();
        }

        if (Utils.isNetworkAvailable()) {
            getDataFromServer();
        } else {
            hideLoading();
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

                                        new Thread(() -> ChatRoomDBHelper.deleteChatRoom(roomNo)).start();

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
                                Log.d(TAG, "adapterList.notifyDataSetChanged 7");
                                Log.d(TAG, "RENAME_ROOM");
                                break;
                            }

                        }

                        if (RecentFavoriteFragment.instance != null) {
                            RecentFavoriteFragment.instance.updateRenameRoom(roomNo, roomTitle);
                        }
                        // Start new thread to update local database
                        new Thread(() -> ChatRoomDBHelper.updateChatRoom(roomNo, roomTitle)).start();
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

    public void reloadDataSet() {
        getDataFromServer();
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
                    Collections.sort(list, (o1, o2) -> {
                        Date date1 = new Date(TimeUtils.getTime(o1.getLastedMsgDate()));
                        Date date2 = new Date(TimeUtils.getTime(o2.getLastedMsgDate()));
                        return date2.compareTo(date1);
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
            Collections.sort(dataSet, (o1, o2) -> {
                Date date1 = new Date(TimeUtils.getTime(o1.getLastedMsgDate()));
                Date date2 = new Date(TimeUtils.getTime(o2.getLastedMsgDate()));
                return date2.compareTo(date1);
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

        if (listOfUsers != null) {
            getChatList(listOfUsers);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isActive = false;
        unregisterGCMReceiver();
    }

    public boolean active() {
        return isActive;
    }

    public void updateReadMsgWhenOnPause(long KEY_INTENT_ROOM_NO, int KEY_INTENT_UNREAD_TOTAL_COUNT, long KEY_INTENT_USER_NO) {
        long roomNo = KEY_INTENT_ROOM_NO;
        int unreadCount = KEY_INTENT_UNREAD_TOTAL_COUNT;
        long userNo = KEY_INTENT_USER_NO;

        // update roomNo and total unread count
        int pos = 0;
        for (ChattingDto dto : dataSet) {
            if (dto.getRoomNo() == roomNo) {
                Log.d(TAG, "unreadCount: " + unreadCount);
                dto.setUnreadTotalCount(unreadCount);
                // fix unread total count
                if (userNo != 0) {
                    Log.d(TAG, "setUnReadCount 3");
                    dto.setUnReadCount(0);
                }
                final int finalPos = pos;
                getActivity().runOnUiThread(() -> {
                    Log.d(TAG, "update unread");
                    adapterList.notifyItemChanged(finalPos);
                });
                break;
            }
            pos++;
        }
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

    private void unregisterGCMReceiver() {
        getActivity().unregisterReceiver(mReceiverNewAssignTask);
    }

    private BroadcastReceiver mReceiverNewAssignTask = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Statics.ACTION_RECEIVER_NOTIFICATION)) {
                handlerReceiverNotification(intent);
            } else if (intent.getAction().equals(Constant.INTENT_FILTER_ADD_USER)) {
                getDataFromServer();
            } else if (intent.getAction().equals(Constant.INTENT_FILTER_NOTIFY_ADAPTER)) {
                long roomNo = intent.getLongExtra("roomNo", 0);
                int type = intent.getIntExtra("type", 0);
                // Search roomNo
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
                        getActivity().runOnUiThread(() -> {
                            if (adapterList != null) {
                                adapterList.notifyItemChanged(finalPos);
                            }
                        });

                        break;
                    }

                    // increase position
                    pos++;
                }
            } else if (intent.getAction().equals(Constant.INTENT_FILTER_GET_MESSAGE_UNREAD_COUNT)) {
                long roomNo = intent.getLongExtra(Constant.KEY_INTENT_ROOM_NO, 0);
                int unReadTotalCount = intent.getIntExtra(Constant.KEY_INTENT_UNREAD_TOTAL_COUNT, 0);
                updateUnReadCount(roomNo, unReadTotalCount);
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
                    Log.d(TAG, "adapterList.notifyDataSetChanged 9");
                    break;
                }
            }
            if (RecentFavoriteFragment.instance != null) {
                RecentFavoriteFragment.instance.updateRenameRoom((int) roomNo, roomTitle);
            }
        }
    }

    private void storeListChatRoomToLocal(final ChattingDto data) {
        new Thread(() -> ChatRoomDBHelper.addChatRoom(data)).start();
    }

    public void getDataFromServer() {
        getChatList(listOfUsers);
    }

    private void getChatList(final List<TreeUserDTOTemp> listOfUsers) {
        HttpRequest.getInstance().GetChatList(new OnGetChatList() {
            @Override
            public void OnGetChatListSuccess(List<ChattingDto> list) {
                hideLoading();
                List<ChattingDto> listChat = ChatRoomDBHelper.getChatRooms();
                int localSize = listChat.size();
                int severSize = list.size();
                Collections.sort(list, (o1, o2) -> {
                    Date date1 = new Date(TimeUtils.getTime(o1.getLastedMsgDate()));
                    Date date2 = new Date(TimeUtils.getTime(o2.getLastedMsgDate()));
                    return date2.compareTo(date1);
                });

                isFirstTime = false;
                if (localSize != severSize) {
                    ChatRoomDBHelper.clearChatRooms();
                    dataSet.clear();
                    adapterList.notifyDataSetChanged();
                    List<TreeUserDTOTemp> list1;
                    TreeUserDTOTemp treeUserDTOTemp1;
                    for (ChattingDto chattingDto : list) {
                        if (!Utils.checkChatId198(chattingDto)) {
                            list1 = new ArrayList<>();
                            // remove duplicate user
                            ArrayList<Integer> cloneArr = new ArrayList<>(chattingDto.getUserNos());
                            Utils.removeArrayDuplicate(cloneArr);
                            for (int id : cloneArr) {
                                if ((myId != id) || (cloneArr.size() == 1 && cloneArr.get(0) == myId && chattingDto.getRoomType() == 1)) {
                                    treeUserDTOTemp1 = Utils.GetUserFromDatabase(listOfUsers, id);
                                    if (treeUserDTOTemp1 != null) {
                                        list1.add(treeUserDTOTemp1);
                                    }
                                } else {

                                }
                            }
                            chattingDto.setListTreeUser(list1);
                            if (Constant.isAddChattingDto(chattingDto) && chattingDto.getListTreeUser() != null && chattingDto.getListTreeUser().size() > 0)
                                dataSet.add(chattingDto);

                            // store data to local database
                            storeListChatRoomToLocal(chattingDto);
                        }
                    }

                    if (dataSet != null && dataSet.size() > 0) {
                        adapterList.notifyDataSetChanged();
                    }
                } else {
                    for (ChattingDto dto : list) {
                        dto.setUnreadTotalCount(dto.getUnReadCount());
                        ChatRoomDBHelper.updateChatRoom(dto.getRoomNo(), dto.getLastedMsg(), dto.getLastedMsgType(), dto.getLastedMsgAttachType(),
                                dto.getLastedMsgDate(), dto.getUnreadTotalCount(), dto.getUnReadCount(), dto.getMsgUserNo(), dto.isFavorite(), dto.getUserNos());
                        for (ChattingDto chat : dataSet) {
                            if (chat.getRoomNo() == dto.getRoomNo()) {
                                chat.setLastedMsg(dto.getLastedMsg());
                                chat.setRoomTitle(dto.getRoomTitle());
                                chat.setLastedMsgType(dto.getLastedMsgType());
                                chat.setLastedMsgAttachType(dto.getLastedMsgAttachType());
                                chat.setAttachFileName(dto.getAttachFileName());
                                chat.setLastedMsgDate(dto.getLastedMsgDate());
                                chat.setMsgUserNo(dto.getMsgUserNo());
                                chat.setUnreadTotalCount(dto.getUnreadTotalCount());
                                chat.setUnReadCount(dto.getUnReadCount());
                                chat.setWriterUserNo(dto.getWriterUserNo());
                                chat.setMsgUserNo(dto.getMsgUserNo());
                                chat.setUserNos(dto.getUserNos());
                                chat.setFavorite(dto.isFavorite());
                                break;
                            }
                        }
                    }

                    Collections.sort(dataSet, (o1, o2) -> {
                        Date date1 = new Date(TimeUtils.getTime(o1.getLastedMsgDate()));
                        Date date2 = new Date(TimeUtils.getTime(o2.getLastedMsgDate()));
                        return date2.compareTo(date1);
                    });

                    adapterList.notifyDataSetChanged();
                }

                getUserStatus();
                updateFavoriteList();
                countDataFromServer(true);
                updateStatus();
            }

            @Override
            public void OnGetChatListFail(ErrorDto errorDto) {
                hideLoading();
                countDataFromServer(true);
                updateStatus();
            }
        });
    }


    public void updateStatus() {
        if (dataSet == null || dataSet.size() == 0) {
            showLnNodata();
        } else {
            hideLnNodata();
        }
        if (MainActivity.instance != null) {
            int a = MainActivity.instance.currentItem();
            if (a == 0) {
                MainActivity.instance.showPAB();
            }
        }
    }

    public void getUserStatus() {
        new getStatus(() -> {
            if (dataSet != null && dataSet.size() > 0) {
                for (ChattingDto dto : dataSet) {
                    if (dto.getListTreeUser() != null && dto.getListTreeUser().size() == 1) {
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

                rvMainList.postDelayed(() -> adapterList.notifyDataSetChanged(), 100);


                if (RecentFavoriteFragment.instance != null) {
                    RecentFavoriteFragment.instance.updateSTT(listOfUsers);
                }
            }

            if (CompanyFragment.instance != null) {
                CompanyFragment.instance.updateListStatus(listOfUsers);
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

    private StatusDto status;

    void getStatusPersonal() {
        status = new GetUserStatus().getStatusOfUsers(new Prefs().getHOST_STATUS(), new Prefs().getCompanyNo());
        if (status != null && listOfUsers != null) {
            for (TreeUserDTOTemp u : listOfUsers) {
                u.setStatus(0);
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

    private void countDataFromServer(boolean isHaveData) {
        if (isHaveData) {
            rvMainList.setVisibility(View.VISIBLE);
            no_item_found.setVisibility(View.GONE);
        } else {
            rvMainList.setVisibility(View.GONE);
            no_item_found.setVisibility(View.VISIBLE);
            no_item_found.setText(getResources().getString(R.string.no_data));
        }
    }

    public void updateData(ChattingDto dto) {
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
                        countDataFromServer(true);
                        adapterList.updateData(dataSet);
                    } else {
                        countDataFromServer(false);
                    }
                }

                @Override
                public void OnGetChatListFail(ErrorDto errorDto) {
                    if (dataSet != null && dataSet.size() > 0) {
                        countDataFromServer(true);
                    } else {
                        countDataFromServer(false);
                    }

                }
            });
        } else {
            Collections.sort(dataSet, (o1, o2) -> {
                Date date1 = new Date(TimeUtils.getTime(o1.getLastedMsgDate()));
                Date date2 = new Date(TimeUtils.getTime(o2.getLastedMsgDate()));
                return date2.compareTo(date1);
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

    private void handlerReceiverNotification(Intent intent) {
        try {
            ChattingDto dto = new Gson().fromJson(intent.getStringExtra(Statics.GCM_DATA_NOTIFICATOON), ChattingDto.class);
            updateDataSet(dto);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateUnReadCount(long roomNo, int unReadCount) {
        for (ChattingDto a : dataSet) {
            if (roomNo == a.getRoomNo()) {
                a.setUnReadCount(unReadCount);
                adapterList.notifyItemChanged(dataSet.indexOf(a));
                break;
            }
        }
    }
}