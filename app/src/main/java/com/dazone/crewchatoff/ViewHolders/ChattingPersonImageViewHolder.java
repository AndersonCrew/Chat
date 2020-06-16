package com.dazone.crewchatoff.ViewHolders;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.ProfileUserActivity;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.interfaces.ILoadImage;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.ImageUtils;

public class ChattingPersonImageViewHolder extends ChattingSelfImageViewHolder {
    private TextView user_name_tv;
    private TextView tvUnread;
    private ImageView avatar_imv;

    public ChattingPersonImageViewHolder(Activity activity, View v, ILoadImage iLoadImage) {
        super(activity, v, iLoadImage);
    }

    @Override
    protected void setup(View v) {
        super.setup(v);
        user_name_tv = v.findViewById(R.id.user_name_tv);
        avatar_imv = v.findViewById(R.id.avatar_imv);
        tvUnread = v.findViewById(R.id.text_unread);
    }

    @Override
    public void bindData(final ChattingDto dto) {
        super.bindData(dto);
        user_name_tv.setText(dto.getName());
        Log.d("bindData", "ChattingPersonImageViewHolder");
        ImageUtils.showRoundImage(dto, avatar_imv);

        String strUnReadCount = dto.getUnReadCount() + "";
        tvUnread.setText(strUnReadCount);
        if (dto.getUnReadCount() == 0) {
            tvUnread.setVisibility(View.GONE);
        } else {
            tvUnread.setVisibility(View.VISIBLE);
            tvUnread.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "tvUnread");
                    Intent intent = new Intent(Constant.INTENT_GOTO_UNREAD_ACTIVITY);
                    intent.putExtra(Statics.MessageNo, dto.getMessageNo());
                    BaseActivity.Instance.sendBroadcast(intent);
                }
            });
            avatar_imv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
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
    }
}