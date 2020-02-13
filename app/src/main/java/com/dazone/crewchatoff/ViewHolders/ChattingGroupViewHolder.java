package com.dazone.crewchatoff.ViewHolders;

import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.R;

public class ChattingGroupViewHolder extends BaseChattingHolder {
    private TextView group_name;
    private ProgressBar progressBar;
    private String TAG = "ChattingGroupViewHolder";

    public ChattingGroupViewHolder(View v) {
        super(v);
    }

    @Override
    protected void setup(View v) {
        group_name = (TextView) v.findViewById(R.id.group_name);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
    }

    @Override
    public void bindData(ChattingDto dto) {
        if (dto.getId() != 0) {
//            progressBar.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            group_name.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            group_name.setVisibility(View.VISIBLE);
            group_name.setText(dto.getName());
            Log.d(TAG, "dto.getName():" + dto.getName());
        }
    }
}