package com.dazone.crewchatoff.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.fragment.ChattingFragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class PullUpLoadMoreRCVAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected final int VIEW_ITEM = 1;
    protected final int VIEW_PROGRESS = 0;
    protected List<T> mDataSet;
    protected final List<T> itemsCopy;

    protected int visibleThreshold = 2;
    protected int lastVisibleItem, totalItemCount;
    protected boolean loading = true;
    protected OnLoadMoreListener onLoadMoreListener;
    protected Context mContext;
    protected boolean isFiltering = false;

    @SuppressLint("HandlerLeak")
    protected final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isFiltering = true;
                        notifyDataSetChanged();
                    }
                });
            }
        }
    };

    public PullUpLoadMoreRCVAdapter(Context context, List<T> myDataSet, RecyclerView recyclerView) {
        itemsCopy = new ArrayList<>();
        mDataSet = myDataSet;
        itemsCopy.addAll(mDataSet);
        mContext = context;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        // End has been reached
                        // Do something
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    public void updateData(List<T> dataSet) {
        mDataSet = dataSet;
        notifyDataSetChanged();
    }

    public void updateData(List<T> dataSet, int position) {
        mDataSet = dataSet;
        notifyItemChanged(position);
    }

    @Override
    public int getItemViewType(int position) {
        return mDataSet.get(position) != null ? VIEW_ITEM : VIEW_PROGRESS;
    }

    @Override
    public abstract RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(RecyclerView.ViewHolder holder, int position);

    public void setLoaded() {
        loading = false;
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }


    public void filter(final String text, List<T> mDataSetCopy) {
        new Thread(() -> {
            if (TextUtils.isEmpty(text)) {
                ChattingFragment.instance.Reload();
            } else {
                itemsCopy.clear();

                for (T message : mDataSetCopy) {
                    if (message instanceof ChattingDto) {
                        if (((ChattingDto) message).getMessage().toLowerCase().contains(text.toLowerCase())) {
                            itemsCopy.add(message);
                        }
                    }
                }

                mDataSet.clear();
                mDataSet.addAll(itemsCopy);
                mHandler.obtainMessage(1).sendToTarget();
            }


        }).start();
    }

    public void filterRecentFavorite(final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (T c : mDataSet) {
                    if (!itemsCopy.contains(c)) {
                        itemsCopy.add(c);
                    }
                }

                mDataSet.clear();

                if (text.isEmpty()) {
                    mDataSet.addAll(itemsCopy);
                } else {
                    mDataSet.clear();

                    for (Object item : itemsCopy) {
                        if (item instanceof ChattingDto) {
                            ChattingDto dto = (ChattingDto) item;
                            String name = "";

                            /** SET TITLE FOR ROOM */
                            if (TextUtils.isEmpty(dto.getRoomTitle())) {
                                if (dto.getListTreeUser() != null && dto.getListTreeUser().size() > 0) {
                                    for (TreeUserDTOTemp treeUserDTOTemp : dto.getListTreeUser()) {
                                        name += treeUserDTOTemp.getName() + ",";
                                    }
                                    if (name.length() != 0) {
                                        name = name.substring(0, name.length() - 1);
                                    }
                                }
                            } else {
                                name = dto.getRoomTitle();
                            }
                            String lastMsg = dto.getLastedMsg();
                            if (lastMsg == null) lastMsg = "";

                            if (name != null && name.trim().length() > 0) {
                                if (name.toLowerCase().contains(text.toLowerCase())/* || lastMsg.toLowerCase().contains(text.toLowerCase())*/) {
                                    mDataSet.add((T) dto);
                                }
                            }
                        }
                    }
                }

                // Send handler to update UI
                mHandler.obtainMessage(1).sendToTarget();
            }
        }).start();
    }

    public List<T> getData() {
        return mDataSet;
    }
}