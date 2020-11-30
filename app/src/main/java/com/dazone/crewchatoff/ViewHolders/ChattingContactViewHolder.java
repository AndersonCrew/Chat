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
import com.dazone.crewchatoff.constant.Constants;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.UserDto;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;

import java.util.Date;

public class ChattingContactViewHolder extends BaseChattingHolder {
    private TextView tv_contact_name, tv_contact_number;
    private TextView date_tv, tvUnread;
    private LinearLayout lnContact;
    private LinearLayout llDate;
    private TextView tvDate;

    public ChattingContactViewHolder(View v) {
        super(v);
    }

    @Override
    protected void setup(View v) {
        tv_contact_name = v.findViewById(R.id.tv_contact_name);
        tv_contact_number = v.findViewById(R.id.tv_contact_number);
        tvUnread = v.findViewById(R.id.text_unread);
        date_tv = v.findViewById(R.id.date_tv);
        lnContact = v.findViewById(R.id.lnContact);
        llDate = v.findViewById(R.id.llDate);
        tvDate = v.findViewById(R.id.time);
    }

    @Override
    public void bindData(final ChattingDto dto) {
        UserDto userDto = dto.getUser();

        llDate.setVisibility(dto.isHeader()? View.VISIBLE : View.GONE);
        tvDate.setText(Utils.getStrDate(dto));

        tv_contact_name.setText(userDto.getFullName());
        if (userDto.getPhoneNumber() != null) {
            tv_contact_number.setText(userDto.getPhoneNumber());
        } else {
            tv_contact_number.setVisibility(View.GONE);
        }

        long regDate = new Date(TimeUtils.getTime(dto.getRegDate())).getTime();
        date_tv.setText(TimeUtils.displayTimeWithoutOffset(CrewChatApplication.getInstance().getApplicationContext(), regDate, 0));

        String strUnReadCount = dto.getUnReadCount() + "";
        tvUnread.setText(strUnReadCount);
        date_tv.setOnClickListener(v -> {
            Intent intent = new Intent(Constant.INTENT_GOTO_UNREAD_ACTIVITY);
            intent.putExtra(Statics.MessageNo, dto.getMessageNo());
            BaseActivity.Instance.sendBroadcast(intent);
        });
        if (dto.getUnReadCount() == 0) {
            tvUnread.setVisibility(View.GONE);
        } else {
            tvUnread.setVisibility(View.VISIBLE);
            tvUnread.setOnClickListener(v -> {
                Intent intent = new Intent(Constant.INTENT_GOTO_UNREAD_ACTIVITY);
                intent.putExtra(Statics.MessageNo, dto.getMessageNo());
                BaseActivity.Instance.sendBroadcast(intent);
            });
        }
        lnContact.setTag(userDto.getPhoneNumber());
        lnContact.setOnClickListener(v -> {
            String phoneNumber = (String) v.getTag();
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            BaseActivity.Instance.startActivity(intent);
        });

        tvUnread.setVisibility(CrewChatApplication.getInstance().getPrefs().getBooleanValue(Constants.HAS_CALL_UNREAD_COUNT, false) || dto.getUnReadCount() == 0? View.GONE : View.VISIBLE);}
}