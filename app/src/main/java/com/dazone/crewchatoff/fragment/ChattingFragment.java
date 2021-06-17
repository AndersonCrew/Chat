package com.dazone.crewchatoff.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.Class.ChatInputView;
import com.dazone.crewchatoff.Enumeration.ChatMessageType;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.ChatViewImageActivity;
import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.activity.UnreadActivity;
import com.dazone.crewchatoff.activity.chatroom.ChattingViewModel;
import com.dazone.crewchatoff.adapter.ChattingAdapter;
import com.dazone.crewchatoff.adapter.EndlessRecyclerOnScrollListener;
import com.dazone.crewchatoff.constant.Constants;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.customs.AudioPlayer;
import com.dazone.crewchatoff.customs.EmojiView;
import com.dazone.crewchatoff.database.AllUserDBHelper;
import com.dazone.crewchatoff.database.ChatMessageDBHelper;
import com.dazone.crewchatoff.database.UserDBHelper;
import com.dazone.crewchatoff.dto.AttachDTO;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.MessageUnreadCountDTO;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.dto.UserDto;
import com.dazone.crewchatoff.eventbus.ReceiveMessage;
import com.dazone.crewchatoff.eventbus.ReloadListMessage;
import com.dazone.crewchatoff.interfaces.ILayoutChange;
import com.dazone.crewchatoff.socket.NetClient;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

import static com.dazone.crewchatoff.database.ChatMessageDBHelper.addMessage;

public class ChattingFragment extends ListFragment<ChattingDto> implements View.OnClickListener, EmojiView.EventListener, View.OnKeyListener, TextView.OnEditorActionListener {
    private String TAG = ChattingFragment.class.getName();
    public long roomNo;
    private ArrayList<Integer> userNos;
    public boolean isActive = false;

    public ChatInputView view;
    private ArrayList<TreeUserDTOTemp> listTemp = null;
    private int userID;
    public static ChattingFragment instance;
    public boolean isVisible = false;
    private boolean isLoading = false;
    private boolean isLoadMore = true;
    private boolean hasLoadMore = false;

    private boolean isShowNewMessage = false;
    private Activity mActivity;
    private UserDto temp = null;

    private Prefs mPrefs;
    public static boolean sendComplete = false;
    public static boolean isSend = true;
    public static long msgEnd = -1;
    int recordTouch = 0;
    private TextView tvDurationDialog;
    private boolean isFlag = false;
    private boolean isThreadRunning = false;
    private Handler handlerTimer = new Handler();
    private int timeDelay = 1000;
    private int timerCount = -1;
    public boolean isFiltering = false;
    public String strFilter = "";
    public boolean isSearchFocused = false;

