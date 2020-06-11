package com.dazone.crewchatoff.ViewHolders;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.activity.ProfileUserActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.AttachDTO;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.interfaces.AudioGetDuration;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Utils;

import java.io.File;

public class ChattingPersonFileViewHolder extends ChattingSelfFileViewHolder {
    private String TAG = "ChattingPersonFileViewHolder";
    private TextView user_name_tv;
    private TextView tvUnread;
    private ImageView avatar_imv;
    private ImageView ivFile;
    private TextView tvDuration, file_name_tv;
    private LinearLayout layoutNotAudio, layoutAudio;

    public ChattingPersonFileViewHolder(View v) {
        super(v);
    }

    @Override
    protected void setup(View v) {
        super.setup(v);
        ivFile = (ImageView) v.findViewById(R.id.file_thumb);
        user_name_tv = (TextView) v.findViewById(R.id.user_name_tv);
        avatar_imv = (ImageView) v.findViewById(R.id.avatar_imv);
        tvUnread = (TextView) v.findViewById(R.id.text_unread);

        tvDuration = (TextView) v.findViewById(R.id.tvDuration);
        layoutNotAudio = (LinearLayout) v.findViewById(R.id.layoutNotAudio);
        layoutAudio = (LinearLayout) v.findViewById(R.id.layoutAudio);
        file_name_tv = (TextView) v.findViewById(R.id.file_name_tv);
    }

    @Override
    public void bindData(final ChattingDto dto) {
        super.bindData(dto);

        /** Set IMAGE FILE TYPE */

        String _fileName = dto.getAttachInfo().getFileName();
        if (_fileName == null || _fileName.trim().length() == 0)
            _fileName = dto.getAttachFileName();
        if (_fileName == null) _fileName = "";
//        if (file_name_tv != null) file_name_tv.setText(_fileName);
//        Log.d(TAG, "_fileName:" + _fileName);
        String fileType = Utils.getFileType(_fileName);
        ImageUtils.imageFileType(file_thumb, fileType);

        user_name_tv.setText(dto.getName());
        ImageUtils.showRoundImage(dto, avatar_imv);
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
//        avatar_imv.setTag(dto.getUserNo());
        avatar_imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        // check fileType is audio or not
        if (Utils.isAudio(fileType)) {
            if (layoutNotAudio != null) layoutNotAudio.setVisibility(View.GONE);
            if (layoutAudio != null) layoutAudio.setVisibility(View.VISIBLE);

            // settext tvDuration
            if (tvDuration != null) {
                AttachDTO attachDTO = dto.getAttachInfo();
                if (attachDTO != null) {
                    String fileName = attachDTO.getFileName();
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Statics.AUDIO_RECORDER_FOLDER + "/" + fileName;
                    File file = new File(path);
                    if (!file.exists()) {
                        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Statics.AUDIO_RECORDER_FOLDER_ROOT + "/" + fileName;
                    }
                    // ex: path = /storage/emulated/0/CrewChat/Audio/17_09_26_08_58_35.mp3
                    // Log.d(TAG, "path:" + path);
                    new Constant.audioGetDuration(BaseActivity.Instance, path, new AudioGetDuration() {
                        @Override
                        public void onComplete(String duration) {
                            tvDuration.setText(duration);
                        }
                    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    tvDuration.setText("");
                }
            }
        } else {
            if (layoutNotAudio != null) layoutNotAudio.setVisibility(View.VISIBLE);
            if (layoutAudio != null) layoutAudio.setVisibility(View.GONE);
        }

    }
}