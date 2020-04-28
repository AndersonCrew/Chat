package com.dazone.crewchatoff.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.base.BaseSingleBackTick;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.fragment.RenameRoomFragment;
import com.dazone.crewchatoff.interfaces.OnTickCallbackSuccess;
import com.dazone.crewchatoff.utils.Utils;

public class RenameRoomActivity extends BaseSingleBackTick {
    private String roomTitle;
    private int roomNo;
    String TAG = "RenameRoomActivity";
    OnTickCallbackSuccess mOnTickSuccess = new OnTickCallbackSuccess() {
        @Override
        public void onTickSuccess(int roomNo, String roomTitle) {
            Intent intent = new Intent();
            intent.putExtra(Statics.ROOM_NO, roomNo);
            intent.putExtra(Statics.ROOM_TITLE, roomTitle);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private RenameRoomFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUPToolBar(getString(R.string.room_rename));
    }

    @Override
    protected void addFragment(Bundle bundle) {
        Intent intent = getIntent();

        if (intent != null) {
            Bundle b = intent.getExtras();
            roomTitle = b.getString(Statics.ROOM_TITLE, "");
            roomNo = b.getInt(Statics.ROOM_NO, -1);
        }
        Log.d(TAG, "roomNo:" + roomNo);
        fragment = RenameRoomFragment.newInstance(roomNo, roomTitle);
        fragment.setTickSuccessCallback(mOnTickSuccess);
        Utils.addFragmentToActivity(getSupportFragmentManager(), fragment, R.id.content_base_single_activity, false, fragment.getClass().getSimpleName());
    }
}