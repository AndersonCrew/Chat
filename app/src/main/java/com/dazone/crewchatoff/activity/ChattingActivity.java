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
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import com.dazone.crewchatoff.eventbus.CloseScreen;
import com.dazone.crewchatoff.fragment.ChattingFragment;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.fragment.CurrentChatListFragment;
import com.dazone.crewchatoff.fragment.RecentFavoriteFragment;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallBack;
import com.dazone.crewchatoff.interfaces.OnGetChatRoom;
import com.dazone.crewchatoff.interfaces.SendChatMessage;
import com.dazone.crewchatoff.libGallery.MediaChooser;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.core.ContactPickerActivity;

import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.dazone.crewchatoff.constant.Statics.CHATTING_VIEW_TYPE_SELECT_VIDEO;

public class ChattingActivity extends BaseSingleStatusActivity implements View.OnClickListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    public static void toActivity(Context context, long roomNo, long myId, ChattingDto tempDto) {
        Intent intent = new Intent(context, ChattingActivity.class);
        Bundle args = new Bundle();
        args.putLong(Constant.KEY_INTENT_ROOM_NO, roomNo);
        args.putLong(Constant.KEY_INTENT_USER_NO, myId);
        args.putSerializable(Constant.KEY_INTENT_ROOM_DTO, tempDto);

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

        getTimeServer();
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
        // if network is connected, sync all chat rom message and store in local database then display it on chat view
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

        // Get room title online if room information was updated
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
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        Log.d(TAG, "onActivityResult");
        if (resultCode == Activity.RESULT_OK) {
            if (fragment != null) {
                if (fragment.view != null && fragment.view.selection_lnl.getVisibility() == View.VISIBLE) {
                    fragment.view.selection_lnl.setVisibility(View.GONE);
                }
            }
            switch (requestCode) {
                case Statics.ADD_USER_SELECT:
                    activityResultAddUser(data);
                    break;

                case Statics.IMAGE_ROTATE_CODE:
                    if (data != null) {
                        String path = data.getStringExtra(Statics.CHATTING_DTO_GALLERY_SINGLE);

                        // Add image to gallery album
                        galleryAddPic(path);

                        ChattingDto chattingDto = new ChattingDto();
                        chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELECT_IMAGE);
                        chattingDto.setAttachFilePath(path);
                        chattingDto.setRoomNo(chattingDto.getRoomNo());
                        chattingDto.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(System.currentTimeMillis() + ""));
                        chattingDto.setLastedMsgAttachType(Statics.ATTACH_IMAGE);


                        chattingDto.setLastedMsgType(Statics.MESSAGE_TYPE_ATTACH);
                        ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);
                        Log.d(TAG, "addNewRow 1");


