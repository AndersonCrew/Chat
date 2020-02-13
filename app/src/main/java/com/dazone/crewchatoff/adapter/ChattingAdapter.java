package com.dazone.crewchatoff.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.ViewHolders.BaseChattingHolder;
import com.dazone.crewchatoff.ViewHolders.ChattingContactViewHolder;
import com.dazone.crewchatoff.ViewHolders.ChattingDateViewHolder;
import com.dazone.crewchatoff.ViewHolders.ChattingGroupViewHolder;
import com.dazone.crewchatoff.ViewHolders.ChattingGroupViewHolderNew;
import com.dazone.crewchatoff.ViewHolders.ChattingPersonFileViewHolder;
import com.dazone.crewchatoff.ViewHolders.ChattingPersonImageViewHolder;
import com.dazone.crewchatoff.ViewHolders.ChattingPersonVideoNotShowViewHolder;
import com.dazone.crewchatoff.ViewHolders.ChattingPersonViewHolder;
import com.dazone.crewchatoff.ViewHolders.ChattingSelfFileViewHolder;
import com.dazone.crewchatoff.ViewHolders.ChattingSelfImageViewHolder;
import com.dazone.crewchatoff.ViewHolders.ChattingSelfVideoViewHolder;
import com.dazone.crewchatoff.ViewHolders.ChattingSelfViewHolder;
import com.dazone.crewchatoff.ViewHolders.ChattingViewHolder4;
import com.dazone.crewchatoff.ViewHolders.ChattingViewHolder5;
import com.dazone.crewchatoff.ViewHolders.ChattingViewHolder6;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.ChattingDto;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChattingAdapter extends PullUpLoadMoreRCVAdapter<ChattingDto> {
    private Activity mActivity;
    String TAG = "ChattingAdapter";
    private Comparator<ChattingDto> mComparator = new Comparator<ChattingDto>() {
        @Override
        public int compare(ChattingDto t1, ChattingDto t2) {
            if (t1.isHasSent() && !t2.isHasSent()) {
                return -1;
            }

            if (!t1.isHasSent() && t2.isHasSent()) {
                return 1;
            }

            return 0;
        }
    };

    public ChattingAdapter(Context context, Activity activity, List<ChattingDto> mDataSet, RecyclerView view) {
        super(context, mDataSet, view);
        mActivity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh = null;
        View v;

        Collections.sort(mDataSet, mComparator);

        switch (viewType) {
            case Statics.CHATTING_VIEW_TYPE_DATE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_date, parent, false);
                vh = new ChattingDateViewHolder(v);
                break;
            case Statics.CHATTING_VIEW_TYPE_SELF:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_self, parent, false);
                ChattingSelfViewHolder tempVh = new ChattingSelfViewHolder(v);
                tempVh.setAdapter(this);
                vh = tempVh;
                break;
            case Statics.CHATTING_VIEW_TYPE_SELF_NOT_SHOW:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_self_not_show, parent, false);
                ChattingSelfViewHolder tempVh2 = new ChattingSelfViewHolder(v);
                tempVh2.setAdapter(this);
                vh = tempVh2;
                break;
            case Statics.CHATTING_VIEW_TYPE_PERSON:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_person, parent, false);
                vh = new ChattingPersonViewHolder(v);
                break;

            case Statics.CHATTING_VIEW_TYPE_PERSON_NOT_SHOW:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_person_not_show, parent, false);
                vh = new ChattingSelfViewHolder(v);
                break;

            case Statics.CHATTING_VIEW_TYPE_GROUP:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_group, parent, false);
                vh = new ChattingGroupViewHolder(v);
                break;

            case Statics.CHATTING_VIEW_TYPE_GROUP_NEW:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_group, parent, false);
                vh = new ChattingGroupViewHolderNew(v);
                break;
            case Statics.CHATTING_VIEW_TYPE_SELF_IMAGE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_self_image, parent, false);
                vh = new ChattingSelfImageViewHolder(mActivity, v);
                break;
            case Statics.CHATTING_VIEW_TYPE_SELF_FILE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_self_file, parent, false);
                vh = new ChattingSelfFileViewHolder(v);
                break;
            case Statics.CHATTING_VIEW_TYPE_PERSON_IMAGE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_person_image, parent, false);
                vh = new ChattingPersonImageViewHolder(mActivity, v);
                break;
            case Statics.CHATTING_VIEW_TYPE_PERSON_IMAGE_NOT_SHOW:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_person_image_not_show, parent, false);
                vh = new ChattingSelfImageViewHolder(mActivity, v);
                break;
            case Statics.CHATTING_VIEW_TYPE_PERSON_FILE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_person_file, parent, false);
                vh = new ChattingPersonFileViewHolder(v);
                break;
            case Statics.CHATTING_VIEW_TYPE_PERSON_FILE_NOT_SHOW:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_person_file_not_show, parent, false);
                vh = new ChattingSelfFileViewHolder(v);
                break;

            case Statics.CHATTING_VIEW_TYPE_SELECT_IMAGE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_self_image, parent, false);
                vh = new ChattingSelfImageViewHolder(mActivity, v);
                break;
            case Statics.CHATTING_VIEW_TYPE_SELECT_FILE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_self_file, parent, false);
                vh = new ChattingSelfFileViewHolder(v);
                break;

            case Statics.CHATTING_VIEW_TYPE_CONTACT:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_contact, parent, false);
                vh = new ChattingContactViewHolder(v);
                break;

            case Statics.CHATTING_VIEW_TYPE_EMPTY:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_empty, parent, false);
                vh = new ChattingDateViewHolder(v);
                break;

            case Statics.CHATTING_VIEW_TYPE_SELF_VIDEO:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_self_video, parent, false);
                vh = new ChattingSelfVideoViewHolder(mActivity, v);
                break;

            case Statics.CHATTING_VIEW_TYPE_SELECT_VIDEO:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_self_video, parent, false);
                vh = new ChattingSelfVideoViewHolder(mActivity, v);
                break;

            case Statics.CHATTING_VIEW_TYPE_PERSON_VIDEO_NOT_SHOW:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_person_video_not_show, parent, false);
                vh = new ChattingPersonVideoNotShowViewHolder(mActivity, v);
                break;
            case Statics.CHATTING_VIEW_TYPE_4:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_type_4_layout, parent, false);
                vh = new ChattingViewHolder4(v);
                break;
            case Statics.CHATTING_VIEW_TYPE_5:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_type_5_layout, parent, false);
                vh = new ChattingViewHolder5(v);
                break;
            case Statics.CHATTING_VIEW_TYPE_6:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_type_6_layout, parent, false);
                vh = new ChattingViewHolder6(v);
                break;
