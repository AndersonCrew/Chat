package com.dazone.crewchatoff.ViewHolders;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;

public class ChattingDateViewHolder extends BaseChattingHolder {
    private TextView time;

    public ChattingDateViewHolder(View v) {
        super(v);
    }

    @Override
    protected void setup(View v) {
        time = (TextView) v.findViewById(R.id.time);
    }

    @Override
    public void bindData(ChattingDto dto) {
        if (TextUtils.isEmpty(dto.getRegDate())) {
            time.setText(TimeUtils.showTimeWithoutTimeZone(dto.getTime(), Statics.DATE_FORMAT_YYYY_MM_DD));
        } else {
            if (dto.getRegDate().equalsIgnoreCase(Utils.getString(R.string.today))) {
                time.setText(Utils.getString(R.string.today));
            }
            else {
                time.setText(TimeUtils.displayTimeWithoutOffset(dto.getRegDate()));
            }
        }
    }
}