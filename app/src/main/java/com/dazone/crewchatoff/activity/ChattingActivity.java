package com.dazone.crewchatoff.activity;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.base.BaseSingleStatusActivity;
import com.dazone.crewchatoff.constant.Config;
import com.dazone.crewchatoff.constant.Constants;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.database.AllUserDBHelper;
import com.dazone.crewchatoff.database.ChatMessageDBHelper;
import com.dazone.crewchatoff.database.ChatRoomDBHelper;
import com.dazone.crewchatoff.dto.AttachDTO;
import com.dazone.crewchatoff.dto.ChatRoomDTO;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.dto.UserDto;
import com.dazone.crewchatoff.fragment.ChattingFragment;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.fragment.CurrentChatListFragment;
import com.dazone.crewchatoff.fragment.RecentFavoriteFragment;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallBack;
import com.dazone.crewchatoff.interfaces.OnGetChatRoom;
import com.dazone.crewchatoff.libGallery.MediaChooser;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.Utils;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.core.ContactPickerActivity;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.dazone.crewchatoff.constant.Statics.CHATTING_VIEW_TYPE_SELECT_VIDEO;

public class ChattingActivity extends BaseSingleStatusActivity implements View.OnClickListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    public static void toActivity(Context context, long roomNo, long myId, ChattingDto tempDto, String typeShare, ArrayList<String> mSelectedImage) {
        Intent intent = new Intent(context, ChattingActivity.class);
        Bundle args = new Bundle();
        args.putLong(Constant.KEY_INTENT_ROOM_NO, roomNo);
        args.putLong(Constant.KEY_INTENT_USER_NO, myId);
        args.putSerializable(Constant.KEY_INTENT_ROOM_DTO, tempDto);
        if (typeShare != null && mSelectedImage != null && mSelectedImage.size() > 0) {
            args.putString(Constants.TYPE_SHARE, typeShare);
            args.putSerializable(Constants.LIST_FILE_PATH_SHARE, mSelectedImage);
        }

