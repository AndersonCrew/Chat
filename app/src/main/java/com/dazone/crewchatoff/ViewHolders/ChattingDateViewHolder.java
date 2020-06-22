package com.dazone.crewchatoff.ViewHolders;

import android.location.Location;
import android.view.View;
import android.widget.TextView;

import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;

import java.util.Date;

public class ChattingDateViewHolder extends BaseChattingHolder {
    private TextView time;

    public ChattingDateViewHolder(View v) {
        super(v);
    }

    @Override
    protected void setup(View v) {
        time = v.findViewById(R.id.time);
    }

    @Override
    public void bindData(ChattingDto dto) {
        if(TimeUtils.checkDateIsToday(dto.getRegDate())) {
            time.setText(Utils.getString(R.string.today));
        } else if(TimeUtils.checkDateIsYesterday(dto.getRegDate())) {
            time.setText(Utils.getString(R.string.yesterday));
        } else {
            long chatTime = new Date(TimeUtils.getTime(dto.getRegDate())).getTime();
            time.setText(TimeUtils.showTimeWithoutTimeZone(chatTime, Statics.DATE_FORMAT_YYYY_MM_DD));
        }
    }
}