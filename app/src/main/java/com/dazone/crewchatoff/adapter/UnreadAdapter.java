package com.dazone.crewchatoff.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.TimeUtils;

import java.util.List;

/**
 * Created by maidinh on 31-Aug-17.
 */

public class UnreadAdapter extends RecyclerView.Adapter<UnreadAdapter.MyViewHolder> {
    private Context context;
    private List<TreeUserDTO> userDTOList;
    private int myId;

    public void update(List<TreeUserDTO> userDTOList) {
        this.userDTOList = userDTOList;
        this.notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName, tvPosition, tvTime;
        public ImageView ivIcon, status_imv;

        public MyViewHolder(View view) {
            super(view);
            ivIcon = (ImageView) view.findViewById(R.id.ivIcon);
            status_imv = (ImageView) view.findViewById(R.id.status_imv);

            tvName = (TextView) view.findViewById(R.id.tvName);
            tvPosition = (TextView) view.findViewById(R.id.tvPosition);
            tvTime = (TextView) view.findViewById(R.id.tvTime);
        }

        public void handler(TreeUserDTO dto) {
            String url = new Prefs().getServerSite() + dto.getAvatarUrl();
            ImageUtils.showCycleImageFromLinkScale(context, url, ivIcon, R.dimen.button_height);

            String nameString = dto.getName();
            tvName.setText(nameString);

           /* String namePosition = dto.getPosition();
            tvPosition.setText(namePosition);*/
            setDutyOrPosition(tvPosition,dto.getDutyName(),dto.getPosition());

            int status = dto.getStatus();
            //Utils.printLogs("User name ="+treeUserDTO.getName()+" status ="+status);
            if (dto.getId() == myId) {
                status_imv.setImageResource(R.drawable.home_status_me);
            } else if (status == Statics.USER_LOGIN) {
                status_imv.setImageResource(R.drawable.home_big_status_01);
            } else if (status == Statics.USER_AWAY) {
                status_imv.setImageResource(R.drawable.home_big_status_02);
            } else { // Logout state
                status_imv.setImageResource(R.drawable.home_big_status_03);
            }

            if(dto.IsRead)tvTime.setText(TimeUtils.displayTimeWithoutOffset(dto.ModDate,Statics.DATE_FORMAT_YYYY_MM_DD_AM_PM_HH_MM));
            else tvTime.setText(context.getResources().getString(R.string.undefined));

        }
    }

    private void setDutyOrPosition(TextView tvPosition, String duty, String position) {
        if (isGetValueEnterAuto() && !duty.equals("")) {
            tvPosition.setText(duty);
        } else {
            tvPosition.setText(position);
        }
    }

    private boolean isGetValueEnterAuto() {
        boolean isEnable = false;
        isEnable = CrewChatApplication.getInstance().getPrefs().getBooleanValue(Statics.IS_ENABLE_ENTER_VIEW_DUTY_KEY, isEnable);
        return isEnable;
    }
    public UnreadAdapter(Context context, List<TreeUserDTO> userDTOList, int myId) {
        this.context = context;
        this.userDTOList = userDTOList;
        this.myId = myId;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_unread_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        TreeUserDTO movie = userDTOList.get(position);
        holder.handler(movie);
    }

    @Override
    public int getItemCount() {
        return userDTOList.size();
    }
}