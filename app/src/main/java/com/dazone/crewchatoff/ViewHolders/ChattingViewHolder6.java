package com.dazone.crewchatoff.ViewHolders;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.MsgDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Utils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maidinh on 25/1/2017.
 */

public class ChattingViewHolder6 extends BaseChattingHolder {
    private TextView tv_6;
    private LinearLayout llDate;
    private TextView tvDate;

    public ChattingViewHolder6(View v) {
        super(v);
    }

    @Override
    protected void setup(View v) {
        tv_6 = v.findViewById(R.id.tv_6);
        llDate = v.findViewById(R.id.llDate);
        tvDate = v.findViewById(R.id.time);
    }

    @Override
    public void bindData(final ChattingDto dto) {

        llDate.setVisibility(dto.isHeader()? View.VISIBLE : View.GONE);
        tvDate.setText(Utils.getStrDate(dto));

        String listUser = "";
        String s = dto.getMessage().trim();
        if (s.startsWith("{") && dto.getType() == 6) {

            List<TreeUserDTOTemp> allUser = null;
            if (CompanyFragment.instance != null) allUser = CompanyFragment.instance.getUser();
            if (allUser == null) allUser = new ArrayList<>();

            MsgDto msgDto = new Gson().fromJson(s, MsgDto.class);

            List<Integer> lst = msgDto.getData().getUserNos();
            for (int i = 0; i < lst.size(); i++) {
                int a = lst.get(i);
                if (i == lst.size() - 1) {
                    listUser += Constant.getUserName(allUser, a);
                } else {
                    listUser += Constant.getUserName(allUser, a) + ", ";
                }
            }
            String msg = "" + Constant.getUserName(allUser, msgDto.getData().getSubjectUserNo()) + " "
                    + CrewChatApplication.getInstance().getResources().getString(R.string.cancel_invite) + " "
                    + listUser + " "
                    + CrewChatApplication.getInstance().getResources().getString(R.string.cancel_invite_2);
            tv_6.setText(msg);
        } else {
            tv_6.setText(s);
        }

    }
}