//            case Statics.CHATTING_VIEW_TYPE_AUDIO_ME:
//                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_me_layout, parent, false);
//                vh = new AudioVH(v, mActivity);
//                break;
//            case Statics.CHATTING_VIEW_TYPE_AUDIO_USER:
//                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_user_layout, parent, false);
//                vh = new AudioVH(v, mActivity);
//                break;
            default:
              /*  v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatting_date, parent, false);
                vh = new ChattingDateViewHolder(v);*/
                break;

        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) != Statics.CHATTING_VIEW_TYPE_EMPTY) {
//            Log.d(TAG,"position:"+position);
            final ChattingDto item = mDataSet.get(position);
            BaseChattingHolder viewHolder = (BaseChattingHolder) holder;
            viewHolder.bindData(item);
        }
    }

    @Override
    public int getItemCount() {
        if (mDataSet.size() == 0 && isFiltering) {
            return 1;
        }

        return mDataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (getItemCount() == 1 && mDataSet.size() == 0) {
            return Statics.CHATTING_VIEW_TYPE_EMPTY;
        }
        if (mDataSet.get(position).getType() == 4) {
            return Statics.CHATTING_VIEW_TYPE_4;
        }
        if (mDataSet.get(position).getType() == 5) {
            return Statics.CHATTING_VIEW_TYPE_5;
        }
        if (mDataSet.get(position).getType() == 6) {
            return Statics.CHATTING_VIEW_TYPE_6;
        }

//        Log.d(TAG,"pos:"+mDataSet.get(position).getName()+" - "+mDataSet.get(position).getmType()+" - "+mDataSet.get(position).getMessage());
//        Log.d(TAG,"data:"+new Gson().toJson(mDataSet.get(position)));


        return mDataSet.get(position).getmType();
    }
}