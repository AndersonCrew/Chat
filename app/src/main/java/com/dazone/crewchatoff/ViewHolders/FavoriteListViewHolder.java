package com.dazone.crewchatoff.ViewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.dazone.crewchatoff.Class.TreeOfficeView;
import com.dazone.crewchatoff.Class.TreeView;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.interfaces.OnDeleteFavoriteGroup;

import java.util.HashMap;

public class FavoriteListViewHolder extends ItemViewHolder<TreeUserDTO> {
    private HashMap<Integer, ImageView> mStatusViewMap;
    private OnDeleteFavoriteGroup mDeleteCallback;

    public FavoriteListViewHolder(View itemView) {
        super(itemView);
    }

    public FavoriteListViewHolder(View itemView, HashMap<Integer, ImageView> statusViewMap, OnDeleteFavoriteGroup deleteCallback) {
        super(itemView);
        this.mStatusViewMap = statusViewMap;
        this.mDeleteCallback = deleteCallback;
    }

    public LinearLayout favorite_lnl;

    @Override
    protected void setup(View v) {
        favorite_lnl = (LinearLayout) v.findViewById(R.id.favorite_lnl);
    }

    @Override
    public void bindData(TreeUserDTO dto) {
        TreeView tree = new TreeOfficeView(favorite_lnl.getContext(), dto, mStatusViewMap, -1, mDeleteCallback);
        tree.addToView(favorite_lnl);
    }
}