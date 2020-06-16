package com.dazone.crewchatoff.ViewHolders;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.R;

public class ChattingGroupViewHolderNew extends BaseChattingHolder {
    private TextView group_name;
    private ProgressBar progressBar;
    String TAG = "ChattingGroupViewHolderNew";

    public ChattingGroupViewHolderNew(View v) {
        super(v);
    }

    @Override
    protected void setup(View v) {
        group_name = v.findViewById(R.id.group_name);
        progressBar = v.findViewById(R.id.progressBar);
    }

    @Override
    public void bindData(ChattingDto dto) {
        progressBar.setVisibility(View.GONE);
        group_name.setText(dto.getMessage());

    }
}