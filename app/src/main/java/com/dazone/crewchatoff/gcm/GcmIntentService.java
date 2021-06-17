package com.dazone.crewchatoff.gcm;

import android.annotation.TargetApi;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
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
import com.dazone.crewchatoff.database.ChatRoomDBHelper;
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


import me.leolin.shortcutbadger.ShortcutBadger;

import static android.support.v4.app.NotificationCompat.PRIORITY_LOW;

public class GcmIntentService extends IntentService {
    private String TAG = ">>>GcmIntentService";
    private String channelId = "0011001";
    private String channelName = "CrewChat 0011001";
    private String channelIdNonSound = "0022002";
    private String channelNameNonSound = "CrewChat 0022002";
    private NotificationChannel channel1, channel2;

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
    boolean isEnableN, isEnableSound, isEnableVibrate, isEnableTime, isPCVersion;

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
                            Log.d("TAG", "Receive new message");
                            receiveCode1(extras);
                            break;
                        case 2:
                            Log.d("TAG", "Case 2 Add Chat Room User ");
                            receiveCode2(extras);
                            break;
                        case 3:
                            Log.d("TAG", "Case 3 Delete Chat Room User ");
                            chatDeleteMember(extras);
                            break;
                        case 4:
                            Log.d("TAG", "Case 4 ###");
                            break;
                        case 5:
                            Log.d("TAG", "Case 5 UpdateMessageUnreadCount");
                            receiveCode5(extras);
                            break;
                        case 8:
                            Log.d("TAG", "Case 8 Update RoomName");
                            receiveCode8(extras);

                            case 7:
                            Log.d("TAG", "Case 7 AddChatRoomUserRestore");
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
                    chattingDto.setUnReadCount(bundleDto.getUnreadTotalCount());
                    chattingDto.setMessage(bundleDto.getMessage());
                    chattingDto.setMessageNo(bundleDto.getMessageNo());
                    chattingDto.setWriterUserNo(bundleDto.getWriteUserNo());

                    chattingDto.setAttachNo(bundleDto.getAttachNo());
                    chattingDto.setAttachFileName(bundleDto.getAttachFileName());
                    chattingDto.setAttachFileType(bundleDto.getAttachFileType());
                    chattingDto.setAttachFilePath(bundleDto.getAttachFilePath());
                    chattingDto.setAttachFileSize(bundleDto.getAttachFileSize());
                    chattingDto.setRegDate(bundleDto.getRegDate());
                    chattingDto.setStrRegDate(bundleDto.getStrRegDate());

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
                    final int unreadCount = bundleDto.getUnreadTotalCountAtAll();

                    if (unreadCount > 0) {
                        ShortcutBadger.applyCount(this, unreadCount); //for 1.1.4
                    } else {
                        ShortcutBadger.removeCount(this);
                    }

                    chattingDto.setLastedMsgDate(bundleDto.getRegDate());

                    new Thread(() -> {
                        Intent intent = new Intent(Statics.ACTION_RECEIVER_NOTIFICATION);
                        intent.putExtra(Statics.GCM_DATA_NOTIFICATOON, new Gson().toJson(chattingDto));
                        intent.putExtra(Statics.GCM_NOTIFY, true);
                        intent.putExtra(Statics.CHATTING_DTO, chattingDto);
                        sendBroadcast(intent);

                        ChatRoomDBHelper.updateUnreadTotalCountChatRoom(roomNo, unreadCount);
                        ChatRoomDBHelper.updateChatRoom(chattingDto.getRoomNo(), chattingDto.getLastedMsg(), chattingDto.getLastedMsgType(), chattingDto.getLastedMsgAttachType(), chattingDto.getLastedMsgDate(), chattingDto.getUnreadTotalCount(), chattingDto.getUnReadCount(), chattingDto.getWriterUserNo());
                    }).start();

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

