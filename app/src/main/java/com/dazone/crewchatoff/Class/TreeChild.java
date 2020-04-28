package com.dazone.crewchatoff.Class;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Utils;

public class TreeChild extends TreeView {
    private ImageView avatar_imv, status_imv;
    private TextView position, status_tv;
    private LinearLayout linearLayout;

    public TreeChild(Context context, TreeUserDTO dto, LinearLayout linearLayout) {
        super(context, dto);
        this.linearLayout = linearLayout;
        setupView();
    }

    @Override
    public void setupView() {
        currentView = inflater.inflate(R.layout.tree_row, null);
        avatar_imv = currentView.findViewById(R.id.avatar_imv);
        status_imv = currentView.findViewById(R.id.status_imv);
        title = currentView.findViewById(R.id.name);
        position = currentView.findViewById(R.id.position);
        status_tv = currentView.findViewById(R.id.status_tv);
        checkBox = currentView.findViewById(R.id.row_check);
        main = currentView.findViewById(R.id.mainParent);
        lnl_child = linearLayout;

        binData();
    }

    private void binData() {
        if (dto == null) {
            return;
        }

        ImageUtils.showRoundImage(dto, avatar_imv);
        title.setText(dto.getItemName());
        position.setText(dto.getPosition());

        if (TextUtils.isEmpty(dto.getStatusString())) {
            status_tv.setVisibility(View.GONE);
        } else {
            status_tv.setText(dto.getStatusString());
        }

        setupStatusImage();
        handleItemClick(false);

        avatar_imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        int myId = Utils.getCurrentId();

        if (dto.getId() != myId) {
            checkBox.setEnabled(true);
        } else {
            checkBox.setEnabled(false);
        }
    }

    private void setupStatusImage() {
        switch (dto.getStatus()) {
            case Statics.USER_STATUS_AWAY:
                status_imv.setImageResource(R.drawable.home_status_02);
                break;
            case Statics.USER_STATUS_RESTING:
                status_imv.setImageResource(R.drawable.home_status_03);
                break;
            case Statics.USER_STATUS_WORKING_OUTSIDE:
                status_imv.setImageResource(R.drawable.home_status_04);
                break;
            case Statics.USER_STATUS_IN_A_CAL:
                status_imv.setImageResource(R.drawable.home_status_05);
                break;
            case Statics.USER_STATUS_METTING:
                status_imv.setImageResource(R.drawable.home_status_06);
                break;
            case Statics.USER_STATUS_WORKING:
            default:
                status_imv.setImageResource(R.drawable.home_status_01);
                break;
        }
    }
}