package com.dazone.crewchatoff.ViewHolders;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.UserDto;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.TimeUtils;

public class ChattingContactViewHolder extends BaseChattingHolder {
    private String TAG = "ChattingContactViewHolder";
    private TextView tv_contact_name, tv_contact_number;
    private TextView date_tv, tvUnread;
    private LinearLayout lnContact;

    public ChattingContactViewHolder(View v) {
        super(v);
    }

    @Override
    protected void setup(View v) {
        tv_contact_name = (TextView) v.findViewById(R.id.tv_contact_name);
        tv_contact_number = (TextView) v.findViewById(R.id.tv_contact_number);
        tvUnread = (TextView) v.findViewById(R.id.text_unread);
        date_tv = (TextView) v.findViewById(R.id.date_tv);
        lnContact = (LinearLayout) v.findViewById(R.id.lnContact);
    }

    @Override
    public void bindData(final ChattingDto dto) {
        UserDto userDto = dto.getUser();

        tv_contact_name.setText(userDto.getFullName());
        if (userDto.getPhoneNumber() != null) {
            tv_contact_number.setText(userDto.getPhoneNumber());
        } else {
            tv_contact_number.setVisibility(View.GONE);
        }

        //ImageUtils.showRoundImage(userDto.getAvatar(), avatar_imv);
        if (!TextUtils.isEmpty(dto.getLastedMsgDate())) {
            date_tv.setText(TimeUtils.displayTimeWithoutOffset(CrewChatApplication.getInstance().getApplicationContext(), dto.getLastedMsgDate(), 0, TimeUtils.KEY_FROM_SERVER));
        } else {
            date_tv.setText(TimeUtils.displayTimeWithoutOffset(CrewChatApplication.getInstance().getApplicationContext(), dto.getRegDate(), 0, TimeUtils.KEY_FROM_SERVER));
        }

        String strUnReadCount = dto.getUnReadCount() + "";
        tvUnread.setText(strUnReadCount);
//        tvUnread.setVisibility(dto.getUnReadCount() == 0 ? View.GONE : View.VISIBLE);
        date_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Constant.INTENT_GOTO_UNREAD_ACTIVITY);
                intent.putExtra(Statics.MessageNo, dto.getMessageNo());
                BaseActivity.Instance.sendBroadcast(intent);
            }
        });
        if (dto.getUnReadCount() == 0) {
            tvUnread.setVisibility(View.GONE);
        } else {
            tvUnread.setVisibility(View.VISIBLE);
            tvUnread.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Constant.INTENT_GOTO_UNREAD_ACTIVITY);
                    intent.putExtra(Statics.MessageNo, dto.getMessageNo());
                    BaseActivity.Instance.sendBroadcast(intent);
                }
            });
        }
        lnContact.setTag(userDto.getPhoneNumber());
        lnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = (String) v.getTag();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                BaseActivity.Instance.startActivity(intent);
//                BaseActivity.Instance.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }
}