package com.dazone.crewchatoff.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.interfaces.ICreateOneUserChatRom;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.Utils;

import java.util.ArrayList;

/**
 * Created by Dat on 4/20/2016.
 */

public class CompanySearchAdapter extends RecyclerView.Adapter<CompanySearchAdapter.CompanySearchViewHolder> implements View.OnClickListener {

    private ArrayList<TreeUserDTOTemp> listData;
    private Context context;
    private int myId;
    private Prefs prefs;

    public CompanySearchAdapter(Context context, ArrayList<TreeUserDTOTemp> listData) {
        this.context = context;
        this.listData = listData;

        prefs = new Prefs();
        myId = Utils.getCurrentId();
    }

    public void updateListData(ArrayList<TreeUserDTOTemp> listData) {
        this.listData = listData;
        notifyDataSetChanged();
    }

    @Override
    public CompanySearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_company_search, parent, false);

        return new CompanySearchViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CompanySearchViewHolder holder, int position) {
        TreeUserDTOTemp item = listData.get(position);
        String url = prefs.getServerSite() + item.getAvatarUrl();
        ImageUtils.showCycleImageFromLink(url, holder.ivAvatar, R.dimen.button_height);

        holder.tvName.setText(item.getName());
        holder.tvPosition.setText(item.getPosition());

        holder.layout.setTag(item);
        holder.layout.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_main:
                TreeUserDTOTemp treeUserDTOTemp = (TreeUserDTOTemp) v.getTag();
                if (myId != treeUserDTOTemp.getUserNo()) {
                    HttpRequest.getInstance().CreateOneUserChatRoom(treeUserDTOTemp.getUserNo(), new ICreateOneUserChatRom() {
                        @Override
                        public void onICreateOneUserChatRomSuccess(ChattingDto chattingDto) {
                            Intent intent = new Intent(BaseActivity.Instance, ChattingActivity.class);
                            intent.putExtra(Statics.CHATTING_DTO, chattingDto);
                            intent.putExtra(Constant.KEY_INTENT_ROOM_NO, chattingDto.getRoomNo());
                            BaseActivity.Instance.startActivity(intent);
                        }

                        @Override
                        public void onICreateOneUserChatRomFail(ErrorDto errorDto) {
                            Utils.showMessageShort("Fail");
                        }
                    });

                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.can_not_chat), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public class CompanySearchViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout layout;
        public ImageView ivAvatar;
        public ImageView ivStatus;
        public TextView tvName;
        public TextView tvPosition;

        public CompanySearchViewHolder(View view) {
            super(view);
            layout = view.findViewById(R.id.layout_main);
            ivAvatar = view.findViewById(R.id.iv_avatar);
            ivStatus = view.findViewById(R.id.iv_status);
            tvName = view.findViewById(R.id.tv_username);
            tvPosition = view.findViewById(R.id.tv_position);
        }
    }
}
