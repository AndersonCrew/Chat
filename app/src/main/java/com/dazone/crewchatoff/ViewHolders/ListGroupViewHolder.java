package com.dazone.crewchatoff.ViewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.utils.DialogUtils;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Prefs;

public class ListGroupViewHolder extends ItemViewHolder<TreeUserDTOTemp> {
    private RelativeLayout layoutMain;
    private TextView tvUserName, tvPosition;
    private ImageView ivAvatar;

    public ListGroupViewHolder(View v) {
        super(v);
    }

    @Override
    protected void setup(View v) {
        layoutMain = v.findViewById(R.id.layout_main);
        tvUserName = v.findViewById(R.id.tv_username);
        ivAvatar = v.findViewById(R.id.iv_avatar);
        tvPosition = v.findViewById(R.id.tv_position);
    }

    @Override
    public void bindData(TreeUserDTOTemp treeUserDTOTemp) {
        if (treeUserDTOTemp != null) {
            tvUserName.setText(treeUserDTOTemp.getName());
            tvPosition.setText(treeUserDTOTemp.getPosition());

            //ImageLoader.getInstance().displayImage(new Prefs().getServerSite() + treeUserDTOTemp.getAvatarUrl(), ivAvatar, Statics.options2);
            ImageUtils.showCycleImageFromLink(new Prefs().getServerSite() + treeUserDTOTemp.getAvatarUrl(), ivAvatar, R.dimen.button_height);

            layoutMain.setTag(treeUserDTOTemp);
            layoutMain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    TreeUserDTOTemp user = (TreeUserDTOTemp) v.getTag();
                    String strName = user.getName();
                    String strPhoneNumber = user.getCellPhone();
                    String strCompanyNumber = user.getCompanyPhone();
                    int userNo = user.getUserNo();
                    DialogUtils.showDialogUser(strName, strPhoneNumber, strCompanyNumber, userNo);
                    return false;
                }
            });
        }
    }
}