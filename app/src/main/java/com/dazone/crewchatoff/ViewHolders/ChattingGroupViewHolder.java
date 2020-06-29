package com.dazone.crewchatoff.ViewHolders;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.utils.Utils;

public class ChattingGroupViewHolder extends BaseChattingHolder {
    private TextView group_name;
    private ProgressBar progressBar;
    private LinearLayout llDate;
    private TextView tvDate;

    public ChattingGroupViewHolder(View v) {
        super(v);
    }

    @Override
    protected void setup(View v) {
        group_name = v.findViewById(R.id.group_name);
        progressBar = v.findViewById(R.id.progressBar);
    }

    @Override
    public void bindData(ChattingDto dto) {

        llDate.setVisibility(dto.isHeader()? View.VISIBLE : View.GONE);
        tvDate.setText(Utils.getStrDate(dto));

        if (dto.getId() != 0) {
            progressBar.setVisibility(View.GONE);
            group_name.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            group_name.setVisibility(View.VISIBLE);
            group_name.setText(dto.getName());
        }
    }
}