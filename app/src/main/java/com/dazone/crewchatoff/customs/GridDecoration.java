package com.dazone.crewchatoff.customs;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GridDecoration extends RecyclerView.ItemDecoration {
    private int offset;

    public GridDecoration(int offset) {
        this.offset = offset;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = offset;
        outRect.right = offset;
        outRect.bottom = offset;
    }
}