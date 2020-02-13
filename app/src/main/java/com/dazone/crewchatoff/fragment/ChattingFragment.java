package com.dazone.crewchatoff.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import static com.dazone.crewchatoff.activity.MainActivity.mSelectedImage;
import static com.dazone.crewchatoff.activity.MainActivity.type;
import static com.dazone.crewchatoff.database.ChatMessageDBHelper.addMessage;

public class ChattingFragment extends ListFragment<ChattingDto> implements View.OnClickListener, EmojiView.EventListener, View.OnLayoutChangeListener, View.OnKeyListener, TextView.OnEditorActionListener {

    private String TAG = ">>>ChattingFragment";
    public long roomNo;
    private ArrayList<Integer> userNos;
    public boolean isActive = false;

    public ChatInputView view;
    private ArrayList<TreeUserDTOTemp> listTemp = null;
    private int userID;
    public static ChattingFragment instance;
    private List<ChattingDto> dataFromServer = new ArrayList<>();
    public boolean isVisible = false;
    public boolean isUpdate = false;
    private boolean isLoading = false;
    private boolean isLoadMore = true;
    private boolean isLoadFist = false;
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
    private int code;
    CountDownTimer waitTimer;

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    protected final android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_CODE_HIDE_PROCESS) {
                progressBar.setVisibility(View.GONE);

            } else if (msg.what == WHAT_CODE_ADD_NEW_DATA) {
                //code = WHAT_CODE_ADD_NEW_DATA;
                mPrefs.putIntValue(Statics.VALUE_CODE_SHARE, WHAT_CODE_ADD_NEW_DATA);
                mPrefs.putBooleanValue(Statics.IS_FIRST_SHARE, true);
                //isFirst = mPrefs.getBooleanValue(Statics.IS_FIRST_SHARE, true);
                Bundle args = msg.getData();
                dataFromServer = (ArrayList<ChattingDto>) args.getSerializable(ADD_NEW_DATA);
                // prepare this
                boolean hasInit = false;
                if (msg.arg1 == WHAT_CODE_HAS_INIT) {
                    hasInit = true;
                }
                addData(dataFromServer, hasInit);
                if (layoutManager != null) {
                    scrollToEndList();
                    Log.d(TAG, "finish add dataFromServer:" + " scrollToEndList();");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setStackFromEnd();
                            Log.d(TAG, "finish add dataFromServer:" + "  setStackFromEnd()");

                        }
                    }, 500);
                }
                logLstDB();
                getFirstDB();


            } else if (msg.what == WHAT_CODE_EMPTY) {
                initData();

            }
            //isFirst = mPrefs.getBooleanValue(Statics.IS_FIRST_SHARE, true);
            code = mPrefs.getIntValue(Statics.VALUE_CODE_SHARE, WHAT_CODE_ADD_NEW_DATA);
            if (!isSendingFile) {
                shareFileRv();
            }
        }
    };

    void logLstDB() {
//        for (ChattingDto obj : dataSet) {
//            Log.d(TAG, new Gson().toJson(obj));
//        }
    }

    void getFirstDB() {

        setFirstItem();
//        Log.d(TAG,new Gson().toJson(dataSet.get(0)));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.isShowIcon = false;
        msgEnd = -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        instance = this;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mPrefs = CrewChatApplication.getInstance().getPrefs();
        msgEnd = -1;
        this.isShowIcon = false;
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
        Log.d(TAG, "roomNo:" + roomNo);
        Constant.cancelAllNotification(CrewChatApplication.getInstance(), (int) roomNo);
        Log.d(TAG, "finish onCreate");
        Log.d("sssDebugdataa", dataSet.size() + "");

    }


//----------------------->Son edit 21/11/2017

    /**
     * enter key auto get from Sharef
     *
     * @return
     */
    private boolean isGetValueEnterAuto() {
        boolean isEnable = false;
        isEnable = mPrefs.getBooleanValue(Statics.IS_ENABLE_ENTER_KEY, isEnable);
        return isEnable;
    }

    //--------------end Son edit
    @Subscribe
    public void reloadMessageWhenNetworkReConnect(ReloadListMessage reloadListMessage) {
        refFreshData();
    }

    //--------------end Son edit
    private void loadClientData() {
        isLoaded = true;
        final List<ChattingDto> listChatMessage = ChatMessageDBHelper.getMsgSession(roomNo, 0, ChatMessageDBHelper.FIRST);

        dataFromServer = listChatMessage;
        if (listChatMessage != null && listChatMessage.size() > 0) {
            Log.d(TAG, "listChatMessage != null");
            for (ChattingDto obj : listChatMessage) {
                Log.d(TAG, "loadClientData:"
                        + "  -  " + obj.getMessage() + "  -  "
                        + obj.isHasSent() + "  -  " + obj.getMessageNo()
                        + " - " + obj.getRegDate());
            }
            Log.d(TAG, "initData 1");
            initData(listChatMessage, 1);
        }
        if (Utils.isNetworkAvailable()) {
            Log.d(TAG, "isNetworkAvailable");
            getOnlineData(roomNo, listChatMessage);
//            updateUnreadCount(roomNo, startNo);
        } else {
            progressBar.setVisibility(View.GONE);
            if (listChatMessage != null && listChatMessage.size() == 0) {
                Log.d(TAG, "listChatMessage !=null initData");
                initData();
            }
        }
    }

    private void updateUnreadCount(long roomNo, long startNo) {
        HttpRequest.getInstance().UpdateMessageUnreadCount(roomNo, userID, startNo);
    }

    long startNo = 0;

    // Thread to get data from server
    private void getOnlineData(final long roomNo, final List<ChattingDto> listChatMessage) {
        Log.d(TAG, "getOnlineData");

//        new Thread(new Runnable() {
//            @Override
//            public void run() {

        final int listMessageSize = listChatMessage.size();
        if (listMessageSize > 0) {
            int last_index = 1;
            int unreadCount = 0;
            try {
                unreadCount = ChatRoomDBHelper.getUnreadCount(roomNo);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(TAG, "unreadCount:" + unreadCount);
            if (unreadCount > 0) {
                last_index += unreadCount;
            }
            int index = listMessageSize - last_index;
            if (index < 0) {
                index = 0;
            }
            startNo = listChatMessage.get(index).getMessageNo();
        }
//        // Call API to update this message
        updateUnreadCount(roomNo, startNo);

        int mesType = startNo == 0 ? ChatMessageDBHelper.FIRST : ChatMessageDBHelper.AFTER;
//        // Get all message from standard message
        Log.d(TAG, "roomNo:" + roomNo);
        Log.d(TAG, "startNo:" + startNo);
        Log.d(TAG, "mesType:" + mesType);
        Log.d(TAG, "URL_GET_CHAT_MSG_SECTION");

//        HttpRequest.getInstance().GetChatMsgSection_v2(roomNo, startNo, mesType, null);
        HttpRequest.getInstance().GetChatMsgSection(roomNo, startNo, mesType, new OnGetChatMessage() {
            @Override
            public void OnGetChatMessageSuccess(List<ChattingDto> listNew) {
//                for(ChattingDto obj:listNew){
//                    Log.d(TAG,"OnGetChatMessageSuccess:"+new Gson().toJson(obj));
//                }
                Log.d(TAG, "OnGetChatMessageSuccess");
                // hide progressBar when loading data from server is success
                mHandler.obtainMessage(WHAT_CODE_HIDE_PROCESS).sendToTarget();
                isLoaded = true;
                // perform thread to sync data server with client
                // add to current data list and notify dataset
                ArrayList<ChattingDto> newDataFromServer = new ArrayList<>();
                if (listNew.size() > 0) {
                    /*
                     * Change follow --> just update unReadCount when loading image success
                     * */
                    // Update unread count to server
                    long startMsgNo = listNew.get(listNew.size() - 1).getMessageNo();
                    // Update unRead count message
                    updateUnreadCount(roomNo, startMsgNo);
                    if (CurrentChatListFragment.fragment != null) {
                        CurrentChatListFragment.fragment.updateRoomUnread(roomNo);
                        //CurrentChatListFragment.fragment.updateRoomUnread(roomNo, listNew.get(listNew.size() - 1).getUnReadCount());
                    }
                    if (RecentFavoriteFragment.instance != null) {
                        RecentFavoriteFragment.instance.updateRoomUnread(roomNo);
                        // CurrentChatListFragment.fragment.updateRoomUnread(roomNo, listNew.get(listNew.size() - 1).getUnReadCount());
                    }
                    // Save online data to local data
                    for (ChattingDto chat : listNew) {
                        boolean isExist = false;
                        for (ChattingDto dto : dataFromServer) {
                            if (chat.getMessageNo() == dto.getMessageNo()) {
                                // My be update something
                                isExist = true;
                                break;
                            }
                        }
                        // Check if message is exist
                        if (!isExist) {
                            // add to server and save to local database
                            Log.d(TAG, "dataFromServer add 5");
                            if (addDataFromServer(chat)) dataFromServer.add(chat);
                            newDataFromServer.add(chat);
                            Log.d(TAG, "addMessage 1:");
                            addMessage(chat);
                        }
                    }
                    // notify database
                    if (newDataFromServer.size() > 0) {
                        Log.d(TAG, "newDataFromServer > 0");
                        // Need to send array list object via handler
                        Message message = Message.obtain();
                        message.what = WHAT_CODE_ADD_NEW_DATA;
                        if (listMessageSize > 0) {
                            message.arg1 = WHAT_CODE_HAS_INIT;
                        }
                        Bundle args = new Bundle();
                        args.putSerializable(ADD_NEW_DATA, newDataFromServer);
                        message.setData(args);
                        mHandler.sendMessage(message);
                        Log.d(TAG, "mHandler after sendMessage");
                    } else {
                        Log.d(TAG, "finish add newDataFromServer <= 0:");
                        logLstDB();
                        getFirstDB();
                        if (listChatMessage.size() == 0) {
                            mHandler.obtainMessage(WHAT_CODE_EMPTY).sendToTarget();
                        } else {
                            updateDataServer();
                        }
                    }
                } else {
                    Log.d(TAG, "finish add listNew <= 0:");
                    logLstDB();
                    getFirstDB();
                }
            }

            @Override
            public void OnGetChatMessageFail(ErrorDto errorDto) {
                Log.d(TAG, "finish add OnGetChatMessageFail:");
                logLstDB();
                getFirstDB();
                isLoaded = true;
                mHandler.obtainMessage(WHAT_CODE_HIDE_PROCESS).sendToTarget();

                if (listChatMessage.size() == 0) {
                    mHandler.obtainMessage(WHAT_CODE_EMPTY).sendToTarget();
                } else {
                    // updateDataServer();
                }
            }
        });

//            }
//        }).start();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (CompanyFragment.instance != null) listTemp = CompanyFragment.instance.getUser();
        if (listTemp == null || listTemp.size() == 0)
            listTemp = AllUserDBHelper.getUser_v2();
        if (listTemp == null) listTemp = new ArrayList<>();

//        if (listTemp != null && listTemp.size() == 0) {
//            Log.d(TAG,"URL_GET_ALL_USER_BE_LONGS 1");
//            HttpRequest.getInstance().GetListOrganize(new IGetListOrganization() {
//                @Override
//                public void onGetListSuccess(final ArrayList<TreeUserDTOTemp> treeUserDTOs) {
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            AllUserDBHelper.addUser(treeUserDTOs);
//                            Log.d(TAG, "addUser 1");
//                            listTemp.addAll(treeUserDTOs);
//                        }
//                    }).start();
//                }
//
//                @Override
//                public void onGetListFail(ErrorDto dto) {
//                }
//            });
//        }
        // Load client data at first, then call load online data on new thread
        // Just load on the first time
        if (!isLoaded) {
            Log.d(TAG, "!isLoaded");
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
        ChattingFragment fragment = new ChattingFragment();
        fragment.setActivity(activity);
        Bundle args = new Bundle();
        args.putLong(Constant.KEY_INTENT_ROOM_NO, roomNo);
        args.putIntegerArrayList(Constant.KEY_INTENT_USER_NO_ARRAY, userNos);


        fragment.setArguments(args);
        return fragment;
    }

    public ChattingFragment newInstance(TreeUserDTO dto1, ChattingDto chattingDto1, Activity activity) {
        ChattingFragment fragment = new ChattingFragment();
        fragment.setActivity(activity);
        Bundle args = new Bundle();
        args.putSerializable(Statics.TREE_USER_PC, dto1);
        args.putSerializable(Statics.CHATTING_DTO, chattingDto1);
        fragment.setArguments(args);
        return fragment;
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


//        layoutManager = new WrapContentLinearLayoutManager(getActivity());
        rvMainList.setLayoutManager(layoutManager);
    }

    @Override
    protected void reloadContentPage() {
    }


    @Override
    protected void initList() {

        view = new ChatInputView(getContext());
        view.addToView(recycler_footer);
        view.mEmojiView.setEventListener(this);
        view.btnSend.setOnClickListener(this);
        view.edt_comment.setOnKeyListener(this);
        view.edt_comment.setOnEditorActionListener(this);
        /*   Log.d("sssDebug",view.edt_comment.getInputType()+"");*/
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

//        view.btnVoice.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.press_hold),Toast.LENGTH_SHORT).show();
//            }
//        });


        // btnVoice
        view.btnVoice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "ACTION_DOWN");

//                        startRecording();
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

    private boolean isThreadRunning = false;
    private Handler handlerTimer = new Handler();
    private int timeDelay = 1000;
    private int timerCount = -1;
    private Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
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
            //Send(path);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.can_not_send_this_file) + " " + filename, Toast.LENGTH_SHORT).show();
        }
    }

    public void addNewRowFromChattingActivity(ChattingDto chattingDto) {
        addLineToday();
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
//            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
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
            Log.d(TAG, "isSuccess:" + isSuccess);
//            if (isSuccess) {
//            }
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
//            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);  //16/11/2017
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

    int recordTouch = 0;
    private TextView tvDurationDialog;
    private boolean isFlag = false;

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
        tvDurationDialog = (TextView) dialog.findViewById(R.id.tvDuration);
        // ivRecord
        final ImageView ivRecord = (ImageView) dialog.findViewById(R.id.ivRecord);
        // btnClose
        FrameLayout btnClose = (FrameLayout) dialog.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecordingFromDialog();
                dialog.dismiss();
            }
        });
        // btnSendRecord
        final Button btnSendRecord = (Button) dialog.findViewById(R.id.btnSendRecord);
        btnSendRecord.setEnabled(false);
        btnSendRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAudio();
                dialog.dismiss();
            }
        });

        // btnRecord

        FrameLayout btnRecord = (FrameLayout) dialog.findViewById(R.id.btnRecord);
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
//                    File file = new File(path);
//                    if (file.exists()) {
//                        Constant.openFile(getActivity(), file);
//                    }
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

