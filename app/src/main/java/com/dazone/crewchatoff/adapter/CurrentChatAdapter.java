package com.dazone.crewchatoff.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dazone.crewchatoff.ViewHolders.*;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.fragment.CurrentChatListFragment;

import java.util.List;

public class CurrentChatAdapter extends PullUpLoadMoreRCVAdapter<ChattingDto> {
    private CurrentChatListFragment.OnContextMenuSelect mOnContextMenuSelect;

    public CurrentChatAdapter(Context context, List<ChattingDto> myDataSet, RecyclerView recyclerView, CurrentChatListFragment.OnContextMenuSelect callback) {
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
            vh = new ListCurrentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_current_chat, parent, false), this.mOnContextMenuSelect);
        } else if (viewType == Statics.CHATTING_VIEW_TYPE_EMPTY) {
            vh = new EmptyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_empty, parent, false));
        } else {
            vh = new ProgressViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_load_more_item, parent, false));
        }

        return vh;
    }

    @Override
    public int getItemCount() {
        if (mDataSet.size() == 0 && !CurrentChatListFragment.fragment.isFirstTime) {
            return 1;
        }

        return mDataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (getItemCount() == 1 && mDataSet.size() == 0) {
            return Statics.CHATTING_VIEW_TYPE_EMPTY;
        }

        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ListCurrentViewHolder) {
            final ChattingDto item = mDataSet.get(position);
            ItemViewHolder viewHolder = (ListCurrentViewHolder) holder;
            if (item.getListTreeUser() != null ) {
                viewHolder.bindData(item);
            }

        } else {
            try {
                if (holder instanceof ProgressViewHolder)
                    ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getTotalUnReadCount() {
        int totalUnReadCount = 0;
        for(ChattingDto dto : mDataSet) {
            totalUnReadCount += dto.getUnReadCount();
        }

        return totalUnReadCount;
    }
}