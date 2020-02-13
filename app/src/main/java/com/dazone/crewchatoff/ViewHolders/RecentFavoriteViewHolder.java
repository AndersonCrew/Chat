package com.dazone.crewchatoff.ViewHolders;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
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
import com.dazone.crewchatoff.database.UserDBHelper;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.DrawImageItem;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.fragment.RecentFavoriteFragment;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallBack;
import com.dazone.crewchatoff.utils.Constant;
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
 * Created by david on 7/17/15.
 */
public class RecentFavoriteViewHolder extends ItemViewHolder<ChattingDto> implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
    private RecentFavoriteFragment.OnContextMenuSelect mOnContextMenuSelect;
    String TAG = "RecentFavoriteViewHolder";

    public RecentFavoriteViewHolder(View itemView, RecentFavoriteFragment.OnContextMenuSelect callback) {
        super(itemView);
        mOnContextMenuSelect = callback;
    }

    public TextView tvUserName, tvDate, tvContent, tvTotalUser;
    public RoundedImageView status_imv;
    private ImageView imgBadge, status_imv_null;
    private ImageView imgAvatar;
    private ImageView ivLastedAttach;
    private View view;
    private RelativeLayout layoutAvatar;

    private String roomTitle = "";
    private long roomNo = -1;
    private boolean isTwoUser = false;
    /**
     * Group Avatar
     */
    private RoundLayoutGroup layoutGroupAvatar;
    private ImageView imgGroupAvatar1;
    private ImageView imgGroupAvatar2;
    private ImageView imgGroupAvatar3;
    private ImageView imgGroupAvatar4;
    private TextView tvGroupAvatar;
    private ImageView ivNotification;
    private ChattingDto tempDto;
    private ImageView ivStatus;
    private int myId;

    @Override
    protected void setup(View v) {
        view = v;
        tvUserName = (TextView) v.findViewById(R.id.user_name_tv);
        tvDate = (TextView) v.findViewById(R.id.date_tv);
        tvContent = (TextView) v.findViewById(R.id.content_tv);
        imgAvatar = (ImageView) v.findViewById(R.id.avatar_imv);
        status_imv_null = (ImageView) v.findViewById(R.id.status_imv_null);
        layoutAvatar = (RelativeLayout) v.findViewById(R.id.layoutAvatar);
        ivStatus = (ImageView) v.findViewById(R.id.status_imv);

        imgBadge = (ImageView) v.findViewById(R.id.image_badge);
        ivLastedAttach = (ImageView) v.findViewById(R.id.iv_lasted_attach);
        tvTotalUser = (TextView) v.findViewById(R.id.tv_user_total);

        layoutGroupAvatar = (RoundLayoutGroup) v.findViewById(R.id.avatar_group);
        imgGroupAvatar1 = (ImageView) v.findViewById(R.id.avatar_group_1);
        imgGroupAvatar2 = (ImageView) v.findViewById(R.id.avatar_group_2);
        imgGroupAvatar3 = (ImageView) v.findViewById(R.id.avatar_group_3);
        imgGroupAvatar4 = (ImageView) v.findViewById(R.id.avatar_group_4);
        tvGroupAvatar = (TextView) v.findViewById(R.id.avatar_group_number);
        ivNotification = (ImageView) v.findViewById(R.id.iv_notification);

        view.setOnCreateContextMenuListener(this);
    }

    @Override
    public void bindData(final ChattingDto dto) {
        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dto.getListTreeUser().size() > 0) {
                    if (dto.getListTreeUser().size() == 1) {
                        Intent intent = new Intent(BaseActivity.Instance, ProfileUserActivity.class);
                        intent.putExtra(Constant.KEY_INTENT_USER_NO, dto.getListTreeUser().get(0).getUserNo());
                        BaseActivity.Instance.startActivity(intent);
                    }

                }

            }
        });
        layoutGroupAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Integer> uNos = new ArrayList<>();
                uNos.add(myId);
                if (isTwoUser) {

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
            if (status_imv_null != null)
                status_imv_null.setImageResource(R.drawable.home_big_status_03);
            if (ivStatus != null)
                ivStatus.setImageResource(R.drawable.home_big_status_03);
//            if (dto.getRoomType() == 1) {
//                tvUserName.setText(name);
//                tvUserName.setTextColor(ContextCompat.getColor(CrewChatApplication.getInstance(), R.color.black));
//            }
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

                if (dto.getRoomType() == 1) {
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

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long roomNo = (long) v.getTag();
                Intent intent = new Intent(BaseActivity.Instance, ChattingActivity.class);


                Bundle args = new Bundle();
                args.putLong(Constant.KEY_INTENT_ROOM_NO, roomNo);
                args.putLong(Constant.KEY_INTENT_USER_NO, myId);
                args.putSerializable(Constant.KEY_INTENT_ROOM_DTO, tempDto);

                intent.putExtras(args);

                BaseActivity.Instance.startActivity(intent);
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.showContextMenu();
                return true;
            }
        });

        tvTotalUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(BaseActivity.Instance, RoomUserInformationActivity.class);
                intent.putIntegerArrayListExtra("userNos", dto.getUserNos());
                intent.putExtra("roomTitle", roomTitle);
                long roomNo = dto.getRoomNo();
                intent.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo);
                BaseActivity.Instance.startActivity(intent);
            }
        });

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        Resources res = CrewChatApplication.getInstance().getResources();
        MenuItem roomRename = menu.add(0, Statics.ROOM_RENAME, 0, res.getString(R.string.room_rename));

        MenuItem roomOpen = menu.add(0, Statics.ROOM_OPEN, 0, res.getString(R.string.room_open));

        MenuItem roomAlarmOnOff;
        if (!tempDto.isNotification()) {
            roomAlarmOnOff = menu.add(0, Statics.ROOM_ALARM_ON, 0, res.getString(R.string.alarm_on));
        } else {
            roomAlarmOnOff = menu.add(0, Statics.ROOM_ALARM_OFF, 0, res.getString(R.string.alarm_off));
        }

        MenuItem roomOut = menu.add(0, Statics.ROOM_LEFT, 0, res.getString(R.string.room_left));
        MenuItem roomRemoveFavorite = menu.add(0, Statics.ROOM_REMOVE_FROM_FAVORITE, 0, res.getString(R.string.room_remove_favorite));


        roomRename.setOnMenuItemClickListener(this);
        roomOpen.setOnMenuItemClickListener(this);
        roomAlarmOnOff.setOnMenuItemClickListener(this);
        roomRemoveFavorite.setOnMenuItemClickListener(this);
        roomOut.setOnMenuItemClickListener(this);

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Bundle roomInfo = null;
        switch (item.getItemId()) {
            case Statics.ROOM_RENAME:

                roomInfo = new Bundle();
                roomInfo.putInt(Statics.ROOM_NO, (int) roomNo);
                roomInfo.putString(Statics.ROOM_TITLE, roomTitle);
//                Log.d(TAG,"roomNo:"+roomNo+" - roomTitle:"+roomTitle);
                mOnContextMenuSelect.onSelect(Statics.ROOM_RENAME, roomInfo);

                break;
            case Statics.ROOM_OPEN:
                roomInfo = new Bundle();
                roomInfo.putInt(Statics.ROOM_NO, (int) roomNo);
                mOnContextMenuSelect.onSelect(Statics.ROOM_OPEN, roomInfo);

                break;

            case Statics.ROOM_REMOVE_FROM_FAVORITE:
                final Resources res = CrewChatApplication.getInstance().getResources();
                HttpRequest.getInstance().removeFromFavorite(roomNo, new BaseHTTPCallBack() {
                    @Override
                    public void onHTTPSuccess() {
                        // update to current chat list
                        ChatRoomDBHelper.updateChatRoomFavorite(roomNo, false);
                        tempDto.setFavorite(false);

                        // Send broadcast to reload list current data
                        Intent intentBroadcast = new Intent(Constant.INTENT_FILTER_NOTIFY_ADAPTER);
                        intentBroadcast.putExtra("roomNo", tempDto.getRoomNo());
                        intentBroadcast.putExtra("type", Constant.TYPE_ACTION_FAVORITE);
                        CrewChatApplication.getInstance().sendBroadcast(intentBroadcast);

                        // Clear self from current favorite list
                        Bundle args = new Bundle();
                        args.putLong(Statics.ROOM_NO, tempDto.getRoomNo());
                        mOnContextMenuSelect.onSelect(Statics.ROOM_REMOVE_FROM_FAVORITE, args);
                    }

                    @Override
                    public void onHTTPFail(ErrorDto errorDto) {
                        Toast.makeText(CrewChatApplication.getInstance(), res.getString(R.string.favorite_remove_failed), Toast.LENGTH_LONG).show();
                    }
                });

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

                        // Send broadcast to reload list current data
                        Intent intentBroadcast = new Intent(Constant.INTENT_FILTER_NOTIFY_ADAPTER);
                        intentBroadcast.putExtra("roomNo", tempDto.getRoomNo());
                        intentBroadcast.putExtra("type", Constant.TYPE_ACTION_ALARM_ON);
                        CrewChatApplication.getInstance().sendBroadcast(intentBroadcast);
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

                                // Send broadcast to reload list current data
                                Intent intentBroadcast = new Intent(Constant.INTENT_FILTER_NOTIFY_ADAPTER);
                                intentBroadcast.putExtra("roomNo", tempDto.getRoomNo());
                                intentBroadcast.putExtra("type", Constant.TYPE_ACTION_ALARM_OFF);
                                CrewChatApplication.getInstance().sendBroadcast(intentBroadcast);
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
                roomInfo.putLong(Statics.ROOM_NO, roomNo);
                mOnContextMenuSelect.onSelect(Statics.ROOM_LEFT, roomInfo);

                break;
        }
        return false;
    }
}
