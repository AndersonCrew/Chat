package com.dazone.crewchatoff.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.Class.ChatInputView;
import com.dazone.crewchatoff.Enumeration.ChatMessageType;
import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.ViewHolders.ChattingSelfFileViewHolder;
import com.dazone.crewchatoff.activity.ChatViewImageActivity;
import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.activity.MainActivity;
import com.dazone.crewchatoff.activity.UnreadActivity;
import com.dazone.crewchatoff.adapter.ChattingAdapter;
import com.dazone.crewchatoff.adapter.EndlessRecyclerOnScrollListener;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.customs.AudioPlayer;
import com.dazone.crewchatoff.customs.EmojiView;
import com.dazone.crewchatoff.database.AllUserDBHelper;
import com.dazone.crewchatoff.database.ChatMessageDBHelper;
import com.dazone.crewchatoff.database.ChatRoomDBHelper;
import com.dazone.crewchatoff.database.UserDBHelper;
import com.dazone.crewchatoff.dto.AttachDTO;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.GroupDto;
import com.dazone.crewchatoff.dto.MessageNotSend;
import com.dazone.crewchatoff.dto.MessageUnreadCountDTO;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.dto.UserDto;
import com.dazone.crewchatoff.eventbus.ReceiveMessage;
import com.dazone.crewchatoff.eventbus.ReloadListMessage;
import com.dazone.crewchatoff.interfaces.ILayoutChange;
import com.dazone.crewchatoff.interfaces.OnGetChatMessage;
import com.dazone.crewchatoff.interfaces.OnGetMessageUnreadCountCallBack;
import com.dazone.crewchatoff.interfaces.SendChatMessage;
import com.dazone.crewchatoff.socket.NetClient;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