    public ChattingViewModel viewModel;
    private Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            if (isThreadRunning) {
                timerCount++;
                String msg = Constant.audioFormatDuration(timerCount);
                setTimer(msg);
                setTextDurationDialog(msg);
                handlerTimer.postDelayed(this, timeDelay);
            }
        }
    };

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isShowIcon = false;
        msgEnd = -1;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        mPrefs = CrewChatApplication.getInstance().getPrefs();
        msgEnd = -1;
        isShowIcon = false;
        userID = Utils.getCurrentId();
        temp = CrewChatApplication.currentUser;

        if (temp == null) {
            temp = UserDBHelper.getUser();
        }

        Bundle bundle = getArguments();
        userID = Utils.getCurrentId();

        if (bundle != null) {
            roomNo = bundle.getLong(Constant.KEY_INTENT_ROOM_NO, 0);
            userNos = bundle.getIntegerArrayList(Constant.KEY_INTENT_USER_NO_ARRAY);
        }

        Constant.cancelAllNotification(CrewChatApplication.getInstance(), (int) roomNo);

        setiLayoutChange(new ILayoutChange() {
            @Override
            public void onKeyBoardShow() {
                if (!isSearchFocused) {
                    rvMainList.postDelayed(() -> rvMainList.smoothScrollToPosition(dataSet.size()), 300);
                }
            }

            @Override
            public void onKeyBoardHide() {
            }
        });
    }

    private boolean hasActionSend = false;

    private void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ChattingViewModel.class);

        /**Handler Error*/
        viewModel.getEventError().observe(this, error -> {
            if (error != null && error.message != null) {
                Utils.showMessage(error.message);
            }
        });

        /**Handler Get ListChat*/
        viewModel.getListChatting().observe(this, list -> {
            if (list != null && list.size() > 0) {
                getActivity().runOnUiThread(() -> {
                    for (ChattingDto chat : list) {
                        Utils.checkExist(chat, dataSet);
                        addMessage(chat);
                    }

                    initData(dataSet);
                });

                if (viewModel.getHasLoadNewMessage()) {
                    viewModel.getMessageUnReadCount(roomNo, dataSet.get(dataSet.size() - 1).getStrRegDate());
                }
            }
        });

        /**Handler Loadmore*/
        viewModel.getListChattingLoadmore().observe(this, listNew -> {
            isLoading = false;
            if (listNew.size() > 0) {
                getActivity().runOnUiThread(() -> {
                    int oldSize = dataSet.size() + 1;
                    for (ChattingDto chat : listNew) {
                        Utils.checkExist(chat, dataSet);
                        addMessage(chat);
                    }

                    initData(dataSet);
                    layoutManager.scrollToPositionWithOffset(dataSet.size() - oldSize, 0);
                    viewModel.getMessageUnReadCount(roomNo, dataSet.get(dataSet.size() - 1).getStrRegDate());
                });
            }
        });

        /**Handler MessageUnreadCount*/
        viewModel.getListMessageUnReadCount().observe(this, list -> {
            getActivity().runOnUiThread(() -> {
                if (list != null && list.size() > 0) {
                    for (final MessageUnreadCountDTO messageUnreadCountDTO : list) {
                        for (int i = dataSet.size() - 1; i > -1; i--) {
                            final ChattingDto chattingDto = dataSet.get(i);
                            if (chattingDto.getMessageNo() == messageUnreadCountDTO.getMessageNo()) {
                                ChatMessageDBHelper.updateUnReadCount(chattingDto);
                                if (chattingDto.getUnReadCount() != messageUnreadCountDTO.getUnreadCount()) {
                                    chattingDto.setUnReadCount(messageUnreadCountDTO.getUnreadCount());
                                }
                                break;
                            }
                        }
                    }

                    adapterList.notifyDataSetChanged();
                }
            });

        });

        /**Handler Send Attach Success*/
        viewModel.getAttachFile().observe(this, attachFile -> {
            if (attachFile != null) {
                Log.d("SEND FILE", "sendAttachFile observe positionUploadImage =  " + attachFile.getPositionUploadImage() + " AttachNo = " + attachFile.getAttachNo());
                updateMessageSendFile(attachFile);
            }
        });

        /**Handle send message fail*/
        viewModel.getDtoFailed().observe(this, dto -> {
            updateSendFail(dto);
            Log.d("CHAT ROOM", "Send Message Fail");
        });

        /**Handle send normal message*/
        viewModel.getNormalMessage().observe(this, dto -> {
            updateSendSuccess(dto);
            Log.d("CHAT ROOM", "Send Message Success");
        });

        /**handle action share*/
        viewModel.getSendActionShare().observe(this, flag -> {
            if (flag != null && flag) {
                new Handler().postDelayed(() -> {
                    try {
                        ChattingActivity.instance.setUpActioneSend();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }, 3000);
            }
        });

    }

    private boolean isGetValueEnterAuto() {
        boolean isEnable = false;
        isEnable = mPrefs.getBooleanValue(Statics.IS_ENABLE_ENTER_KEY, isEnable);
        return isEnable;
    }

    @Subscribe
    public void reloadMessageWhenNetworkReConnect(ReloadListMessage reloadListMessage) {
        refFreshData();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (CompanyFragment.instance != null) listTemp = CompanyFragment.instance.getUser();
        if (listTemp == null || listTemp.size() == 0) {
            listTemp = AllUserDBHelper.getUser_v2();
        }

        if (listTemp == null) {
            listTemp = new ArrayList<>();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        CrewChatApplication.currentRoomNo = 0;
    }

    public ChattingFragment newInstance(long roomNo, ArrayList<Integer> userNos, Activity activity) {
        if (instance == null)
            instance = new ChattingFragment();
        instance.setActivity(activity);
        Bundle args = new Bundle();
        args.putLong(Constant.KEY_INTENT_ROOM_NO, roomNo);
        args.putIntegerArrayList(Constant.KEY_INTENT_USER_NO_ARRAY, userNos);
        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideNewMessage();
        unregisterGCMReceiver();
    }

    @Override
    protected void initAdapter() {
        rvMainList.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrolledUp() {
                super.onScrolledUp();
                isShowNewMessage = true;
            }

            @Override
            public void onScrolledDown() {
                super.onScrolledDown();
            }

            @Override
            public void onScrolledToBottom() {
                super.onScrolledToBottom();

                hideNewMessage();
                isShowNewMessage = false;
            }

            @Override
            public void onScrolledToTop() {
                super.onScrolledToTop();
                try {
                    loadMoreData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isShowNewMessage = true;
            }
        });

        adapterList = new ChattingAdapter(mContext, mActivity, dataSet, rvMainList, (chattingDto) -> rvMainList.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dataSet.contains(chattingDto) && dataSet.size() - dataSet.indexOf(chattingDto) <= 6 && !isShowNewMessage)
                    scrollToEndList();
            }
        }, 500));

        initViewModel();
        viewModel.getChatListFirst(roomNo, userID);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initList() {
        view = new ChatInputView(getContext());
        view.addToView(recycler_footer);
        view.mEmojiView.setEventListener(this);
        view.btnSend.setOnClickListener(this);
        view.edt_comment.setOnKeyListener(this);
        view.edt_comment.setOnEditorActionListener(this);

        list_content_rl.setBackgroundColor(ImageUtils.getColor(getContext(), R.color.chat_list_bg_color));
        disableSwipeRefresh();


        view.edt_comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString();
                if (str == null || str.length() == 0) {
                    view.btnVoice.setVisibility(View.VISIBLE);
                    view.btnSend.setVisibility(View.GONE);
                } else {
                    view.btnVoice.setVisibility(View.GONE);
                    view.btnSend.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        view.btnVoice.setOnLongClickListener(v -> {
            startRecording();
            return true;
        });

        // btnVoice
        view.btnVoice.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_UP:
                    if (!isThreadRunning) {
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.press_hold), Toast.LENGTH_SHORT).show();
                    }
                    stopRecording();
                    break;
            }
            return false;
        });

        rlNewMessage.setOnClickListener(v -> {
            hideNewMessage();
            scrollToEndList();
        });
    }

    private void startCount() {
        isThreadRunning = true;
        timerCount = -1;
        handlerTimer.post(updateTimer);
    }

    private void stopCount() {
        isThreadRunning = false;
        handlerTimer.removeCallbacks(updateTimer);
    }

    private void showDialog() {
        startCount();
        if (layoutSpeak != null) {
            layoutSpeak.setVisibility(View.VISIBLE);
        }
    }

    void dismissDialog() {
        stopCount();
        if (layoutSpeak != null) {
            layoutSpeak.setVisibility(View.GONE);
        }
    }

    private MediaRecorder.OnErrorListener errorListener = (mr, what, extra) -> Log.d(TAG, "Error: " + what + ", " + extra);

    private MediaRecorder.OnInfoListener infoListener = (mr, what, extra) -> Log.d(TAG, "Warning: " + what + ", " + extra);
    private String fileAudioName = "fileAudioName";
    private MediaRecorder recorder = null;
    private int currentFormat = 0;

    public void stopRecording() {
        dismissDialog();
        if (null != recorder) {
            boolean isSuccess = true;
            try {
                recorder.stop();
                recorder.reset();
                recorder.release();
            } catch (Exception e) {
                isSuccess = false;
                e.printStackTrace();
            }
            recorder = null;
            Log.d(TAG, "isSuccess:" + isSuccess);
            if (isSuccess) {
                sendAudio();
            }
        }
    }

    private void sendAudio() {
        List<ChattingDto> integerList = new ArrayList<>();
        String path = Constant.getFilename(currentFormat, fileAudioName);
        File file = new File(path);
        String filename = path.substring(path.lastIndexOf("/") + 1);
        if (filename.contains(".")) {
            ChattingDto chattingDto = new ChattingDto();
            chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELECT_FILE);
            chattingDto.setAttachFilePath(path);
            chattingDto.setAttachFileName(filename);
            chattingDto.setLastedMsgAttachType(Statics.ATTACH_FILE);
            chattingDto.setLastedMsgType(Statics.MESSAGE_TYPE_ATTACH);
            chattingDto.setAttachFileSize((int) file.length());
            chattingDto.setRegDate(Utils.getTimeNewChat(0));
            chattingDto.setStrRegDate(Utils.getTimeFormat(CrewChatApplication.getInstance().getTimeLocal()));
            chattingDto.setPositionUploadImage(dataSet.size() - 1);
            integerList.add(chattingDto);

            addNewRowFromChattingActivity(chattingDto);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.can_not_send_this_file) + " " + filename, Toast.LENGTH_SHORT).show();
        }
    }

    public void addNewRowFromChattingActivity(ChattingDto chattingDto) {
        dataSet.add(chattingDto);
        scrollToEndList();
    }

    private void notifyItemInserted() {
        Collections.sort(dataSet, mComparator);
        adapterList.notifyItemInserted(dataSet.size());
    }

    private void scrollToEndList() {
        layoutManager.scrollToPosition(dataSet.size() - 1);
    }

    public void startRecording() {
        if (ChattingActivity.instance.checkPermissionsAudio()) {
            showDialog();
            fileAudioName = TimeUtils.showTimeWithoutTimeZone(Calendar.getInstance().getTimeInMillis(), Statics.yy_MM_dd_hh_mm_aa);
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(Statics.output_formats[currentFormat]);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncodingBitRate(16000);
            recorder.setAudioChannels(1);
            recorder.setOutputFile(Constant.getFilename(currentFormat, fileAudioName));
            recorder.setOnErrorListener(errorListener);
            recorder.setOnInfoListener(infoListener);
            try {
                recorder.prepare();
                recorder.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            ChattingActivity.instance.setPermissionsAudio();
        }
    }

    public boolean stopRecordingFromDialog() {
        stopCount();
        boolean isSuccess = false;
        if (null != recorder) {
            try {
                recorder.stop();
                recorder.reset();
                recorder.release();
                isSuccess = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            recorder = null;
        }
        return isSuccess;
    }

    public void startRecordingFromDialog() {
        if (ChattingActivity.instance.checkPermissionsAudio()) {
            startCount();
            fileAudioName = TimeUtils.showTimeWithoutTimeZone(Calendar.getInstance().getTimeInMillis(), Statics.yy_MM_dd_hh_mm_aa);
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(Statics.output_formats[currentFormat]);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncodingBitRate(16000);
            recorder.setAudioChannels(1);
            recorder.setOutputFile(Constant.getFilename(currentFormat, fileAudioName));
            recorder.setOnErrorListener(errorListener);
            recorder.setOnInfoListener(infoListener);
            try {
                recorder.prepare();
                recorder.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            ChattingActivity.instance.setPermissionsAudio();
        }
    }

    private void setTextDurationDialog(String msg) {
        if (tvDurationDialog != null) tvDurationDialog.setText(msg);
    }

    public void recordDialog() {
        ChattingActivity.instance.isChoseFile = true;
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_record_layout);
        recordTouch = 0;
        isFlag = false;

        // tvDuration
        tvDurationDialog = dialog.findViewById(R.id.tvDuration);

        // ivRecord
        final ImageView ivRecord = dialog.findViewById(R.id.ivRecord);

        // btnClose
        FrameLayout btnClose = dialog.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            stopRecordingFromDialog();
            dialog.dismiss();
        });

        // btnSendRecord
        final Button btnSendRecord = dialog.findViewById(R.id.btnSendRecord);
        btnSendRecord.setEnabled(false);
        btnSendRecord.setOnClickListener(v -> {
            sendAudio();
            dialog.dismiss();
        });

        // btnRecord
        FrameLayout btnRecord = dialog.findViewById(R.id.btnRecord);
        btnRecord.setOnClickListener(v -> {
            if (recordTouch == 0) {
                // start record
                ivRecord.setImageResource(R.drawable.ic_stop_black_36dp);
                startRecordingFromDialog();
            } else if (recordTouch == 1) {
                // stop record
                ivRecord.setImageResource(R.drawable.ic_play_arrow_black_36dp);
                isFlag = stopRecordingFromDialog();
                if (isFlag) {
                    btnSendRecord.setEnabled(true);
                }
            } else {
                // play
                String path = Constant.getFilename(currentFormat, fileAudioName);
                new AudioPlayer(getActivity(), path, fileAudioName + Statics.file_exts[currentFormat]).show();
            }
            recordTouch++;
        });

        // setOnKeyListener dialog
        dialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                Log.d(TAG, "KEYCODE_BACK");
            }
            return true;
        });

        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    public void addNewChat(ChattingDto chattingDto, boolean isUpdate, boolean fromGCM) {
        UserDto user = null;
        switch (chattingDto.getType()) {
            case ChatMessageType.Normal:
                if (chattingDto.getWriterUser() == userID) {
                    user = new UserDto(String.valueOf(temp.Id), temp.FullName, temp.avatar);
                    boolean isCheck = false;
                    if (dataSet != null && dataSet.size() > 0) {
                        isCheck = Utils.getChattingType(chattingDto, dataSet.get(dataSet.size() - 1));
                    }

                    if (isCheck) {
                        chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELF_NOT_SHOW);
                    } else {
                        chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELF);
                    }
                } else {
                    TreeUserDTOTemp treeUserDTOTemp = Utils.GetUserFromDatabase(listTemp, chattingDto.getWriterUser());

                    if (treeUserDTOTemp != null) {
                        user = new UserDto(String.valueOf(treeUserDTOTemp.getUserNo()), treeUserDTOTemp.getName(), treeUserDTOTemp.getAvatarUrl());
                    }

                    boolean isCheck = false;

                    if (dataSet != null && dataSet.size() > 0) {
                        isCheck = Utils.getChattingType(chattingDto, dataSet.get(dataSet.size() - 1));
                    }

                    if (isCheck) {
                        chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_PERSON_NOT_SHOW);
                    } else {
                        chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_PERSON);
                    }
                }
                break;

            case ChatMessageType.Group:
                if (chattingDto.getWriterUser() == userID) {
                    user = new UserDto(String.valueOf(temp.Id), temp.FullName, temp.avatar);
                } else {
                    TreeUserDTOTemp treeUserDTOTemp = Utils.GetUserFromDatabase(listTemp, chattingDto.getWriterUser());

                    if (treeUserDTOTemp != null) {
                        user = new UserDto(String.valueOf(treeUserDTOTemp.getUserNo()), treeUserDTOTemp.getName(), treeUserDTOTemp.getAvatarUrl());
                    }
                }
                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_GROUP_NEW);
                break;

            case ChatMessageType.Attach:
                if (chattingDto.getWriterUser() == userID) {
                    user = new UserDto(String.valueOf(temp.Id), temp.FullName, temp.avatar);

                    if (chattingDto.getAttachInfo() != null) {
                        if (chattingDto.getAttachInfo().getType() == 1) {
                            chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELF_IMAGE);
                        } else {
                            String filename = chattingDto.getAttachInfo().getFileName();

                            if (filename == null) {
                                String filePath = chattingDto.getAttachInfo().getFullPath();

                                if (filePath != null) {
                                    String pattern = Pattern.quote(System.getProperty("file.separator"));
                                    String[] files = filePath.split(pattern);

                                    if (files.length > 0) {
                                        filename = files[files.length - 1];
                                    }
                                }
                            }

                            if (Utils.isVideo(filename)) {
                                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELF_VIDEO);
                            } else {
                                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELF_FILE);
                            }
                        }
                    }
                } else {
                    TreeUserDTOTemp treeUserDTOTemp = Utils.GetUserFromDatabase(listTemp, chattingDto.getWriterUser());

                    if (treeUserDTOTemp != null) {
                        user = new UserDto(String.valueOf(treeUserDTOTemp.getUserNo()), treeUserDTOTemp.getName(), treeUserDTOTemp.getAvatarUrl());
                    }

                    if (chattingDto.getAttachInfo() != null) {
                        if (chattingDto.getAttachInfo().getType() == 1) {
                            boolean isCheck = false;

                            if (dataSet != null && dataSet.size() > 0) {
                                isCheck = Utils.getChattingType(chattingDto, dataSet.get(dataSet.size() - 1));
                            }

                            if (isCheck) {
                                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_PERSON_IMAGE_NOT_SHOW);
                            } else {
                                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_PERSON_IMAGE);
                            }
                        } else {
                            boolean isCheck = false;

                            if (dataSet != null && dataSet.size() > 0) {
                                isCheck = Utils.getChattingType(chattingDto, dataSet.get(dataSet.size() - 1));
                            }

                            if (isCheck) {
                                String filename = chattingDto.getAttachInfo().getFileName();

                                if (Utils.isVideo(filename)) {
                                    chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_PERSON_VIDEO_NOT_SHOW);
                                } else {
                                    chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_PERSON_FILE_NOT_SHOW);
                                }
                            } else {
                                String filename = chattingDto.getAttachInfo().getFileName();

                                if (Utils.isVideo(filename)) {
                                    chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_PERSON_VIDEO_NOT_SHOW);
                                } else {
                                    chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_PERSON_FILE);
                                }
                            }
                        }
                    }
                }
                break;

            default:
                if (chattingDto.getWriterUser() == userID) {
                    user = new UserDto(String.valueOf(temp.Id), temp.FullName, temp.avatar);
                    chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELF);
                } else {
                    TreeUserDTOTemp treeUserDTOTemp = Utils.GetUserFromDatabase(listTemp, chattingDto.getWriterUser());
                    if (treeUserDTOTemp != null) {
                        user = new UserDto(String.valueOf(treeUserDTOTemp.getUserNo()), treeUserDTOTemp.getName(), treeUserDTOTemp.getAvatarUrl());
                    }

                    chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_PERSON);
                }
                break;
        }

        if (chattingDto.getType() == 2) {
            if (chattingDto.getAttachInfo() == null) {
                return;
            }
        }

        chattingDto.setUser(user);
        chattingDto.setContent(chattingDto.getMessage());

        if (fromGCM) {
            Iterator<ChattingDto> it = dataSet.iterator();
            while (it.hasNext()) {
                ChattingDto chat = it.next();
                if (chat.getMessageNo() == chattingDto.getMessageNo() && roomNo == chattingDto.getRoomNo()) {
                    it.remove();
                    break;
                }
            }
        }

        dataSet.add(chattingDto);
    }

    private void updateMessageSendFile(ChattingDto chattingDto) {
        view.linearEmoji.setVisibility(View.GONE);
        UserDto user = new UserDto(String.valueOf(Utils.getCurrentUser().Id), Utils.getCurrentUser().FullName, Utils.getCurrentUser().avatar);
        chattingDto.setUser(user);
        chattingDto.setContent(chattingDto.getMessage());

        int position = 0;
        for (int i = 0; i < dataSet.size(); i++) {
            if (dataSet.get(i).getPositionUploadImage() == chattingDto.getPositionUploadImage()) {
                position = i;
                break;
            }
        }

        dataSet.set(position, chattingDto);
        adapterList.notifyItemChanged(position);
    }

    private void updateSendFail(ChattingDto dto) {
        view.linearEmoji.setVisibility(View.GONE);
        sendComplete = false;
        isSend = true;

        Iterator<ChattingDto> it = dataSet.iterator();
        while (it.hasNext()) {
            ChattingDto chat = it.next();
            if (chat.getPositionUploadImage() == dto.getPositionUploadImage() && roomNo == dto.getRoomNo()) {
                chat.setHasSent(false);
                chat.isSendding = false;
                break;
            }
        }

        adapterList.notifyDataSetChanged();
    }

    private void updateSendSuccess(ChattingDto dto) {
        view.linearEmoji.setVisibility(View.GONE);
        sendComplete = false;
        isSend = true;
        adapterList.notifyDataSetChanged();
    }

    private void initData(List<ChattingDto> list) {
        List<ChattingDto> tempList = new ArrayList<>();
        tempList.addAll(list);
        dataSet.clear();
        for (int i = 0; i < tempList.size(); i++) {
            addNewChat(tempList.get(i), false, false);
        }

        // Scroll to bottom
        if (!hasLoadMore) {
            int a = dataSet.size() - 1;
            if (a >= 0)
                rvMainList.scrollToPosition(a);
            hasLoadMore = false;
        }


        Collections.sort(dataSet, mComparator);
        dataSetCopy.clear();
        dataSetCopy.addAll(dataSet);
        adapterList.notifyDataSetChanged();
    }

    private Comparator<ChattingDto> mComparator = (o1, o2) -> {
        SimpleDateFormat formatter = new SimpleDateFormat(Statics.yyyy_MM_dd_HH_mm_ss_SSS, Locale.getDefault());
        Date date1 = new Date(TimeUtils.getTime(o1.getRegDate()));
        Date date2 = new Date(TimeUtils.getTime(o2.getRegDate()));

        try {
            date1 = formatter.parse(o1.getStrRegDate());
            date2 = formatter.parse(o2.getStrRegDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return date1.compareTo(date2);
    };

    private void senAction() {
        final String message = view.edt_comment.getText().toString();
        if (!TextUtils.isEmpty(message) && message.length() > 0) {
            view.edt_comment.setText("");
            CrewChatApplication.getInstance().getPrefs().putStringValue(Constants.TEXT_NOT_SEND + roomNo, "");
            isSend = false;

            ChattingDto newDto = new ChattingDto();
            newDto.setMessageNo(Long.MAX_VALUE);
            newDto.setMessage(message);
            newDto.setUserNo(userID);
            newDto.setType(Statics.MESSAGE_TYPE_NORMAL);
            newDto.setRoomNo(roomNo);
            newDto.setmType(Statics.CHATTING_VIEW_TYPE_SELF_NOT_SHOW);
            newDto.setUnReadCount(ChattingActivity.userNos.size() - 1);
            newDto.setWriterUser(userID);
            newDto.setHasSent(true);
            newDto.setRegDate(Utils.getTimeNewChat(0));
            newDto.setStrRegDate(Utils.getTimeFormat(CrewChatApplication.getInstance().getTimeLocal()));
            newDto.setPositionUploadImage(new Random().nextInt(1000));
            newDto.setId(new Random().nextInt());
            newDto.isSendding = true;

            dataSet.add(newDto);
            scrollToEndList();
            viewModel.sendNormalMessage(roomNo, message, newDto);
        }
    }

    public void reSendMessage(ChattingDto newDto) {
        viewModel.sendNormalMessage(roomNo, newDto.getMessage(), newDto);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSend:// 전송버튼
                sendComplete = true;
                senAction();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        isVisible = true;
        isActive = true;
        registerGCMReceiver();
        CrewChatApplication.currentRoomNo = roomNo;

        if (!TextUtils.isEmpty(CrewChatApplication.getInstance().getPrefs().getStringValue(Constants.TEXT_NOT_SEND + roomNo, ""))) {
            view.edt_comment.setText(CrewChatApplication.getInstance().getPrefs().getStringValue(Constants.TEXT_NOT_SEND + roomNo, ""));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isVisible = false;
        isActive = false;
        CrewChatApplication.currentRoomNo = 0;

        if (!TextUtils.isEmpty(view.edt_comment.getText())) {
            CrewChatApplication.getInstance().getPrefs().putStringValue(Constants.TEXT_NOT_SEND + roomNo, view.edt_comment.getText().toString());
        }
    }

    private void registerGCMReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Statics.ACTION_RECEIVER_NOTIFICATION);
        filter.addAction(Constant.INTENT_FILTER_GET_MESSAGE_UNREAD_COUNT);
        filter.addAction(Constant.INTENT_FILTER_ADD_USER);
        filter.addAction(Constant.INTENT_FILTER_UPDATE_ROOM_NAME);
        filter.addAction(Constant.INTENT_GOTO_UNREAD_ACTIVITY);

        if (mActivity != null) {
            mActivity.registerReceiver(mReceiverNewAssignTask, filter);
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void showNewMessage(final ChattingDto dto) {
        if (isAdded())
            mActivity.runOnUiThread(() -> {
                TreeUserDTOTemp treeUserDTOTemp = Utils.GetUserFromDatabase(listTemp, dto.getWriterUserNo());
                String userName;

                if (treeUserDTOTemp != null) {
                    userName = treeUserDTOTemp.getName();
                } else {
                    userName = "Unknown";
                }

                int textSize1 = getResources().getDimensionPixelSize(R.dimen.text_16_32);
                int textSize2 = getResources().getDimensionPixelSize(R.dimen.text_15_30);

                SpannableString span1 = new SpannableString(userName);
                span1.setSpan(new AbsoluteSizeSpan(textSize1), 0, userName.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                SpannableString span2 = new SpannableString(dto.getMessage());
                span2.setSpan(new AbsoluteSizeSpan(textSize2), 0, dto.getMessage().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                // let's put both spans together with a separator and all
                CharSequence finalText = TextUtils.concat(span1, " : ", span2);
                tvUserNameMessage.setText(finalText);

                rlNewMessage.setVisibility(View.VISIBLE);
                ivScrollDown.setOnClickListener(v -> scrollToEndList());
            });
    }

    private void hideNewMessage() {
        if (mActivity == null) {
            mActivity = getActivity();
        }

        if (mActivity != null) {
            mActivity.runOnUiThread(() -> rlNewMessage.setVisibility(View.GONE));
        }
    }

    private void unregisterGCMReceiver() {
        try {
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this);
            }
            getActivity().unregisterReceiver(mReceiverNewAssignTask);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void receiveMessage(final ReceiveMessage message) {
        //mActivity.runOnUiThread(() -> processReceivingMessage(message.getChattingDto()));
    }

    private BroadcastReceiver mReceiverNewAssignTask = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (!sendComplete) {
                BroadcastEvent(intent);
            }
        }
    };

    private void BroadcastEvent(Intent intent) {
        isShowIcon = false;
        if (intent.getAction().equals(Constant.INTENT_FILTER_GET_MESSAGE_UNREAD_COUNT)) {
            final long roomNo = intent.getLongExtra(Constant.KEY_INTENT_ROOM_NO, 0);
            if (roomNo != 0 && dataSet.size() > 0) {
                String baseDate = intent.getStringExtra(Constant.KEY_INTENT_BASE_DATE);
                viewModel.getMessageUnReadCount(roomNo, baseDate);
            }
        } else if (intent.getAction().equals(Constant.INTENT_FILTER_ADD_USER)) {
            long roomNo = intent.getLongExtra(Constant.KEY_INTENT_ROOM_NO, 0);
            if (roomNo != 0 && roomNo == ChattingFragment.this.roomNo) {
                Reload();
            }
        } else if (intent.getAction().equals(Constant.INTENT_FILTER_UPDATE_ROOM_NAME)) {
            if (intent != null) {
                long roomNo = intent.getLongExtra(Statics.ROOM_NO, 0);
                String roomTitle = intent.getStringExtra(Statics.ROOM_TITLE);
                if (ChattingActivity.instance != null) {
                    ChattingActivity.instance.updateRoomName(roomTitle);
                    ChattingActivity.instance.updateRoomName(roomTitle);
                }

                Prefs prefs = CrewChatApplication.getInstance().getPrefs();
                prefs.setRoomName(roomTitle);
                prefs.putRoomId((int) roomNo);

            }
        } else if (intent.getAction().equals(Constant.INTENT_GOTO_UNREAD_ACTIVITY)) {
            if (intent != null) {
                try {
                    goToUnreadActivity(intent.getLongExtra(Statics.MessageNo, 0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if(intent.getAction().equals(Statics.ACTION_RECEIVER_NOTIFICATION)) {
            if (intent != null) {
               ChattingDto dto = (ChattingDto) intent.getSerializableExtra(Statics.CHATTING_DTO);
               processReceivingMessage(dto);
            }
        }
    }

    private void processReceivingMessage(ChattingDto dataDto) {
        //UpdateMessageUnreadCount
        viewModel.updateMessageUnReadCount(roomNo, userID, dataDto.getStrRegDate(), true);
        if (dataSet != null) {
            for (ChattingDto chattingDto : dataSet) {
                if (chattingDto.getMessageNo() == dataDto.getMessageNo() && dataDto.getRoomNo() == roomNo) {
                    return;
                }
            }
        }


        boolean isShow = dataDto.getRoomNo() == roomNo;

        dataDto.setWriterUser(dataDto.getWriterUserNo());
        dataDto.setCheckFromServer(true);

        if (roomNo == dataDto.getRoomNo()) {
            if (!TextUtils.isEmpty(dataDto.getMessage()) || dataDto.getAttachNo() != 0) {
                if (dataDto.getType() != 6) {
                    msgEnd = -1;
                    isShowIcon = false;
                }

                addNewChat(dataDto, true, true);
                if (isShowNewMessage && isShow) {
                    showNewMessage(dataDto);
                } else {
                    hideNewMessage();
                    notifyItemInserted();
                    scrollToEndList();
                }
            }
        } else {
            dataDto.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(dataDto.getRegDate()));
            notifyToCurrentChatList(dataDto);
        }
    }

    public int checkBack() {
        int i = 0;
        if (view != null) {
            if (view.linearEmoji.getVisibility() == View.VISIBLE) {
                i = 2;
            }

            if (view.selection_lnl.getVisibility() == View.VISIBLE) {
                i = 1;
            }
        }
        return i;
    }

    public void hidden(int task) {
        if (view != null)
            if (task == 1) {
                view.selection_lnl.setVisibility(View.GONE);
            } else {
                view.linearEmoji.setVisibility(View.GONE);
            }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        boolean handled = false;
        switch (v.getId()) {
            case R.id.edt_comment:
                if (keyCode == EditorInfo.IME_ACTION_DONE
                        || event.getKeyCode() == KeyEvent.KEYCODE_ENTER && isGetValueEnterAuto() == true) {
                    handled = true;
                    senAction();
                } else if (event.getAction() == KeyEvent.KEYCODE_ENTER || event.getAction() == KeyEvent.ACTION_DOWN && isGetValueEnterAuto() == false) {
                    handled = false;
                }
                break;
        }

        return handled;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (isGetValueEnterAuto()) {
            if ((actionId & EditorInfo.IME_MASK_ACTION) == EditorInfo.IME_ACTION_DONE) {
                senAction();
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    public void goToUnreadActivity(long msgNo) {
        Intent intent = new Intent(getActivity(), UnreadActivity.class);
        intent.putExtra(Statics.MessageNo, msgNo);
        intent.putExtra("userNos", userNos);
        intent.putExtra(Statics.ROOM_NO, roomNo);
        startActivity(intent);
    }

    public void SendTo(ChattingDto chattingDto, ProgressBar progressBar) {
        if (view != null) {
            view.selection_lnl.setVisibility(View.GONE);
        }

        new SendToServer(chattingDto, progressBar).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    @Override
    public void onBackspace() {
        view.edt_comment.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
    }

    @Override
    public void onEmojiSelected(final String res) {
        int codePointCopyright = Integer.parseInt(res, 16);
        String headPhoneString2 = new String(Character.toChars(codePointCopyright));

        view.edt_comment.setText(view.edt_comment.getText().append(headPhoneString2).toString());
        view.edt_comment.setSelection(view.edt_comment.getText().length());
    }

    public class SendToServer extends AsyncTask<Void, Void, Integer> {
        private ChattingDto chattingDto;
        private ProgressBar progressBar;

        private SendToServer(ChattingDto chattingDto, ProgressBar progressBar) {
            this.chattingDto = chattingDto;
            this.progressBar = progressBar;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            AttachDTO attachDTO = new AttachDTO();
            attachDTO.setFileName(Utils.getFileName(chattingDto.getAttachFilePath()));
            attachDTO.setFileType(Utils.getFileType(attachDTO.getFileName()));
            attachDTO.setFullPath(chattingDto.getAttachFilePath());

            String siteDomain = new Prefs().getServerSite();

            if (siteDomain.startsWith("http://")) {
                siteDomain = siteDomain.replace("http://", "");
            }

            if (siteDomain.contains(":")) {
                siteDomain = siteDomain.substring(0, siteDomain.indexOf(":"));
            }

            InetAddress ip = null;

            try {
                ip = InetAddress.getByName(siteDomain);
            } catch (Exception e) {
                e.printStackTrace();
            }

            NetClient nc;

            if (ip == null) {
                // ip 값이 없다면 도메인명을 통해 파일서버로 접속하여 전송 처리.
                nc = new NetClient(siteDomain, new Prefs().getFILE_SERVER_PORT());
            } else {
                nc = new NetClient(ip.getHostAddress(), new Prefs().getFILE_SERVER_PORT());
            }

            // 실제 파일 데이터를 전송 처리 합니다.
            nc.sendDataWithStringTest(attachDTO, progressBar);
            return nc.receiveDataFromServer();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            viewModel.sendAttachFile(integer, roomNo, chattingDto);
        }
    }

    public void Reload() {
        viewModel.getChatListFirst(roomNo, userID);
    }

    public static boolean isShowIcon = false;

    private void loadMoreData() {
        if (isFiltering) {
            if (Utils.isNetworkAvailable()) {
                //viewModel.getChatList(dataSet.get(0).getStrRegDate(), roomNo, ChatMessageDBHelper.FILTER, userID, null);
            } else {
                viewModel.loadMoreLocal(roomNo, dataSet.get(0).getMessageNo(), ChatMessageDBHelper.FILTER, strFilter);
            }
        } else {
            if (!isLoading && isLoadMore) {
                isLoading = true;
                hasLoadMore = true;

                if (Utils.isNetworkAvailable()) {
                    viewModel.getChatList(dataSet.get(0).getStrRegDate(), roomNo, ChatMessageDBHelper.BEFORE, userID, null);
                } else {
                    viewModel.loadMoreLocal(roomNo, dataSet.get(0).getMessageNo(), ChatMessageDBHelper.BEFORE, "");
                }
            }
        }
    }

    private void refFreshData() {
        initData(dataSet);
        adapterList.notifyDataSetChanged();
    }

    public void ViewImageFull(ChattingDto chattingDto) {
        ArrayList<ChattingDto> urls = new ArrayList<>();
        int position = 0;

        for (ChattingDto chattingDto1 : dataSet) {
            if (chattingDto1.getAttachInfo() != null && chattingDto1.getAttachInfo().getType() == 1) {
                urls.add(chattingDto1);
            }
        }

        for (ChattingDto chattingDto1 : urls) {
            if (chattingDto.getMessageNo() == chattingDto1.getMessageNo()) {
                position = urls.indexOf(chattingDto);
            }
        }
        Prefs prefs = CrewChatApplication.getInstance().getPrefs();
        if (urls.size() > 0)
            prefs.setIMAGE_LIST(new Gson().toJson(urls));
        else
            prefs.setIMAGE_LIST("");
        Intent intent = new Intent(mActivity, ChatViewImageActivity.class);
        intent.putExtra(Statics.CHATTING_DTO_GALLERY_POSITION, position);
        mActivity.startActivity(intent);
    }

    private void notifyToCurrentChatList(ChattingDto dto) {
        if (CurrentChatListFragment.fragment != null) {
            try {
                ChattingDto dt = (ChattingDto) dto.clone();
                dt.setUnReadCount(0);
                CurrentChatListFragment.fragment.updateData(dt);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }
}