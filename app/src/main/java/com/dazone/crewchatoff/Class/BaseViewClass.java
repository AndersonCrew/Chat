package com.dazone.crewchatoff.Class;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseViewClass {
    protected View currentView;
    protected Context context;
    protected LayoutInflater inflater;

    public BaseViewClass(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    protected abstract void setupView();

    public void addToView(ViewGroup view) {
        view.addView(currentView);
    }
}