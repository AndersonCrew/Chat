package com.dazone.crewchatoff.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.ViewHolders.EmptyViewHolder;
import com.dazone.crewchatoff.ViewHolders.ItemViewHolder;

import com.dazone.crewchatoff.ViewHolders.ProgressViewHolder;
import com.dazone.crewchatoff.ViewHolders.TransferMsgViewHolder;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.ChattingDto;

import com.dazone.crewchatoff.fragment.TabCurrentChatFragment;


import java.util.List;

/**
 * Created by maidinh on 9/2/2017.
 */

public class TransferMsgAdapter extends PullUpLoadMoreRCVAdapter<ChattingDto> {
    String TAG = "CurrentChatAdapter";
    private TabCurrentChatFragment.OnContextMenuSelect mOnContextMenuSelect;



    public TransferMsgAdapter(Context context, List<ChattingDto> myDataSet, RecyclerView recyclerView, TabCurrentChatFragment.OnContextMenuSelect callback) {
        super(context, myDataSet, recyclerView);
        this.mOnContextMenuSelect = callback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;

        if (viewType == VIEW_ITEM) {
            vh = new TransferMsgViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_current_chat_fm, parent, false), this.mOnContextMenuSelect);
        } else if (viewType == Statics.CHATTING_VIEW_TYPE_EMPTY) {
            vh = new EmptyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_empty, parent, false));
        } else {
            vh = new ProgressViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_load_more_item, parent, false));
        }

        return vh;
    }

    @Override
    public int getItemCount() {
        if (mDataSet.size() == 0) {
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
        if (holder instanceof TransferMsgViewHolder) {
            final ChattingDto item = mDataSet.get(position);
            ItemViewHolder viewHolder = (TransferMsgViewHolder) holder;
            viewHolder.bindData(item);
        } else {
            try {
                if (holder instanceof ProgressViewHolder)
                    ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}