//        Log.d(TAG, new Gson().toJson(chattingDto));
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
                            Log.d(TAG, "filename:" + filename);
                            if (Utils.isVideo(filename)) {
                                Log.d(TAG, "isVideo 1");
                                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELF_VIDEO);
                            } else {
                                Log.d(TAG, "Not isVideo 1");
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
                CurrentChatListFragment.fragment.updateData(chattingDto, false);
            }
        }

        chattingDto.setUser(user);
        chattingDto.setContent(chattingDto.getMessage());
        if (chattingDto.getMessageNo() == Long.MAX_VALUE) {
//            Log.d(TAG, "dataSet.add msgNo 1:" + chattingDto.getMessageNo() + " getRegDate" + chattingDto.getRegDate());
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
//                        Log.d(TAG, "dataSet.add msgNo 2:" + chattingDto.getMessageNo() + " getRegDate" + chattingDto.getRegDate());
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
                return (o1.getMessageNo() == 0 || o2.getMessageNo()== 0 || (o1.getMessageNo() == o2.getMessageNo())) ? 0:
                        o1.getMessageNo() < o2.getMessageNo() ? -1 : 1;
//                o1.getMessageNo() < o2.getMessageNo() ? -1 :
//                        o1.getMessageNo() > o2.getMessageNo() ? 1 : 0;
            }
        });

        notifyItemInserted();

        setStackFromEnd();

        if (layoutManager.findLastCompletelyVisibleItemPosition() == dataSet.size() - 2) {
            int b = dataSet.size() - 1;
            if (b >= 0)
                layoutManager.scrollToPosition(b);
        }
        if (isFromNotification) {
            isFromNotification = false;
        }
    }

    private void setStackFromEnd() {
        int firstItem = layoutManager.findFirstCompletelyVisibleItemPosition();
        Log.d(TAG, "firstItem:" + firstItem);
        if (firstItem == 0) {
            layoutManager.setStackFromEnd(false);
        } else {
            layoutManager.setStackFromEnd(true);
            // layoutManager.setReverseLayout(true);
        }

        if (dataSet != null && dataSet.size() == 1) {
            layoutManager.setStackFromEnd(false);
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


    public static long msgEnd = -1;
//    int isScroll = 0;

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
                                Log.d(TAG, "isVideo 3");
                                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELF_VIDEO);
                            } else {
                                Log.d(TAG, "Not isVideo 3");
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
            Log.d(TAG, "getLastedMsgAttachType:" + chattingDto.getLastedMsgAttachType());
            Log.d(TAG, "setLastedMsgType:" + chattingDto.getLastedMsgType());

            CurrentChatListFragment.fragment.updateData(chattingDto, false);
        }

        chattingDto.setUser(user);
        chattingDto.setContent(chattingDto.getMessage());


        dataSet.set(position, chattingDto);
        Log.d(TAG, "dataSet set 2:" + chattingDto.getMessageNo() + " - " + chattingDto.getMessage());
        Log.d(TAG, "adapterList.notifyItemChanged(position);");
        adapterList.notifyItemChanged(position);
        if (callBack != null) callBack.onFinish();

        // Go to the bottom
