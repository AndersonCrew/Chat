package com.dazone.crewchatoff.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.ViewHolders.ItemViewHolder;
import com.dazone.crewchatoff.ViewHolders.ProgressViewHolder;
import com.dazone.crewchatoff.ViewHolders.RecentFavoriteViewHolder;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.fragment.RecentFavoriteFragment;

import java.util.List;

public class RecentFavoriteAdapter extends PullUpLoadMoreRCVAdapter<ChattingDto> {
    String TAG = "RecentFavoriteAdapter";
    private RecentFavoriteFragment.OnContextMenuSelect mOnContextMenuSelect;

    public RecentFavoriteAdapter(Context context, List<ChattingDto> myDataSet, RecyclerView recyclerView, RecentFavoriteFragment.OnContextMenuSelect callback) {
        super(context, myDataSet, recyclerView);
        this.mOnContextMenuSelect = callback;
        setHasStableIds(true);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_current_chat, parent, false);
            vh = new RecentFavoriteViewHolder(v, this.mOnContextMenuSelect);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.progress_load_more_item, parent, false);

            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RecentFavoriteViewHolder) {
            final ChattingDto item = mDataSet.get(position);
            ItemViewHolder viewHolder = (RecentFavoriteViewHolder) holder;
            viewHolder.bindData(item);
        } else if (holder instanceof ProgressViewHolder) {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }


}
