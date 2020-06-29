package com.dazone.crewchatoff.ViewHolders;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Utils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maidinh on 24/1/2017.
 */

public class ChattingViewHolder5 extends BaseChattingHolder {
    private TextView tv_5;
    private LinearLayout llDate;
    private TextView tvDate;


    public ChattingViewHolder5(View v) {
        super(v);
    }

    @Override
    protected void setup(View v) {
        tv_5 = v.findViewById(R.id.tv_5);
        llDate = v.findViewById(R.id.llDate);
        tvDate = v.findViewById(R.id.time);
    }

    @Override
    public void bindData(final ChattingDto dto) {
        String s = dto.getMessage().trim();
        llDate.setVisibility(dto.isHeader()? View.VISIBLE : View.GONE);
        tvDate.setText(Utils.getStrDate(dto));

        if (s.startsWith("{") && dto.getType() == 5) {
            Msg msg = new Gson().fromJson(s, Msg.class);
            MsgDetails msgDetails = msg.getData();
            int userNo = msgDetails.getUserNo();

            List<TreeUserDTOTemp> allUser = null;
            if (CompanyFragment.instance != null) allUser = CompanyFragment.instance.getUser();
            if (allUser == null) allUser = new ArrayList<>();


            String str = "";
            str = "" + Constant.getUserName(allUser, userNo) + " "
                    + CrewChatApplication.getInstance().getResources().getString(R.string.has_left);
            tv_5.setText(str);
        } else {
            tv_5.setText(s);
        }
    }

    class Msg {
        int Code;
        MsgDetails Data;

        public int getCode() {
            return Code;
        }

        public void setCode(int code) {
            Code = code;
        }

        public MsgDetails getData() {
            return Data;
        }

        public void setData(MsgDetails data) {
            Data = data;
        }
    }

    class MsgDetails {
        int RoomNo;
        int UserNo;

        public int getRoomNo() {
            return RoomNo;
        }

        public void setRoomNo(int roomNo) {
            RoomNo = roomNo;
        }

        public int getUserNo() {
            return UserNo;
        }

        public void setUserNo(int userNo) {
            UserNo = userNo;
        }
    }
}