                                    if (chattingDto.getType() == 3) {
                                        String[] msg = chattingDto.getMessage().split("\n");
                                        if (msg.length > 1)
                                            sendNotification(msg[1], msg[0], url, myIntent, chattingDto.getUnreadTotalCount(), roomNo);
                                        else
                                            sendNotification(chattingDto.getMessage(), name, url, myIntent, chattingDto.getUnreadTotalCount(), roomNo);
                                    } else
                                        sendNotification(chattingDto.getMessage(), name, url, myIntent, chattingDto.getUnreadTotalCount(), roomNo);
                                }
                            } else {
                                if (isShowNotification) {
                                    sendNotification(chattingDto.getMessage(), "Crew Chat", "", myIntent, chattingDto.getUnreadTotalCount(), roomNo);
                                }
                            }
                        }
                    }

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
            if (bundle.containsKey("Data")) {
                /** GET UserNo*/

                String objExtra = bundle.getString("Data", "");
                JSONObject object = new JSONObject(objExtra);
                int userNo = Integer.parseInt(object.getString("SubjectUserNo"));
                long roomNo = Long.parseLong(object.getString("RoomNo"));
                Intent intent = new Intent(this, ChattingActivity.class);
                intent.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo);

                sendNotification(Utils.getString(R.string.notification_add_user),
                        Utils.getString(R.string.app_name),
                        null,
                        intent,
                        0,
                        roomNo);


                if ((CurrentChatListFragment.fragment != null && CurrentChatListFragment.fragment.isActive) || (ChattingFragment.instance != null && ChattingFragment.instance.isActive)) {
                    Intent intentBroadcast = new Intent(Constant.INTENT_FILTER_ADD_USER);
                    intentBroadcast.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo);
                    sendBroadcast(intentBroadcast);
                } else if (CurrentChatListFragment.fragment != null) {
                    CurrentChatListFragment.fragment.isUpdate = true;
                    CurrentChatListFragment.fragment.reloadDataSet();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        if (extras.containsKey("Data")) {
            try {
                String objExtra = extras.getString("Data", "");
                JSONObject object = new JSONObject(objExtra);

                long userNo = 0;
                if (extras.containsKey("UserNo")) {
                    userNo = Long.parseLong(extras.getString("UserNo", "0"));
                }


                final long roomNo = object.getLong("RoomNo");
                final String baseDate = object.getString("BaseDate");
                final String strBaseDate = object.getString("strBaseDate");
                final int unReadTotalCount = object.getInt("UnreadTotalCount");
                final int unReadTotalCountAtAll = object.getInt("UnreadTotalCountAtAll");

                if (unReadTotalCountAtAll > 0) {
                    ShortcutBadger.applyCount(this, unReadTotalCountAtAll); //for 1.1.4
                } else {
                    ShortcutBadger.removeCount(this);
                }

                Constant.cancelAllNotification(CrewChatApplication.getInstance(), (int) roomNo);
                Intent intent = new Intent(Constant.INTENT_FILTER_GET_MESSAGE_UNREAD_COUNT);
                intent.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo);
                intent.putExtra(Constant.KEY_INTENT_UNREAD_TOTAL_COUNT, unReadTotalCount);
                intent.putExtra(Constant.KEY_INTENT_USER_NO, userNo);
                intent.putExtra(Constant.KEY_INTENT_BASE_DATE, strBaseDate);
                sendBroadcast(intent);

                if (CurrentChatListFragment.fragment != null) {
                    boolean flag = CurrentChatListFragment.fragment.active();
                    if (!flag) {
                        CurrentChatListFragment.fragment.updateReadMsgWhenOnPause(roomNo, unReadTotalCount, userNo);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void receiveCode8(Bundle extras) {
        if (extras.containsKey("Data")) {
            try {
                String objExtra = extras.getString("Data", "");
                JSONObject object = new JSONObject(objExtra);

                final long roomNo = object.getLong("RoomNo");
                HttpRequest.getInstance().GetChatRoom(roomNo, new OnGetChatRoom() {
                    @Override
                    public void OnGetChatRoomSuccess(final ChatRoomDTO chatRoomDTO) {
                        if (chatRoomDTO != null) {
                            new Thread(() -> ChatRoomDBHelper.updateChatRoom(roomNo, chatRoomDTO.getRoomTitle().trim())).start();
                            Intent intent = new Intent(Constant.INTENT_FILTER_UPDATE_ROOM_NAME);
                            intent.putExtra(Statics.ROOM_NO, roomNo);
                            intent.putExtra(Statics.ROOM_TITLE, chatRoomDTO.getRoomTitle().trim());
                            sendBroadcast(intent);
                        }
                    }

                    @Override
                    public void OnGetChatRoomFail(ErrorDto errorDto) {
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel1 = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel2 = new NotificationChannel(channelIdNonSound, channelNameNonSound, NotificationManager.IMPORTANCE_LOW);
        }
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

        NotificationChannel mChannel = null;
        String chanelId = isEnableSound ? channelId : channelIdNonSound;
        if(isEnableSound) {
            mChannel = channel1;
        } else {
            mChannel = channel2;
        }

        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            stopSelf();
        }

        return chanelId;
    }

    private void sendNotification(String msg, final String title, String avatarUrl, Intent myIntent, final int unReadCount, final long roomNo) {
        if(!isPCVersion) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final long[] vibrate = new long[]{1000, 1000, 1000, 1000, 1000};
            final Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel mChannel = isEnableSound ? channel1 : channel2;
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

                String idChanel = isEnableSound ? channelId : channelIdNonSound;
                mBuilder = new NotificationCompat.Builder(getApplicationContext(), idChanel);
                mBuilder.setNumber(unReadCount)
                        .setSmallIcon(R.drawable.small_icon_chat)
                        .setLargeIcon(bitmap)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msgTemp))
                        .setContentText(msgTemp)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setChannelId(idChanel)
                        .setAutoCancel(false);

                // Check notification setting and config notification
                if (isEnableSound) {
                    mBuilder.setSound(soundUri);
                } else {
                    mBuilder.setSound(null);
                }

                if (isEnableVibrate) {
                    mBuilder.setVibrate(vibrate);
                    Vibrator v = (Vibrator) CrewChatApplication.getInstance().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(500);
                } else {
                    final long[] noVibrate = new long[]{0, 0, 0, 0, 0};
                    mBuilder.setVibrate(noVibrate);
                    Vibrator v = (Vibrator) CrewChatApplication.getInstance().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(0);
                }

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
                        .setAutoCancel(false);

                // Check notification setting and config notification
                if (isEnableSound) {
                    mBuilder.setSound(soundUri);
                } else {
                    mBuilder.setSound(null);
                }

                if (isEnableVibrate) {
                    mBuilder.setVibrate(vibrate);
                    Vibrator v = (Vibrator) CrewChatApplication.getInstance().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(500);
                } else {
                    final long[] noVibrate = new long[]{0, 0, 0, 0, 0};
                    mBuilder.setVibrate(noVibrate);
                    Vibrator v = (Vibrator) CrewChatApplication.getInstance().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(0);
                }

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
        intent.putExtra(Statics.CHATTING_DTO, dto);
        sendBroadcast(intent);
        Log.d(TAG, "sendBroadcastToActivity ACTION_RECEIVER_NOTIFICATION");
        //EventBus.getDefault().post(new ReceiveMessage(dto));
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