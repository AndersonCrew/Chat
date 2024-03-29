package com.dazone.crewchatoff.ViewHolders;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Views.RoundedImageView;
import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.activity.ProfileUserActivity;
import com.dazone.crewchatoff.activity.RoomUserInformationActivity;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.customs.RoundLayoutGroup;
import com.dazone.crewchatoff.database.ChatRoomDBHelper;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.DrawImageItem;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.fragment.CurrentChatListFragment;
import com.dazone.crewchatoff.fragment.RecentFavoriteFragment;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallBack;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ListCurrentViewHolder extends ItemViewHolder<ChattingDto> implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
    private CurrentChatListFragment.OnContextMenuSelect mOnContextMenuSelect;
    String TAG = "ListCurrentViewHolder";

    public ListCurrentViewHolder(View itemView, CurrentChatListFragment.OnContextMenuSelect callback) {
        super(itemView);
        mOnContextMenuSelect = callback;
    }

    private TextView tvUserName, tvDate, tvContent, tvTotalUser;
    public RoundedImageView status_imv;
    private ImageView imgBadge;
    private ImageView imgAvatar, status_imv_null;
    private RelativeLayout avatar_null;
    private ImageView ivLastedAttach;
    private ImageView ivFavorite;
    private ImageView ivNotification;
    private View view;
    private RelativeLayout layoutAvatar;
    private ImageView ivStatus;
    private String roomTitle = "";
    private long roomNo = -1;
    private ChattingDto tempDto;

    private RoundLayoutGroup layoutGroupAvatar;
    private ImageView imgGroupAvatar1;
    private ImageView imgGroupAvatar2;
    private ImageView imgGroupAvatar3;
    private ImageView imgGroupAvatar4;
    private TextView tvGroupAvatar;

    private final Resources res = CrewChatApplication.getInstance().getResources();
    private int myId;

    @Override
    protected void setup(final View v) {
        view = v;
        tvUserName = (TextView) v.findViewById(R.id.user_name_tv);
        tvDate = (TextView) v.findViewById(R.id.date_tv);
        tvContent = (TextView) v.findViewById(R.id.content_tv);
        status_imv_null = (ImageView) v.findViewById(R.id.status_imv_null);
        imgAvatar = (ImageView) v.findViewById(R.id.avatar_imv);
        ivStatus = (ImageView) v.findViewById(R.id.status_imv);
        layoutAvatar = (RelativeLayout) v.findViewById(R.id.layoutAvatar);

        imgBadge = (ImageView) v.findViewById(R.id.image_badge);
        ivLastedAttach = (ImageView) v.findViewById(R.id.iv_lasted_attach);
        tvTotalUser = (TextView) v.findViewById(R.id.tv_user_total);
        avatar_null = (RelativeLayout) v.findViewById(R.id.avatar_null);
        layoutGroupAvatar = (RoundLayoutGroup) v.findViewById(R.id.avatar_group);
        imgGroupAvatar1 = (ImageView) v.findViewById(R.id.avatar_group_1);
        imgGroupAvatar2 = (ImageView) v.findViewById(R.id.avatar_group_2);
        imgGroupAvatar3 = (ImageView) v.findViewById(R.id.avatar_group_3);
        imgGroupAvatar4 = (ImageView) v.findViewById(R.id.avatar_group_4);
        tvGroupAvatar = (TextView) v.findViewById(R.id.avatar_group_number);
        ivFavorite = (ImageView) v.findViewById(R.id.iv_favorite);
        ivNotification = (ImageView) v.findViewById(R.id.iv_notification);

        //gestureDetector = new GestureDetector(CrewChatApplication.getInstance(), new CustomGestureDetector(view));
        view.setOnCreateContextMenuListener(this);
    }

    @Override
    public void bindData(final ChattingDto dto) {
        myId = Utils.getCurrentId();
        tempDto = dto;

        String name = "";
        // Set total user in current room, if user > 2 display this, else hide it
        boolean isFilter = false;
        int totalUser;
        List<TreeUserDTOTemp> list1 = new ArrayList<>();
        TreeUserDTOTemp treeUserDTOTemp1;

        ArrayList<TreeUserDTOTemp> listUsers =null;
        if (CompanyFragment.instance != null) listUsers = CompanyFragment.instance.getUser();
        if (listUsers == null) listUsers = new ArrayList<>();


        if (dto.getListTreeUser() != null && dto.getListTreeUser().size() < dto.getUserNos().size()) {
            totalUser = dto.getListTreeUser().size() + 1;
            isFilter = true;
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
            tvTotalUser.setVisibility(View.GONE);
        }

        if (dto.isFavorite()) {
            ivFavorite.setVisibility(View.VISIBLE);
        } else {
            ivFavorite.setVisibility(View.GONE);
        }

        if (dto.isNotification()) {
            ivNotification.setVisibility(View.GONE);
        } else {
            ivNotification.setVisibility(View.VISIBLE);
        }

        if (dto.getWriterUserNo() == myId) {
            imgBadge.setVisibility(View.GONE);
        } else {
            if (dto.getUnReadCount() != 0) {
                imgBadge.setVisibility(View.VISIBLE);
                ImageUtils.showBadgeImage(dto.getUnReadCount(), imgBadge);
            } else {
                imgBadge.setVisibility(View.GONE);
            }
        }

        if (TextUtils.isEmpty(dto.getRoomTitle())) {
            if (dto.getListTreeUser() != null && dto.getListTreeUser().size() > 0) {
                for (TreeUserDTOTemp treeUserDTOTemp : dto.getListTreeUser()) {
                    if (treeUserDTOTemp.getUserNo() != myId || dto.getRoomType() == 1) {
                        if (TextUtils.isEmpty(name)) {
                            name += treeUserDTOTemp.getName();
                        } else {
                            name += "," + treeUserDTOTemp.getName();
                        }
                    }
                }
            }
        } else {
            name = dto.getRoomTitle();
        }

//        if (name.length() == 0 && dto.getRoomType() == 1) {
//            if (CrewChatApplication.currentName != null && CrewChatApplication.currentName.length() > 0) {
//
//            } else {
//                CrewChatApplication.currentName = Constant.getUserName(AllUserDBHelper.getUser(), Utils.getCurrentId());
//            }
//            String msg = "";
//            msg = "[Me]" + CrewChatApplication.currentName;
//            name = msg;
//        }


        // Global value
        roomTitle = name;
        roomNo = dto.getRoomNo();

        if (dto.getListTreeUser() == null || dto.getListTreeUser().size() == 0) {
            tvUserName.setTextColor(ContextCompat.getColor(CrewChatApplication.getInstance(), R.color.gray));
            tvUserName.setText(CrewChatApplication.getInstance().getResources().getString(R.string.unknown));
            status_imv_null.setImageResource(R.drawable.home_big_status_03);
        } else {
            tvUserName.setTextColor(ContextCompat.getColor(CrewChatApplication.getInstance(), R.color.black));
            tvUserName.setText(name);
        }

        String strLastMsg = "";
        Resources res = CrewChatApplication.getInstance().getResources();
        switch (dto.getLastedMsgType()) {
            case Statics.MESSAGE_TYPE_NORMAL:
                ivLastedAttach.setVisibility(View.GONE);
                strLastMsg += dto.getLastedMsg();
                break;

            case Statics.MESSAGE_TYPE_SYSTEM:
                strLastMsg = dto.getLastedMsg();
                ivLastedAttach.setVisibility(View.GONE);
                break;

            case Statics.MESSAGE_TYPE_ATTACH:
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
                default:
                    strLastMsg = dto.getLastedMsg()+"";
                    ivLastedAttach.setVisibility(View.GONE);
                    break;
        }

        if (strLastMsg != null && strLastMsg.contains("\n")) {
            String[] mess = strLastMsg.split("\\n");
            String ms = "";

            for (String ss : mess) {
                if (ss != null && ss.trim().length() > 0) {
                    ms = ss;
                    break;
                }
            }

            tvContent.setText(ms);
        } else {
            tvContent.setText(strLastMsg);
        }

        String tempTimeString = dto.getLastedMsgDate();

        if (!TextUtils.isEmpty(tempTimeString)) {
            if (Locale.getDefault().getLanguage().toUpperCase().equalsIgnoreCase("KO")) {
                tvDate.setText(TimeUtils.displayTimeWithoutOffset(CrewChatApplication.getInstance().getApplicationContext(), dto.getLastedMsgDate(), 1, TimeUtils.KEY_FROM_SERVER));
            } else {
                tvDate.setText(TimeUtils.displayTimeWithoutOffset(CrewChatApplication.getInstance().getApplicationContext(), dto.getLastedMsgDate(), 0, TimeUtils.KEY_FROM_SERVER));
            }
        }

        if (dto.getListTreeUser() != null && dto.getListTreeUser().size() > 0) {
            if (dto.getListTreeUser().size() < 2) {
                layoutGroupAvatar.setVisibility(View.GONE);
                layoutAvatar.setVisibility(View.VISIBLE);

//                ImageUtils.showRoundImage(dto.getListTreeUser().get(0), imgAvatar);
                DrawImageItem obj = dto.getListTreeUser().get(0);
                String linkIMG = obj.getImageLink();
//                Log.d(TAG, "linkIMG:" + linkIMG);

                if (linkIMG != null && linkIMG.length() > 0) {
                    String rootUrl = new Prefs().getServerSite() + linkIMG;


//                    String s= "http://122.41.175.67:8080/_Repository/_UserPhoto/70.png?date=636047265850000000";
//                    if(s.equals(rootUrl))
//                    {
//                        Log.d(TAG,"rootUrl:");
//                    }

                    ImageUtils.showCycleImageFromLink(rootUrl, imgAvatar, R.dimen.button_height);
//                    if (dto.getUserNos().get(0) == 70) {
//                        Log.d(TAG, "showCycleImageFromLinkScale");
//                    }
                } else {
//                    if (dto.getUserNos().get(0) == 70) {
//                        Log.d(TAG, "showRoundImage");
//                    }
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
                if (dto.getRoomType() == 1) {
                    ivStatus.setImageResource(R.drawable.home_status_me);
                }
            } else {
//                if(dto.getRoomNo()==2547)
//                {
//                    Log.d(TAG,"data:"+new Gson().toJson(dto));
//                }

                layoutGroupAvatar.setVisibility(View.VISIBLE);
                layoutAvatar.setVisibility(View.GONE);

                String url1, url2, url3, url4;

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

//                        Glide.with(CrewChatApplication.getInstance()).load(url1).bitmapTransform(ImageUtils.mCropSquareTransformation).into(imgGroupAvatar1);
//                        Glide.with(CrewChatApplication.getInstance()).load(url2).bitmapTransform(ImageUtils.mCropSquareTransformation).into(imgGroupAvatar2);

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

//                        Glide.with(CrewChatApplication.getInstance()).load(url1).bitmapTransform(ImageUtils.mCropSquareTransformation).into(imgGroupAvatar1);
//                        Glide.with(CrewChatApplication.getInstance()).load(url3).bitmapTransform(ImageUtils.mCropSquareTransformation).into(imgGroupAvatar3);
//                        Glide.with(CrewChatApplication.getInstance()).load(url4).bitmapTransform(ImageUtils.mCropSquareTransformation).into(imgGroupAvatar4);

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

//                        Glide.with(CrewChatApplication.getInstance()).load(url1).bitmapTransform(ImageUtils.mCropSquareTransformation).into(imgGroupAvatar1);
//                        Glide.with(CrewChatApplication.getInstance()).load(url2).bitmapTransform(ImageUtils.mCropSquareTransformation).into(imgGroupAvatar2);
//                        Glide.with(CrewChatApplication.getInstance()).load(url3).bitmapTransform(ImageUtils.mCropSquareTransformation).into(imgGroupAvatar3);
//                        Glide.with(CrewChatApplication.getInstance()).load(url4).bitmapTransform(ImageUtils.mCropSquareTransformation).into(imgGroupAvatar4);

                        ImageUtils.setImgFromUrl(url1, imgGroupAvatar1);
                        ImageUtils.setImgFromUrl(url2, imgGroupAvatar2);
                        ImageUtils.setImgFromUrl(url3, imgGroupAvatar3);
                        ImageUtils.setImgFromUrl(url4, imgGroupAvatar4);


                        break;

                    default:
                        imgGroupAvatar1.getLayoutParams().height = (int) CrewChatApplication.getInstance().getResources().getDimension(R.dimen.common_avatar_group);
                        imgGroupAvatar1.getLayoutParams().width = (int) CrewChatApplication.getInstance().getResources().getDimension(R.dimen.common_avatar_group);
                        imgGroupAvatar2.getLayoutParams().height = (int) CrewChatApplication.getInstance().getResources().getDimension(R.dimen.common_avatar_group);
                       // imgGroupAvatar2.getLayoutParams().width = (int) CrewChatApplication.getInstance().getResources().getDimension(R.dimen.common_avatar_group);
                        imgGroupAvatar2.setVisibility(View.VISIBLE);
                        imgGroupAvatar3.setVisibility(View.VISIBLE);
                        imgGroupAvatar4.setVisibility(View.VISIBLE);

                        tvGroupAvatar.setVisibility(View.VISIBLE);
                        String strNumber = dto.getListTreeUser().size() - 3 + "";
//                        Log.d(TAG, "strNumber:" + strNumber);
                        tvGroupAvatar.setText(strNumber);

                        url1 = new Prefs().getServerSite() + dto.getListTreeUser().get(0).getAvatarUrl();
                        url2 = new Prefs().getServerSite() + dto.getListTreeUser().get(1).getAvatarUrl();
                        url3 = new Prefs().getServerSite() + dto.getListTreeUser().get(2).getAvatarUrl();
                        url4 = "drawable://" + R.drawable.avatar_group_bg;

//                        Glide.with(CrewChatApplication.getInstance()).load(url1).bitmapTransform(ImageUtils.mCropSquareTransformation).into(imgGroupAvatar1);
//                        Glide.with(CrewChatApplication.getInstance()).load(url2).bitmapTransform(ImageUtils.mCropSquareTransformation).into(imgGroupAvatar2);
//                        Glide.with(CrewChatApplication.getInstance()).load(url3).bitmapTransform(ImageUtils.mCropSquareTransformation).into(imgGroupAvatar3);
//                        Glide.with(CrewChatApplication.getInstance()).load(url4).into(imgGroupAvatar4);


                        ImageUtils.setImgFromUrl(url1, imgGroupAvatar1);
                        ImageUtils.setImgFromUrl(url2, imgGroupAvatar2);
                        ImageUtils.setImgFromUrl(url3, imgGroupAvatar3);
                        imgGroupAvatar4.setImageResource(R.drawable.avatar_group_bg);

                        break;
                }
            }
        }

        if (dto.getListTreeUser() == null || dto.getListTreeUser().size() == 0) {
            layoutGroupAvatar.setVisibility(View.GONE);
            layoutAvatar.setVisibility(View.GONE);
//            layoutAvatar.setVisibility(View.VISIBLE);
//            String url = "drawable://" + R.drawable.avatar_l;
//            Glide.with(CrewChatApplication.getInstance()).load(url).into(imgAvatar);
            avatar_null.setVisibility(View.VISIBLE);


        } else {
            avatar_null.setVisibility(View.GONE);
        }

        view.setTag(dto.getRoomNo());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                long roomNo = (long) v.getTag();
/*

                long roomNo = (long) v.getTag();
                Intent intent = new Intent(BaseActivity.Instance, ChattingActivity.class);

                Bundle args = new Bundle();
                args.putLong(Constant.KEY_INTENT_ROOM_NO, roomNo);
                args.putLong(Constant.KEY_INTENT_USER_NO, myId);
                args.putSerializable(Constant.KEY_INTENT_ROOM_DTO, tempDto);

                intent.putExtras(args);

                BaseActivity.Instance.startActivity(intent);*/
                ChattingActivity.toActivity(BaseActivity.Instance,roomNo,myId,tempDto);
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.showContextMenu();
                return true;
            }
        });

        final boolean finalIsFilter = isFilter;

        tvTotalUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Integer> uNos = new ArrayList<>();
                uNos.add(myId);
                if (finalIsFilter) {

                    for (TreeUserDTOTemp tree : dto.getListTreeUser()) {
                        uNos.add(tree.getUserNo());
                    }
                } else {
                    for (int id : dto.getUserNos()) {
                        if (myId != id) {
                            uNos.add(id);
                        }
                    }
                }

                Intent intent = new Intent(BaseActivity.Instance, RoomUserInformationActivity.class);
                intent.putIntegerArrayListExtra("userNos", uNos);
                intent.putExtra("roomTitle", roomTitle);
                long roomNo = dto.getRoomNo();
                intent.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo);
                BaseActivity.Instance.startActivity(intent);
            }
        });

        layoutGroupAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Integer> uNos = new ArrayList<>();
                uNos.add(myId);
                if (finalIsFilter) {

                    for (TreeUserDTOTemp tree : dto.getListTreeUser()) {
                        uNos.add(tree.getUserNo());
                    }
                } else {
                    for (int id : dto.getUserNos()) {
                        if (myId != id) {
                            uNos.add(id);
                        }
                    }
                }

                long roomNo = dto.getRoomNo();
                Log.d(TAG, "roomNo put:" + roomNo);

                Intent intent = new Intent(BaseActivity.Instance, RoomUserInformationActivity.class);
                intent.putIntegerArrayListExtra("userNos", uNos);
                intent.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo);
                intent.putExtra("roomTitle", roomTitle);
                BaseActivity.Instance.startActivity(intent);
            }
        });

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dto.getListTreeUser() != null && dto.getListTreeUser().size() > 0) {

                    Intent intent = new Intent(BaseActivity.Instance, ProfileUserActivity.class);
                    intent.putExtra(Constant.KEY_INTENT_USER_NO, dto.getListTreeUser().get(0).getUserNo());
                    BaseActivity.Instance.startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Resources res = CrewChatApplication.getInstance().getResources();
        MenuItem roomRename = menu.add(0, Statics.ROOM_RENAME, 0, res.getString(R.string.room_name));

        MenuItem roomAddFavorite;
        if (tempDto.isFavorite()) {
            roomAddFavorite = menu.add(0, Statics.ROOM_REMOVE_FROM_FAVORITE, 0, res.getString(R.string.room_remove_favorite));
        } else {
            roomAddFavorite = menu.add(0, Statics.ROOM_ADD_TO_FAVORITE, 0, res.getString(R.string.room_favorite));
        }

        MenuItem roomAlarmOnOff;
        if (!tempDto.isNotification()) {
            roomAlarmOnOff = menu.add(0, Statics.ROOM_ALARM_ON, 0, res.getString(R.string.alarm_on));
        } else {
            roomAlarmOnOff = menu.add(0, Statics.ROOM_ALARM_OFF, 0, res.getString(R.string.alarm_off));
        }

        roomRename.setOnMenuItemClickListener(this);
        roomAddFavorite.setOnMenuItemClickListener(this);
        roomAlarmOnOff.setOnMenuItemClickListener(this);

        MenuItem roomOut = menu.add(0, Statics.ROOM_LEFT, 0, res.getString(R.string.room_left));
        roomOut.setOnMenuItemClickListener(this);

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Bundle roomInfo;
        switch (item.getItemId()) {
            case Statics.ROOM_RENAME:

                roomInfo = new Bundle();
                roomInfo.putInt(Statics.ROOM_NO, (int) roomNo);
                roomInfo.putString(Statics.ROOM_TITLE, roomTitle);
                mOnContextMenuSelect.onSelect(Statics.ROOM_RENAME, roomInfo);

                break;
            case Statics.ROOM_OPEN:
                roomInfo = new Bundle();
                roomInfo.putInt(Statics.ROOM_NO, (int) roomNo);
                roomInfo.putSerializable(Constant.KEY_INTENT_ROOM_DTO, tempDto);

                mOnContextMenuSelect.onSelect(Statics.ROOM_OPEN, roomInfo);

                break;

            case Statics.ROOM_REMOVE_FROM_FAVORITE:
                /*final Bundle finalRoomInfo = roomInfo;
                mOnContextMenuSelect.onSelect(Statics.ROOM_ADD_TO_FAVORITE, finalRoomInfo);*/

                HttpRequest.getInstance().removeFromFavorite(roomNo, new BaseHTTPCallBack() {
                    @Override
                    public void onHTTPSuccess() {
                        ivFavorite.setVisibility(View.GONE);

                        ChatRoomDBHelper.updateChatRoomFavorite(roomNo, false);
                        tempDto.setFavorite(false);

                        if (RecentFavoriteFragment.instance != null) {
                            RecentFavoriteFragment.instance.removeFavorite(roomNo);
                        }
                    }

                    @Override
                    public void onHTTPFail(ErrorDto errorDto) {
                        Toast.makeText(CrewChatApplication.getInstance(), res.getString(R.string.favorite_remove_failed), Toast.LENGTH_LONG).show();
                    }
                });

                break;

            case Statics.ROOM_ADD_TO_FAVORITE:
                /*roomInfo = new Bundle();
                roomInfo.putInt(Statics.ROOM_NO, (int) roomNo);*/
                /*final Bundle finalRoomInfo = roomInfo;
                mOnContextMenuSelect.onSelect(Statics.ROOM_ADD_TO_FAVORITE, finalRoomInfo);*/

                if (Utils.isNetworkAvailable()) {
                    HttpRequest.getInstance().addRoomToFavorite(roomNo, new BaseHTTPCallBack() {
                        @Override
                        public void onHTTPSuccess() {
                            Toast.makeText(CrewChatApplication.getInstance(), res.getString(R.string.favorite_add_success), Toast.LENGTH_SHORT).show();
                            ivFavorite.setVisibility(View.VISIBLE);

                            ChatRoomDBHelper.updateChatRoomFavorite(roomNo, true);
                            if (RecentFavoriteFragment.instance != null) {
                                RecentFavoriteFragment.instance.addFavorite(tempDto);
                            }
                            tempDto.setFavorite(true);
                        }

                        @Override
                        public void onHTTPFail(ErrorDto errorDto) {
                            Toast.makeText(CrewChatApplication.getInstance(), res.getString(R.string.favorite_add_success), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(CrewChatApplication.getInstance(), res.getString(R.string.no_connection_error), Toast.LENGTH_SHORT).show();
                }


                break;

            case Statics.ROOM_ALARM_ON:
                HttpRequest.getInstance().updateChatRoomNotification(roomNo, true, new BaseHTTPCallBack() {
                    @Override
                    public void onHTTPSuccess() {
                        ivNotification.setVisibility(View.GONE);
                        tempDto.setNotification(true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ChatRoomDBHelper.updateChatRoomNotification(roomNo, true);
                            }
                        }).start();
                    }

                    @Override
                    public void onHTTPFail(ErrorDto errorDto) {
                    }
                });
                break;

            case Statics.ROOM_ALARM_OFF:
                HttpRequest.getInstance().updateChatRoomNotification(roomNo, false, new BaseHTTPCallBack() {
                    @Override
                    public void onHTTPSuccess() {
                        ivNotification.setVisibility(View.VISIBLE);
                        tempDto.setNotification(false);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ChatRoomDBHelper.updateChatRoomNotification(roomNo, false);
                            }
                        }).start();
                    }

                    @Override
                    public void onHTTPFail(ErrorDto errorDto) {

                    }
                });
                break;

            case Statics.ROOM_LEFT:
                roomInfo = new Bundle();
                roomInfo.putInt(Statics.ROOM_NO, (int) roomNo);
                mOnContextMenuSelect.onSelect(Statics.ROOM_LEFT, roomInfo);
                break;
        }

        return false;
    }
}