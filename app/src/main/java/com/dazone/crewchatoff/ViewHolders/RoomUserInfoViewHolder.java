package com.dazone.crewchatoff.ViewHolders;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.activity.ProfileUserActivity;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.Utils;

public class RoomUserInfoViewHolder extends ItemViewHolder<TreeUserDTO> {

    public RoomUserInfoViewHolder(View itemView) {
        super(itemView);
    }

    private ImageView avatar;
    private ImageView folderIcon;
    private ImageView ivUserStatus;
    private TextView name, position;
    private RelativeLayout relAvatar;
    private String roomTitle = "";
    private long roomNo = -1;
    private boolean isTwoUser = false;

    private TextView tvWorkPhone, tvPersonalPhone;
    int myId = Utils.getCurrentId();

    private Context mContext;

    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    protected void setup(View view) {
        avatar = view.findViewById(R.id.avatar);
        folderIcon = view.findViewById(R.id.ic_folder);
        ivUserStatus = view.findViewById(R.id.status_imv);
        relAvatar = view.findViewById(R.id.relAvatar);
        name = view.findViewById(R.id.name);
        position = view.findViewById(R.id.position);
        tvWorkPhone = view.findViewById(R.id.tv_work_phone);
        tvPersonalPhone = view.findViewById(R.id.tv_personal_phone);
    }

    @Override
    public void bindData(final TreeUserDTO treeUserDTO) {
        relAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BaseActivity.Instance, ProfileUserActivity.class);
                intent.putExtra(Constant.KEY_INTENT_USER_NO, treeUserDTO.getId());
                BaseActivity.Instance.startActivity(intent);
            }
        });

        String nameString = treeUserDTO.getName();
        String namePosition = treeUserDTO.getPosition();
        String nameDuty = treeUserDTO.getDutyName();

        if (treeUserDTO.getType() == 2) {
            int status = treeUserDTO.getStatus();
            //Utils.printLogs("User name ="+treeUserDTO.getName()+" status ="+status);
            if (treeUserDTO.getId() == myId) {
                ivUserStatus.setImageResource(R.drawable.home_status_me);
            } else if (status == Statics.USER_LOGIN) {
                ivUserStatus.setImageResource(R.drawable.home_big_status_01);
            } else if (status == Statics.USER_AWAY) {
                ivUserStatus.setImageResource(R.drawable.home_big_status_02);
            } else { // Logout state
                ivUserStatus.setImageResource(R.drawable.home_big_status_03);
            }

            String url = new Prefs().getServerSite() + treeUserDTO.getAvatarUrl();

            if (mContext != null) {
                ImageUtils.showCycleImageFromLinkScale(mContext, url, avatar, R.dimen.button_height);
            } else {
                ImageUtils.showCycleImageFromLink(url, avatar, R.dimen.button_height);
            }

            position.setVisibility(View.VISIBLE);
            folderIcon.setVisibility(View.GONE);
            relAvatar.setVisibility(View.VISIBLE);

        } else {
            position.setVisibility(View.GONE);
            relAvatar.setVisibility(View.GONE);
            folderIcon.setVisibility(View.VISIBLE);
        }

        name.setText(nameString);
       /* position.setText(namePosition);*/
        setDutyOrPosition(position,nameDuty,namePosition);

        final String companyNumber = treeUserDTO.getCompanyNumber().trim();
        final String phoneNumber = treeUserDTO.getPhoneNumber().trim();

        if (companyNumber.length() != 0) {
            tvWorkPhone.setVisibility(View.VISIBLE);
            tvWorkPhone.setText(companyNumber);
            tvWorkPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.CallPhone(CrewChatApplication.getInstance(), companyNumber);
                }
            });
        } else {
            tvWorkPhone.setVisibility(View.INVISIBLE);
        }

        if (phoneNumber.length() != 0) {
            tvPersonalPhone.setVisibility(View.VISIBLE);
            tvPersonalPhone.setText(phoneNumber);
            tvPersonalPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.CallPhone(CrewChatApplication.getInstance(), phoneNumber);
                }
            });
        } else {
            tvPersonalPhone.setVisibility(View.INVISIBLE);
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
    private void doColorSpanForSecondString(String firstString, String lastString, TextView txtSpan) {
        String changeString = (lastString != null ? lastString : "");
        String totalString;

        if (TextUtils.isEmpty(firstString) || TextUtils.isEmpty(lastString)) {
            totalString = firstString + changeString;
            Spannable spanText = new SpannableString(totalString);
            spanText.setSpan(new ForegroundColorSpan(CrewChatApplication.getInstance().getResources()
                    .getColor(R.color.gray)), (firstString + 3)
                    .length(), totalString.length(), 0);
            txtSpan.setText(spanText);
        } else {
            totalString = firstString + " / " + changeString;
            Spannable spanText = new SpannableString(totalString);
            spanText.setSpan(new ForegroundColorSpan(CrewChatApplication.getInstance().getResources()
                    .getColor(R.color.gray)), (firstString + " / ")
                    .length(), totalString.length(), 0);
            txtSpan.setText(spanText);
        }
    }
}