import static com.dazone.crewchatoff.activity.MainActivity.mSelectedImage;
import static com.dazone.crewchatoff.activity.MainActivity.type;
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
    private List<ChattingDto> dataFromServer = new ArrayList<>();
    public boolean isVisible = false;
    private boolean isLoading = false;
    private boolean isLoadMore = true;
    private boolean hasLoadMore = false;

    private boolean isFromNotification = false;
    private boolean isShowNewMessage = false;
    private Activity mActivity;
    private UserDto temp = null;

    // 메시지 핸들러 코드값
    private int WHAT_CODE_HIDE_PROCESS = 0;
    private int WHAT_CODE_ADD_NEW_DATA = 2;
    private int WHAT_CODE_EMPTY = 3;
    private int WHAT_CODE_HAS_INIT = 4;

    private String ADD_NEW_DATA = "AddNewData";
    private Prefs mPrefs;
    private boolean isLoaded = false;
    public static boolean sendComplete = false;
    CountDownTimer waitTimer;
    int sendTo = 0;
    public static boolean isSend = true;
    public static long msgEnd = -1;
    int recordTouch = 0;
    private TextView tvDurationDialog;
    private boolean isFlag = false;
    private boolean isThreadRunning = false;
    private Handler handlerTimer = new Handler();
    private int timeDelay = 1000;
    private int timerCount = -1;

    private Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            if (isThreadRunning) {
                timerCount++;
                String msg = Constant.audioFormatDuration(timerCount);
                setTimer(msg);
                setTextDurationDialog(msg);
                Log.d(TAG, "timerCount:" + timerCount);
                handlerTimer.postDelayed(this, timeDelay);
            }
        }
    };

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    @SuppressLint("HandlerLeak")
    protected final android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_CODE_HIDE_PROCESS) {
                progressBar.setVisibility(View.GONE);
            } else if (msg.what == WHAT_CODE_ADD_NEW_DATA) {
                //code = WHAT_CODE_ADD_NEW_DATA;
                mPrefs.putIntValue(Statics.VALUE_CODE_SHARE, WHAT_CODE_ADD_NEW_DATA);
                mPrefs.putBooleanValue(Statics.IS_FIRST_SHARE, true);
                Bundle args = msg.getData();
                List<ChattingDto> chattingDtoList = (ArrayList<ChattingDto>) args.getSerializable(ADD_NEW_DATA);

                addData(chattingDtoList);
                if (layoutManager != null) {
                    scrollToEndList();
                }

                getFirstDB();

            }

            if (!isSendingFile) {
                shareInterFace();
            }
        }
    };

    void getFirstDB() {
        setFirstItem();
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
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
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
                rvMainList.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rvMainList.smoothScrollToPosition(dataSet.size());
                    }
                }, 300);
            }

            @Override
            public void onKeyBoardHide() {

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

    private void loadClientData() {
        isLoaded = true;
        final List<ChattingDto> listChatMessage = ChatMessageDBHelper.getMsgSession(roomNo, 0, ChatMessageDBHelper.FIRST);

        dataFromServer = listChatMessage;
        if (listChatMessage != null && listChatMessage.size() > 0) {
            initData(listChatMessage);
        }
        if (Utils.isNetworkAvailable()) {
            getOnlineData(roomNo, listChatMessage);
        } else {
            progressBar.setVisibility(View.GONE);
        }

        Log.d("A", "A");
    }

    // Thread to get data from server
    private void getOnlineData(final long roomNo, final List<ChattingDto> listChatMessage) {
        String baseDate = CrewChatApplication.getInstance().getTimeServer();
        int mesType = 1;
        int index;
        if (listChatMessage.size() > 0) {
            index = listChatMessage.size() - 1;
            baseDate = listChatMessage.get(index).getRegDate();
            mesType = ChatMessageDBHelper.AFTER;
        }

        HttpRequest.getInstance().GetChatMsgSection(roomNo, mesType, baseDate, new OnGetChatMessage() {
            @Override
            public void OnGetChatMessageSuccess(List<ChattingDto> listNew) {
                mHandler.obtainMessage(WHAT_CODE_HIDE_PROCESS).sendToTarget();
                isLoaded = true;

                ArrayList<ChattingDto> newDataFromServer = new ArrayList<>();
                if (listNew.size() > 0) {
                    // Update unRead count message
                    HttpRequest.getInstance().UpdateMessageUnreadCount(roomNo, userID, listNew.get(listNew.size() - 1).getRegDate());
                    if (CurrentChatListFragment.fragment != null) {
                        CurrentChatListFragment.fragment.updateRoomUnread(roomNo);
                    }

                    if (RecentFavoriteFragment.instance != null) {
                        RecentFavoriteFragment.instance.updateRoomUnread(roomNo);
                    }

                    // Save online data to local data
                    for (ChattingDto chat : listNew) {
                        boolean isExist = false;
                        for (ChattingDto dto : dataFromServer) {
                            if (chat.getMessageNo() == dto.getMessageNo()) {
                                isExist = true;
                                break;
                            }
                        }
                        // Check if message is exist
                        if (!isExist) {
                            if (addDataFromServer(chat)) dataFromServer.add(chat);
                            newDataFromServer.add(chat);
                            addMessage(chat);
                        }
                    }

                    if (newDataFromServer.size() > 0) {
                        Message message = Message.obtain();
                        message.what = WHAT_CODE_ADD_NEW_DATA;
                        if (listChatMessage.size() > 0) {
                            message.arg1 = WHAT_CODE_HAS_INIT;
                        }
                        Bundle args = new Bundle();
                        args.putSerializable(ADD_NEW_DATA, newDataFromServer);
                        message.setData(args);
                        mHandler.sendMessage(message);
                    } else {
                        getFirstDB();
                        if (listChatMessage.size() == 0) {
                            mHandler.obtainMessage(WHAT_CODE_EMPTY).sendToTarget();
                        }
                    }
                } else {
                    getFirstDB();
                }

                Log.d("A", "A");
            }

            @Override
            public void OnGetChatMessageFail(ErrorDto errorDto) {
                getFirstDB();
                isLoaded = true;
                mHandler.obtainMessage(WHAT_CODE_HIDE_PROCESS).sendToTarget();

                if (listChatMessage.size() == 0) {
                    mHandler.obtainMessage(WHAT_CODE_EMPTY).sendToTarget();
                }
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        if (CompanyFragment.instance != null) listTemp = CompanyFragment.instance.getUser();
        if (listTemp == null || listTemp.size() == 0)
            listTemp = AllUserDBHelper.getUser_v2();
        if (listTemp == null) listTemp = new ArrayList<>();
        // Load client data at first, then call load online data on new thread
        // Just load on the first time
        if (!isLoaded) {
            loadClientData();
        }

        set_msg_for_edit_text();
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
            public void onScrolledToBottom() {
                super.onScrolledToBottom();

                hideNewMessage();
                isShowNewMessage = false;

            }

            @Override
            public void onScrolledToTop() {
                super.onScrolledToTop();
                Log.d(TAG, "onScrolledToTop");
                try {
                    loadMoreData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isShowNewMessage = true;
            }
        });

        adapterList = new ChattingAdapter(mContext, mActivity, dataSet, rvMainList);
        rvMainList.setLayoutManager(layoutManager);
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
        if (isGetValueEnterAuto()) {
            if (Build.VERSION.SDK_INT >= 24) {
                view.edt_comment.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
                view.edt_comment.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            }

        } else {
            view.edt_comment.setInputType(131073);
        }
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

        view.btnVoice.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startRecording();
                return true;
            }
        });

        // btnVoice
        view.btnVoice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "ACTION_UP");
                        if (!isThreadRunning) {
                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.press_hold), Toast.LENGTH_SHORT).show();
                        }
                        stopRecording();
                        break;
                }
                return false;
            }
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

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Log.d(TAG, "Error: " + what + ", " + extra);
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            Log.d(TAG, "Warning: " + what + ", " + extra);
        }
    };
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
        Log.d(TAG, "path:" + path);
        File file = new File(path);
        String filename = path.substring(path.lastIndexOf("/") + 1);
        Log.d(TAG, "filename:" + filename);
        if (filename.contains(".")) {
            //ChattingDto chattingDto = this.chattingDto;
            ChattingDto chattingDto = new ChattingDto();
            chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELECT_FILE);
            chattingDto.setAttachFilePath(path);
            chattingDto.setAttachFileName(filename);
            chattingDto.setLastedMsgAttachType(Statics.ATTACH_FILE);
            chattingDto.setLastedMsgType(Statics.MESSAGE_TYPE_ATTACH);
            chattingDto.setAttachFileSize((int) file.length());

            addNewRowFromChattingActivity(chattingDto);

            chattingDto.setPositionUploadImage(dataSet.size() - 1);
            integerList.add(chattingDto);
            sendFileWithQty_v2(integerList, 0);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.can_not_send_this_file) + " " + filename, Toast.LENGTH_SHORT).show();
        }
    }

    public void addNewRowFromChattingActivity(ChattingDto chattingDto) {
        addLineToday(chattingDto);
        dataSet.add(chattingDto);
        Log.d(TAG, ": dataSet.size()" + dataSet.size());
        notifyItemInserted();
        scrollToEndList();
    }

    private void notifyItemInserted() {
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
                Log.d(TAG, "Start Recording");
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
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecordingFromDialog();
                dialog.dismiss();
            }
        });
        // btnSendRecord
        final Button btnSendRecord = dialog.findViewById(R.id.btnSendRecord);
        btnSendRecord.setEnabled(false);
        btnSendRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAudio();
                dialog.dismiss();
            }
        });

        // btnRecord
        FrameLayout btnRecord = dialog.findViewById(R.id.btnRecord);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        // setOnKeyListener dialog
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    Log.d(TAG, "KEYCODE_BACK");
                }
                return true;
            }
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

        if (!chattingDto.isCheckFromServer()) {
            if (view != null) {
                view.edt_comment.setText("");
            }
        }

        if (chattingDto.getType() == 2) {
            if (chattingDto.getAttachInfo() == null) {
                return;
            }
        }

        if (isUpdate) {
            if (CurrentChatListFragment.fragment != null) {
                chattingDto.setUnReadCount(0);
                CurrentChatListFragment.fragment.updateData(chattingDto);
            }
        }

        chattingDto.setUser(user);
        chattingDto.setContent(chattingDto.getMessage());
        if (chattingDto.getMessageNo() == Long.MAX_VALUE) {
            dataSet.add(chattingDto);
        } else {
            boolean isAdd = true;
            for (int i = 0; i < dataSet.size(); i++) {
                if (dataSet.get(i).getMessageNo() == Long.MAX_VALUE && Utils.getCurrentId() == chattingDto.getWriterUser()) {
                    isAdd = false;
                    dataSet.set(i, chattingDto);
                    break;
                }
            }
            if (isAdd) {
                if (isAddMsg(dataSet, chattingDto.getMessageNo(), chattingDto.getRegDate())) {
                    if (isAddChat(dataSet, chattingDto.getMessageNo())) {
                        dataSet.add(chattingDto);
                    }
                } else {
                    dataSet.set(dataSet.size() - 1, chattingDto);
                }
            }
        }
        int a = dataSet.size() - 1;
        if (a >= 0) {
            msgEnd = dataSet.get(dataSet.size() - 1).getMessageNo();
        }

        Collections.sort(dataSet, new Comparator<ChattingDto>() {
            @Override
            public int compare(ChattingDto o1, ChattingDto o2) {
                return (o1.getMessageNo() == 0 || o2.getMessageNo() == 0 || (o1.getMessageNo() == o2.getMessageNo())) ? 0 :
                        o1.getMessageNo() < o2.getMessageNo() ? -1 : 1;
            }
        });

        notifyItemInserted();

        if (layoutManager.findLastCompletelyVisibleItemPosition() == dataSet.size() - 2) {
            int b = dataSet.size() - 1;
            if (b >= 0)
                layoutManager.scrollToPosition(b);
        }
        if (isFromNotification) {
            isFromNotification = false;
        }
    }

    boolean isAddChat(List<ChattingDto> dataSet, long msgNo) {
        if (dataSet != null && dataSet.size() > 0) {
            for (int i = 0; i < dataSet.size(); i++) {

                ChattingDto obj = dataSet.get(i);
                long msgId = obj.getMessageNo();
                if (msgId == msgNo && msgId != Long.MAX_VALUE)
                    return false;
            }
            return true;
        } else {
            return true;
        }
    }

    boolean isAddMsg(List<ChattingDto> lst, long msgNo, String regDate) {
        if (regDate == null) regDate = "";
        if (lst.size() > 0) {
            int n = lst.size() - 1;
            ChattingDto obj = lst.get(n);
            Log.d(TAG, obj.getMessage() + ":" + obj.getMessageNo());
            String s2 = obj.getRegDate();
            if (s2 == null) s2 = "";
            if (obj.getMessageNo() == msgNo && regDate.equals(s2)) {
                Log.d(TAG, "isAddMsg duplicate:" + msgNo + " msg:" + obj.getMessage());
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public void addNewChat(ChattingDto chattingDto, int position, WatingUpload callBack) {
        UserDto user = null;
        UserDto temp = Utils.getCurrentUser();
        switch (chattingDto.getType()) {
            case 0:
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
                    if (treeUserDTOTemp != null)
                        user = new UserDto(String.valueOf(treeUserDTOTemp.getUserNo()), treeUserDTOTemp.getName(), treeUserDTOTemp.getAvatarUrl());

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
            case 1:
                if (chattingDto.getWriterUser() == userID) {
                    user = new UserDto(String.valueOf(temp.Id), temp.FullName, temp.avatar);
                } else {
                    TreeUserDTOTemp treeUserDTOTemp = Utils.GetUserFromDatabase(listTemp, chattingDto.getWriterUser());
                    if (treeUserDTOTemp != null)
                        user = new UserDto(String.valueOf(treeUserDTOTemp.getUserNo()), treeUserDTOTemp.getName(), treeUserDTOTemp.getAvatarUrl());
                }
                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_GROUP_NEW);
                break;
            case 2:
                if (chattingDto.getWriterUser() == userID) {
                    user = new UserDto(String.valueOf(temp.Id), temp.FullName, temp.avatar);
                    if (chattingDto.getAttachInfo() != null) {
                        if (chattingDto.getAttachInfo().getType() == 1)
                            chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELF_IMAGE);
                        else {
                            // Check is video or normal file
                            String filename = chattingDto.getAttachInfo().getFileName();
                            if (Utils.isVideo(filename)) {
                                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELF_VIDEO);
                            } else {
                                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELF_FILE);
                            }
                        }
                    }
                } else {
                    TreeUserDTOTemp treeUserDTOTemp = Utils.GetUserFromDatabase(listTemp, chattingDto.getWriterUser());
                    if (treeUserDTOTemp != null)
                        user = new UserDto(String.valueOf(treeUserDTOTemp.getUserNo()), treeUserDTOTemp.getName(), treeUserDTOTemp.getAvatarUrl());
                    if (chattingDto.getAttachInfo() != null)
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
                                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_PERSON_FILE_NOT_SHOW);
                            } else {
                                // Check is video or normal file
                                String filename = chattingDto.getAttachInfo().getFileName();
                                if (Utils.isVideo(filename)) {
                                    chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_PERSON_VIDEO_NOT_SHOW);
                                } else {
                                    chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_PERSON_FILE);
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
                    if (treeUserDTOTemp != null)
                        user = new UserDto(String.valueOf(treeUserDTOTemp.getUserNo()), treeUserDTOTemp.getName(), treeUserDTOTemp.getAvatarUrl());
                    chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_PERSON);
                }
                break;
        }

        if (chattingDto.getType() == 2) {
            if (chattingDto.getAttachInfo() == null) {
                return;
            }
        }

        if (CurrentChatListFragment.fragment != null) {
            chattingDto.setUnReadCount(0);
            CurrentChatListFragment.fragment.updateData(chattingDto);
        }

        chattingDto.setUser(user);
        chattingDto.setContent(chattingDto.getMessage());


        dataSet.set(position, chattingDto);
        adapterList.notifyItemChanged(position);
        if (callBack != null) callBack.onFinish();
    }

    private void addData(List<ChattingDto> list) {
        for (int i = 0; i < list.size(); i++) {
            ChattingDto chattingDto = list.get(i);
            if(list.size() == 1 && dataSet.size() <= 0) {
                ChattingDto time = new ChattingDto();
                dataSet.add(time);
                time.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                time.setTime(new Date(TimeUtils.getTime(chattingDto.getRegDate())).getTime());
                time.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(new Date(TimeUtils.getTime(chattingDto.getRegDate())).getTime() - 100 + ""));
            } else if(list.size() > 1 && dataSet.size() > 1) {
                String date1 = chattingDto.getRegDate();
                String date2 = dataSet.size() == 1 ? dataSet.get(0).getRegDate() : dataSet.get(dataSet.size() - 1).getRegDate();

                if (!TimeUtils.checkBetweenDate(date1, date2)) {
                    ChattingDto time = new ChattingDto();
                    dataSet.add(time);
                    time.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                    time.setTime(new Date(TimeUtils.getTime(chattingDto.getRegDate())).getTime());
                    time.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(new Date(TimeUtils.getTime(chattingDto.getRegDate())).getTime() - 100 + ""));
                }
            }

            addNewChat(chattingDto, false, false);
        }
    }

    private void initData(List<ChattingDto> list) {
        dataSet.clear();
        for (int i = 0; i < list.size(); i++) {
            ChattingDto chattingDto = list.get(i);
            if (i == 0) {
                ChattingDto time = new ChattingDto();
                dataSet.add(time);
                time.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                time.setTime(new Date(TimeUtils.getTime(chattingDto.getRegDate())).getTime());
                time.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(new Date(TimeUtils.getTime(chattingDto.getRegDate())).getTime() - 100 + ""));
            } else {
                if (!TimeUtils.checkBetweenDate(chattingDto.getRegDate(), list.get(i - 1).getRegDate())) {
                    ChattingDto time = new ChattingDto();
                    dataSet.add(time);
                    time.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                    time.setTime(new Date(TimeUtils.getTime(chattingDto.getRegDate())).getTime());
                    time.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(new Date(TimeUtils.getTime(chattingDto.getRegDate())).getTime() - 100 + ""));
                }
            }

            addNewChat(chattingDto, false, false);
        }

        // Scroll to bottom
        if (!hasLoadMore) {
            int a = dataSet.size() - 1;
            if (a >= 0)
                rvMainList.scrollToPosition(a);
            hasLoadMore = false;
        }
    }

    public void addLineToday(ChattingDto chattingDto) {
        // Add new line for new message, it's may be today
        if (dataSet != null) {
            if (dataSet.get(dataSet.size() - 1) != null && dataSet.get(dataSet.size() - 1).getRegDate() != null) {
                if (TimeUtils.checkDateIsToday(dataSet.get(dataSet.size() - 1).getRegDate())) {
                    ChattingDto time = new ChattingDto();
                    time.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                    time.setTime(System.currentTimeMillis());
                    time.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(new Date(TimeUtils.getTime(chattingDto.getRegDate())).getTime() - 100 + ""));
                    dataSet.add(time);
                }
            }
        }
    }

    private void senAction() {
        final String message = view.edt_comment.getText().toString();
        if (!TextUtils.isEmpty(message) && message.length() > 0) {
            view.edt_comment.setText("");
            isSend = false;

            if (dataSet.size() < 2) {
                ChattingDto time = new ChattingDto();
                time.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                time.setTime(System.currentTimeMillis());
                time.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(System.currentTimeMillis() + ""));
                dataSet.add(time);
            } else {
                String date = dataSet.get(dataSet.size() - 1).getRegDate();
                if (date != null && !TimeUtils.checkDateIsToday(date)) {
                    ChattingDto time = new ChattingDto();
                    time.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                    time.setTime(System.currentTimeMillis());
                    time.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(System.currentTimeMillis() + ""));
                    dataSet.add(time);
                }
            }

            // Add new chat before send, and resend if it sent failed
            final ChattingDto newDto = new ChattingDto();
            newDto.setMessageNo(Long.MAX_VALUE);
            newDto.setMessage(message);
            newDto.setUserNo(userID);
            newDto.setType(Statics.MESSAGE_TYPE_NORMAL);
            newDto.setRoomNo(roomNo);
            newDto.setUnReadCount(ChattingActivity.userNos.size() - 1);
            newDto.setWriterUser(userID);
            newDto.setHasSent(true);
            newDto.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(System.currentTimeMillis() + ""));
            newDto.setId(new Random().nextInt());
            newDto.isSendding = true;
            addNewChat(newDto, true, false);
            if (addDataFromServer(newDto)) dataFromServer.add(newDto);

            final boolean isNetWork;
            isNetWork = Utils.isNetworkAvailable();

            HttpRequest.getInstance().SendChatMsg(roomNo, message, new SendChatMessage() {
                @Override
                public void onSendChatMessageSuccess(final ChattingDto chattingDto) {
                    isSend = true;
                    newDto.setHasSent(true);
                    newDto.setMessageNo(chattingDto.getMessageNo());
                    newDto.setUnReadCount(chattingDto.getUnReadCount());
                    newDto.setRegDate(chattingDto.getRegDate());
                    adapterList.notifyDataSetChanged();
                    sendComplete = false;
                }

                @Override
                public void onSendChatMessageFail(ErrorDto errorDto, String url) {
                    newDto.isSendding = false;
                    sendComplete = false;
                    isSend = true;
                    if (isNetWork) {
                        newDto.setHasSent(true);
                    } else {
                        newDto.setUnReadCount(0);
                        newDto.setHasSent(false);
                    }
                    adapterList.notifyDataSetChanged();

                }
            });

            adapterList.notifyDataSetChanged();
            if (dataSet != null) {
                scrollEndList(dataSet.size());
            }
        }
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

    boolean addDataFromServer(ChattingDto chattingDto) {
        boolean flag = false;
        if (dataFromServer != null) {
            flag = true;
            long msgNo = chattingDto.getMessageNo();
            for (ChattingDto obj : dataFromServer) {
                if (msgNo != Long.MAX_VALUE && obj.getMessageNo() == msgNo) {
                    flag = false;
                    break;
                }
            }
        }
        return flag;
    }

    @Override
    public void onResume() {
        super.onResume();

        isVisible = true;
        isActive = true;
        registerGCMReceiver();
        CrewChatApplication.currentRoomNo = roomNo;
    }

    private void shareAction() {
        try {
            if (MainActivity.imageUri != null && type.startsWith("video/")) {
                List<ChattingDto> integerList = new ArrayList<>();
                String path = Utils.getPathFromURI(MainActivity.imageUri, getContext());
                File file = new File(path);
                String filename = path.substring(path.lastIndexOf("/") + 1);
                ChattingDto chattingDto = new ChattingDto();
                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELECT_VIDEO);
                chattingDto.setAttachFilePath(path);
                chattingDto.setAttachFileName(filename);
                chattingDto.setAttachFileSize((int) file.length());
                ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);

                chattingDto.setPositionUploadImage(ChattingFragment.instance.dataSet.size() - 1);
                integerList.add(chattingDto);
                sendFileWithQty_v2(integerList, 0);
                MainActivity.imageUri = null;
                mSelectedImage.clear();
                integerList.clear();
            } else if (MainActivity.imageUri != null && type.startsWith("audio/")) {
                String path = Utils.getPathFromURI(MainActivity.imageUri, getContext());
                sendAudioV2(path);
                MainActivity.imageUri = null;
                mSelectedImage.clear();
            } else if (MainActivity.imageUri != null && type.startsWith("text/")) {
                ContentResolver cr = getActivity().getContentResolver();
                InputStream stream = null;
                try {
                    stream = cr.openInputStream(MainActivity.imageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                StringBuffer fileContent = new StringBuffer();
                int ch;
                try {
                    while ((ch = stream.read()) != -1)
                        fileContent.append((char) ch);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String data = new String(fileContent);
                final ChattingDto chattingDto = new ChattingDto();
                UserDto userDto = new UserDto();
                userDto.setFullName(data);
                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_CONTACT);
                chattingDto.setUser(userDto);
                chattingDto.setMessage(data);
                chattingDto.setHasSent(false);
                chattingDto.setUserNo(Utils.getCurrentId());
                chattingDto.setRoomNo(roomNo);
                chattingDto.setWriterUser(Utils.getCurrentId());
                // perform update when send message success
                String currentTime = System.currentTimeMillis() + "";
                String time = TimeUtils.convertTimeDeviceToTimeServerDefault(currentTime);
                chattingDto.setRegDate(time);
                Log.d(TAG, "addNewRow 6");
                /*final long lastId = ChatMessageDBHelper.addSimpleMessage(chattingDto);*/

                /** SEND MESSAGE Anderson*/
                HttpRequest.getInstance().SendChatMsg(roomNo, data + "\n", new SendChatMessage() {
                    @Override
                    public void onSendChatMessageSuccess(final ChattingDto chattingDto1) {
                        // update old chat message model --> messageNo from server
                        chattingDto.setHasSent(true);
                        chattingDto.setMessage(chattingDto1.getMessage());
                        chattingDto.setMessageNo(chattingDto1.getMessageNo());
                        chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_CONTACT);
                        chattingDto.setUnReadCount(chattingDto1.getUnReadCount());
                        // perform update when send message success
                        String time = TimeUtils.convertTimeDeviceToTimeServerDefault(chattingDto1.getRegDate());
                        chattingDto.setRegDate(time);

                        /*new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ChatMessageDBHelper.updateMessage(chattingDto, lastId);
                            }
                        }).start();*/
                    }

                    @Override
                    public void onSendChatMessageFail(ErrorDto errorDto, String url) {

                    }
                });
                MainActivity.imageUri = null;
            } else {
                if (mSelectedImage != null && mSelectedImage.size() > 0) {
                    if (mSelectedImage.size() > 10) {
                        Toast.makeText(getContext(), "Limit is 10 file", Toast.LENGTH_SHORT).show();
                    } else {
                        final List<ChattingDto> integerList = new ArrayList<>();
                        for (String path : mSelectedImage) {
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
                                addNewRowFromChattingActivity(chattingDto);

                                chattingDto.setPositionUploadImage(dataSet.size() - 1);
                                integerList.add(chattingDto);
                            } else {
                                Toast.makeText(getContext(), getResources().getString(R.string.can_not_send_this_file) + " " + filename, Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (integerList.size() > 0) {
                            ChattingFragment.instance.sendFileWithQty_v2(integerList, 0);
                            mSelectedImage.clear();
                            MainActivity.imageUri = null;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shareInterFace() {
        if (waitTimer != null) {
            waitTimer.cancel();
            waitTimer = null;
        }

        waitTimer = new CountDownTimer(2000, 300) {
            public void onTick(long millisUntilFinished) {
                if (mSelectedImage != null && mSelectedImage.size() > 0) {
                    ChattingActivity.instance.showProgressDialog();
                } else if (MainActivity.imageUri != null) {
                    ChattingActivity.instance.showProgressDialog();
                }
                //called every 300 milliseconds, which could be used to
                //send messages or some other action
            }

            public void onFinish() {
                try {

                    ChattingActivity.instance.dismissProgressDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //After 60000 milliseconds (60 sec) finish current
                //if you would like to execute something when time finishes
                shareAction();
            }
        }.start();
    }

    private void sendAudioV2(String path) {
        List<ChattingDto> integerList = new ArrayList<>();
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
            addNewRowFromChattingActivity(chattingDto);
            chattingDto.setPositionUploadImage(dataSet.size() - 1);
            integerList.add(chattingDto);
            sendFileWithQty_v2(integerList, 0);
        }
    }

    private final static int MSG_UPDATE_DISPLAY = 2;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_DISPLAY:
                    // TODO
                    String text = (String) msg.obj; // get contents
                    if (text == null) text = "";
                    if (view.edt_comment != null)
                        view.edt_comment.setText(text);
                    break;
            }
        }
    };

    void set_msg_for_edit_text() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String message = "";
                try {
                    Prefs prefs = new Prefs();
                    String data = prefs.getMsgNotSend();
                    if (data.length() == 0) return;
                    Type listType = new TypeToken<ArrayList<MessageNotSend>>() {
                    }.getType();
                    List<MessageNotSend> list = new Gson().fromJson(data, listType);
                    int n = list.size();
                    for (int i = 0; i < n; i++) {
                        MessageNotSend obj = list.get(i);
                        if (obj.roomNo == roomNo) {
                            message = obj.msg;
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handler.obtainMessage(MSG_UPDATE_DISPLAY, message).sendToTarget();
            }
        }).start();
    }

    void save_msg_not_send() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                do_save();
            }
        }).start();
    }

    void do_save() {
        Prefs prefs = new Prefs();
        String message = "";
        try {
            message = view.edt_comment.getText().toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String data = prefs.getMsgNotSend();
        try {
            if (data.length() > 0) {
                Type listType = new TypeToken<ArrayList<MessageNotSend>>() {
                }.getType();
                List<MessageNotSend> list = new Gson().fromJson(data, listType);
                boolean flag = true;
                int n = list.size();
                for (int i = 0; i < n; i++) {
                    MessageNotSend obj = list.get(i);
                    if (obj.roomNo == roomNo) {
                        obj.msg = message;
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    //add room
                    MessageNotSend obj = new MessageNotSend();
                    obj.roomNo = roomNo;
                    obj.msg = message;
                    list.add(obj);
                } else {
                    // update msg
                }
                prefs.setMsgNotSend(new Gson().toJson(list));
            } else {
                List<MessageNotSend> list = new ArrayList<>();
                MessageNotSend obj = new MessageNotSend();
                obj.roomNo = roomNo;
                obj.msg = message;
                list.add(obj);
                prefs.setMsgNotSend(new Gson().toJson(list));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        save_msg_not_send();
        isVisible = false;
        isActive = false;
        unregisterGCMReceiver();
        CrewChatApplication.currentRoomNo = 0;
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
    }

    private void showNewMessage(final ChattingDto dto) {
        if (isAdded())
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
                    ivScrollDown.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            scrollToEndList();
                        }
                    });
                }
            });
    }

    private void hideNewMessage() {
        if (mActivity == null) {
            mActivity = getActivity();
        }

        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rlNewMessage.setVisibility(View.GONE);
                }
            });
        }
    }

    private void unregisterGCMReceiver() {
        try {
            mActivity.unregisterReceiver(mReceiverNewAssignTask);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void receiveMessage(final ReceiveMessage message) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                processReceivingMessage(message.getChattingDto());
            }
        });
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
        if (intent.getAction().equals(Statics.ACTION_RECEIVER_NOTIFICATION)) {
            ChattingDto dto = new Gson().fromJson(intent.getStringExtra(Statics.GCM_DATA_NOTIFICATOON), ChattingDto.class);
        } else if (intent.getAction().equals(Constant.INTENT_FILTER_GET_MESSAGE_UNREAD_COUNT)) {
            final long roomNo = intent.getLongExtra(Constant.KEY_INTENT_ROOM_NO, 0);
            if (roomNo != 0 && dataFromServer.size() > 0) {
                HttpRequest.getInstance().GetMessageUnreadCount(roomNo, dataFromServer.get(dataFromServer.size() - 1).getRegDate(), new OnGetMessageUnreadCountCallBack() {
                    @Override
                    public void onHTTPSuccess(String result) {
                        Type listType = new TypeToken<List<MessageUnreadCountDTO>>() {
                        }.getType();
                        List<MessageUnreadCountDTO> list = new Gson().fromJson(result, listType);

                        for (final MessageUnreadCountDTO messageUnreadCountDTO : list) {
                            for (int i = dataSet.size() - 1; i > -1; i--) {
                                final ChattingDto chattingDto = dataSet.get(i);

                                if (chattingDto.getMessageNo() == messageUnreadCountDTO.getMessageNo()) {
                                    ChatMessageDBHelper.updateUnReadCount(chattingDto, roomNo, messageUnreadCountDTO.getMessageNo(), messageUnreadCountDTO.getUnreadCount());
                                    if (chattingDto.getUnReadCount() != messageUnreadCountDTO.getUnreadCount()) {
                                        chattingDto.setUnReadCount(messageUnreadCountDTO.getUnreadCount());
                                        adapterList.notifyItemChanged(i);
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onHTTPFail(ErrorDto errorDto) {
                    }
                });
            }
        } else if (intent.getAction().equals(Constant.INTENT_FILTER_ADD_USER)) {
            long roomNo = intent.getLongExtra(Constant.KEY_INTENT_ROOM_NO, 0);
            if (roomNo != 0 && roomNo == ChattingFragment.this.roomNo) {
                Reload();
            }
        } else if (intent.getAction().equals(Constant.INTENT_FILTER_UPDATE_ROOM_NAME)) {
            Log.d(TAG, "INTENT_FILTER_UPDATE_ROOM_NAME");
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
            Log.d(TAG, "INTENT_GOTO_UNREAD_ACTIVITY");
            if (intent != null) {
                try {
                    goToUnreadActivity(intent.getLongExtra(Statics.MessageNo, 0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void processReceivingMessage(ChattingDto dataDto) {
        // Add new line for new message, it's may be today
        String baseDate = CrewChatApplication.getInstance().getTimeServer();
        if (dataFromServer != null && dataFromServer.size() > 0) {
            baseDate = dataFromServer.get(dataFromServer.size() - 1).getRegDate();
        }

        HttpRequest.getInstance().UpdateMessageUnreadCount(roomNo, userID, baseDate);

        if (dataSet.size() < 2) {
            ChattingDto time = new ChattingDto();
            time.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
            time.setTime(System.currentTimeMillis());
            time.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(System.currentTimeMillis() + ""));
            dataSet.add(time);
        } else {
            String date = dataSet.get(dataSet.size() - 1).getRegDate();
            if (date != null && !TimeUtils.checkDateIsToday(date)) {
                ChattingDto time = new ChattingDto();
                time.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                time.setTime(System.currentTimeMillis());
                time.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(new Date(TimeUtils.getTime(baseDate)).getTime() - 100 + ""));
                dataSet.add(time);
            }
        }

        boolean checkNotification = true;
        if (dataSet != null) {
            for (ChattingDto chattingDto : dataSet) {
                if (chattingDto.getMessageNo() == dataDto.getMessageNo()) {
                    checkNotification = false;
                    break;
                }
            }
        }

        if (isSendingFile && dataDto != null
                && dataDto.getAttachFileName() != null
                && dataDto.getAttachFileName().length() > 0
                && dataDto.getWriterUser() == Utils.getCurrentId()) {
            checkNotification = false;
        }

        if (checkNotification) {
            boolean isShow = dataDto.getRoomNo() == roomNo;
            if (isShowNewMessage && isShow) {
                showNewMessage(dataDto);
            } else {
                hideNewMessage();
            }

            dataDto.setWriterUser(dataDto.getWriterUserNo());
            dataDto.setCheckFromServer(true);

            if (roomNo == dataDto.getRoomNo()) {
                dataDto.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(dataDto.getRegDate()));
                isFromNotification = true;

                if (!TextUtils.isEmpty(dataDto.getMessage()) || dataDto.getAttachNo() != 0) {

                    if (dataDto.getType() != 6) {
                        msgEnd = -1;
                        isShowIcon = false;
                    }
                    adapterList.notifyDataSetChanged();
                    addNewChat(dataDto, true, true);
                }

                if (CurrentChatListFragment.fragment != null) {
                    dataDto.setUnReadCount(0);
                    CurrentChatListFragment.fragment.updateData(dataDto);
                }
                if (addDataFromServer(dataDto)) dataFromServer.add(dataDto);
            } else {
                dataDto.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(dataDto.getRegDate()));

                if (CurrentChatListFragment.fragment != null) {
                    dataDto.setUnReadCount(0);
                    CurrentChatListFragment.fragment.updateData(dataDto);
                }
            }
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

    private boolean isSendingFile = false;

    public void sendAttachFile(int attachNo, long roomNo, final int position, final WatingUpload callBack, final ChattingDto chattingDto) {
        isSendingFile = true;
        /**SEND ATTACHFILE*/
        HttpRequest.getInstance().SendChatAttachFile(roomNo, attachNo, new SendChatMessage() {
            @Override
            public void onSendChatMessageSuccess(final ChattingDto dto) {
                isSendingFile = false;
                Log.d(TAG, "sendAttachFile success");
                progressBar.setVisibility(View.GONE);
                dto.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(dto.getRegDate()));

                if (CurrentChatListFragment.fragment != null) {
                    dto.setUnReadCount(0);
                    CurrentChatListFragment.fragment.updateData(dto);
                }
                Log.d(TAG, "addNewChat 12");
                dto.setLastedMsgAttachType(chattingDto.getLastedMsgAttachType());
                dto.setLastedMsgType(chattingDto.getLastedMsgType());
                addNewChat(dto, position, callBack);
                Log.d(TAG, "dataFromServer add 2");
                if (addDataFromServer(dto)) dataFromServer.add(dto);
            }

            @Override
            public void onSendChatMessageFail(ErrorDto errorDto, String url) {
                isSendingFile = false;
                progressBar.setVisibility(View.GONE);
            }
        });
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

    public interface WatingUpload {
        void onFinish();
    }

    public void goToUnreadActivity(long msgNo) {
        Intent intent = new Intent(getActivity(), UnreadActivity.class);
        intent.putExtra(Statics.MessageNo, msgNo);
        intent.putExtra("userNos", userNos);
        intent.putExtra(Statics.ROOM_NO, roomNo);
        startActivity(intent);
    }

    public void sendFileWithQty_v2(final List<ChattingDto> integerList,
                                   final int index) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "index:" + index);
                if (integerList == null || integerList.size() == 0 || index >= integerList.size())
                    return;

                final int pos = integerList.get(index).getPositionUploadImage();

                RecyclerView.ViewHolder holder = rvMainList.findViewHolderForAdapterPosition(pos);
                if (holder == null) Log.d(TAG, "holder null: pos:" + pos);
                else Log.d(TAG, "holder not null");

                ProgressBar progressBar = null;
                if (holder != null && holder instanceof ChattingSelfFileViewHolder)
                    progressBar = ((ChattingSelfFileViewHolder) holder).getProgressBar();
                if (progressBar == null) Log.d(TAG, "progressBar null");
                else Log.d(TAG, "progressBar not null");

                SendTo(integerList.get(index), progressBar, integerList.get(index).getPositionUploadImage(), new WatingUpload() {
                    @Override
                    public void onFinish() {
                        int next = index + 1;
                        if (next < integerList.size())
                            sendFileWithQty_v2(integerList, next);
                        else {
                            Log.d(TAG, "finish send image");
                        }
                    }
                });
            }
        }, 500);
    }

    public void sendFileWithQty(final List<ChattingDto> integerList, final int index) {
        Log.d(TAG, "index:" + index);
        if (integerList == null || integerList.size() == 0 || index >= integerList.size())
            return;
        SendTo(integerList.get(index), null, integerList.get(index).getPositionUploadImage(), new WatingUpload() {
            @Override
            public void onFinish() {
                int next = index + 1;
                if (next < integerList.size())
                    sendFileWithQty(integerList, next);
                else Log.d(TAG, "finish send image");
            }
        });
    }

    public void SendTo(ChattingDto chattingDto, ProgressBar progressBar, int position, WatingUpload callBack) {
        Log.d(TAG, "SendTo");
        sendTo++;

        if (view != null) {
            view.selection_lnl.setVisibility(View.GONE);
        }

        new SendToServer(chattingDto, progressBar, position, callBack).execute();
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
        private int position;
        private WatingUpload callBack;

        private SendToServer(ChattingDto chattingDto, ProgressBar progressBar, int position, WatingUpload callBack) {
            this.chattingDto = chattingDto;
            this.progressBar = progressBar;
            this.position = position;
            this.callBack = callBack;
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
            sendAttachFile(integer, roomNo, position, callBack, chattingDto);
        }
    }

    public void Reload() {
        progressBar.setVisibility(View.VISIBLE);
        final List<ChattingDto> listChatMessage = ChatMessageDBHelper.getMsgSession(roomNo, 0, ChatMessageDBHelper.FIRST);

        if (Utils.isNetworkAvailable()) {
            getOnlineData(roomNo, listChatMessage);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    public static boolean isShowIcon = false;

    void setFirstItem() {
        if (dataSet != null) {
            if (dataSet.size() > 0) {
                ChattingDto chattingDto2 = dataSet.get(0);
                chattingDto2.setId(999);
                adapterList.notifyItemChanged(0);
            }
        }
    }

    private void loadMoreData() {
        if (!isLoading && isLoadMore && dataSet.size() > 3) {
            isLoading = true;
            hasLoadMore = true;

            long baseMsgNo = dataFromServer.get(0).getMessageNo();
            String regDate = dataFromServer.get(0).getRegDate();
            ChattingDto chattingDto2 = dataSet.get(0);
            chattingDto2.setId(999);

            adapterList.notifyItemChanged(0);


            List<ChattingDto> localData = ChatMessageDBHelper.getMsgSession(roomNo, baseMsgNo, ChatMessageDBHelper.BEFORE);

            if (Utils.isNetworkAvailable()) {
                if (localData != null)
                    localData.clear();
            }

            if (localData != null && localData.size() > 0) {
                long currentMessageNo = 0;
                for (ChattingDto dto : dataSet) {
                    if (dto.getMessageNo() > 0) {
                        currentMessageNo = dto.getMessageNo();
                        break;
                    }
                }

                dataFromServer.addAll(0, localData);
                dataFromServer = Constant.sortTimeList(dataFromServer);
                initData(dataFromServer);
                int currentPosition = 0, length = dataSet.size();
                for (int i = 0; i < length; i++) {
                    if (dataSet.get(i).getMessageNo() == currentMessageNo) {
                        currentPosition = i;
                        break;
                    }
                }
                adapterList.notifyDataSetChanged();
                layoutManager.scrollToPositionWithOffset(currentPosition, 0);
                isLoading = false;
                setFirstItem();
            } else {
                HttpRequest.getInstance().GetChatMsgSection(roomNo, 2, regDate, new OnGetChatMessage() {
                    @Override
                    public void OnGetChatMessageSuccess(final List<ChattingDto> listNew) {
                        if (listNew.size() > 0) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    for (ChattingDto chat : listNew) {
                                        addMessage(chat);
                                    }
                                }
                            }).start();

                            long currentMessageNo = 0;

                            for (ChattingDto dto : dataSet) {
                                if (dto.getMessageNo() > 0) {
                                    currentMessageNo = dto.getMessageNo();
                                    break;
                                }
                            }
                            dataFromServer.addAll(0, listNew);
                            dataFromServer = Constant.sortTimeList(dataFromServer);
                            initData(dataFromServer);

                            int currentPosition = 0, length = dataSet.size();

                            for (int i = 0; i < length; i++) {
                                if (dataSet.get(i).getMessageNo() == currentMessageNo) {
                                    currentPosition = i;
                                    break;
                                }
                            }

                            adapterList.notifyDataSetChanged();
                            layoutManager.scrollToPositionWithOffset(currentPosition, 0);

                            isLoading = false;
                            setFirstItem();
                        } else {
                            ChattingDto chattingDto2 = dataSet.get(0);
                            chattingDto2.setId(0);

                            adapterList.notifyItemChanged(0);
                            isLoadMore = false;

                        }
                    }

                    @Override
                    public void OnGetChatMessageFail(ErrorDto errorDto) {
                        ChattingDto chattingDto2 = dataSet.get(0);
                        chattingDto2.setId(0);
                        adapterList.notifyItemChanged(0);
                    }
                });
            }
        } else {
            if (dataSet != null) {
                if (dataSet.size() > 0) {
                    dataSet.get(0).setId(0);
                    adapterList.notifyItemChanged(0);
                    isLoading = false;
                }
            }
        }
    }

    private void refFreshData() {
        try {
            dataFromServer = Constant.sortTimeList(dataFromServer);
            initData(dataFromServer);
            adapterList.notifyDataSetChanged();
        } catch (Exception e) {
        }
    }

    public void ViewImageFull(ChattingDto chattingDto) {
        ArrayList<ChattingDto> urls = new ArrayList<>();
        int position = 0;

        for (ChattingDto chattingDto1 : dataFromServer) {
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
}