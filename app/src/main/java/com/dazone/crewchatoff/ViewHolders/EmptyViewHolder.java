package com.dazone.crewchatoff.ViewHolders;

import android.view.View;
import com.dazone.crewchatoff.dto.ChattingDto;

public class EmptyViewHolder extends BaseChattingHolder {

    public EmptyViewHolder(View v) {
        super(v);
    }

    @Override
    protected void setup(View v) {
    }

    @Override
    public void bindData(ChattingDto dto) {
    }
}