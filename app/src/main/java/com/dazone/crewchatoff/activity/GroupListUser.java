package com.dazone.crewchatoff.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.dazone.crewchatoff.activity.base.BaseSingleActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.fragment.GroupListFragment;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.Utils;

import java.util.ArrayList;

public class GroupListUser extends BaseSingleActivity implements View.OnClickListener {
    private ChattingDto chattingDto;
    private GroupListFragment fragment = new GroupListFragment();
    private ArrayList<Integer> userNos = new ArrayList<>();

    private void initToolBar() {
        setTitle("Group List");
        HideBtnMore();
        HideStatus();

        /** MENU ITEM CALL */
        ivCall.setOnClickListener(this);
        ivCall.setVisibility(View.GONE);

        /** SETUP CALL VISIBLE */
        ArrayList<TreeUserDTOTemp> allUser = null;
        if (CompanyFragment.instance != null) allUser = CompanyFragment.instance.getUser();
        if (allUser == null) allUser = new ArrayList<>();
        ivCall.setVisibility(Utils.isCallVisible(userNos,allUser) ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void addFragment(Bundle bundle) {
        Bundle bundle1 = getIntent().getExtras();
        if (bundle1 != null) {
            try {
                userNos = bundle1.getIntegerArrayList(Constant.KEY_INTENT_USER_NO_ARRAY);
                chattingDto = (ChattingDto) bundle1.getSerializable(Statics.CHATTING_DTO_FOR_GROUP_LIST);
            } catch (Exception e) {
                chattingDto = null;
                e.printStackTrace();
            }
        }

        if (bundle == null) {
            Utils.addFragmentToActivity(getSupportFragmentManager(), fragment.instance(userNos), R.id.content_base_single_activity, false);
        }

        initToolBar();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.call_menu:
                showCallList();
                break;
        }
    }

    private void showCallList() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(GroupListUser.this);
        builderSingle.setTitle("Call");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(GroupListUser.this, R.layout.row_chatting_call);
        ArrayList<TreeUserDTOTemp> allUser = null;
        if (CompanyFragment.instance != null) allUser = CompanyFragment.instance.getUser();
        if (allUser == null) allUser = new ArrayList<>();
        Utils.addCallArray(userNos, arrayAdapter,allUser);

        builderSingle.setNegativeButton(
                "cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String phoneNumber = GetPhoneNumber(arrayAdapter.getItem(which));
                        Utils.CallPhone(GroupListUser.this, phoneNumber);
                    }
                });
        AlertDialog dialog = builderSingle.create();

        if (arrayAdapter.getCount() > 0) {
            dialog.show();
        }

        Button b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (b != null) {
            b.setTextColor(ContextCompat.getColor(mContext, R.color.light_black));
        }
    }

    private String GetPhoneNumber(String strPhone) {
        String result = strPhone.split("\\(")[1];
        result = result.split("\\)")[0];
        return result;
    }
}