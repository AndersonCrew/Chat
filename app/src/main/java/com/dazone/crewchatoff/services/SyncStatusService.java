package com.dazone.crewchatoff.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;

public class SyncStatusService extends Service {
    private final IBinder mBinder = new Binder();

    public class Binder extends android.os.Binder {
        public SyncStatusService getMyService() {
            return SyncStatusService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void syncStatusString() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void syncData() {
    }
}