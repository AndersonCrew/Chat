package com.dazone.crewchatoff.ViewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.MsgDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.fragment.ChattingFragment;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.fragment.CurrentChatListFragment;
import com.dazone.crewchatoff.fragment.RecentFavoriteFragment;
import com.dazone.crewchatoff.interfaces.IF_RestoreUser;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Utils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maidinh on 24/1/2017.
 */

public class ChattingViewHolder4 extends BaseChattingHolder {
    private TextView tv_4;
    private ImageView iv_cancel;
    private LinearLayout llDate;
    private TextView tvDate;

    public ChattingViewHolder4(View v) {
        super(v);
    }

    @Override
    protected void setup(View v) {
        tv_4 = v.findViewById(R.id.tv_4);
        iv_cancel = v.findViewById(R.id.iv_cancel);
        llDate = v.findViewById(R.id.llDate);
        tvDate = v.findViewById(R.id.time);
    }

    @Override
    public void bindData(final ChattingDto dto) {
        llDate.setVisibility(dto.isHeader()? View.VISIBLE : View.GONE);
        tvDate.setText(Utils.getStrDate(dto));

        String listUser = "";
        String s = dto.getMessage().trim();
        if (s.startsWith("{") && dto.getType() == 4) {
            List<TreeUserDTOTemp> allUser = null;
            if (CompanyFragment.instance != null) allUser = CompanyFragment.instance.getUser();
            if (allUser == null) allUser = new ArrayList<>();

            final MsgDto msgDto = new Gson().fromJson(s, MsgDto.class);

            final List<Integer> lst = msgDto.getData().getUserNos();
            for (int i = 0; i < lst.size(); i++) {
                int a = lst.get(i);
                if (i == lst.size() - 1) {
                    listUser += Constant.getUserName(allUser, a);
                } else {
                    listUser += Constant.getUserName(allUser, a) + ", ";
                }
            }
            String msg = "" + Constant.getUserName(allUser, msgDto.getData().getSubjectUserNo()) + " "
                    + CrewChatApplication.getInstance().getResources().getString(R.string.invited) + " "
                    + listUser + " "
                    + CrewChatApplication.getInstance().getResources().getString(R.string.gotogroup);
            tv_4.setText(msg);
            iv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HttpRequest.getInstance().UserRestore(lst, msgDto.getData().getRoomNo(), new IF_RestoreUser() {
                        @Override
                        public void onSuccess() {
                            iv_cancel.setVisibility(View.GONE);
                            // update list
                            if (ChattingFragment.instance != null) {
                                ChattingFragment.isShowIcon = false;
                                ChattingFragment.instance.Reload();
                            }
                            if (ChattingActivity.instance != null) {
                                for (int i = 0; i < lst.size(); i++) {
                                    ChattingActivity.instance.removeUserList(lst.get(i));
                                }
                                ChattingActivity.instance.updateSTT();
                            }
                            if (CurrentChatListFragment.fragment != null) {
                                CurrentChatListFragment.fragment.updateWhenRemoveUser(msgDto.getData().getRoomNo(), lst);
                            }
                            if (RecentFavoriteFragment.instance != null) {
                                RecentFavoriteFragment.instance.updateWhenRemoveUser(msgDto.getData().getRoomNo(), lst);
                            }

                        }
                    });
                }
            });
        } else {
            tv_4.setText(s);
        }
        if (ChattingFragment.instance != null) {
            if (ChattingFragment.isShowIcon && ChattingFragment.msgEnd == dto.getMessageNo()) {
                iv_cancel.setVisibility(View.VISIBLE);
            } else {
                iv_cancel.setVisibility(View.GONE);
            }
        } else {
            iv_cancel.setVisibility(View.GONE);
        }
//        if (flag) {
//            iv_cancel.setVisibility(View.VISIBLE);
//        } else {
//            iv_cancel.setVisibility(View.GONE);
//        }


    }
}