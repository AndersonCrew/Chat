package com.dazone.crewchatoff.ViewHolders;

import android.view.View;
import android.widget.ProgressBar;

import com.dazone.crewchatoff.R;

public class ProgressViewHolder extends ItemViewHolder {
    public ProgressBar progressBar;

    public ProgressViewHolder(View v) {
        super(v);
        progressBar = v.findViewById(R.id.progressBar);
    }

    @Override
    protected void setup(View v) {

    }

    @Override
    public void bindData(Object o) {

    }

    @Override
    public String toString() {
        return super.toString();
    }
}