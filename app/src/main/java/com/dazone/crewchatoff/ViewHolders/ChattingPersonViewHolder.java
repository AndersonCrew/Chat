package com.dazone.crewchatoff.ViewHolders;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.ProfileUserActivity;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.constant.Constants;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.utils.CircleTransform;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.Utils;

public class ChattingPersonViewHolder extends ChattingSelfViewHolder {
    private TextView user_name_tv;
    private TextView tvUnread;
    private ImageView avatar_imv;
    private Context mContext;
    private LinearLayout llDate;
    private TextView tvDate;

    public ChattingPersonViewHolder(View v) {
        super(v);
        mContext = v.getContext();
    }


    @Override
    protected void setup(View v) {
        super.setup(v);
        user_name_tv = v.findViewById(R.id.user_name_tv);
        avatar_imv = v.findViewById(R.id.avatar_imv);
        tvUnread = v.findViewById(R.id.text_unread);

        llDate = v.findViewById(R.id.llDate);
        tvDate = v.findViewById(R.id.time);
    }

    @Override
    public void bindData(final ChattingDto dto) {
        super.bindData(dto);

        llDate.setVisibility(dto.isHeader()? View.VISIBLE : View.GONE);
        tvDate.setText(Utils.getStrDate(dto));

        user_name_tv.setText(dto.getName() != null ? dto.getName() : "");
        String url = "";

        try {
            if (dto.getImageLink() != null) {
                url = new Prefs().getServerSite() + dto.getImageLink();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!url.trim().equals("http://core.crewcloud.net")) {
            ImageUtils.showCycleImageFromLink(url, avatar_imv, R.dimen.button_height);
        } else {
            //not have avt
            Glide.with(mContext).load(R.drawable.avatar_l).transform(new CircleTransform(CrewChatApplication.getInstance())).into(avatar_imv);
        }

        String strUnReadCount = dto.getUnReadCount() + "";
        tvUnread.setText(strUnReadCount);
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

        tvUnread.setVisibility(CrewChatApplication.getInstance().getPrefs().getBooleanValue(Constants.HAS_CALL_UNREAD_COUNT, false) || dto.getUnReadCount() == 0? View.GONE : View.VISIBLE);}
}