//        rvMainList.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                rvMainList.scrollToPosition(dataSet.size() - 1);
//            }
//        }, 1000);
    }

    private void addData(List<ChattingDto> list, boolean hasInit) {
        for (int i = 0; i < list.size(); i++) {
            ChattingDto chattingDto = list.get(i);
            if (i == 0) {
                long isTime = TimeUtils.getTimeForMail(TimeUtils.getTime(chattingDto.getRegDate()));
                if (isTime == -2) {
                    if (!hasInit) {
                        ChattingDto time = new ChattingDto();
                        dataSet.add(time);
                        Log.d(TAG, "dataSet add time 1 Today 1");
                        time.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                        time.setRegDate(Utils.getString(R.string.today));
                    } else {
                        if (dataSet.size() > 0) {
                            long noTemp = TimeUtils.getTime((dataSet.get(dataSet.size() - 1).getRegDate()));
                            long noTemp2 = TimeUtils.getTime((chattingDto.getRegDate()));
                            if (!TimeUtils.compareTime(noTemp, noTemp2)) {
                                ChattingDto time = new ChattingDto();
                                dataSet.add(time);
                                Log.d(TAG, "else dataSet add time 1 Today");
                                time.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                                time.setRegDate(Utils.getString(R.string.today));
                            }
                        }
                    }
                    Log.d(TAG, "hasInit:" + hasInit);
                    Log.d(TAG, "addNewChat 1");
                    addNewChat(chattingDto, false, false);
                } else {
                    ChattingDto time = new ChattingDto();
                    dataSet.add(time);
                    Log.d(TAG, "dataSet add time 2 getMessage:" + time.getMessage() + " : getMessageNo:" + time.getMessageNo());
                    time.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                    time.setTime(TimeUtils.getTime(list.get(0).getRegDate()));
                    Log.d(TAG, "addNewChat 2");
                    addNewChat(chattingDto, false, false);
                }
            } else {
                long noTemp = TimeUtils.getTime((list.get(i - 1).getRegDate()));
                long noTemp2 = TimeUtils.getTime((chattingDto.getRegDate()));
                long isTime = TimeUtils.getTimeForMail(noTemp2);
                if (TimeUtils.compareTime(noTemp, noTemp2)) {
                    Log.d(TAG, "addNewChat 3:");
                    addNewChat(chattingDto, false, false);
                } else {
                    if (isTime == -2) {
                        if (!hasInit) {
                            ChattingDto time = new ChattingDto();
                            dataSet.add(time);
                            Log.d(TAG, "dataSet add time 3 Today getMessage:" + time.getMessage() + " : getMessageNo:" + time.getMessageNo());
                            time.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                            time.setRegDate(Utils.getString(R.string.today));
                        }
                    } else {
                        ChattingDto time2 = new ChattingDto();
                        dataSet.add(time2);
                        Log.d(TAG, "dataSet add time 4 Today getMessage:" + time2.getMessage() + " : getMessageNo:" + time2.getMessageNo());
                        time2.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                        time2.setRegDate(chattingDto.getRegDate());
                    }
                    Log.d(TAG, "addNewChat 4");
                    addNewChat(chattingDto, false, false);
                }
            }

        }
    }


    private void initData(List<ChattingDto> list, final int firstOpen) {

        dataSet.clear();
        List<UserDto> userDtos = new ArrayList<>();
        if (userNos != null && userNos.size() > 0)
            for (int id : userNos) {
                TreeUserDTOTemp treeUserDTOTemp = Utils.GetUserFromDatabase(listTemp, id);
                if (treeUserDTOTemp != null) {
                    userDtos.add(new UserDto(String.valueOf(treeUserDTOTemp.getUserNo()), treeUserDTOTemp.getName(), treeUserDTOTemp.getAvatarUrl()));
                }
            }
        if (userDtos.size() > 0) {
            dataSet.add(new GroupDto(userDtos));

        }

//        Log.d(TAG, "--------------------- start create dataSet ---------------------");
        for (int i = 0; i < list.size(); i++) {

            ChattingDto chattingDto = list.get(i);
//            Log.d(TAG, "for_chattingDto:" + chattingDto.getMessage() + " i:" + i);
            if (i == 0) {
                long isTime = TimeUtils.getTimeForMail(TimeUtils.getTime(chattingDto.getRegDate()));
                if (isTime == -2) {
                    ChattingDto time = new ChattingDto();
//                    time.setMessageNo(chattingDto.getMessageNo() - 1);
                    dataSet.add(time);

                    Log.d(TAG, "dataSet add time 6 Today");
                    time.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                    time.setRegDate(Utils.getString(R.string.today));
                    Log.d(TAG, "addNewChat 5:" + chattingDto.getMessage());
                    addNewChat(chattingDto, false, false);
                } else {
                    ChattingDto time = new ChattingDto();
//                    time.setMessageNo(chattingDto.getMessageNo() - 1);
                    dataSet.add(time);

                    Log.d(TAG, "dataSet add time 7");
                    time.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                    time.setTime(TimeUtils.getTime(list.get(0).getRegDate()));
                    Log.d(TAG, "addNewChat 6:" + chattingDto.getMessage());
                    addNewChat(chattingDto, false, false);
                }
            } else {
                long noTemp = TimeUtils.getTime((list.get(i - 1).getRegDate()));
                long noTemp2 = TimeUtils.getTime((chattingDto.getRegDate()));
                long isTime = TimeUtils.getTimeForMail(noTemp2);
                if (TimeUtils.compareTime(noTemp, noTemp2)) {
                    // 동일한 날짜 일 경우
                    Log.d(TAG, "addNewChat 7:" + chattingDto.getMessage());
                    addNewChat(chattingDto, false, false);
                } else {
                    // 날짜가 틀려졌을 경우
                    if (isTime == -2) {
                        ChattingDto time = new ChattingDto();
//                        time.setMessageNo(chattingDto.getMessageNo() - 1);
                        dataSet.add(time);

                        Log.d(TAG, "dataSet add time 8 Today");
                        time.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                        time.setRegDate(Utils.getString(R.string.today));
//                        addNewChat(time, false, false);
                    } else {
                        ChattingDto time2 = new ChattingDto();
//                        time2.setMessageNo(chattingDto.getMessageNo() - 1);
                        dataSet.add(time2);

                        Log.d(TAG, "dataSet add time 9 Today getMessage:" + time2.getMessage() + " : getMessageNo:" + time2.getMessageNo());
                        time2.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                        time2.setRegDate(chattingDto.getRegDate());
//                        addNewChat(time2, false, false);
                    }
                    Log.d(TAG, "addNewChat 8:" + chattingDto.getMessage());
                    addNewChat(chattingDto, false, false);
                }
            }
        }

        // Scroll to bottom
        if (!hasLoadMore) {
            int a = dataSet.size() - 1;
            if (a >= 0)
                rvMainList.scrollToPosition(a);
            hasLoadMore = false;
        }


        Log.d(TAG, "initData finish");
        if (firstOpen == 1) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setStackFromEnd();
                }
            }, 500);
        }


    }

    public void updateDataSet(List<ChattingDto> listNew) {
        Log.d(TAG, "Start updateDataSet------------------------------");


        boolean isSort = false;
        for (ChattingDto obj : listNew) {
            long newMsgNo = obj.getMessageNo();
            boolean flag = true;
            for (ChattingDto dto : dataSet) {
                long oldMsgNo = dto.getMessageNo();
                if (newMsgNo == oldMsgNo) {
                    flag = false;
                    dto.setRegDate(obj.getRegDate());
                    break;
                }
            }
            if (flag) {
                isSort = true;
                Log.d(TAG, "add msg:" + obj.getMessageNo() + ":" + obj.getMessage());
//                Log.d(TAG, "add msg:" + new Gson().toJson(obj));
                addNewChat(obj, false, false);
            }
        }
        Log.d(TAG, "isSort:" + isSort);
//        TimeUtils.getTime();
        if (isSort) {
            // sort list
//            for (ChattingDto obj : dataSet) {
//                Log.d(TAG, "beforesortTimeList: " + obj.getMessage() + " : " + obj.getMessageNo() + " : " + obj.getRegDate());
//            }
            Log.d(TAG, "----------------- sort list ----------------");
            dataFromServer = Constant.sortTimeList(dataSet);
            initData(dataFromServer, 0);
//            Log.d(TAG, "dataFromServer:" + dataFromServer.size());
//            for (ChattingDto dto : dataSet) {
//                Log.d(TAG, "for:" + dto.getRegDate() + " - " + TimeUtils.getTime(dto.getRegDate()) + " - " + dto.getMessageNo() + " - " + dto.getMessage());
//            }
            // update adapter
            adapterList.notifyDataSetChanged();
        }
    }


    public void updateDataServer() {

        Log.d(TAG, "updateDataServer dataSet:" + dataSet.size());
        int mesType = 1;
        int startNo = 0;
        Log.d(TAG, "URL_GET_CHAT_MSG_SECTION 2");
        HttpRequest.getInstance().GetChatMsgSection(roomNo, startNo, mesType, new OnGetChatMessage() {
            @Override
            public void OnGetChatMessageSuccess(List<ChattingDto> listNew) {
                Log.d(TAG, "updateDataServer OnGetChatMessageSuccess");

                try {
                    updateDataSet(listNew);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void OnGetChatMessageFail(ErrorDto errorDto) {
                Log.d(TAG, "updateDataServer finish add OnGetChatMessageFail:");
            }
        });
    }

    public void initData() {
        List<UserDto> userDtos = new ArrayList<>();

        if (userNos != null && userNos.size() > 0) {
            for (int id : userNos) {
                TreeUserDTOTemp treeUserDTOTemp = Utils.GetUserFromDatabase(listTemp, id);

                if (treeUserDTOTemp != null) {
                    userDtos.add(new UserDto(String.valueOf(treeUserDTOTemp.getUserNo()), treeUserDTOTemp.getName(), treeUserDTOTemp.getAvatarUrl()));
                }
            }
        }

        if (userDtos.size() > 0) {
            ChattingDto group = new GroupDto(userDtos);
            dataSet.add(group);
            Log.d(TAG, "dataSet add 10 group");
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*System.out.println("aaaaaaaaaaaaaaaaaaaa onActivityResult Fragment");
        Uri selectedImage = null;
        Utils.printLogs("Activity.RESULT_OK " + Activity.RESULT_OK);
        switch (requestCode) {
            case Statics.CAMERA_CAPTURE_IMAGE_REQUEST_CODE:

                Utils.printLogs("resultCode " + resultCode);
                if (resultCode == Activity.RESULT_OK) {
                    selectedImage = data.getData();
                }

                break;
            case Statics.IMAGE_PICKER_SELECT:

                Utils.printLogs("resultCode " + resultCode);
                if (resultCode == Activity.RESULT_OK) {
                    selectedImage = data.getData();
                }
                break;
        }
        if (selectedImage != null) {
            //String avatarRealPath = Utils.getPathFromURI(selectedImage, getActivity());
            //addNewChat(avatarRealPath,Statics.CHATTING_VIEW_TYPE_SELF_IMAGE);
        }*/
    }

    public void addLineToday() {
        // Add new line for new message, it's may be today
        String date = "";
        if (dataSet != null) {
            if (dataSet.size() > 2) {
                date = dataSet.get(dataSet.size() - 1).getRegDate();
            } else if (dataSet.size() > 1) {
                date = dataSet.get(1).getRegDate();
            }
            if (!TextUtils.isEmpty(date)) {
                if (!date.equalsIgnoreCase(Utils.getString(R.string.today))) {
                    long isTime = TimeUtils.getTimeForMail(TimeUtils.getTime(date));
                    if (isTime != -2) {
                        ChattingDto time = new ChattingDto();
                        time.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                        time.setRegDate(Utils.getString(R.string.today));
                        dataSet.add(time);
                        Log.d(TAG, "dataSet add time 11 Today getMessage:" + time.getMessage() + " : getMessageNo:" + time.getMessageNo());
                    }
                }
            }
        }
    }

    public static boolean isSend = true;

    private void senAction() {
        //                updateUnreadCount(roomNo, startNo);
        Log.d(TAG, "btnSend");

//                Log.d(TAG, "***************************:" + dataSet.size());
//                for (ChattingDto obj : dataSet) {
//                    Log.d(TAG, "chattingDtoList:" + obj.getMessage() + " : " + obj.getMessageNo() + " : " + obj.getRegDate());
//                }
//                Log.d(TAG, "***************************:" + dataSet.size());


        final String message = view.edt_comment.getText().toString();
        if (!TextUtils.isEmpty(message) && message.length() > 0) {
            Log.d(TAG, "message:" + message);
            view.edt_comment.setText("");
            isSend = false;
            // Add new line for new message, it's may be today
            String date = "";
            if (dataSet != null) {
                if (dataSet.size() > 2) {
                    date = dataSet.get(dataSet.size() - 1).getRegDate();

                } else if (dataSet.size() > 1) {
                    date = dataSet.get(1).getRegDate();

                }

                if (!TextUtils.isEmpty(date)) {
                    if (!date.equalsIgnoreCase(Utils.getString(R.string.today))) {
                        long isTime = TimeUtils.getTimeForMail(TimeUtils.getTime(date));

                        if (isTime != -2) {
                            ChattingDto time = new ChattingDto();
                            time.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                            time.setRegDate(Utils.getString(R.string.today));
                            dataSet.add(time);
                            Log.d(TAG, "dataSet add time 12 Today");
                        }
                    }
                }
            }

            // Add new chat before send, and resend if it sent failed
            final ChattingDto newDto = new ChattingDto();

            // please check solution again
            // long tempMessageNo = dataFromServer.get(dataFromServer.size() - 1).getMessageNo() + 1;
            newDto.setMessageNo(Long.MAX_VALUE);
            newDto.setMessage(message);
            newDto.setUserNo(userID);
            newDto.setType(Statics.MESSAGE_TYPE_NORMAL);
            newDto.setRoomNo(roomNo);
            // if (Utils.isNetworkAvailable()) {
            newDto.setUnReadCount(ChattingActivity.userNos.size() - 1);
            // }
            newDto.setWriterUser(userID);
            newDto.setHasSent(true);
            newDto.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(String.valueOf(System.currentTimeMillis())));
//                    for test
//                    Calendar calendar=Calendar.getInstance();
//                    calendar.set(Calendar.MONTH,5);
//                    calendar.set(Calendar.DAY_OF_MONTH,5);
//                    newDto.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(String.valueOf(calendar.getTimeInMillis())));


            final long finalLastId = ChatMessageDBHelper.addSimpleMessage(newDto);
//                    Log.d(TAG,"finalLastId fm:"+finalLastId);
            newDto.setId((int) finalLastId);
            newDto.isSendding = true;
            Log.d(TAG, "addNewChat 10");
            addNewChat(newDto, true, false);
            Log.d(TAG, "dataFromServer add 6");
            if (addDataFromServer(newDto)) dataFromServer.add(newDto);

            // If send success then update

            // 실제 서버로 메시지 데이터를 보냅니다.
         /*   handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                   if(Utils.isNetworkAvailable()){*/
            final boolean isNetWork;
            if (Utils.isNetworkAvailable()) {
                isNetWork = true;
            } else {
                isNetWork = false;
            }
            HttpRequest.getInstance().SendChatMsg(roomNo, message, new SendChatMessage() {
                @Override
                public void onSendChatMessageSuccess(final ChattingDto chattingDto) {
                    isSend = true;
                    newDto.setHasSent(true);
                    newDto.setMessageNo(chattingDto.getMessageNo());
                    newDto.setUnReadCount(chattingDto.getUnReadCount());
                    String time = TimeUtils.convertTimeDeviceToTimeServerDefault(chattingDto.getRegDate());
                    newDto.setRegDate(time);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ChatMessageDBHelper.updateMessage(newDto, finalLastId);
                        }
                    }).start();
                    adapterList.notifyDataSetChanged();
                    Log.d(TAG, "notifyDataSetChanged 1");
                    sendComplete = false;
                }

                @Override
                public void onSendChatMessageFail(ErrorDto errorDto, String url) {
                    newDto.isSendding = false;
                    sendComplete = false;
                    isSend = true;
                    ///Toast.makeText(mActivity, "Send message failed !", Toast.LENGTH_SHORT).show();
                    if (isNetWork) {
                        newDto.setHasSent(true);
                        //ChatMessageDBHelper.deleteByIdTmp(newDto, finalLastId);
                    } else {
                        newDto.setUnReadCount(0);
                        newDto.setHasSent(false);
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ChatMessageDBHelper.updateMessage(newDto, finalLastId);
                        }
                    }).start();
                    adapterList.notifyDataSetChanged();

                }
            });
               /*    }
                }
            }, 200);*/

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

    public void updateData(ChattingDto chattingDto) {
        Log.d(TAG, "updateData");
        chattingDto.setWriterUser(chattingDto.getWriterUserNo());
        chattingDto.setCheckFromServer(true);
        int userNo = Utils.getCurrentId();
        long startMsgNo = chattingDto.getMessageNo();
        HttpRequest.getInstance().UpdateMessageUnreadCount(roomNo, userNo, startMsgNo);
        chattingDto.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(chattingDto.getRegDate()));

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

            case 1:
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

            case 2:
                if (chattingDto.getWriterUser() == userID) {
                    user = new UserDto(String.valueOf(temp.Id), temp.FullName, temp.avatar);
                    if (chattingDto.getAttachInfo() != null) {
                        if (chattingDto.getAttachInfo().getType() == 1) {
                            chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELF_IMAGE);
                        } else {
                            // Check is video or normal file
                            String filename = chattingDto.getAttachInfo().getFileName();

                            if (Utils.isVideo(filename)) {
                                Log.d(TAG, "isVideo 2");
                                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELF_VIDEO);
                            } else {
                                Log.d(TAG, "Not isVideo 2");
                                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELF_FILE);
                            }
                        }
                    }
                } else {
                    TreeUserDTOTemp treeUserDTOTemp = Utils.GetUserFromDatabase(listTemp, chattingDto.getWriterUser());
                    if (treeUserDTOTemp != null) {
                        user = new UserDto(String.valueOf(treeUserDTOTemp.getUserNo()), treeUserDTOTemp.getName(), treeUserDTOTemp.getAvatarUrl());
                    }

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
                                // check is video or file
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

        if (!chattingDto.isCheckFromServer()) {
            if (view != null) {
                view.edt_comment.setText("");
            }
        }

        if (chattingDto.getType() != 0) {
            if (chattingDto.getAttachInfo() == null) {
                return;
            }
        }

        chattingDto.setUser(user);
        chattingDto.setContent(chattingDto.getMessage());

        isFromNotification = true;
        Log.d(TAG, "dataFromServer add 7");
        if (addDataFromServer(chattingDto))
            dataFromServer.add(chattingDto);

    }

    boolean addDataFromServer(ChattingDto chattingDto) {
        boolean flag = false;
        if (dataFromServer != null) {
            flag = true;
            long msgNo = chattingDto.getMessageNo();
            for (ChattingDto obj : dataFromServer) {
                if (msgNo != Long.MAX_VALUE && obj.getMessageNo() == msgNo) {
                    Log.d(TAG, "addDataFromServer duplicate");
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
        //  if (IsWHAT_CODE_ADD_NEW_DATA) {
        // isFirst = mPrefs.getBooleanValue(Statics.IS_FIRST_SHARE, true);
        if (!isSendingFile) {
            // shareFileRv();
            //  shareFileRv();
           /* mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (MainActivity.imageUri != null && type.startsWith("video/")) {
                        List<ChattingDto> integerList = new ArrayList<>();
                        String path = Utils.getPathFromURI(MainActivity.imageUri, getContext());
                        File file = new File(path);
                        String filename = path.substring(path.lastIndexOf("/") + 1);
                        //ChattingDto chattingDto = this.chattingDto;
                        ChattingDto chattingDto = new ChattingDto();
                        chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELECT_VIDEO);
                        chattingDto.setAttachFilePath(path);
                        chattingDto.setAttachFileName(filename);
                        chattingDto.setAttachFileSize((int) file.length());
                        ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);
                        Log.d(TAG, "addNewRow 5");

                        chattingDto.setPositionUploadImage(ChattingFragment.instance.dataSet.size() - 1);
                        integerList.add(chattingDto);
                        sendFileWithQty_v2(integerList, 0);
                        MainActivity.imageUri = null;
                        mSelectedImage.clear();
                    } *//*else if (mSelectedImage != null && mSelectedImage.size() > 0 && type.startsWith("image/")) {
                Intent imageIntent = new Intent();
                imageIntent.setAction(MediaChooser.IMAGE_SELECTED_ACTION_FROM_MEDIA_CHOOSER);
                imageIntent.putStringArrayListExtra("list", mSelectedImage);
                ChattingActivity.instance.sendBroadcast(imageIntent);
                mSelectedImage.clear();
                MainActivity.imageUri = null;
            }*//* else if (MainActivity.imageUri != null && type.startsWith("audio/")) {
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
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        StringBuffer fileContent = new StringBuffer("");
                        int ch;
                        try {
                            while ((ch = stream.read()) != -1)
                                fileContent.append((char) ch);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        String data = new String(fileContent);
                        // for (final Contact contact : contacts) {
                        final ChattingDto dto = new ChattingDto();
                        UserDto userDto = new UserDto();
                        userDto.setFullName(data);
                        //  userDto.setPhoneNumber(contact.getPhone(0));
                        // userDto.setAvatar(contact.getPhotoUri() != null ? contact.getPhotoUri().toString() : null);

                        dto.setmType(Statics.CHATTING_VIEW_TYPE_CONTACT);
                        dto.setUser(userDto);
                        dto.setMessage(data);
                        dto.setHasSent(false);
                        dto.setUserNo(Utils.getCurrentId());
                        dto.setRoomNo(roomNo);
                        dto.setWriterUser(Utils.getCurrentId());
                        // perform update when send message success
                        String currentTime = System.currentTimeMillis() + "";
                        String time = TimeUtils.convertTimeDeviceToTimeServerDefault(currentTime);
                        dto.setRegDate(time);
                        //  ChattingFragment.instance.addNewRowFromChattingActivity(dto);
                        Log.d(TAG, "addNewRow 6");

                        final long lastId = ChatMessageDBHelper.addSimpleMessage(dto);

                        HttpRequest.getInstance().SendChatMsg(roomNo, data + "\n", new SendChatMessage() {
                            @Override
                            public void onSendChatMessageSuccess(final ChattingDto chattingDto) {
                                // update old chat message model --> messageNo from server
                                dto.setHasSent(true);
                                dto.setMessage(chattingDto.getMessage());
                                dto.setMessageNo(chattingDto.getMessageNo());
                                dto.setmType(Statics.CHATTING_VIEW_TYPE_CONTACT);
                                dto.setUnReadCount(chattingDto.getUnReadCount());
                                // perform update when send message success
                                String time = TimeUtils.convertTimeDeviceToTimeServerDefault(chattingDto.getRegDate());
                                dto.setRegDate(time);

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ChatMessageDBHelper.updateMessage(dto, lastId);
                                    }
                                }).start();

                                // Notify current adapter
                                // dataFromServer.add(newDto);

//                                    Toast.makeText(getApplicationContext(), "Send message success !", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onSendChatMessageFail(ErrorDto errorDto, String url) {

                            }
                        });
              *//*  //  }
                Log.i("TAG", "data: " + data);
                if (ChattingActivity.instance != null) {
                    if (ChattingActivity.instance.checkPermissionsContacts()) {
                        Intent intent = new Intent(ChattingActivity.Instance, ContactPickerActivity.class)
                                //.putExtra(ContactPickerActivity.EXTRA_THEME, mDarkTheme ? R.style.Theme_Dark : R.style.Theme_Light)
                                .putExtra(ContactPickerActivity.EXTRA_CONTACT_BADGE_TYPE, ContactPictureType.ROUND.name())
                                .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION, ContactDescription.ADDRESS.name())
                                .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                                .putExtra(ContactPickerActivity.EXTRA_CONTACT_SORT_ORDER, ContactSortOrder.AUTOMATIC.name());

                        ChattingActivity.Instance.startActivityForResult(intent, Statics.CONTACT_PICKER_SELECT);
                    } else {
                        ChattingActivity.instance.setPermissionsCameraContacts();
                    }
                } else {
                    Toast.makeText(CrewChatApplication.getInstance(), CrewChatApplication.getInstance().getResources().getString(R.string.can_not_check_permission), Toast.LENGTH_SHORT).show();
                }*//*
                        MainActivity.imageUri = null;
                    } else {
                        if (mSelectedImage != null && mSelectedImage.size() > 0) {
                            if (mSelectedImage.size() > 10) {
                                // show notify
                                Toast.makeText(getContext(), "Limit is 10 file", Toast.LENGTH_SHORT).show();
                            } else {
                                List<ChattingDto> integerList = new ArrayList<>();
                                for (String path : mSelectedImage) {
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
                                        ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);
                                        Log.d(TAG, "addNewRow 5");

                                        chattingDto.setPositionUploadImage(ChattingFragment.instance.dataSet.size() - 1);
                                        integerList.add(chattingDto);
                                        //Send(path);
                                    } else {
                                        Toast.makeText(getContext(), getResources().getString(R.string.can_not_send_this_file) + " " + filename, Toast.LENGTH_SHORT).show();
                                    }
                                }
                                if (ChattingFragment.instance != null && integerList.size() > 0)
                                    ChattingFragment.instance.sendFileWithQty_v2(integerList, 0);
                                mSelectedImage.clear();
                                MainActivity.imageUri = null;

                            }
                        }

                    }
                }
            }, 3000);

        } else {
            if (MainActivity.imageUri != null && type.startsWith("video/")) {
                List<ChattingDto> integerList = new ArrayList<>();
                String path = Utils.getPathFromURI(MainActivity.imageUri, getContext());
                File file = new File(path);
                String filename = path.substring(path.lastIndexOf("/") + 1);
                //ChattingDto chattingDto = this.chattingDto;
                ChattingDto chattingDto = new ChattingDto();
                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELECT_VIDEO);
                chattingDto.setAttachFilePath(path);
                chattingDto.setAttachFileName(filename);
                chattingDto.setAttachFileSize((int) file.length());
                ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);
                Log.d(TAG, "addNewRow 5");

                chattingDto.setPositionUploadImage(ChattingFragment.instance.dataSet.size() - 1);
                integerList.add(chattingDto);
                sendFileWithQty_v2(integerList, 0);
                MainActivity.imageUri = null;
                mSelectedImage.clear();
            } *//*else if (mSelectedImage != null && mSelectedImage.size() > 0 && type.startsWith("image/")) {
                Intent imageIntent = new Intent();
                imageIntent.setAction(MediaChooser.IMAGE_SELECTED_ACTION_FROM_MEDIA_CHOOSER);
                imageIntent.putStringArrayListExtra("list", mSelectedImage);
                ChattingActivity.instance.sendBroadcast(imageIntent);
                mSelectedImage.clear();
                MainActivity.imageUri = null;
            }*//* else if (MainActivity.imageUri != null && type.startsWith("audio/")) {
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
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                StringBuffer fileContent = new StringBuffer("");
                int ch;
                try {
                    while ((ch = stream.read()) != -1)
                        fileContent.append((char) ch);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                String data = new String(fileContent);
                // for (final Contact contact : contacts) {
                final ChattingDto dto = new ChattingDto();
                UserDto userDto = new UserDto();
                userDto.setFullName(data);
                //  userDto.setPhoneNumber(contact.getPhone(0));
                // userDto.setAvatar(contact.getPhotoUri() != null ? contact.getPhotoUri().toString() : null);

                dto.setmType(Statics.CHATTING_VIEW_TYPE_CONTACT);
                dto.setUser(userDto);
                dto.setMessage(data);
                dto.setHasSent(false);
                dto.setUserNo(Utils.getCurrentId());
                dto.setRoomNo(roomNo);
                dto.setWriterUser(Utils.getCurrentId());
                // perform update when send message success
                String currentTime = System.currentTimeMillis() + "";
                String time = TimeUtils.convertTimeDeviceToTimeServerDefault(currentTime);
                dto.setRegDate(time);
                //  ChattingFragment.instance.addNewRowFromChattingActivity(dto);
                Log.d(TAG, "addNewRow 6");

                final long lastId = ChatMessageDBHelper.addSimpleMessage(dto);

                HttpRequest.getInstance().SendChatMsg(roomNo, data + "\n", new SendChatMessage() {
                    @Override
                    public void onSendChatMessageSuccess(final ChattingDto chattingDto) {
                        // update old chat message model --> messageNo from server
                        dto.setHasSent(true);
                        dto.setMessage(chattingDto.getMessage());
                        dto.setMessageNo(chattingDto.getMessageNo());
                        dto.setmType(Statics.CHATTING_VIEW_TYPE_CONTACT);
                        dto.setUnReadCount(chattingDto.getUnReadCount());
                        // perform update when send message success
                        String time = TimeUtils.convertTimeDeviceToTimeServerDefault(chattingDto.getRegDate());
                        dto.setRegDate(time);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ChatMessageDBHelper.updateMessage(dto, lastId);
                            }
                        }).start();

                        // Notify current adapter
                        // dataFromServer.add(newDto);

//                                    Toast.makeText(getApplicationContext(), "Send message success !", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSendChatMessageFail(ErrorDto errorDto, String url) {

                    }
                });
              *//*  //  }
                Log.i("TAG", "data: " + data);
                if (ChattingActivity.instance != null) {
                    if (ChattingActivity.instance.checkPermissionsContacts()) {
                        Intent intent = new Intent(ChattingActivity.Instance, ContactPickerActivity.class)
                                //.putExtra(ContactPickerActivity.EXTRA_THEME, mDarkTheme ? R.style.Theme_Dark : R.style.Theme_Light)
                                .putExtra(ContactPickerActivity.EXTRA_CONTACT_BADGE_TYPE, ContactPictureType.ROUND.name())
                                .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION, ContactDescription.ADDRESS.name())
                                .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                                .putExtra(ContactPickerActivity.EXTRA_CONTACT_SORT_ORDER, ContactSortOrder.AUTOMATIC.name());

                        ChattingActivity.Instance.startActivityForResult(intent, Statics.CONTACT_PICKER_SELECT);
                    } else {
                        ChattingActivity.instance.setPermissionsCameraContacts();
                    }
                } else {
                    Toast.makeText(CrewChatApplication.getInstance(), CrewChatApplication.getInstance().getResources().getString(R.string.can_not_check_permission), Toast.LENGTH_SHORT).show();
                }*//*
                MainActivity.imageUri = null;
            } else {
                if (mSelectedImage != null && mSelectedImage.size() > 0) {
                    if (mSelectedImage.size() > 10) {
                        // show notify
                        Toast.makeText(getContext(), "Limit is 10 file", Toast.LENGTH_SHORT).show();
                    } else {
                        List<ChattingDto> integerList = new ArrayList<>();
                        for (String path : mSelectedImage) {
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
                                ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);
                                Log.d(TAG, "addNewRow 5");

                                chattingDto.setPositionUploadImage(ChattingFragment.instance.dataSet.size() - 1);
                                integerList.add(chattingDto);
                                //Send(path);
                            } else {
                                Toast.makeText(getContext(), getResources().getString(R.string.can_not_send_this_file) + " " + filename, Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (ChattingFragment.instance != null && integerList.size() > 0)
                            ChattingFragment.instance.sendFileWithQty_v2(integerList, 0);
                        mSelectedImage.clear();
                        MainActivity.imageUri = null;

                    }
                }

            }

        }
        *//*}else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (MainActivity.imageUri != null && type.startsWith("video/")) {
                        List<ChattingDto> integerList = new ArrayList<>();
                        String path = Utils.getPathFromURI(MainActivity.imageUri, getContext());
                        File file = new File(path);
                        String filename = path.substring(path.lastIndexOf("/") + 1);
                        //ChattingDto chattingDto = this.chattingDto;
                        ChattingDto chattingDto = new ChattingDto();
                        chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELECT_VIDEO);
                        chattingDto.setAttachFilePath(path);
                        chattingDto.setAttachFileName(filename);
                        chattingDto.setAttachFileSize((int) file.length());
                        ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);
                        Log.d(TAG, "addNewRow 5");

                        chattingDto.setPositionUploadImage(ChattingFragment.instance.dataSet.size() - 1);
                        integerList.add(chattingDto);
                        sendFileWithQty_v2(integerList, 0);
                        MainActivity.imageUri = null;
                        mSelectedImage.clear();
                    } *//**//*else if (mSelectedImage != null && mSelectedImage.size() > 0 && type.startsWith("image/")) {
                Intent imageIntent = new Intent();
                imageIntent.setAction(MediaChooser.IMAGE_SELECTED_ACTION_FROM_MEDIA_CHOOSER);
                imageIntent.putStringArrayListExtra("list", mSelectedImage);
                ChattingActivity.instance.sendBroadcast(imageIntent);
                mSelectedImage.clear();
                MainActivity.imageUri = null;
            }*//**//* else if (MainActivity.imageUri != null && type.startsWith("audio/")) {
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
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        StringBuffer fileContent = new StringBuffer("");
                        int ch;
                        try {
                            while ((ch = stream.read()) != -1)
                                fileContent.append((char) ch);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStỏnackTrace();
                        }
                        String data = new String(fileContent);
                        // for (final Contact contact : contacts) {
                        final ChattingDto dto = new ChattingDto();
                        UserDto userDto = new UserDto();
                        userDto.setFullName(data);
                        //  userDto.setPhoneNumber(contact.getPhone(0));
                        // userDto.setAvatar(contact.getPhotoUri() != null ? contact.getPhotoUri().toString() : null);

                        dto.setmType(Statics.CHATTING_VIEW_TYPE_CONTACT);
                        dto.setUser(userDto);
                        dto.setMessage(data);
                        dto.setHasSent(false);
                        dto.setUserNo(Utils.getCurrentId());
                        dto.setRoomNo(roomNo);
                        dto.setWriterUser(Utils.getCurrentId());
                        // perform update when send message success
                        String currentTime = System.currentTimeMillis() + "";
                        String time = TimeUtils.convertTimeDeviceToTimeServerDefault(currentTime);
                        dto.setRegDate(time);
                        //  ChattingFragment.instance.addNewRowFromChattingActivity(dto);
                        Log.d(TAG, "addNewRow 6");

                        final long lastId = ChatMessageDBHelper.addSimpleMessage(dto);

                        HttpRequest.getInstance().SendChatMsg(roomNo, data + "\n", new SendChatMessage() {
                            @Override
                            public void onSendChatMessageSuccess(final ChattingDto chattingDto) {
                                // update old chat message model --> messageNo from server
                                dto.setHasSent(true);
                                dto.setMessage(chattingDto.getMessage());
                                dto.setMessageNo(chattingDto.getMessageNo());
                                dto.setmType(Statics.CHATTING_VIEW_TYPE_CONTACT);
                                dto.setUnReadCount(chattingDto.getUnReadCount());
                                // perform update when send message success
                                String time = TimeUtils.convertTimeDeviceToTimeServerDefault(chattingDto.getRegDate());
                                dto.setRegDate(time);

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ChatMessageDBHelper.updateMessage(dto, lastId);
                                    }
                                }).start();

                                // Notify current adapter
                                // dataFromServer.add(newDto);

//                                    Toast.makeText(getApplicationContext(), "Send message success !", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onSendChatMessageFail(ErrorDto errorDto, String url) {

                            }
                        });
              *//**//*  //  }
                Log.i("TAG", "data: " + data);
                if (ChattingActivity.instance != null) {
                    if (ChattingActivity.instance.checkPermissionsContacts()) {
                        Intent intent = new Intent(ChattingActivity.Instance, ContactPickerActivity.class)
                                //.putExtra(ContactPickerActivity.EXTRA_THEME, mDarkTheme ? R.style.Theme_Dark : R.style.Theme_Light)
                                .putExtra(ContactPickerActivity.EXTRA_CONTACT_BADGE_TYPE, ContactPictureType.ROUND.name())
                                .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION, ContactDescription.ADDRESS.name())
                                .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                                .putExtra(ContactPickerActivity.EXTRA_CONTACT_SORT_ORDER, ContactSortOrder.AUTOMATIC.name());

                        ChattingActivity.Instance.startActivityForResult(intent, Statics.CONTACT_PICKER_SELECT);
                    } else {
                        ChattingActivity.instance.setPermissionsCameraContacts();
                    }
                } else {
                    Toast.makeText(CrewChatApplication.getInstance(), CrewChatApplication.getInstance().getResources().getString(R.string.can_not_check_permission), Toast.LENGTH_SHORT).show();
                }*//**//*
                        MainActivity.imageUri = null;
                    } else {
                        if (mSelectedImage != null && mSelectedImage.size() > 0) {
                            if (mSelectedImage.size() > 10) {
                                // show notify
                                Toast.makeText(getContext(), "Limit is 10 file", Toast.LENGTH_SHORT).show();
                            } else {
                                List<ChattingDto> integerList = new ArrayList<>();
                                for (String path : mSelectedImage) {
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
                                        ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);
                                        Log.d(TAG, "addNewRow 5");

                                        chattingDto.setPositionUploadImage(ChattingFragment.instance.dataSet.size() - 1);
                                        integerList.add(chattingDto);
                                        //Send(path);
                                    } else {
                                        Toast.makeText(getContext(), getResources().getString(R.string.can_not_send_this_file) + " " + filename, Toast.LENGTH_SHORT).show();
                                    }
                                }
                                if (ChattingFragment.instance != null && integerList.size() > 0)
                                    ChattingFragment.instance.sendFileWithQty_v2(integerList, 0);
                                mSelectedImage.clear();
                                MainActivity.imageUri = null;

                            }
                        }

                    }
                }
            }, 0);
        }*//*
             */
        }
    }

    private void shareAction() {
        try {
            if (MainActivity.imageUri != null && type.startsWith("video/")) {
                List<ChattingDto> integerList = new ArrayList<>();
                String path = Utils.getPathFromURI(MainActivity.imageUri, getContext());
                File file = new File(path);
                String filename = path.substring(path.lastIndexOf("/") + 1);
                //ChattingDto chattingDto = this.chattingDto;
                ChattingDto chattingDto = new ChattingDto();
                chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELECT_VIDEO);
                chattingDto.setAttachFilePath(path);
                chattingDto.setAttachFileName(filename);
                chattingDto.setAttachFileSize((int) file.length());
                ChattingFragment.instance.addNewRowFromChattingActivity(chattingDto);
                Log.d(TAG, "addNewRow 5");

                chattingDto.setPositionUploadImage(ChattingFragment.instance.dataSet.size() - 1);
                integerList.add(chattingDto);
                sendFileWithQty_v2(integerList, 0);
                MainActivity.imageUri = null;
                mSelectedImage.clear();
                integerList.clear();
            } /*else if (mSelectedImage != null && mSelectedImage.size() > 0 && type.startsWith("image/")) {
                Intent imageIntent = new Intent();
                imageIntent.setAction(MediaChooser.IMAGE_SELECTED_ACTION_FROM_MEDIA_CHOOSER);
                imageIntent.putStringArrayListExtra("list", mSelectedImage);
                ChattingActivity.instance.sendBroadcast(imageIntent);
                mSelectedImage.clear();
                MainActivity.imageUri = null;
            }*/ else if (MainActivity.imageUri != null && type.startsWith("audio/")) {
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
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                StringBuffer fileContent = new StringBuffer("");
                int ch;
                try {
                    while ((ch = stream.read()) != -1)
                        fileContent.append((char) ch);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                String data = new String(fileContent);
                // for (final Contact contact : contacts) {
                final ChattingDto chattingDto = new ChattingDto();
                UserDto userDto = new UserDto();
                userDto.setFullName(data);
                //  userDto.setPhoneNumber(contact.getPhone(0));
                // userDto.setAvatar(contact.getPhotoUri() != null ? contact.getPhotoUri().toString() : null);

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
                //  ChattingFragment.instance.addNewRowFromChattingActivity(dto);
                Log.d(TAG, "addNewRow 6");

                final long lastId = ChatMessageDBHelper.addSimpleMessage(chattingDto);

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

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ChatMessageDBHelper.updateMessage(chattingDto, lastId);
                            }
                        }).start();

                        // Notify current adapter
                        // dataFromServer.add(newDto);

//                                    Toast.makeText(getApplicationContext(), "Send message success !", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSendChatMessageFail(ErrorDto errorDto, String url) {

                    }
                });
              /*  //  }
                Log.i("TAG", "data: " + data);
                if (ChattingActivity.instance != null) {
                    if (ChattingActivity.instance.checkPermissionsContacts()) {
                        Intent intent = new Intent(ChattingActivity.Instance, ContactPickerActivity.class)
                                //.putExtra(ContactPickerActivity.EXTRA_THEME, mDarkTheme ? R.style.Theme_Dark : R.style.Theme_Light)
                                .putExtra(ContactPickerActivity.EXTRA_CONTACT_BADGE_TYPE, ContactPictureType.ROUND.name())
                                .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION, ContactDescription.ADDRESS.name())
                                .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                                .putExtra(ContactPickerActivity.EXTRA_CONTACT_SORT_ORDER, ContactSortOrder.AUTOMATIC.name());

                        ChattingActivity.Instance.startActivityForResult(intent, Statics.CONTACT_PICKER_SELECT);
                    } else {
                        ChattingActivity.instance.setPermissionsCameraContacts();
                    }
                } else {
                    Toast.makeText(CrewChatApplication.getInstance(), CrewChatApplication.getInstance().getResources().getString(R.string.can_not_check_permission), Toast.LENGTH_SHORT).show();
                }*/
                MainActivity.imageUri = null;
            } else {
                if (mSelectedImage != null && mSelectedImage.size() > 0) {
                    if (mSelectedImage.size() > 10) {
                        // show notify
                        Toast.makeText(getContext(), "Limit is 10 file", Toast.LENGTH_SHORT).show();
                    } else {
                        final List<ChattingDto> integerList = new ArrayList<>();
                        for (String path : mSelectedImage) {
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

                                //Send(path);
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

/*
        Log.d("codecodecode", " code" + code);
        if (code == WHAT_CODE_ADD_NEW_DATA) {
            mPrefs.putIntValue(Statics.VALUE_CODE_SHARE, -1);
            if (isFirst) {
                //   Log.d("codecodecode", " isFirst");
                isFirst = false;
                shareAction();
            }
        } else if (isFirst) {
            isFirst = false;
            if (code != WHAT_CODE_ADD_NEW_DATA && code == -1) {
                //  Log.d("codecodecode", " isSecond");
                if (!isSendingFile) {
                    shareAction();
                }
            }
        }*/
    }

    private void shareFileRv() {
        /*   new AsyncShare().execute();*/
        shareInterFace();

    }

    private void sendAudioV2(String path) {
        List<ChattingDto> integerList = new ArrayList<>();
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
        }
    }

    private final static int MSG_UPDATE_DISPLAY = 2;
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
        Log.d(">>>>>>", "receiveMessage");
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
            Log.d(">>>>", "sendComplete " + sendComplete);
            if (!sendComplete) {
                BroadcastEvent(intent);
            }
        }
    };


    private void BroadcastEvent(Intent intent) {
        isShowIcon = false;
        if (intent.getAction().equals(Statics.ACTION_RECEIVER_NOTIFICATION)) {

            String gcmDto = intent.getStringExtra(Statics.GCM_DATA_NOTIFICATOON);
            Log.d(TAG, "ACTION_RECEIVER_NOTIFICATION:" + gcmDto);
            final ChattingDto dataDto = new Gson().fromJson(gcmDto, ChattingDto.class);
//            processReceivingMessage(dataDto);

        } else if (intent.getAction().equals(Constant.INTENT_FILTER_GET_MESSAGE_UNREAD_COUNT)) {
            Log.d(TAG, "INTENT_FILTER_GET_MESSAGE_UNREAD_COUNT");
            final long roomNo = intent.getLongExtra(Constant.KEY_INTENT_ROOM_NO, 0);

            if (roomNo != 0 && dataFromServer.size() > 0) {
                long msgNo = dataFromServer.get(0).getMessageNo();

                HttpRequest.getInstance().GetMessageUnreadCount(roomNo, msgNo, new OnGetMessageUnreadCountCallBack() {
                    @Override
                    public void onHTTPSuccess(String result) {
                        Type listType = new TypeToken<List<MessageUnreadCountDTO>>() {
                        }.getType();
                        List<MessageUnreadCountDTO> list = new Gson().fromJson(result, listType);

                        for (final MessageUnreadCountDTO messageUnreadCountDTO : list) {
                            for (int i = dataSet.size() - 1; i > -1; i--) {
                                final ChattingDto chattingDto = dataSet.get(i);

                                if (chattingDto.getMessageNo() == messageUnreadCountDTO.getMessageNo()) {
                                    if (chattingDto.getUnReadCount() != messageUnreadCountDTO.getUnreadCount()) {
                                        chattingDto.setUnReadCount(messageUnreadCountDTO.getUnreadCount());

                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ChatMessageDBHelper.updateMessage(chattingDto.getMessageNo(), messageUnreadCountDTO.getUnreadCount());
                                            }
                                        }).start();
                                        //  if (!isReSend) {
                                        adapterList.notifyItemChanged(i);
                                        //  }
                                        // adapterList.notifyDataSetChanged();
                                        Log.d(TAG, "adapterList.notifyItemChanged(i);");
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
        String date = dataDto.getRegDate();
        String last_time = "";

        if (dataSet != null) {
            if (dataSet.size() > 2) {
                last_time = dataSet.get(dataSet.size() - 1).getRegDate();
            } else if (dataSet.size() > 1) {
                last_time = dataSet.get(1).getRegDate();
            }

            if (!TextUtils.isEmpty(date) && !TextUtils.isEmpty(last_time)) {
                if (!date.equalsIgnoreCase(Utils.getString(R.string.today))) {
                    long isTime = TimeUtils.getStttimeMessage(TimeUtils.getTime(date), TimeUtils.getTime(last_time));

                    if (isTime != -2) {
                        ChattingDto time = new ChattingDto();
                        time.setmType(Statics.CHATTING_VIEW_TYPE_DATE);
                        time.setRegDate(Utils.getString(R.string.today));

                        dataSet.add(time);
                        Log.d(TAG, "dataSet add time 13 Today");

                        adapterList.notifyDataSetChanged();
                        Log.d(TAG, "notifyDataSetChanged 2");
                    }
                }
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

        // my msg dont need update
//                if (dataDto.getWriterUser() == Utils.getCurrentId()) {
//                    Log.d(TAG, "my msg dont need update");
//                    checkNotification = false;
//                }

        // file: if myid and exist msgNo -> checkNotification = false to dont update msg;


        if (isSendingFile && dataDto != null
                && dataDto.getAttachFileName() != null
                && dataDto.getAttachFileName().length() > 0
                && dataDto.getWriterUser() == Utils.getCurrentId()) {
            Log.d(TAG, "isSendingFile dont update");
            checkNotification = false;
        }


        Log.d(TAG, "checkNotification:" + checkNotification);
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
                long startMsgNo = dataDto.getMessageNo();
                HttpRequest.getInstance().UpdateMessageUnreadCount(dataDto.getRoomNo(), userID, startMsgNo);
                dataDto.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(dataDto.getRegDate()));
                isFromNotification = true;

                if (!TextUtils.isEmpty(dataDto.getMessage()) || dataDto.getAttachNo() != 0) {
                    Log.d(TAG, "addNewChat 11");

                    if (dataDto.getType() != 6) {
                        msgEnd = -1;
                        isShowIcon = false;
                    }
                    adapterList.notifyDataSetChanged();
                    Log.d(TAG, "notifyDataSetChanged 3");
                    addNewChat(dataDto, true, true);
                }

                if (CurrentChatListFragment.fragment != null) {
                    CurrentChatListFragment.fragment.updateData(dataDto, false);
                }
                Log.d(TAG, "dataFromServer add 1");
                if (addDataFromServer(dataDto)) dataFromServer.add(dataDto);
            } else {
                dataDto.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(dataDto.getRegDate()));

                if (CurrentChatListFragment.fragment != null) {
                    CurrentChatListFragment.fragment.updateData(dataDto, true);
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

    public void sendAttachFile(int attachNo, long roomNo, final int position,
                               final WatingUpload callBack, final ChattingDto chattingDto) {
        isSendingFile = true;
        HttpRequest.getInstance().SendChatAttachFile(roomNo, attachNo, new SendChatMessage() {
            @Override
            public void onSendChatMessageSuccess(final ChattingDto dto) {
                isSendingFile = false;
                Log.d(TAG, "sendAttachFile success");
                progressBar.setVisibility(View.GONE);
                dto.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(dto.getRegDate()));

                if (CurrentChatListFragment.fragment != null) {
                    CurrentChatListFragment.fragment.updateData(dto, false);
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
                //do something here.
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

//        Intent intent2 = new Intent(ChattingActivity.this, RoomUserInformationActivity.class);
//        intent2.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo);
//        intent2.putExtra("userNos", userNos);
//        intent2.putExtra("roomTitle", title);
//        startActivity(intent2);
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

    int sendTo = 0;

    public void SendTo(ChattingDto chattingDto, ProgressBar progressBar,
                       int position, WatingUpload callBack) {
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

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                               int oldLeft,
                               int oldTop, int oldRight, int oldBottom) {
        if (bottom < oldBottom) {
            rvMainList.postDelayed(new Runnable() {
                @Override
                public void run() {
                    rvMainList.scrollToPosition(dataSet.size());
                }
            }, 100);
        }
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
//            //please check solution again
//            // long tempMessageNo = dataFromServer.get(dataFromServer.size() - 1).getMessageNo() + 1;
//            chattingDto.setMessageNo(Long.MAX_VALUE);
//            chattingDto.setUserNo(userID);
//            chattingDto.setmType(Statics.CHATTING_VIEW_TYPE_SELF_IMAGE);
//            chattingDto.setRoomNo(roomNo);
//            chattingDto.setWriterUser(userID);
//            chattingDto.setHasSent(false);
//
//            String currentTime = System.currentTimeMillis() + "";
//            chattingDto.setRegDate(TimeUtils.convertTimeDeviceToTimeServer(currentTime));
//            addNewChat(chattingDto, true);
//
//            dataFromServer.add(chattingDto);
//
//            ChatMessageDBHelper.addSimpleMessage(chattingDto);

            sendAttachFile(integer, roomNo, position, callBack, chattingDto);
        }

    }

    public void Reload() {
        str_lastID = "";
        lastID = 0;
        dataSet.clear();
        adapterList.notifyDataSetChanged();
        Log.d(TAG, "notifyDataSetChanged 4");
        adapterList.setLoaded();
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "URL_GET_CHAT_MSG_SECTION 3");
        HttpRequest.getInstance().GetChatMsgSection(roomNo, 0, 1, new OnGetChatMessage() {
            @Override
            public void OnGetChatMessageSuccess(List<ChattingDto> listNew) {
                Log.d(TAG, "OnGetChatMessageSuccess");
//                for(ChattingDto obj:listNew)
//                {
//                    Log.d(TAG,new Gson().toJson(obj));
//                }
                progressBar.setVisibility(View.GONE);
                isLoaded = true;
                //saveUnread();
                dataFromServer = listNew;
                if (listNew.size() > 0) {
                    int userNo = Utils.getCurrentId();
                    long startMsgNo = listNew.get(listNew.size() - 1).getMessageNo();
                    HttpRequest.getInstance().UpdateMessageUnreadCount(roomNo, userNo, startMsgNo);
                    Log.d(TAG, "initData 3");
                    initData(listNew, 0);
                } else {
                    initData();
                }

            }

            @Override
            public void OnGetChatMessageFail(ErrorDto errorDto) {
                progressBar.setVisibility(View.GONE);
                isLoaded = true;
                initData();
            }
        });
    }

    public static boolean isShowIcon = false;

    void setFirstItem() {
        if (dataSet != null) {
            if (dataSet.size() > 0) {
                ChattingDto chattingDto2 = dataSet.get(0);
                chattingDto2.setId(999);
                adapterList.notifyItemChanged(0);
                Log.d(TAG, "adapterList.notifyItemChanged(0);");
            }
        }
    }


    private void loadMoreData() {
        Log.d(TAG, "loadMoreData");
        if (!isLoading && isLoadMore && dataSet.size() > 3) {

            isLoading = true;
            hasLoadMore = true;

            long baseMsgNo = dataFromServer.get(0).getMessageNo();
            ChattingDto chattingDto2 = dataSet.get(0);
            chattingDto2.setId(999);

            adapterList.notifyItemChanged(0);


            List<ChattingDto> localData = ChatMessageDBHelper.getMsgSession(roomNo, baseMsgNo, ChatMessageDBHelper.BEFORE);

            if (Utils.isNetworkAvailable()) {
                if (localData != null)
                    localData.clear();
            } else {
            }

            if (localData != null && localData.size() > 0) {
                Log.d(TAG, "loadMoreData offline");
                long currentMessageNo = 0;
                for (ChattingDto dto : dataSet) {
                    if (dto.getMessageNo() > 0) {
                        currentMessageNo = dto.getMessageNo();
                        break;
                    }
                }
                Log.d(TAG, "dataFromServer add 3");
                dataFromServer.addAll(0, localData);
                Log.d(TAG, "initData 4");
                dataFromServer = Constant.sortTimeList(dataFromServer);
                initData(dataFromServer, 0);
                int currentPosition = 0, length = dataSet.size();
                for (int i = 0; i < length; i++) {
                    if (dataSet.get(i).getMessageNo() == currentMessageNo) {
                        currentPosition = i;
                        break;
                    }
                }
                adapterList.notifyDataSetChanged();
                Log.d(TAG, "notifyDataSetChanged 5");
                layoutManager.scrollToPositionWithOffset(currentPosition, 0);
                isLoading = false;
                setFirstItem();
            } else {
                Log.d(TAG, "URL_GET_CHAT_MSG_SECTION 4");
                HttpRequest.getInstance().GetChatMsgSection(roomNo, baseMsgNo, 2, new OnGetChatMessage() {
                    @Override
                    public void OnGetChatMessageSuccess(final List<ChattingDto> listNew) {
                        if (listNew.size() > 0) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    for (ChattingDto chat : listNew) {
                                        Log.d(TAG, "addMessage 2:");
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
                            Log.d(TAG, "dataFromServer add 4");
                            dataFromServer.addAll(0, listNew);
                            Log.d(TAG, "initData 5");
                            dataFromServer = Constant.sortTimeList(dataFromServer);
                            initData(dataFromServer, 0);

                            int currentPosition = 0, length = dataSet.size();

                            for (int i = 0; i < length; i++) {
                                if (dataSet.get(i).getMessageNo() == currentMessageNo) {
                                    currentPosition = i;
                                    break;
                                }
                            }

                            adapterList.notifyDataSetChanged();
                            Log.d(TAG, "notifyDataSetChanged 6");
                            layoutManager.scrollToPositionWithOffset(currentPosition, 0);

                            isLoading = false;
                            setFirstItem();
                        } else {
                            ChattingDto chattingDto2 = dataSet.get(0);
                            chattingDto2.setId(0);

                            adapterList.notifyItemChanged(0);
                            Log.d(TAG, "adapterList.notifyItemChanged(0);");
                            isLoadMore = false;

                        }
                    }

                    @Override
                    public void OnGetChatMessageFail(ErrorDto errorDto) {

                        ChattingDto chattingDto2 = dataSet.get(0);
                        chattingDto2.setId(0);

                        adapterList.notifyItemChanged(0);
                        Log.d(TAG, "OnGetChatMessageFail adapterList.notifyItemChanged(0);");
                        //   isLoadMore = false;
                    }
                });
            }
        } else {
            Log.d(TAG, "dont loadMoreData");

//            Log.d(TAG,"isLoading:"+isLoading);
//            Log.d(TAG,"isLoadMore:"+isLoadMore);
//            Log.d(TAG,"dataSet.size():"+dataSet.size());

            if (dataSet != null) {
                if (dataSet.size() > 0) {
                    dataSet.get(0).setId(0);
                    adapterList.notifyItemChanged(0);
                    Log.d(TAG, "dataSet.size() > 0 adapterList.notifyItemChanged(0);");
                    isLoading = false;
                }
            }
        }
    }

    private void refFreshData() {
        try {
            dataFromServer = Constant.sortTimeList(dataFromServer);
            initData(dataFromServer, 0);
            adapterList.notifyDataSetChanged();
            //sendComplete=false;
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
//        intent.putExtra(Statics.CHATTING_DTO_GALLERY_LIST, urls);
        intent.putExtra(Statics.CHATTING_DTO_GALLERY_POSITION, position);
        mActivity.startActivity(intent);
    }

}