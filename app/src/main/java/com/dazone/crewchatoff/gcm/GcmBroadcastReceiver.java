package com.dazone.crewchatoff.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.dazone.crewchatoff.database.AllUserDBHelper;
import com.dazone.crewchatoff.database.BelongsToDBHelper;
import com.dazone.crewchatoff.database.ChatMessageDBHelper;
import com.dazone.crewchatoff.database.ChatRoomDBHelper;
import com.dazone.crewchatoff.database.DepartmentDBHelper;
import com.dazone.crewchatoff.database.FavoriteGroupDBHelper;
import com.dazone.crewchatoff.database.FavoriteUserDBHelper;
import com.dazone.crewchatoff.database.UserDBHelper;
import com.dazone.crewchatoff.eventbus.ReloadActivity;
import com.dazone.crewchatoff.utils.CrewChatApplication;

import org.greenrobot.eventbus.EventBus;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().compareTo(Intent.ACTION_LOCALE_CHANGED) == 0) {
            BelongsToDBHelper.clearBelong();
            AllUserDBHelper.clearUser();
            ChatRoomDBHelper.clearChatRooms();
            ChatMessageDBHelper.clearMessages();
            DepartmentDBHelper.clearDepartment();
            UserDBHelper.clearUser();
            FavoriteGroupDBHelper.clearGroups();
            FavoriteUserDBHelper.clearFavorites();
            CrewChatApplication.resetValue();
            EventBus.getDefault().post(new ReloadActivity());
        }

        ComponentName comp = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, (intent.setComponent(comp)));
        } else {
            startWakefulService(context, (intent.setComponent(comp)));
        }

        setResultCode(Activity.RESULT_OK);
    }
}