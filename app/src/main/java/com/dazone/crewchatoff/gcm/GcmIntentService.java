package com.dazone.crewchatoff.gcm;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.database.AllUserDBHelper;
import com.dazone.crewchatoff.database.ChatMessageDBHelper;
import com.dazone.crewchatoff.database.ChatRoomDBHelper;
import com.dazone.crewchatoff.database.UserDBHelper;
import com.dazone.crewchatoff.dto.AttachDTO;
import com.dazone.crewchatoff.dto.ChatRoomDTO;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.NotificationBundleDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.eventbus.ReceiveMessage;
import com.dazone.crewchatoff.fragment.ChattingFragment;
import com.dazone.crewchatoff.fragment.CurrentChatListFragment;
import com.dazone.crewchatoff.interfaces.OnGetChatRoom;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

import static android.support.v4.app.NotificationCompat.PRIORITY_LOW;

public class GcmIntentService extends IntentService {
    String TAG = ">>>GcmIntentService";
    String channelId = "channel-01";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    private int Code = 0;
    private static int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    ArrayList<TreeUserDTOTemp> listTemp = AllUserDBHelper.getUser();
    ChattingDto chattingDto;
    private Prefs prefs;
    private NotificationCompat.Builder mBuilder;
    //public static NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this.getApplicationContext());
    //  private NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
    boolean isEnableN, isEnableSound, isEnableVibrate, isEnableTime, isPCVersion;

    private boolean timeAvaiable() {
        boolean isTimeEnable = prefs.getBooleanValue(Statics.ENABLE_TIME, false);

        if (!isTimeEnable) { // Check is enable notification time
            return true; // if check time is disable. the condition always true
        }

        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        int start_hour = prefs.getIntValue(Statics.START_NOTIFICATION_HOUR, Statics.DEFAULT_START_NOTIFICATION_TIME);
        int start_minutes = prefs.getIntValue(Statics.START_NOTIFICATION_MINUTES, 0);
        int end_hour = prefs.getIntValue(Statics.END_NOTIFICATION_HOUR, Statics.DEFAULT_END_NOTIFICATION_TIME);
        int end_minutes = prefs.getIntValue(Statics.END_NOTIFICATION_MINUTES, 0);

        boolean isBetween = (currentHour > start_hour) && (currentHour < end_hour);
        boolean isLeft = (currentHour == start_hour) && (currentMinute > start_minutes);
        boolean isRight = (currentHour == end_hour) && (currentMinute < end_minutes);

        return isBetween || isLeft || isRight;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        prefs = CrewChatApplication.getInstance().getPrefs();

        isEnableN = prefs.getBooleanValue(Statics.ENABLE_NOTIFICATION, true);
        isEnableSound = prefs.getBooleanValue(Statics.ENABLE_SOUND, true);
        isEnableVibrate = prefs.getBooleanValue(Statics.ENABLE_VIBRATE, true);
        isEnableTime = prefs.getBooleanValue(Statics.ENABLE_TIME, false);
        isPCVersion = prefs.getBooleanValue(Statics.ENABLE_NOTIFICATION_WHEN_USING_PC_VERSION, true);

        if (extras != null) { // Check enable notification and current time avaiable [on time table]
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                //TODO sendNotification("Send error",extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
               //TODO sendNotification("Deleted messages on server ", extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                if (extras.containsKey("Code")) {
                    Code = Integer.parseInt(extras.getString("Code", "0"));
                    switch (Code) {
                        case 1:
                            Log.d("TAG", "Case 1 ###");
                            receiveCode1(extras);
                            break;
                        case 2:
                            Log.d("TAG", "Case 2 ###");
                            receiveCode2(extras);
                            break;
                        case 3:
                            Log.d("TAG", "Case 3 ###");
                            chatDeleteMember(extras);
                            break;
                        case 4:
                            Log.d("TAG", "Case 4 ###");
                            break;
                        case 5:
                            Log.d("TAG", "Case 5 ###");
                            receiveCode5(extras);
                            break;
                        case 8:
                            Log.d("TAG", "Case 8 ###");
                            // dont apply this  version
                            receiveCode8(extras);
                            break;
                        default:
                            Log.d("TAG", "Case 0 ###");
                            break;
                    }
                }
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
        NOTIFICATION_ID = NOTIFICATION_ID + 1;
    }

