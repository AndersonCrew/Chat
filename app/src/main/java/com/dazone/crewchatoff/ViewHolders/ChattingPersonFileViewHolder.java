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
import com.dazone.crewchatoff.constant.Constants;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.AttachDTO;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.interfaces.AudioGetDuration;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Utils;

import java.io.File;

public class ChattingPersonFileViewHolder extends ChattingSelfFileViewHolder {
    private String TAG = "ChattingPersonFileViewHolder";
    private TextView user_name_tv;
    private TextView tvUnread;
    private ImageView avatar_imv;
    private TextView tvDuration;
    private LinearLayout layoutNotAudio, layoutAudio;
    private LinearLayout llDate;
    private TextView tvDate;

    public ChattingPersonFileViewHolder(View v) {
        super(v);
    }

    @Override
    protected void setup(View v) {
        super.setup(v);
        user_name_tv = v.findViewById(R.id.user_name_tv);
        avatar_imv = v.findViewById(R.id.avatar_imv);
        tvUnread = v.findViewById(R.id.text_unread);

        tvDuration = v.findViewById(R.id.tvDuration);
        layoutNotAudio = v.findViewById(R.id.layoutNotAudio);
        layoutAudio = v.findViewById(R.id.layoutAudio);
        llDate = v.findViewById(R.id.llDate);
        tvDate = v.findViewById(R.id.time);
    }

    @Override
    public void bindData(final ChattingDto dto) {
        super.bindData(dto);

        llDate.setVisibility(dto.isHeader()? View.VISIBLE : View.GONE);
        tvDate.setText(Utils.getStrDate(dto));

        String _fileName = dto.getAttachInfo().getFileName();
        if (_fileName == null || _fileName.trim().length() == 0)
            _fileName = dto.getAttachFileName();
        if (_fileName == null) _fileName = "";
        String fileType = Utils.getFileType(_fileName);
        ImageUtils.imageFileType(imgFileThumb, fileType);

        user_name_tv.setText(dto.getName());
        ImageUtils.showRoundImage(dto, avatar_imv);
        String strUnReadCount = String.valueOf(dto.getUnReadCount());
        tvUnread.setText(strUnReadCount);
        tvUnread.setOnClickListener(v -> {
            Log.d(TAG, "tvUnread");
            Intent intent = new Intent(Constant.INTENT_GOTO_UNREAD_ACTIVITY);
            intent.putExtra(Statics.MessageNo, dto.getMessageNo());
            BaseActivity.Instance.sendBroadcast(intent);
        });

        avatar_imv.setOnClickListener(v -> {
            try {
                int userNo = dto.getUserNo();
                Intent intent = new Intent(BaseActivity.Instance, ProfileUserActivity.class);
                intent.putExtra(Constant.KEY_INTENT_USER_NO, userNo);
                BaseActivity.Instance.startActivity(intent);
                BaseActivity.Instance.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // check fileType is audio or not
        if (Utils.isAudio(fileType)) {
            if (layoutNotAudio != null) layoutNotAudio.setVisibility(View.GONE);
            if (layoutAudio != null) layoutAudio.setVisibility(View.VISIBLE);

            if (tvDuration != null) {
                AttachDTO attachDTO = dto.getAttachInfo();
                if (attachDTO != null) {
                    String fileName = attachDTO.getFileName();
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Statics.AUDIO_RECORDER_FOLDER + "/" + fileName;
                    File file = new File(path);
                    if (!file.exists()) {
                        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Statics.AUDIO_RECORDER_FOLDER_ROOT + "/" + fileName;
                    }
                    new Constant.audioGetDuration(BaseActivity.Instance, path, duration -> tvDuration.setText(duration)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    tvDuration.setText("");
                }
            }
        } else {
            if (layoutNotAudio != null) layoutNotAudio.setVisibility(View.VISIBLE);
            if (layoutAudio != null) layoutAudio.setVisibility(View.GONE);
        }

        tvUnread.setVisibility(CrewChatApplication.getInstance().getPrefs().getBooleanValue(Constants.HAS_CALL_UNREAD_COUNT, false) || dto.getUnReadCount() == 0? View.GONE : View.VISIBLE);}
}