package com.dazone.crewchatoff.ViewHolders;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.activity.ProfileUserActivity;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.AttachDTO;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.interfaces.AudioGetDuration;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by maidinh on 25-Sep-17.
 */

public class AudioVH extends BaseChattingHolder {
    private TextView date_tv;
    private TextView tvUnread;
    private TextView tvDuration;
    private ImageView avatar_imv;
    private LinearLayout btnPlayAudio;
    private  String TAG = "AudioVH";
    private Activity mActivity;
    private ProgressBar progressBar;

    public AudioVH(View v,Activity mActivity) {
        super(v);
        this.mActivity=mActivity;
    }

    @Override
    protected void setup(View v) {
        date_tv = (TextView) v.findViewById(R.id.date_tv);
        tvUnread = (TextView) v.findViewById(R.id.text_unread);
        tvDuration = (TextView) v.findViewById(R.id.tvDuration);

        avatar_imv = (ImageView) v.findViewById(R.id.avatar_imv);
        btnPlayAudio= (LinearLayout) v.findViewById(R.id.btnPlayAudio);
        progressBar=(ProgressBar)v.findViewById(R.id.progressBar);
    }

    @Override
    public void bindData(final ChattingDto dto) {
        // hide progressBar when start
        progressBar.setVisibility(View.GONE);

        // date text
        if (!TextUtils.isEmpty(dto.getLastedMsgDate())) {
            date_tv.setText(TimeUtils.displayTimeWithoutOffset(CrewChatApplication.getInstance().getApplicationContext(), dto.getLastedMsgDate(), 0, TimeUtils.KEY_FROM_SERVER));
        } else {
            date_tv.setText(TimeUtils.displayTimeWithoutOffset(CrewChatApplication.getInstance().getApplicationContext(), dto.getRegDate(), 0, TimeUtils.KEY_FROM_SERVER));
        }
        // tvUnread
        String strUnReadCount = String.valueOf(dto.getUnReadCount());
        tvUnread.setText(strUnReadCount);
//        tvUnread.setVisibility(dto.getUnReadCount() == 0 ? View.GONE : View.VISIBLE);
        tvUnread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "tvUnread");
                Intent intent = new Intent(Constant.INTENT_GOTO_UNREAD_ACTIVITY);
                intent.putExtra(Statics.MessageNo, dto.getMessageNo());
                BaseActivity.Instance.sendBroadcast(intent);
            }
        });

        // avatar
        if (avatar_imv != null) {
            String url = "";
            try {
                if (dto.getImageLink() != null) {
                    url = new Prefs().getServerSite() + dto.getImageLink();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            ImageLoader.getInstance().displayImage(url, avatar_imv, Statics.options2);
            avatar_imv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("avatar_imv onClick", TAG);
                    try {
//                    int userNo = (int) v.getTag();
                        int userNo = dto.getUserNo();
                        Intent intent = new Intent(BaseActivity.Instance, ProfileUserActivity.class);
                        intent.putExtra(Constant.KEY_INTENT_USER_NO, userNo);
                        BaseActivity.Instance.startActivity(intent);
                        BaseActivity.Instance.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        // get duration file
        if (tvDuration != null) {
            AttachDTO attachDTO = dto.getAttachInfo();
            if (attachDTO != null) {
                String fileName = attachDTO.getFileName();
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Statics.AUDIO_RECORDER_FOLDER + "/" + fileName;
                // ex: path = /storage/emulated/0/CrewChat/Audio/17_09_26_08_58_35.mp3
//                Log.d(TAG, "path:" + path);
                new Constant.audioGetDuration(BaseActivity.Instance, path, new AudioGetDuration() {
                    @Override
                    public void onComplete(String duration) {
                        tvDuration.setText(duration);
                    }
                }).execute();
            } else {
                tvDuration.setText("");
            }
        }

        // btnPlayAudio
        btnPlayAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ChattingActivity.instance.checkPermissionsReadExternalStorage()){
                    // download

                }else{
                    // set permission
                    ChattingActivity.instance.setPermissionsReadExternalStorage();
                }
            }
        });
    }

//    void downloadFile(){
//        new Constant.DownloadFile(mActivity, progressDownloading, url, fileName, new DownloadFileFromUrl() {
//            @Override
//            public void onFinish(File file) {
//                Log.d(TAG, "onFinish");
////                try {
////                    galleryAddPic(file.getPath());
////                } catch (Exception e) {
////                    e.printStackTrace();
////                }
////                try {
////                    getVideoMeta(file);
////                } catch (Exception e) {
////                    e.printStackTrace();
////                }
//            }
//        }).execute();
//    }

}