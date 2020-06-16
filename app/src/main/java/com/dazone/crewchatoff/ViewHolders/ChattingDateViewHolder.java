package com.dazone.crewchatoff.ViewHolders;

import android.view.View;
import android.widget.TextView;

import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;

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
        } else time.setText(TimeUtils.showTimeWithoutTimeZone(dto.getTime() - CrewChatApplication.getInstance().getPrefs().getLongValue(Statics.TIME_SERVER_MILI, 0), Statics.DATE_FORMAT_YYYY_MM_DD));
    }
}