    /**
     * RECEIVE CODE 1
     */
    private void receiveCode1(Bundle extras) {
        if (extras.containsKey("Data")) {
            long userNo = Long.parseLong(extras.getString("UserNo", "0"));

            if (userNo == Utils.getCurrentId()) {
                try {
                    NotificationBundleDto bundleDto = new Gson().fromJson(extras.getString("Data"), NotificationBundleDto.class);

                    chattingDto = new ChattingDto();
                    chattingDto.setRoomNo(bundleDto.getRoomNo());
                    chattingDto.setUnreadTotalCount(bundleDto.getUnreadTotalCount());
                    chattingDto.setMessage(bundleDto.getMessage());
                    chattingDto.setMessageNo(bundleDto.getMessageNo());
                    chattingDto.setWriterUserNo(bundleDto.getWriteUserNo());

                    chattingDto.setAttachNo(bundleDto.getAttachNo());
                    chattingDto.setAttachFileName(bundleDto.getAttachFileName());
                    chattingDto.setAttachFileType(bundleDto.getAttachFileType());
                    chattingDto.setAttachFilePath(bundleDto.getAttachFilePath());
                    chattingDto.setAttachFileSize(bundleDto.getAttachFileSize());

                    AttachDTO attachInfo = new AttachDTO();
                    attachInfo.setType(bundleDto.getAttachFileType());
                    attachInfo.setAttachNo(bundleDto.getAttachNo());
                    attachInfo.setSize(bundleDto.getAttachFileSize());
                    attachInfo.setFullPath(bundleDto.getAttachFilePath());

                    chattingDto.setAttachInfo(attachInfo);

                    chattingDto.setLastedMsg(bundleDto.getMessage());
                    chattingDto.setMsgUserNo(bundleDto.getWriteUserNo());
                    chattingDto.setWriterUser(bundleDto.getWriteUserNo());
                    if (bundleDto.getMessageType() == 3) {
                        chattingDto.setType(3);
                    } else {
                        if (TextUtils.isEmpty(bundleDto.getAttachFilePath())) {
                            chattingDto.setLastedMsgType(Statics.MESSAGE_TYPE_NORMAL);
                            chattingDto.setType(0);
                        } else {
                            chattingDto.setLastedMsgType(Statics.MESSAGE_TYPE_ATTACH);
                            chattingDto.setType(2);
                        }
                    }

                    chattingDto.setLastedMsgAttachType(bundleDto.getAttachFileType());

                    final long roomNo = chattingDto.getRoomNo();
                    final long unreadCount = bundleDto.getUnreadTotalCount();
                    // Update unreadTotalCount to database in new thread, hihi

                    Log.d(TAG, "roomNo:" + roomNo);
                    Log.d(TAG, "unreadCount:" + unreadCount);

                    ShortcutBadger.applyCount(this, (int) unreadCount); //for 1.1.4

                    String currentTime = System.currentTimeMillis() + "";
                    chattingDto.setRegDate(currentTime);
                    chattingDto.setLastedMsgDate(currentTime);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ChatRoomDBHelper.updateUnreadTotalCountChatRoom(roomNo, unreadCount);
                            ChatRoomDBHelper.updateChatRoom(chattingDto.getRoomNo(), chattingDto.getLastedMsg(), chattingDto.getLastedMsgType(), chattingDto.getLastedMsgAttachType(), chattingDto.getLastedMsgDate(), chattingDto.getUnreadTotalCount(), chattingDto.getUnReadCount(), chattingDto.getWriterUserNo());
                        }
                    }).start();

                    // When user receive a notification we will store in to database
                    // If chatting1 Fragment is visible then store this message to database, else get from ChattingFragment
                    if (ChattingFragment.instance != null) {
                        if (ChattingFragment.instance.roomNo == roomNo && ChattingFragment.instance.isVisible) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "addMessage 3");
//                                    Log.d(TAG,new Gson().toJson(chattingDto));
                                    ChatMessageDBHelper.addMessage(chattingDto);
                                }
                            }).start();
                        }
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final List<ChattingDto> listChatMessage = ChatMessageDBHelper.getMsgSession(chattingDto.getRoomNo(), 0, ChatMessageDBHelper.FIRST);
                                if (listChatMessage != null && listChatMessage.size() > 0) {
                                    Log.d(TAG, "addMessage 3 with ChattingFragment null & listChatMessage != null");
                                    ChatMessageDBHelper.addMessage(chattingDto);
                                } else {
                                    Log.d(TAG, "dont add because listChatMessage no data");
                                }
                            }
                        }).start();
                    }

                    if (chattingDto.getWriterUserNo() != Utils.getCurrentId()) {
                        Intent myIntent = new Intent(this, ChattingActivity.class);
                        myIntent.putExtra(Statics.CHATTING_DTO, chattingDto);
                        myIntent.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo);
                        TreeUserDTOTemp treeUserDTOTemp = Utils.GetUserFromDatabase(listTemp, chattingDto.getWriterUserNo());
                        boolean isShowNotification = bundleDto.isShowNotification();
                        if (roomNo != CrewChatApplication.currentRoomNo) {
                            if (treeUserDTOTemp != null) {
                                if (isShowNotification) {

                                    String url = "";
                                    String name = "";
                                    for (TreeUserDTOTemp u : listTemp) {
                                        if (u.getUserNo() == chattingDto.getWriterUserNo()) {
                                            url = new Prefs().getServerSite() + u.getAvatarUrl();
                                            name = u.getName();
                                            break;
                                        }
                                    }

                                    sendNotification(chattingDto.getMessage(), name, url, myIntent, chattingDto.getUnreadTotalCount(), roomNo);
                                }
                            } else {
                                if (isShowNotification) {
                                    sendNotification(chattingDto.getMessage(), "Crew Chat", "", myIntent, chattingDto.getUnreadTotalCount(), roomNo);
                                }
                            }
                        }
                    }

                    if (CurrentChatListFragment.fragment != null) {
                        if (ChattingFragment.instance != null && ChattingFragment.instance.isVisible && ChattingFragment.instance.roomNo == roomNo) {
                            chattingDto.setLastedMsgDate(TimeUtils.convertTimeDeviceToTimeServerDefault(chattingDto.getRegDate()));

                            CurrentChatListFragment.fragment.isUpdate = true;
                            Log.d(TAG, "CurrentChatListFragment 1");
                            CurrentChatListFragment.fragment.updateDataSet(chattingDto);

                        } else {
                            Log.d(TAG, "CurrentChatListFragment 2");
                            chattingDto.setLastedMsgDate(TimeUtils.convertTimeDeviceToTimeServerDefault(chattingDto.getRegDate()));
                            CurrentChatListFragment.fragment.isUpdate = true;


                            long roomNoGet = chattingDto.getRoomNo();
                            Log.d(TAG, "roomNoGet:" + roomNoGet);
//                            Log.d(TAG, "chattingDto:" + new Gson().toJson(chattingDto));
                            CurrentChatListFragment.fragment.updateDataSet(chattingDto);


                        }

                    }

                    // Just send notification
                    sendBroadcastToActivity(chattingDto, true);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * RECEIVE CODE 2
     */
    private void receiveCode2(Bundle bundle) {
        try {
            if (bundle.containsKey("UserNo")) {
                /** GET UserNo*/
                int userNo = Integer.parseInt(bundle.getString("UserNo", "0"));
                if (userNo == Utils.getCurrentId()) {
                    if (bundle.containsKey("Data")) {
                        /** GET RoomNo */
                        long roomNo = Long.parseLong(bundle.getString("Data", "0"));

                        /** Set Intent */
                        Intent intent = new Intent(this, ChattingActivity.class);
                        intent.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo);

                        /** Notification */

                        sendNotification(Utils.getString(R.string.notification_add_user),
                                Utils.getString(R.string.app_name),
                                null,
                                intent,
                                0,
                                roomNo);


                        /** Send Broadcast */
                        if ((CurrentChatListFragment.fragment != null && CurrentChatListFragment.fragment.isActive) || (ChattingFragment.instance != null && ChattingFragment.instance.isActive)) {
                            Intent intentBroadcast = new Intent(Constant.INTENT_FILTER_ADD_USER);
                            intentBroadcast.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo);
                            sendBroadcast(intentBroadcast);
                        } else if (CurrentChatListFragment.fragment != null) {
                            CurrentChatListFragment.fragment.isUpdate = true;
                            CurrentChatListFragment.fragment.reloadDataSet();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * CHAT DELETE MEMBER CODE = 3
     * */

    private void chatDeleteMember(Bundle extras) {
        if (extras.containsKey("Data")) {
            try {
                /** Get RoomNo */
                String objExtra = extras.getString("Data", "");
                JSONObject object = new JSONObject(objExtra);

                long roomNo = Long.parseLong(object.getString("RoomNo"));
                /** Send Broadcast */
                Intent intent = new Intent(Constant.INTENT_FILTER_CHAT_DELETE_USER);
                intent.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo);
                sendBroadcast(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * RECEIVE CODE 5
     */
    private void receiveCode5(Bundle extras) {
//        Log.d(TAG,"receiveCode5"+new Gson().toJson(extras));
        if (extras.containsKey("Data")) {
            try {
                /** Get RoomNo */
                String objExtra = extras.getString("Data", "");
                JSONObject object = new JSONObject(objExtra);

                long userNo = 0;
                if (extras.containsKey("UserNo")) {
                    userNo = Long.parseLong(extras.getString("UserNo", "0"));
                }


                final long roomNo = object.getLong("RoomNo");
                final int unReadTotalCount = object.getInt("UnreadTotalCount");

                // Update unreadTotalCount to database in new thread, hihi

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ChatRoomDBHelper.updateUnreadTotalCountChatRoom(roomNo, unReadTotalCount);
                    }
                }).start();

                ShortcutBadger.applyCount(this, unReadTotalCount); //for 1.1.4

                Constant.cancelAllNotification(CrewChatApplication.getInstance(), (int) roomNo);
                Intent intent = new Intent(Constant.INTENT_FILTER_GET_MESSAGE_UNREAD_COUNT);
                intent.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo);
                intent.putExtra(Constant.KEY_INTENT_UNREAD_TOTAL_COUNT, unReadTotalCount);
                intent.putExtra(Constant.KEY_INTENT_USER_NO, userNo);
                sendBroadcast(intent);

                if (CurrentChatListFragment.fragment != null) {

                    boolean flag = CurrentChatListFragment.fragment.active();
                    if (!flag) {
                        // update read msg when onpause activity
                        CurrentChatListFragment.fragment.updateReadMsgWhenOnPause(roomNo, unReadTotalCount, userNo);
                        Log.d("TAG", "update read msg when onpause activity");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void receiveCode8(Bundle extras) {
        Log.d(TAG, "receiveCode8");
        if (extras.containsKey("Data")) {
            Log.d(TAG, "containsKey Data");
            try {
                /** Get RoomNo */
                String objExtra = extras.getString("Data", "");
                JSONObject object = new JSONObject(objExtra);

                final long roomNo = object.getLong("RoomNo");
                Log.d(TAG, "roomNo:" + roomNo);

                HttpRequest.getInstance().GetChatRoom(roomNo, new OnGetChatRoom() {
                    @Override
                    public void OnGetChatRoomSuccess(final ChatRoomDTO chatRoomDTO) {

                        if (chatRoomDTO != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    ChatRoomDBHelper.updateChatRoom(roomNo, chatRoomDTO.getRoomTitle().trim());
                                }
                            }).start();
                            Intent intent = new Intent(Constant.INTENT_FILTER_UPDATE_ROOM_NAME);
                            intent.putExtra(Statics.ROOM_NO, roomNo);
                            intent.putExtra(Statics.ROOM_TITLE, chatRoomDTO.getRoomTitle().trim());
                            sendBroadcast(intent);
                        }

                    }

                    @Override
                    public void OnGetChatRoomFail(ErrorDto errorDto) {
                        Log.d(TAG, "OnGetChatRoomFail");
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    interface getBitmap {
        void onSuccess(Bitmap result);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1, getNotification());
    }

    public Notification getNotification() {
        String channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            channel = createChannel();
        else {
            channel = "";
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channel).setSmallIcon(android.R.drawable.ic_menu_mylocation).setContentTitle("crewChat");
        Notification notification = mBuilder
                .setPriority(PRIORITY_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();


        return notification;
    }

    @NonNull
    @TargetApi(26)
    private synchronized String createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        String name = "crewChat";
        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel mChannel = new NotificationChannel("crewChat channel", name, importance);

        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            stopSelf();
        }
        return "crewChat channel";
    }

    private void sendNotification(String msg, final String title, String avatarUrl, Intent myIntent, final int unReadCount, final long roomNo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final long[] vibrate = new long[]{1000, 1000, 0, 0, 0};
            final Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // CharSequence name = getString(R.string.channel_name);
            // if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(channelId, "crewChat", importance);
            mChannel.setShowBadge(true);
            mNotificationManager.createNotificationChannel(mChannel);
            myIntent.putExtra(Statics.CHATTING_DTO, chattingDto);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (chattingDto != null && chattingDto.getAttachNo() != 0) {
                msg = Utils.getString(R.string.notification_file) + chattingDto.getAttachFileName();
                chattingDto.setType(2);
                chattingDto.setAttachInfo(new AttachDTO());
                chattingDto.getAttachInfo().setAttachNo(chattingDto.getAttachNo());
                chattingDto.getAttachInfo().setType(chattingDto.getAttachFileType());
                chattingDto.getAttachInfo().setFullPath(chattingDto.getAttachFilePath());
                chattingDto.getAttachInfo().setFileName(chattingDto.getAttachFileName());
                chattingDto.getAttachInfo().setSize(chattingDto.getAttachFileSize());
            } else {
                if (TextUtils.isEmpty(msg)) {
                    msg = Utils.getString(R.string.notification_add_user);
                }
            }

            final String msgTemp = msg;
            if (avatarUrl != null) {
                Bitmap bitmap = getBitmapFromURL(avatarUrl);

                mBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId);
                mBuilder.setNumber(unReadCount)
                        .setSmallIcon(R.drawable.small_icon_chat)
                        .setLargeIcon(bitmap)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msgTemp))
                        .setContentText(msgTemp)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setChannelId(channelId)
                        .setAutoCancel(true);

                // Check notification setting and config notification
                if (isEnableSound) mBuilder.setSound(soundUri);
                if (isEnableVibrate) mBuilder.setVibrate(vibrate);
                mBuilder.setContentIntent(contentIntent);
                if (msgTemp.contains("\r\n")) {
                    NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
                    /** STYLE BIG TEXT */
                    String bigText = msgTemp.replaceAll("\r\n", "<br/>");
                    bigTextStyle.bigText(Html.fromHtml(bigText));
                    mBuilder.setStyle(bigTextStyle);
                    mBuilder.setContentText(msgTemp.split("\r\n")[0]);
                }
                if ((int) roomNo != (int) CrewChatApplication.currentNotification) {
                    CrewChatApplication.currentNotification = roomNo;
                    mNotificationManager.cancelAll();
                }
                Notification notification = mBuilder.build();
                notification.number = 100;
                notification.tickerText = getTickerText(unReadCount);
                mNotificationManager.notify((int) roomNo, mBuilder.build());
                startForeground(2, notification);
            }
        } else {
            final long[] vibrate = new long[]{1000, 1000, 0, 0, 0};
            final Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            myIntent.putExtra(Statics.CHATTING_DTO, chattingDto);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (chattingDto != null && chattingDto.getAttachNo() != 0) {
                msg = Utils.getString(R.string.notification_file) + chattingDto.getAttachFileName();
                chattingDto.setType(2);
                chattingDto.setAttachInfo(new AttachDTO());
                chattingDto.getAttachInfo().setAttachNo(chattingDto.getAttachNo());
                chattingDto.getAttachInfo().setType(chattingDto.getAttachFileType());
                chattingDto.getAttachInfo().setFullPath(chattingDto.getAttachFilePath());
                chattingDto.getAttachInfo().setFileName(chattingDto.getAttachFileName());
                chattingDto.getAttachInfo().setSize(chattingDto.getAttachFileSize());
            } else {
                if (TextUtils.isEmpty(msg)) {
                    msg = Utils.getString(R.string.notification_add_user);
                }
            }

            final String msgTemp = msg;
            if (avatarUrl != null) {
                Bitmap bitmap = getBitmapFromURL(avatarUrl);
                mBuilder = new NotificationCompat.Builder(getApplicationContext());
                mBuilder.setNumber(unReadCount)
                        .setSmallIcon(R.drawable.small_icon_chat)
                        .setLargeIcon(bitmap)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msgTemp))
                        .setContentText(msgTemp)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setAutoCancel(true);

                // Check notification setting and config notification
                if (isEnableSound) mBuilder.setSound(soundUri);
                if (isEnableVibrate) mBuilder.setVibrate(vibrate);
                mBuilder.setContentIntent(contentIntent);
                if (msgTemp.contains("\r\n")) {
                    NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
                    /** STYLE BIG TEXT */
                    String bigText = msgTemp.replaceAll("\r\n", "<br/>");
                    bigTextStyle.bigText(Html.fromHtml(bigText));
                    mBuilder.setStyle(bigTextStyle);
                    mBuilder.setContentText(msgTemp.split("\r\n")[0]);
                }
                if ((int) roomNo != (int) CrewChatApplication.currentNotification) {
                    CrewChatApplication.currentNotification = roomNo;
                    mNotificationManager.cancelAll();
                }
                Notification notification = mBuilder.build();
                notification.number = 100;
                notification.tickerText = getTickerText(unReadCount);
                mNotificationManager.notify((int) roomNo, mBuilder.build());
                startForeground(1, notification);
            }
        }

    }

    private String getTickerText(int total) {
        String result;

        switch (total) {
            case 1:
                result = total + " New Message";
                break;
            default:
                result = total + " New Messages";
                break;
        }

        return result;
    }

    // called to send data to Activity
    private void sendBroadcastToActivity(ChattingDto dto, boolean isNotify) {
        Intent intent = new Intent(Statics.ACTION_RECEIVER_NOTIFICATION);
        intent.putExtra(Statics.GCM_DATA_NOTIFICATOON, new Gson().toJson(dto));
        intent.putExtra(Statics.GCM_NOTIFY, isNotify);
        sendBroadcast(intent);
        Log.d(TAG, "sendBroadcastToActivity ACTION_RECEIVER_NOTIFICATION");
        EventBus.getDefault().post(new ReceiveMessage(dto));
    }

    public Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}