        intent.putExtras(args);
        context.startActivity(intent);
    }

    private String TAG = "ChattingActivity";
    private ChattingFragment fragment;
    private ArrayList<TreeUserDTOTemp> treeUserDTOTempArrayList = null;
    public static Uri uri = null;
    private long roomNo;
    public static ArrayList<Integer> userNos;
    private boolean isOne = false;
    private boolean isShow = true;
    private String title;
    private String roomTitle = "";
    private long myId;
    public static Uri videoPath = null;
    private ChattingDto mDto = null;
    public static ChattingActivity instance = null;
    int IV_STATUS = -1;
    private ArrayList<String> mSelectedImage = new ArrayList<>();
    private String typeShare;
    public boolean isChoseFile = false;

    public void removeUserList(int userId) {
        if (userNos != null) {
            if (userNos.size() > 0) {
                for (int i = 0; i < userNos.size(); i++) {
                    if (userId == userNos.get(i)) {
                        userNos.remove(i);
                    }
                }
            }
        }
    }

    public void updateSTT() {
        String subtitle = "";
        try {
            subtitle = CrewChatApplication.getInstance().getResources().getString(R.string.room_info_participant_count, String.valueOf(userNos.size()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (subtitle.length() > 0) {
            setStatus(subtitle);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (CompanyFragment.instance != null)
            treeUserDTOTempArrayList = CompanyFragment.instance.getUser();
        if (treeUserDTOTempArrayList == null || treeUserDTOTempArrayList.size() == 0)
            treeUserDTOTempArrayList = AllUserDBHelper.getUser_v2();
        if (treeUserDTOTempArrayList == null)
            treeUserDTOTempArrayList = new ArrayList<>();

        /** ADD OnClick Menu */
        ivCall.setOnClickListener(this);
        ivMore.setOnClickListener(this);
        ivSearch.setOnClickListener(this);
        hideCall();
        setupSearchView();

        IntentFilter imageIntentFilter = new IntentFilter(MediaChooser.IMAGE_SELECTED_ACTION_FROM_MEDIA_CHOOSER);
        imageIntentFilter.addAction(MediaChooser.VIDEO_SELECTED_ACTION_FROM_MEDIA_CHOOSER);
        registerReceiver(imageBroadcastReceiver, imageIntentFilter);

        receiveData();
        if (Utils.isNetworkAvailable()) {
            getChatRoomInfo();
        }

        // Set local database for current room, may be launch on new thread
        if (mDto != null) {
            roomTitle = mDto.getRoomTitle();
            userNos = Constant.removeDuplicatePosition(mDto.getUserNos());
            boolean isExistMe = false;

            for (int u = 0; u < userNos.size(); u++) {
                if (userNos.get(u) == myId) {
                    if (!isExistMe) {
                        isExistMe = true;
                    } else {
                        userNos.remove(u);
                    }
                }
            }

            isOne = userNos.size() == 2;
            String subTitle = "";

            if (isOne) { // Get user status
                int userId = 0;
                try {
                    userId = (userNos.get(0) != myId) ? userNos.get(0) : userNos.get(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String userStatus = AllUserDBHelper.getAUserStatus(userId);
                if (userStatus != null && userStatus.length() > 0) {
                    subTitle = userStatus;
                }
            } else { // set default title
                int roomSize = 0;
                if (mDto.getUserNos() != null) {
                    roomSize = userNos.size();
                }
                subTitle = CrewChatApplication.getInstance().getResources().getString(R.string.room_info_participant_count, String.valueOf(roomSize));
            }
            setupTitleRoom(mDto.getUserNos(), roomTitle, subTitle);
        }

        if (!isFinishing()) {
            addFragment();
        }
    }

    /**
     * RECEIVE DATA FROM INTENT
     */
    private void receiveData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            try {
                roomNo = bundle.getLong(Constant.KEY_INTENT_ROOM_NO, 0);
                myId = bundle.getLong(Constant.KEY_INTENT_USER_NO, 0);

                if (myId == 0) {
                    myId = Utils.getCurrentId();
                }

                mDto = (ChattingDto) bundle.getSerializable(Constant.KEY_INTENT_ROOM_DTO);

                IV_STATUS = bundle.getInt(Statics.IV_STATUS, -1);

                typeShare = bundle.getString(Constants.TYPE_SHARE);
                mSelectedImage = (ArrayList<String>) bundle.getSerializable(Constants.LIST_FILE_PATH_SHARE);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean hasSendActionShare = false;

    public void setUpActioneSend() {
        if (typeShare != null && mSelectedImage.size() > 0 && !hasSendActionShare) {
            hasSendActionShare = true;
            handleActionSend(typeShare, mSelectedImage);
            typeShare = null;
            mSelectedImage.clear();
            MainActivity.imageUri = null;
            MainActivity.type = null;
            MainActivity.mSelectedImage.clear();
        }
    }

    /**
     * GET CHAT ROOM INFO
     * 채팅방 정보를 가져옵니다.
     */
    private void getChatRoomInfo() {
        HttpRequest.getInstance().GetChatRoom(roomNo, new OnGetChatRoom() {
            @Override
            public void OnGetChatRoomSuccess(ChatRoomDTO chatRoomDTO) {
                userNos = Constant.removeDuplicatePosition(chatRoomDTO.getUserNos());
                boolean isExistMe = false;
                for (int u = 0; u < userNos.size(); u++) {
                    if (userNos.get(u) == myId) {
                        if (!isExistMe) {
                            isExistMe = true;
                        } else {
                            userNos.remove(u);
                        }
                    }
                }

                isOne = userNos.size() == 2;
                String subTitle = "";

                if (isOne) { // Get user status
                    int userId = 0;

                    try {
                        userId = (userNos.get(0) != myId) ? userNos.get(0) : userNos.get(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String userStatus = AllUserDBHelper.getAUserStatus(userId);

                    if (userStatus != null && userStatus.length() > 0) {
                        subTitle = userStatus;
                    }
                } else { // set default title
                    int roomSize = 0;

                    if (chatRoomDTO.getUserNos() != null) {
                        roomSize = userNos.size();
                    }

                    subTitle = CrewChatApplication.getInstance().getResources().getString(R.string.room_info_participant_count, String.valueOf(roomSize));
                }

                setupTitleRoom(chatRoomDTO.getUserNos(), chatRoomDTO.getRoomTitle(), subTitle);

                // May be update unread count
                if (CurrentChatListFragment.fragment != null) {
                    CurrentChatListFragment.fragment.updateRoomUnread(roomNo, chatRoomDTO.getUnReadCount());
                }
                if (RecentFavoriteFragment.instance != null) {
                    RecentFavoriteFragment.instance.updateRoomUnread(roomNo, chatRoomDTO.getUnReadCount());
                }
            }

            @Override
            public void OnGetChatRoomFail(ErrorDto errorDto) {
            }
        });
    }

    public void updateRoomName(String title) {
        setTitle(title);
        roomTitle = title;
    }

    private void setupTitleRoom(ArrayList<Integer> userNos, String roomTitle, String status) {
        if (mDto != null) {
            if (mDto.getRoomType() != 1) {
                if (userNos.size() == 2) {
                    showIvStt(Constant.getSTT(mDto.getStatus()));
                }
            }
        } else {
            if (userNos.size() == 2) {
                showIvStt(Constant.getSTT(IV_STATUS));
            }
        }

        title = roomTitle;

        if (title != null && TextUtils.isEmpty(title.trim())) {
            title = getGroupTitleName(userNos);
        }
        this.roomTitle = title;
        setTitle(title);
        setStatus(status);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            roomNo = bundle.getLong(Constant.KEY_INTENT_ROOM_NO, 0);
            getChatRoomInfo();
            fragment = new ChattingFragment().newInstance(roomNo, userNos, this);
            Utils.addFragmentToActivity(getSupportFragmentManager(), fragment, R.id.content_base_single_activity, false, fragment.getClass().getSimpleName());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
        this.unregisterReceiver(imageBroadcastReceiver);
        ChattingFragment.instance = null;
    }

    private void addFragment() {
        fragment = new ChattingFragment().newInstance(roomNo, userNos, this);
        Utils.addFragmentToActivity(getSupportFragmentManager(), fragment, R.id.content_base_single_activity, false, fragment.getClass().getSimpleName());
    }

    @Override
    protected void addFragment(Bundle bundle) {
    }

    private String getGroupTitleName(ArrayList<Integer> userNos) {
        boolean flag = false;
        String result = "";
        for (int i : userNos) {
            if (i != myId || flag) {
                for (TreeUserDTOTemp treeUserDTOTemp : treeUserDTOTempArrayList) {
                    if (i == treeUserDTOTemp.getUserNo()) {
                        result += treeUserDTOTemp.getName() + ",";
                        break;
                    }
                }
            }
        }

        if (result.length() == 0) {
            if (userNos.size() == 1) {
                if (userNos.get(0) == myId) {
                    if (mDto == null) {
                        result = Constant.getUserName(treeUserDTOTempArrayList, (int) myId);
                        if (result.length() > 0)
                            result += ",";
                    } else {
                        if (mDto.getRoomType() == 1) {
                            result = Constant.getUserName(treeUserDTOTempArrayList, (int) myId);
                            if (result.length() > 0)
                                result += ",";
                        }
                    }
                }
            }
        }

        if (TextUtils.isEmpty(result.trim())) {
            return "Unknown";
        }

        return result.substring(0, result.length() - 1);
    }

    public void activityResultAddUser(Intent data) {
        try {
            Bundle bc = data.getExtras();
            if (bc != null) {
                ArrayList<Integer> userNosAdded = bc.getIntegerArrayList(Constant.KEY_INTENT_USER_NO_ARRAY);
                ArrayList<Integer> lstNew = new ArrayList<>();
                if (userNosAdded != null) {
                    for (int i : userNosAdded) {
                        if (Constant.isAddUser(userNos, i)) {
                            userNos.add(i);
                            lstNew.add(i);
                        }
                    }
                    setTitle(getGroupTitleName(userNos));

                    if (CurrentChatListFragment.fragment != null) {
                        CurrentChatListFragment.fragment.updateWhenAddUser(roomNo, lstNew);
                    }
                    if (RecentFavoriteFragment.instance != null) {
                        RecentFavoriteFragment.instance.updateWhenAddUser(roomNo, lstNew);
                    }
                    updateSTT();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (fragment == null) {
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            fragment.view.selection_lnl.setVisibility(View.GONE);
            switch (requestCode) {
                case Statics.ADD_USER_SELECT:
                    activityResultAddUser(data);
                    break;

                case Statics.IMAGE_ROTATE_CODE:
                    handleImageRotate(data, null);
                    break;

                case Statics.CAMERA_CAPTURE_IMAGE_REQUEST_CODE:
                    handleCameraCapture();
                    break;

                case Statics.CAMERA_VIDEO_REQUEST_CODE:
                    handleVideoRecoder(data);
                    break;

                case Statics.VIDEO_PICKER_SELECT:
                    handleVideoSelected(data, null);
                    break;

                case Statics.FILE_PICKER_SELECT:
                    handleFileSelected(data, null);
                    break;

                case Statics.CONTACT_PICKER_SELECT:
                    handleContactSelected(data);
                    break;

                case Statics.RENAME_ROOM:
                    handleRenameRoom(data);
                    break;
            }
        } else if (resultCode == Constant.INTENT_RESULT_CREATE_NEW_ROOM) {
            if (data.getExtras() != null) {
                newRoom(data.getExtras());
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    public void newRoom(Bundle bc) {
        ChattingDto chattingDto = (ChattingDto) bc.getSerializable(Constant.KEY_INTENT_CHATTING_DTO);
        Intent intent = new Intent(this, ChattingActivity.class);
        intent.putExtra(Statics.CHATTING_DTO, chattingDto);
        intent.putExtra(Constant.KEY_INTENT_ROOM_NO, chattingDto.getRoomNo());
        intent.putExtra(Constant.KEY_INTENT_ROOM_TITLE, bc.getStringArrayList(Constant.KEY_INTENT_ROOM_TITLE));
        startActivity(intent);
        finish();
    }

    public static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), Constant.pathDownload);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat(Statics.DATE_FORMAT_PICTURE, Locale.getDefault()).format(new Date());

        File mediaFile;

        if (type == Statics.MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + Utils.getString(R.string.pre_file_name) + timeStamp + Statics.IMAGE_JPG);
        } else if (type == Statics.MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath(), File.separator
                    + Utils.getString(R.string.pre_file_name) + timeStamp + Statics.VIDEO_MP4);
        } else {
            return null;
        }

        return mediaFile;
    }

    public static void setOutputMediaFileUri_v7(Uri u) {
        uri = u;
    }

    //Get uri from captured
    public static Uri getOutputMediaFileUri(int type) {
        uri = Uri.fromFile(getOutputMediaFile(type));
        return uri;
    }

    private final BroadcastReceiver imageBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (fragment != null) {
                if (fragment.view != null && fragment.view.selection_lnl.getVisibility() == View.VISIBLE) {
                    fragment.view.selection_lnl.setVisibility(View.GONE);
                }
            }

            List<String> listFilePath = intent.getStringArrayListExtra("list");
            long diffTime = 0;
            if (listFilePath != null && listFilePath.size() > 0) {
                for (int i = 0; i < listFilePath.size(); i++) {
                    String path = listFilePath.get(i);
                    ChattingDto chattingDto = new ChattingDto();
                    diffTime += Config.TIME_WAIT * i;

                    chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELECT_IMAGE);
                    chattingDto.setAttachFilePath(path);
                    chattingDto.setRoomNo(chattingDto.getRoomNo());
                    chattingDto.setRegDate(Utils.getTimeNewChat(diffTime));
                    chattingDto.setStrRegDate(Utils.getTimeFormat(CrewChatApplication.getInstance().getTimeLocal() + diffTime));
                    chattingDto.setLastedMsgType(Statics.MESSAGE_TYPE_ATTACH);
                    chattingDto.setLastedMsgAttachType(Statics.ATTACH_IMAGE);
                    chattingDto.setUnReadCount(ChattingActivity.userNos.size() - 1);
                    chattingDto.setPositionUploadImage(new Random().nextInt(1000));

                    ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);

                    try {
                        Thread.sleep(diffTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backToListChat();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        backToListChat();
    }

    private void backToListChat() {
        if (ChattingFragment.instance != null) {
            int i = ChattingFragment.instance.checkBack();
            if (i != 0) {
                ChattingFragment.instance.hidden(i);
            } else {
                if (MainActivity.active) {
                    finish();
                } else {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        } else {
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.call_menu:
                showCallMenu(ivCall);
                break;
            case R.id.more_menu:
                showFilterPopup(ivMore);
                break;

            case R.id.search_menu:
                showSearchView();
                break;
        }
    }

    private void showCallMenu(View v) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(ChattingActivity.this);
        builderSingle.setTitle("Call");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ChattingActivity.this, R.layout.row_chatting_call);
        Utils.addCallArray(userNos, arrayAdapter, treeUserDTOTempArrayList);
        builderSingle.setNegativeButton(
                "cancel",
                (dialog, which) -> dialog.dismiss());

        builderSingle.setAdapter(
                arrayAdapter,
                (dialog, which) -> {
                    String phoneNumber = GetPhoneNumber(arrayAdapter.getItem(which));
                    Utils.CallPhone(ChattingActivity.this, phoneNumber);
                });

        AlertDialog dialog = builderSingle.create();
        if (arrayAdapter.getCount() > 0) {
            dialog.show();
        }

        Button b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (b != null) {
            b.setTextColor(ContextCompat.getColor(mContext, R.color.light_black));
        }
    }

    private String GetPhoneNumber(String strPhone) {
        String result = strPhone.split("\\(")[1];
        result = result.split("\\)")[0];
        return result;
    }

    private void showFilterPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        // Inflate the menu from xml
        popup.getMenuInflater().inflate(R.menu.menu_in_chatting, popup.getMenu());
        Menu menu = popup.getMenu();

        if (CrewChatApplication.getInstance().getPrefs().getDDSServer().contains(Statics.chat_jw_group_co_kr)) {
            menu.findItem(R.id.menu_send_file).setVisible(false);
        }

        if (isOne) {
            menu.findItem(R.id.menu_left_group).setVisible(false);
        } else {
            menu.findItem(R.id.menu_left_group).setVisible(true);
        }

        // Setup menu item selection
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_list_chat:
                    menu_list_chat();
                    return true;
                case R.id.menu_add_chat:
                    final Intent intent = new Intent(ChattingActivity.this, InviteUserActivity.class);
                    intent.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo);
                    intent.putExtra(Constant.KEY_INTENT_COUNT_MEMBER, userNos);
                    intent.putExtra(Constant.KEY_INTENT_ROOM_TITLE, title);
                    intent.putExtra(Constants.LIST_MEMBER, (Serializable) CompanyFragment.instance.getSubordinates());
                    startActivityForResult(intent, Statics.ADD_USER_SELECT);
                    return true;
                case R.id.menu_left_group:
                    HttpRequest.getInstance().DeleteChatRoomUser(roomNo, myId, new BaseHTTPCallBack() {
                        @Override
                        public void onHTTPSuccess() {
                            // delete local db this room
                            Log.d(TAG, "menu_left_group roomNo:" + roomNo);
                            ChatMessageDBHelper.deleteMessageByLocalRoomNo(roomNo);

                            Intent intent1 = new Intent(ChattingActivity.this, MainActivity.class);
                            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent1);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }

                        @Override
                        public void onHTTPFail(ErrorDto errorDto) {
                        }
                    });
                    return true;
                case R.id.menu_send_file:
                    if (checkPermissionsReadExternalStorage()) {
                        Intent i = new Intent(ChattingActivity.Instance, FilePickerActivity.class);
                        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
                        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                        ChattingActivity.Instance.startActivityForResult(i, Statics.FILE_PICKER_SELECT);
                    } else {
                        ChattingActivity.instance.setPermissionsReadExternalStorage();
                    }
                    return true;
                case R.id.menu_close:
                    finish();
                    return true;
                case R.id.menu_room_rename:
                    renameRoom();
                    return true;
                case R.id.menu_iv_file_box:
                    ivFileBox();
                    return true;
                case R.id.menu_attach_file_box:
                    attachFileBox();
                    return true;
                default:
                    return false;
            }
        });

        popup.show();
    }

    public void menu_list_chat() {
        Intent intent2 = new Intent(ChattingActivity.this, RoomUserInformationActivity.class);
        intent2.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo);
        intent2.putExtra("userNos", userNos);
        intent2.putExtra("roomTitle", title);
        startActivity(intent2);
    }

    public void attachFileBox() {
        Intent intent = new Intent(ChattingActivity.this, AttachFileBoxActivity.class);
        Log.d(TAG, "attachFileBox roomNo:" + roomNo);
        intent.putExtra(Statics.ROOM_NO, roomNo);
        startActivity(intent);
    }

    public void ivFileBox() {
        Intent intent = new Intent(ChattingActivity.this, ImageFileBoxActivity.class);
        Log.d(TAG, "ivFileBox roomNo:" + roomNo);
        intent.putExtra(Statics.ROOM_NO, roomNo);
        startActivity(intent);
    }

    public void renameRoom() {
        Bundle roomInfo = new Bundle();
        roomInfo.putInt(Statics.ROOM_NO, (int) roomNo);
        roomInfo.putString(Statics.ROOM_TITLE, roomTitle);
        Log.d(TAG, "roomNo:" + roomNo);
        Log.d(TAG, "roomTitle:" + roomTitle);
        Intent intent = new Intent(this, RenameRoomActivity.class);
        intent.putExtras(roomInfo);
        startActivityForResult(intent, Statics.RENAME_ROOM);
    }

    private void showSearchView() {
        if (isShow) {
            mSearchView.setIconified(true);
            isShow = true;
        } else {
            mSearchView.setIconified(false);
            isShow = false;
        }
    }

    private void setupSearchView() {
        if (isAlwaysExpanded()) {
            mSearchView.setIconifiedByDefault(false);
        }

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        if (searchManager != null) {
            List<SearchableInfo> searchables = searchManager.getSearchablesInGlobalSearch();
            SearchableInfo info = searchManager.getSearchableInfo(getComponentName());

            for (SearchableInfo inf : searchables) {
                if (inf.getSuggestAuthority() != null
                        && inf.getSuggestAuthority().startsWith("applications")) {
                    info = inf;
                }
            }

            mSearchView.setSearchableInfo(info);
        }

        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnClickListener(view -> {
            ChattingFragment.instance.isSearchFocused = true;
        });

        mSearchView.setOnCloseListener(() -> {
            ChattingFragment.instance.isSearchFocused = false;
            return false;
        });

        mSearchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Log.d("AC", "A");
            }
        });
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChattingFragment.instance.isSearchFocused = true;
            }
        });
    }

    protected boolean isAlwaysExpanded() {
        return false;
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d(TAG, "onQueryTextSubmit");
        return false;
    }

    private String strSearch = "";
    @Override
    public boolean onQueryTextChange(String newText) {
        strSearch = newText;
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, 1000);
        return false;
    }

    private Runnable mRunnable = () -> {
        ChattingFragment.instance.adapterList.filter(strSearch, ChattingFragment.instance.dataSetCopy);
        ChattingFragment.instance.isFiltering = !TextUtils.isEmpty(strSearch);
        ChattingFragment.instance.strFilter = strSearch;
    };

    private Handler mHandler = new Handler();

    int CAMERA_PERMISSIONS_REQUEST_CODE = 0;

    public boolean checkPermissionsAudio() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public void setPermissionsAudio() {
        String[] requestPermission;
        requestPermission = new String[]{Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, requestPermission, CAMERA_PERMISSIONS_REQUEST_CODE);
    }

    public boolean checkPermissionsReadExternalStorage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public void setPermissionsReadExternalStorage() {
        String[] requestPermission;
        requestPermission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, requestPermission, CAMERA_PERMISSIONS_REQUEST_CODE);
    }

    public boolean checkPermissionsWriteExternalStorage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public boolean checkPermissionsCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        return true;
    }

    public void setPermissionsCamera() {
        String[] requestPermission;
        requestPermission = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, requestPermission, CAMERA_PERMISSIONS_REQUEST_CODE);
    }

    public boolean checkPermissionsContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public void setPermissionsCameraContacts() {
        String[] requestPermission;
        requestPermission = new String[]{Manifest.permission.READ_CONTACTS};
        ActivityCompat.requestPermissions(this, requestPermission, CAMERA_PERMISSIONS_REQUEST_CODE);
    }

    public boolean checkPermissionsWandR() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    int RandW_PERMISSIONS_REQUEST_CODE = 1;

    public void setPermissionsRandW() {
        String[] requestPermission;
        requestPermission = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, requestPermission, RandW_PERMISSIONS_REQUEST_CODE);
    }

    private void handleImageRotate(Intent data, ArrayList<String> mSelectedImage) {
        String pathImageRotate = "";
        if (data != null && data.getStringExtra(Statics.CHATTING_DTO_GALLERY_SINGLE) != null) {
            pathImageRotate = data.getStringExtra(Statics.CHATTING_DTO_GALLERY_SINGLE);
        } else if (mSelectedImage.size() > 0) {
            pathImageRotate = Utils.getPathFromURI(Uri.parse(mSelectedImage.get(0)), this);
        } else if (uri != null) {
            pathImageRotate = Utils.getPathImage(this, uri);
        } else return;

        // Add image to gallery album
        if (mSelectedImage != null && mSelectedImage.size() > 1) {
            long diffTime = 0;
            for (String uriPath : mSelectedImage) {
                diffTime += Config.TIME_WAIT * mSelectedImage.indexOf(uriPath);
                galleryAddPic(pathImageRotate);
                ChattingDto chattingDto = new ChattingDto();
                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELECT_IMAGE);
                chattingDto.setAttachFilePath(Utils.getPathFromURI(Uri.parse(uriPath), this));
                chattingDto.setRoomNo(chattingDto.getRoomNo());
                chattingDto.setRegDate(Utils.getTimeNewChat(diffTime));
                chattingDto.setStrRegDate(Utils.getTimeFormat(CrewChatApplication.getInstance().getTimeLocal() + diffTime));
                chattingDto.setLastedMsgAttachType(Statics.ATTACH_IMAGE);

                chattingDto.setLastedMsgType(Statics.MESSAGE_TYPE_ATTACH);
                chattingDto.setPositionUploadImage(new Random().nextInt(1000));
                ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);
                isChoseFile = true;
                try {
                    Thread.sleep(diffTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            galleryAddPic(pathImageRotate);

            ChattingDto chattingDto = new ChattingDto();
            chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELECT_IMAGE);
            chattingDto.setAttachFilePath(pathImageRotate);
            chattingDto.setRoomNo(chattingDto.getRoomNo());
            chattingDto.setRegDate(Utils.getTimeNewChat(0));
            chattingDto.setStrRegDate(Utils.getTimeFormat(CrewChatApplication.getInstance().getTimeLocal()));
            chattingDto.setLastedMsgAttachType(Statics.ATTACH_IMAGE);

            chattingDto.setLastedMsgType(Statics.MESSAGE_TYPE_ATTACH);
            chattingDto.setPositionUploadImage(new Random().nextInt(1000));
            ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);
            isChoseFile = true;
        }

    }

    private void handleCameraCapture() {
        if (uri != null) {
            isChoseFile = true;
            String path = Utils.getPathFromURI(uri, this);
            Intent intent = new Intent(this, RotateImageActivity.class);
            intent.putExtra(Statics.CHATTING_DTO_GALLERY_SINGLE, path);
            String currentTime = CrewChatApplication.getInstance().getTimeLocal() + "";
            intent.putExtra(Statics.CHATTING_DTO_REG_DATE, currentTime);
            startActivityForResult(intent, Statics.IMAGE_ROTATE_CODE);
        }
    }

    private void handleVideoRecoder(Intent data) {
        Uri videoUri = null;
        if (data != null) {
            videoUri = data.getData();
        } else if (videoPath != null) {
            videoUri = videoPath;
        }

        if (videoUri != null) {
            String path = Utils.getPathFromURI(videoUri, this);
            galleryAddPic(path);

            File file = new File(path);
            String filename = path.substring(path.lastIndexOf("/") + 1);
            ChattingDto chattingDto = new ChattingDto();
            chattingDto.setmType(CHATTING_VIEW_TYPE_SELECT_VIDEO);
            chattingDto.setAttachFilePath(path);
            chattingDto.setAttachFileName(filename);
            chattingDto.setAttachFileSize((int) file.length());
            chattingDto.setUnReadCount(ChattingActivity.userNos.size() - 1);
            chattingDto.setRegDate(Utils.getTimeNewChat(0));
            chattingDto.setStrRegDate(Utils.getTimeFormat(CrewChatApplication.getInstance().getTimeLocal()));
            chattingDto.setPositionUploadImage(new Random().nextInt(1000));
            ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);
        }
    }

    private void handleVideoSelected(Intent data, ArrayList<String> mSelectedImage) {
        Uri videoUriPick = null;
        if (data != null) {
            videoUriPick = data.getData();
        } else if (mSelectedImage.size() > 0) {
            videoUriPick = Uri.parse(mSelectedImage.get(0));
        }

        if (mSelectedImage != null && mSelectedImage.size() > 1) {
            long diffTime = 0;
            for (String uriPath : mSelectedImage) {
                diffTime += Config.TIME_WAIT * mSelectedImage.indexOf(uriPath);
                String path = Utils.getPathFromURI(Uri.parse(uriPath), this);
                File file = new File(path);
                String filename = path.substring(path.lastIndexOf("/") + 1);
                ChattingDto chattingDto = new ChattingDto();
                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELECT_VIDEO);
                chattingDto.setAttachFilePath(path);
                chattingDto.setAttachFileName(filename);
                chattingDto.setUnReadCount(ChattingActivity.userNos.size() - 1);
                chattingDto.setAttachFileSize((int) file.length());
                chattingDto.setPositionUploadImage(new Random().nextInt(1000));

                // Add new attach info
                AttachDTO attachInfo = new AttachDTO();
                attachInfo.setFileName(filename);
                chattingDto.setAttachInfo(attachInfo);
                chattingDto.setRegDate(Utils.getTimeNewChat(diffTime));
                chattingDto.setStrRegDate(Utils.getTimeFormat(CrewChatApplication.getInstance().getTimeLocal() + diffTime));

                ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);
                try {
                    Thread.sleep(diffTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (videoUriPick != null) {
                String path;
                path = Utils.getPathFromURI(videoUriPick, this);
                File file = new File(path);
                String filename = path.substring(path.lastIndexOf("/") + 1);
                ChattingDto chattingDto = new ChattingDto();
                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELECT_VIDEO);
                chattingDto.setAttachFilePath(path);
                chattingDto.setAttachFileName(filename);
                chattingDto.setUnReadCount(ChattingActivity.userNos.size() - 1);
                chattingDto.setAttachFileSize((int) file.length());
                chattingDto.setPositionUploadImage(new Random().nextInt(1000));

                // Add new attach info
                AttachDTO attachInfo = new AttachDTO();
                attachInfo.setFileName(filename);
                chattingDto.setAttachInfo(attachInfo);
                chattingDto.setRegDate(Utils.getTimeNewChat(0));
                chattingDto.setStrRegDate(Utils.getTimeFormat(CrewChatApplication.getInstance().getTimeLocal()));

                ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);
            }
        }
    }

    private void handleFileSelected(Intent data, ArrayList<String> mSelectedImage) {
        List<Uri> pathUri = new ArrayList<>();
        if (data != null) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                ClipData clip = data.getClipData();
                if (clip != null) {
                    for (int i = 0; i < clip.getItemCount(); i++) {
                        pathUri.add(clip.getItemAt(i).getUri());
                    }
                }
            } else {
                pathUri.add(data.getData());
            }
        } else if (mSelectedImage.size() > 0) {
            for (String uriPath : mSelectedImage) {
                pathUri.add(Uri.parse(uriPath));
            }
        }


        if (pathUri.size() > 0) {
            if (pathUri.size() > 10) {
                Toast.makeText(getApplicationContext(), "Limit is 10 file", Toast.LENGTH_SHORT).show();
            } else {
                long diffTime = 0;
                for (Uri obj : pathUri) {
                    diffTime += Config.TIME_WAIT * pathUri.indexOf(obj);
                    String path = Utils.getPathFromURI(obj, this);
                    File file = new File(path);
                    String filename = file.getName();
                    if (filename.contains(".")) {
                        ChattingDto chattingDto = new ChattingDto();
                        chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELECT_FILE);
                        chattingDto.setAttachFilePath(path);
                        chattingDto.setAttachFileName(filename);
                        chattingDto.setLastedMsgAttachType(Statics.ATTACH_FILE);
                        chattingDto.setLastedMsgType(Statics.MESSAGE_TYPE_ATTACH);
                        chattingDto.setAttachFileSize((int) file.length());
                        chattingDto.setUnReadCount(ChattingActivity.userNos.size() - 1);
                        chattingDto.setRegDate(Utils.getTimeNewChat(diffTime));
                        chattingDto.setStrRegDate(Utils.getTimeFormat(CrewChatApplication.getInstance().getTimeLocal() + diffTime));
                        chattingDto.setPositionUploadImage(new Random().nextInt(1000));
                        ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);

                        try {
                            Thread.sleep(diffTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.can_not_send_this_file) + " " + filename, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void handleContactSelected(Intent data) {
        if (data != null && data.hasExtra(ContactPickerActivity.RESULT_CONTACT_DATA)) {
            List<Contact> contacts = (List<Contact>) data.getSerializableExtra(ContactPickerActivity.RESULT_CONTACT_DATA);

            long diffTime = 0;
            for (final Contact contact : contacts) {
                diffTime += Config.TIME_WAIT * contacts.indexOf(contact);
                final ChattingDto dto = new ChattingDto();
                UserDto userDto = new UserDto();
                userDto.setFullName(contact.getDisplayName());
                userDto.setPhoneNumber(contact.getPhone(0));
                userDto.setAvatar(contact.getPhotoUri() != null ? contact.getPhotoUri().toString() : null);

                dto.setmType(Statics.CHATTING_VIEW_TYPE_CONTACT);
                dto.setUser(userDto);
                dto.setMessage(contact.getPhone(0) == null ? contact.getDisplayName() : contact.getDisplayName() + "\n" + contact.getPhone(0));
                dto.setHasSent(false);
                dto.setUserNo(Utils.getCurrentId());
                dto.setRoomNo(roomNo);
                dto.setWriterUser(Utils.getCurrentId());
                dto.setRegDate(Utils.getTimeNewChat(diffTime));
                dto.setStrRegDate(Utils.getTimeFormat(CrewChatApplication.getInstance().getTimeLocal() + diffTime));
                dto.setPositionUploadImage(new Random().nextInt(1000));
                ChattingFragment.instance.addNewRowFromChattingActivity(dto);
                ChattingFragment.instance.reSendMessage(dto);

                try {
                    Thread.sleep(diffTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleRenameRoom(Intent data) {
        final int roomNo = data.getIntExtra(Statics.ROOM_NO, 0);
        final String roomTitle = data.getStringExtra(Statics.ROOM_TITLE);
        updateRoomName(roomTitle);
        Prefs prefs = CrewChatApplication.getInstance().getPrefs();
        prefs.setRoomName(roomTitle);
        prefs.putRoomId(roomNo);
        new Thread(() -> ChatRoomDBHelper.updateChatRoom(roomNo, roomTitle)).start();
    }

    private void handleActionSend(String typeShare, ArrayList<String> mSelectedImage) {
        if (typeShare.equals("text/plain")) {
            // handleSendText(intent);
        } else if (typeShare.startsWith("video/")) {
            handleVideoSelected(null, mSelectedImage);
        } else if (typeShare.startsWith("audio/")) {
            //handleSendAudio(intent);
        } else if (typeShare.startsWith("text/")) {
            // handleSendContact(intent);
        } else if (typeShare.startsWith("image/")) {
            handleImageRotate(null, mSelectedImage);
        } else {
            handleFileSelected(null, mSelectedImage);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isChoseFile) {
            ChattingFragment.instance.Reload();
        }

        isChoseFile = false;
    }
}