package com.dazone.crewchatoff.ViewHolders;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Views.RoundedImageView;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.database.UserDBHelper;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.DrawImageItem;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.fragment.TabCurrentChatFragment;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by maidinh on 9/2/2017.
 */

public class TransferMsgViewHolder extends ItemViewHolder<ChattingDto> implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
    private TabCurrentChatFragment.OnContextMenuSelect mOnContextMenuSelect;
    String TAG = "TransferMsgViewHolder";

    public TransferMsgViewHolder(View itemView, TabCurrentChatFragment.OnContextMenuSelect callback) {
        super(itemView);
        mOnContextMenuSelect = callback;
    }


    public TextView tvUserName, tvDate, tvContent, tvTotalUser;
    public RoundedImageView status_imv;
    private ImageView imgBadge;
    private ImageView imgAvatar, ivStatus;
    private RelativeLayout layoutAvatar;
    private ImageView ivLastedAttach;
    private View view;
    private CheckBox cb;
    private String roomTitle = "";
    private long roomNo = -1;
    private boolean isTwoUser = false;

    /**
     * Group Avatar
     */

    private RelativeLayout layoutGroupAvatar;
    private ImageView imgGroupAvatar1;
    private ImageView imgGroupAvatar2;
    private ImageView imgGroupAvatar3;
    private ImageView imgGroupAvatar4;
    private TextView tvGroupAvatar;
    private ImageView ivNotification;
    private ChattingDto tempDto;

    private int myId;

    @Override
    protected void setup(View v) {
        view = v;
        cb = (CheckBox) v.findViewById(R.id.cb);
        tvUserName = (TextView) v.findViewById(R.id.user_name_tv);
        tvDate = (TextView) v.findViewById(R.id.date_tv);
        tvContent = (TextView) v.findViewById(R.id.content_tv);
        imgAvatar = (ImageView) v.findViewById(R.id.avatar_imv);
        ivStatus = (ImageView) v.findViewById(R.id.status_imv);
        layoutAvatar = (RelativeLayout) v.findViewById(R.id.layoutAvatar);
        imgBadge = (ImageView) v.findViewById(R.id.image_badge);
        ivLastedAttach = (ImageView) v.findViewById(R.id.iv_lasted_attach);
        tvTotalUser = (TextView) v.findViewById(R.id.tv_user_total);

        layoutGroupAvatar = (RelativeLayout) v.findViewById(R.id.avatar_group);
        imgGroupAvatar1 = (ImageView) v.findViewById(R.id.avatar_group_1);
        imgGroupAvatar2 = (ImageView) v.findViewById(R.id.avatar_group_2);
        imgGroupAvatar3 = (ImageView) v.findViewById(R.id.avatar_group_3);
        imgGroupAvatar4 = (ImageView) v.findViewById(R.id.avatar_group_4);
        tvGroupAvatar = (TextView) v.findViewById(R.id.avatar_group_number);
        ivNotification = (ImageView) v.findViewById(R.id.iv_notification);


    }

    @Override
    public void bindData(final ChattingDto dto) {
        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        layoutGroupAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        myId = Utils.getCurrentId();
        tempDto = dto;

        String name = "";
        // Set total user in current room, if user > 2 display this, else hide it
//        int totalUser = dto.getUserNos().size();
        int totalUser = 0;
        ArrayList<TreeUserDTOTemp> listUsers = CrewChatApplication.listUsers;
        TreeUserDTOTemp treeUserDTOTemp1;
        List<TreeUserDTOTemp> list1 = new ArrayList<>();
        if (dto.getListTreeUser() != null && dto.getListTreeUser().size() < dto.getUserNos().size()) {
            totalUser = dto.getListTreeUser().size() + 1;

        } else {
            ArrayList<Integer> users = dto.getUserNos();
            ArrayList<Integer> usersClone = new ArrayList<>(users);
            Utils.removeArrayDuplicate(usersClone);

            for (int i = 0; i < usersClone.size(); i++) {
                if (listUsers != null) {
                    treeUserDTOTemp1 = Utils.GetUserFromDatabase(listUsers, usersClone.get(i));

                    if (treeUserDTOTemp1 != null) {
                        list1.add(treeUserDTOTemp1);
                    }
                }
            }

            dto.setListTreeUser(list1);
            totalUser = list1.size();
        }
        if (totalUser > 2) {
            tvTotalUser.setVisibility(View.VISIBLE);
            tvTotalUser.setText(String.valueOf(totalUser));

        } else {
            isTwoUser = true;
            tvTotalUser.setVisibility(View.GONE);
        }


        if (dto.getUnReadCount() != 0) {
            imgBadge.setVisibility(View.VISIBLE);
            ImageUtils.showBadgeImage(dto.getUnReadCount(), imgBadge);
        } else {
            imgBadge.setVisibility(View.GONE);
        }

        if (dto.isNotification()) {
            ivNotification.setVisibility(View.GONE);
        } else {
            ivNotification.setVisibility(View.VISIBLE);
        }

        /** SET TITLE FOR ROOM */
        if (TextUtils.isEmpty(dto.getRoomTitle())) {
            if (dto.getListTreeUser() != null && dto.getListTreeUser().size() > 0) {
                for (TreeUserDTOTemp treeUserDTOTemp : dto.getListTreeUser()) {
                    name += treeUserDTOTemp.getName() + ",";
                }
                if (name.length() != 0) {
                    name = name.substring(0, name.length() - 1);
                }
            }
        } else {
            name = dto.getRoomTitle();
        }

        // Global value
        roomTitle = name;
        roomNo = dto.getRoomNo();

        if (dto.getListTreeUser() == null || dto.getListTreeUser().size() == 0) {
            tvUserName.setTextColor(ContextCompat.getColor(CrewChatApplication.getInstance(), R.color.gray));
            tvUserName.setText(CrewChatApplication.getInstance().getResources().getString(R.string.unknown));
        } else {
            tvUserName.setTextColor(ContextCompat.getColor(CrewChatApplication.getInstance(), R.color.black));
            tvUserName.setText(name);
        }


        /** SET LAST MESSAGE */
        String strLastMsg = "";
        Resources res = CrewChatApplication.getInstance().getResources();
        switch (dto.getLastedMsgType()) {
            case Statics.MESSAGE_TYPE_NORMAL:
                ivLastedAttach.setVisibility(View.GONE);
                strLastMsg = dto.getLastedMsg();
                break;

            case Statics.MESSAGE_TYPE_SYSTEM:
                strLastMsg = dto.getLastedMsg();
                ivLastedAttach.setVisibility(View.GONE);
                break;

            case Statics.MESSAGE_TYPE_ATTACH:

                // Attach type switch
                switch (dto.getLastedMsgAttachType()) {
                    case Statics.ATTACH_NONE:
                        strLastMsg = dto.getLastedMsg();
                        ivLastedAttach.setVisibility(View.GONE);
                        break;

                    case Statics.ATTACH_IMAGE:
                        strLastMsg = res.getString(R.string.attach_image);
                        ivLastedAttach.setImageResource(R.drawable.home_attach_ic_images);
                        break;

                    case Statics.ATTACH_FILE:
                        strLastMsg = res.getString(R.string.attach_file);
                        ivLastedAttach.setImageResource(R.drawable.home_attach_ic_file);
                        break;
                }
                ivLastedAttach.setVisibility(View.VISIBLE);
                break;
        }
        tvContent.setText(strLastMsg);

        /** Test */
        String tempTimeString = dto.getLastedMsgDate();
        if (!TextUtils.isEmpty(tempTimeString)) {
            long time = TimeUtils.getTime(tempTimeString);

            if (Locale.getDefault().getLanguage().toUpperCase().equalsIgnoreCase("KO")) {
                tvDate.setText(TimeUtils.displayTimeWithoutOffset(CrewChatApplication.getInstance().getApplicationContext(), time, 1));
            } else {
                tvDate.setText(TimeUtils.displayTimeWithoutOffset(CrewChatApplication.getInstance().getApplicationContext(), time, 0));
            }
        }

        if (dto.getListTreeUser() != null && dto.getListTreeUser().size() > 0) {
            if (dto.getListTreeUser().size() < 2) {
                layoutGroupAvatar.setVisibility(View.GONE);
                layoutAvatar.setVisibility(View.VISIBLE);
//                ImageUtils.showRoundImage(dto.getListTreeUser().get(0), imgAvatar);
                DrawImageItem obj = dto.getListTreeUser().get(0);
                String linkIMG = obj.getImageLink();
                if (linkIMG != null && linkIMG.length() > 0) {
                    String rootUrl = new Prefs().getServerSite() + linkIMG;
                    ImageUtils.showCycleImageFromLink(rootUrl, imgAvatar, R.dimen.button_height);
                } else {
                    ImageUtils.showRoundImage(dto.getListTreeUser().get(0), imgAvatar);
                }
                int status = dto.getStatus();
                if (status == Statics.USER_LOGIN) {
                    ivStatus.setImageResource(R.drawable.home_big_status_01);
                } else if (status == Statics.USER_AWAY) {
                    ivStatus.setImageResource(R.drawable.home_big_status_02);
                } else { // Logout state
                    ivStatus.setImageResource(R.drawable.home_big_status_03);
                }
                if(dto.getRoomType()==1)
                {
                    ivStatus.setImageResource(R.drawable.home_status_me);
                }
            } else {
                layoutGroupAvatar.setVisibility(View.VISIBLE);
                layoutAvatar.setVisibility(View.GONE);
                String url1 = new Prefs().getServerSite() + UserDBHelper.getUser().avatar;
                String url2;
                String url3;
                String url4;


                switch (dto.getListTreeUser().size()) {
                    case 2:
                        imgGroupAvatar1.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                        imgGroupAvatar1.getLayoutParams().width = (int) CrewChatApplication.getInstance().getResources().getDimension(R.dimen.common_avatar_group);
                        imgGroupAvatar2.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                        imgGroupAvatar2.setVisibility(View.VISIBLE);
                        imgGroupAvatar3.setVisibility(View.GONE);
                        imgGroupAvatar4.setVisibility(View.GONE);
                        tvGroupAvatar.setVisibility(View.GONE);
                        url1 = new Prefs().getServerSite() + dto.getListTreeUser().get(0).getAvatarUrl();
                        url2 = new Prefs().getServerSite() + dto.getListTreeUser().get(1).getAvatarUrl();

//                        ImageLoader.getInstance().displayImage(url1, imgGroupAvatar1, Statics.avatarGroupTL);
//                        ImageLoader.getInstance().displayImage(url2, imgGroupAvatar2, Statics.avatarGroupTR);

                        ImageUtils.setImgFromUrl(url1, imgGroupAvatar1);
                        ImageUtils.setImgFromUrl(url2, imgGroupAvatar2);
                        break;
                    case 3:
                        imgGroupAvatar1.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                        imgGroupAvatar2.setVisibility(View.GONE);
                        imgGroupAvatar3.setVisibility(View.VISIBLE);
                        imgGroupAvatar4.setVisibility(View.VISIBLE);
                        tvGroupAvatar.setVisibility(View.GONE);
                        url1 = new Prefs().getServerSite() + dto.getListTreeUser().get(0).getAvatarUrl();
                        url3 = new Prefs().getServerSite() + dto.getListTreeUser().get(1).getAvatarUrl();
                        url4 = new Prefs().getServerSite() + dto.getListTreeUser().get(2).getAvatarUrl();

//                        ImageLoader.getInstance().displayImage(url1, imgGroupAvatar1, Statics.avatarGroupTOP);
//                        ImageLoader.getInstance().displayImage(url3, imgGroupAvatar3, Statics.avatarGroupBL);
//                        ImageLoader.getInstance().displayImage(url4, imgGroupAvatar4, Statics.avatarGroupBR);
                        ImageUtils.setImgFromUrl(url1, imgGroupAvatar1);
                        ImageUtils.setImgFromUrl(url3, imgGroupAvatar3);
                        ImageUtils.setImgFromUrl(url4, imgGroupAvatar4);

                        break;
                    case 4:
                        imgGroupAvatar1.getLayoutParams().height = (int) CrewChatApplication.getInstance().getResources().getDimension(R.dimen.common_avatar_group);
                        imgGroupAvatar1.getLayoutParams().width = (int) CrewChatApplication.getInstance().getResources().getDimension(R.dimen.common_avatar_group);
                        imgGroupAvatar2.getLayoutParams().height = (int) CrewChatApplication.getInstance().getResources().getDimension(R.dimen.common_avatar_group);
                        imgGroupAvatar2.setVisibility(View.VISIBLE);
                        imgGroupAvatar3.setVisibility(View.VISIBLE);
                        imgGroupAvatar4.setVisibility(View.VISIBLE);
                        tvGroupAvatar.setVisibility(View.GONE);
                        url1 = new Prefs().getServerSite() + dto.getListTreeUser().get(0).getAvatarUrl();
                        url2 = new Prefs().getServerSite() + dto.getListTreeUser().get(1).getAvatarUrl();
                        url3 = new Prefs().getServerSite() + dto.getListTreeUser().get(2).getAvatarUrl();
                        url4 = new Prefs().getServerSite() + dto.getListTreeUser().get(3).getAvatarUrl();

//                        ImageLoader.getInstance().displayImage(url1, imgGroupAvatar1, Statics.avatarGroupTL);
//                        ImageLoader.getInstance().displayImage(url2, imgGroupAvatar2, Statics.avatarGroupTR);
//                        ImageLoader.getInstance().displayImage(url3, imgGroupAvatar3, Statics.avatarGroupBL);
//                        ImageLoader.getInstance().displayImage(url4, imgGroupAvatar4, Statics.avatarGroupBR);
                        ImageUtils.setImgFromUrl(url1, imgGroupAvatar1);
                        ImageUtils.setImgFromUrl(url2, imgGroupAvatar2);
                        ImageUtils.setImgFromUrl(url3, imgGroupAvatar3);
                        ImageUtils.setImgFromUrl(url4, imgGroupAvatar4);
                        break;
                    default:
                        imgGroupAvatar1.getLayoutParams().height = (int) CrewChatApplication.getInstance().getResources().getDimension(R.dimen.common_avatar_group);
                        imgGroupAvatar1.getLayoutParams().width = (int) CrewChatApplication.getInstance().getResources().getDimension(R.dimen.common_avatar_group);
                        imgGroupAvatar2.getLayoutParams().height = (int) CrewChatApplication.getInstance().getResources().getDimension(R.dimen.common_avatar_group);
                        imgGroupAvatar2.setVisibility(View.VISIBLE);
                        imgGroupAvatar3.setVisibility(View.VISIBLE);
                        imgGroupAvatar4.setVisibility(View.VISIBLE);

                        tvGroupAvatar.setVisibility(View.VISIBLE);
                        String strNumber = dto.getListTreeUser().size() - 3 + "";
                        tvGroupAvatar.setText(strNumber);
                        url1 = new Prefs().getServerSite() + dto.getListTreeUser().get(0).getAvatarUrl();
                        url2 = new Prefs().getServerSite() + dto.getListTreeUser().get(1).getAvatarUrl();
                        url3 = new Prefs().getServerSite() + dto.getListTreeUser().get(2).getAvatarUrl();
                        url4 = "drawable://" + R.drawable.avatar_group_bg;
//                        ImageLoader.getInstance().displayImage(url1, imgGroupAvatar1, Statics.avatarGroupTL);
//                        ImageLoader.getInstance().displayImage(url2, imgGroupAvatar2, Statics.avatarGroupTR);
//                        ImageLoader.getInstance().displayImage(url3, imgGroupAvatar3, Statics.avatarGroupBL);
//                        ImageLoader.getInstance().displayImage(url4, imgGroupAvatar4, Statics.avatarGroupBR);
                        ImageUtils.setImgFromUrl(url1, imgGroupAvatar1);
                        ImageUtils.setImgFromUrl(url2, imgGroupAvatar2);
                        ImageUtils.setImgFromUrl(url3, imgGroupAvatar3);
                        ImageUtils.setImgFromUrl(url4, imgGroupAvatar4);
                        break;
                }


            }
        }

        if (dto.getListTreeUser() == null || dto.getListTreeUser().size() == 0) {
            layoutGroupAvatar.setVisibility(View.GONE);
            layoutAvatar.setVisibility(View.VISIBLE);
            String url = "drawable://" + R.drawable.avatar_l;
            ImageLoader.getInstance().displayImage(url, imgAvatar, Statics.options2);
        }
        view.setTag(dto.getRoomNo());
        cb.setChecked(dto.isCbChoose());
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean flag = cb.isChecked();
                dto.setCbChoose(flag);
                Log.d(TAG, "cbonClick:" + dto.getRoomNo());
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long roomNo = (long) v.getTag();
                boolean flag = dto.isCbChoose();
                if (flag) {
                    cb.setChecked(false);
                    dto.setCbChoose(false);
                } else {
                    cb.setChecked(true);
                    dto.setCbChoose(true);
                }
                Log.d(TAG, "roomNo:" + roomNo);
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                return true;
            }
        });

        tvTotalUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Bundle roomInfo = null;
        switch (item.getItemId()) {

        }
        return false;
    }
}