                        if (ChattingFragment.instance != null) {
                            List<ChattingDto> integerList = new ArrayList<>();
                            chattingDto.setPositionUploadImage(ChattingFragment.instance.dataSet.size() - 1);
                            integerList.add(chattingDto);
                            ChattingFragment.instance.sendFileWithQty(integerList, 0);
                        }
                    }
                    break;

                case Statics.CAMERA_CAPTURE_IMAGE_REQUEST_CODE:
                    Log.d(TAG, "CAMERA_CAPTURE_IMAGE_REQUEST_CODE");
                    if (uri != null) {
                        String path = Utils.getPathFromURI(uri, this);
                        // Processing to rotate Image
                        Intent intent = new Intent(this, RotateImageActivity.class);
                        intent.putExtra(Statics.CHATTING_DTO_GALLERY_SINGLE, path);
                        String currentTime = System.currentTimeMillis() + "";
                        intent.putExtra(Statics.CHATTING_DTO_REG_DATE, currentTime);
                        startActivityForResult(intent, Statics.IMAGE_ROTATE_CODE);
                    }
                    break;

                case Statics.CAMERA_VIDEO_REQUEST_CODE:
                    if (data != null) {
                        Log.d(TAG, "data!=null");
                        Uri videoUri = data.getData();
                        if (videoUri != null) {
                            Log.d(TAG, "videoUri!=null:" + videoUri.toString());
                            String path = Utils.getPathFromURI(videoUri, this);

                            galleryAddPic(path);

                            File file = new File(path);
                            Log.d(TAG, "path:" + path);
                            String filename = path.substring(path.lastIndexOf("/") + 1);
                            Log.d(TAG, "filename:" + filename);
                            ChattingDto chattingDto = new ChattingDto();
                            chattingDto.setmType(CHATTING_VIEW_TYPE_SELECT_VIDEO);
                            chattingDto.setAttachFilePath(path);
                            chattingDto.setAttachFileName(filename);
                            chattingDto.setAttachFileSize((int) file.length());
                            chattingDto.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(System.currentTimeMillis() + ""));
                            ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);
                            Log.d(TAG, "addNewRow 2");
                        }
                    } else {
                        Log.d(TAG, "data==null");
                        if (videoPath != null) {
                            Log.d(TAG, "videoPath!=null:" + videoPath.toString());
                            Uri videoUri = videoPath;
                            String path = Utils.getPathFromURI(videoUri, this);

                            galleryAddPic(path);

                            File file = new File(path);
                            String filename = path.substring(path.lastIndexOf("/") + 1);
                            ChattingDto chattingDto = new ChattingDto();
                            chattingDto.setmType(CHATTING_VIEW_TYPE_SELECT_VIDEO);
                            chattingDto.setAttachFilePath(path);
                            chattingDto.setAttachFileName(filename);
                            chattingDto.setAttachFileSize((int) file.length());
                            chattingDto.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(System.currentTimeMillis() + ""));
                            ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);
                            Log.d(TAG, "addNewRow 3");
                        }
                    }
                    break;

                case Statics.VIDEO_PICKER_SELECT:
                    Uri videoUriPick = data.getData();
                    if (videoUriPick != null) {
                        String path = "";
                        path = Utils.getPath(this, videoUriPick);
                        File file = new File(path);
                        String filename = path.substring(path.lastIndexOf("/") + 1);
                        ChattingDto chattingDto = new ChattingDto();
                        chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELECT_VIDEO);
                        chattingDto.setAttachFilePath(path);
                        chattingDto.setAttachFileName(filename);
                        chattingDto.setAttachFileSize((int) file.length());

                        // Add new attach info
                        AttachDTO attachInfo = new AttachDTO();
                        attachInfo.setFileName(filename);
                        chattingDto.setAttachInfo(attachInfo);
                        chattingDto.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(System.currentTimeMillis() + ""));

                        ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);
                    }
                    break;
                case Statics.FILE_PICKER_SELECT:
                    List<Uri> pathUri = new ArrayList<>();
                    if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                        Log.d(TAG, "getBooleanExtra true");
                        // For JellyBean and above
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            ClipData clip = data.getClipData();
                            if (clip != null) {
                                for (int i = 0; i < clip.getItemCount(); i++) {
                                    pathUri.add(clip.getItemAt(i).getUri());
                                }
                            }
                            // For Ice Cream Sandwich
                        } else {
                            ArrayList<String> paths = data.getStringArrayListExtra(FilePickerActivity.EXTRA_PATHS);
                            if (paths != null) {
                                for (String path : paths) {
                                    pathUri.add(Uri.parse(path));
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "getBooleanExtra false");
                        pathUri.add(data.getData());

                    }

                    if (pathUri != null && pathUri.size() > 0) {
                        if (pathUri.size() > 10) {
                            // show notify
                            Toast.makeText(getApplicationContext(), "Limit is 10 file", Toast.LENGTH_SHORT).show();
                        } else {
                            List<ChattingDto> integerList = new ArrayList<>();
                            for (Uri obj : pathUri) {
                                Log.d(TAG, "pathUri != null:" + obj.toString());
                                String path = Utils.getPathFromURI(obj, this);
                                Log.d(TAG, "path:" + path);
                                File file = new File(path);
                                String filename = path.substring(path.lastIndexOf("/") + 1);
                                Log.d(TAG, "filename:" + filename);
                                if (filename.contains(".")) {
                                    ChattingDto chattingDto = new ChattingDto();
                                    chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELECT_FILE);
                                    chattingDto.setAttachFilePath(path);
                                    chattingDto.setAttachFileName(filename);
                                    chattingDto.setLastedMsgAttachType(Statics.ATTACH_FILE);
                                    chattingDto.setLastedMsgType(Statics.MESSAGE_TYPE_ATTACH);
                                    chattingDto.setAttachFileSize((int) file.length());
                                    chattingDto.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(System.currentTimeMillis() + ""));
                                    ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);
                                    Log.d(TAG, "addNewRow 5");

                                    chattingDto.setPositionUploadImage(ChattingFragment.instance.dataSet.size() - 1);
                                    integerList.add(chattingDto);
                                } else {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.can_not_send_this_file) + " " + filename, Toast.LENGTH_SHORT).show();
                                }
                            }
                            if (ChattingFragment.instance != null && integerList.size() > 0)
                                ChattingFragment.instance.sendFileWithQty_v2(integerList, 0);
                        }
                    }
                    break;

                case Statics.CONTACT_PICKER_SELECT:
                    if (data != null && data.hasExtra(ContactPickerActivity.RESULT_CONTACT_DATA)) {
                        // Loop all contact that user has picked and display it on chat windows
                        // we got a result from the contact picker
                        List<Contact> contacts = (List<Contact>) data.getSerializableExtra(ContactPickerActivity.RESULT_CONTACT_DATA);

                        for (final Contact contact : contacts) {
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
                            // perform update when send message success
                            String currentTime = System.currentTimeMillis() + "";
                            String time = TimeUtils.convertTimeDeviceToTimeServerDefault(currentTime);
                            dto.setRegDate(time);
                            //final long lastId = ChatMessageDBHelper.addSimpleMessage(dto);
                            HttpRequest.getInstance().SendChatMsg(roomNo, contact.getPhone(0) == null ? contact.getDisplayName() : contact.getDisplayName() + "\n" + contact.getPhone(0), new SendChatMessage() {
                                @Override
                                public void onSendChatMessageSuccess(final ChattingDto chattingDto) {
                                    // update old chat message model --> messageNo from server
                                    dto.setHasSent(true);
                                    dto.setMessage(chattingDto.getMessage());
                                    dto.setMessageNo(chattingDto.getMessageNo());
                                    dto.setmType(Statics.CHATTING_VIEW_TYPE_CONTACT);
                                    dto.setUnReadCount(chattingDto.getUnReadCount());
                                    String time = TimeUtils.convertTimeDeviceToTimeServerDefault(chattingDto.getRegDate());
                                    dto.setRegDate(time);
                                    ChattingFragment.instance.addNewRowFromChattingActivity(dto);

                                }

                                @Override
                                public void onSendChatMessageFail(ErrorDto errorDto, String url) {
                                    Toast.makeText(getApplicationContext(), "Send message failed !", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                    break;
                case Statics.RENAME_ROOM:

                    if (data != null) {
                        final int roomNo = data.getIntExtra(Statics.ROOM_NO, 0);
                        final String roomTitle = data.getStringExtra(Statics.ROOM_TITLE);
                        // Update current chat list
                        updateRoomName(roomTitle);

                        Prefs prefs = CrewChatApplication.getInstance().getPrefs();
                        prefs.setRoomName(roomTitle);
                        prefs.putRoomId(roomNo);
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
        } else if (resultCode == Constant.INTENT_RESULT_CREATE_NEW_ROOM) {
            Log.d(TAG, "INTENT_RESULT_CREATE_NEW_ROOM");
            try {
                Bundle bc = data.getExtras();
                if (bc != null) {
                    // Update room title
                    newRoom(bc);
                }
            } catch (Exception e) {
                e.printStackTrace();
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
            if (listFilePath != null && listFilePath.size() > 0) {
                List<ChattingDto> integerList = new ArrayList<>();
                for (int i = 0; i < listFilePath.size(); i++) {
                    String path = listFilePath.get(i);
                    ChattingDto chattingDto = new ChattingDto();

                    if (intent.getAction().equals(MediaChooser.IMAGE_SELECTED_ACTION_FROM_MEDIA_CHOOSER)) {
                        chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELECT_IMAGE);
                    } else {
                        chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELECT_FILE);
                    }

                    chattingDto.setAttachFilePath(path);
                    chattingDto.setRoomNo(chattingDto.getRoomNo());
                    chattingDto.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(TimeUtils.getTime(ChattingFragment.instance.dataSet.get(ChattingFragment.instance.dataSet.size() - 1).getRegDate()) + 100 + ""));
                    ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);

                    chattingDto.setLastedMsgType(Statics.MESSAGE_TYPE_ATTACH);
                    chattingDto.setLastedMsgAttachType(Statics.ATTACH_IMAGE);
                    chattingDto.setPositionUploadImage(ChattingFragment.instance.dataSet.size() - 1);
                    integerList.add(chattingDto);
                }

                if (intent.getAction().equals(MediaChooser.IMAGE_SELECTED_ACTION_FROM_MEDIA_CHOOSER)) {
                    if (ChattingFragment.instance != null)
                        ChattingFragment.instance.sendFileWithQty(integerList, 0);
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

    @Override
    public boolean onQueryTextChange(String newText) {
        ChattingFragment.instance.adapterList.filter(newText);
        Log.d(TAG, "onQueryTextChange");
        return false;
    }

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

    @Subscribe
    public void closeScreen(CloseScreen closeScreen) {
        try {
            backToListChat();
        } catch (Exception e) {

        }
    }

    private void getTimeServer() {

    }
}