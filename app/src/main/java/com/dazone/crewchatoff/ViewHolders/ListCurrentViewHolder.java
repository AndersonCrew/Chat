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
import com.dazone.crewchatoff.activity.MainActivity;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ListCurrentViewHolder extends ItemViewHolder<ChattingDto> implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
    private CurrentChatListFragment.OnContextMenuSelect mOnContextMenuSelect;

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
        tvUserName = v.findViewById(R.id.user_name_tv);
        tvDate = v.findViewById(R.id.date_tv);
        tvContent = v.findViewById(R.id.content_tv);
        status_imv_null = v.findViewById(R.id.status_imv_null);
        imgAvatar = v.findViewById(R.id.avatar_imv);
        ivStatus = v.findViewById(R.id.status_imv);
        layoutAvatar = v.findViewById(R.id.layoutAvatar);

        imgBadge = v.findViewById(R.id.image_badge);
        ivLastedAttach = v.findViewById(R.id.iv_lasted_attach);
        tvTotalUser = v.findViewById(R.id.tv_user_total);
        avatar_null = v.findViewById(R.id.avatar_null);
        layoutGroupAvatar = v.findViewById(R.id.avatar_group);
        imgGroupAvatar1 = v.findViewById(R.id.avatar_group_1);
        imgGroupAvatar2 = v.findViewById(R.id.avatar_group_2);
        imgGroupAvatar3 = v.findViewById(R.id.avatar_group_3);
        imgGroupAvatar4 = v.findViewById(R.id.avatar_group_4);
        tvGroupAvatar = v.findViewById(R.id.avatar_group_number);
        ivFavorite = v.findViewById(R.id.iv_favorite);
        ivNotification = v.findViewById(R.id.iv_notification);

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

        ArrayList<TreeUserDTOTemp> listUsers = null;
        if (CompanyFragment.instance != null) listUsers = CompanyFragment.instance.getUser();
        if (listUsers == null) listUsers = new ArrayList<>();

        totalUser = dto.getUserNos().size();

        if (dto.getListTreeUser() != null && dto.getListTreeUser().size() < dto.getUserNos().size()) {
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
                strLastMsg = dto.getLastedMsg() + "";
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
            tvDate.setText(TimeUtils.displayTimeWithoutOffset(CrewChatApplication.getInstance().getApplicationContext(), dto.getLastedMsgDate(), 0, TimeUtils.KEY_FROM_SERVER));
        }

        if (dto.getListTreeUser() != null && dto.getListTreeUser().size() > 0) {
            if (dto.getListTreeUser().size() < 2) {
                layoutGroupAvatar.setVisibility(View.GONE);
                layoutAvatar.setVisibility(View.VISIBLE);

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
            avatar_null.setVisibility(View.VISIBLE);


        } else {
            avatar_null.setVisibility(View.GONE);
        }

        view.setTag(dto.getRoomNo());

        view.setOnClickListener(v -> {
            long roomNo = (long) v.getTag();
            ChattingActivity.toActivity(BaseActivity.Instance, roomNo, myId, tempDto, MainActivity.type, MainActivity.mSelectedImage);
            MainActivity.type = null;
            MainActivity.imageUri = null;
        });

        view.setOnLongClickListener(v -> {
            v.showContextMenu();
            return true;
        });

        final boolean finalIsFilter = isFilter;

        tvTotalUser.setOnClickListener(v -> {
            ArrayList<Integer> uNos = new ArrayList<>();
            uNos.add(myId);
            for (int id : dto.getUserNos()) {
                if (myId != id) {
                    uNos.add(id);
                }
            }

            Intent intent = new Intent(BaseActivity.Instance, RoomUserInformationActivity.class);
            intent.putIntegerArrayListExtra("userNos", uNos);
            intent.putExtra("roomTitle", roomTitle);
            long roomNo = dto.getRoomNo();
            intent.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo);
            BaseActivity.Instance.startActivity(intent);
        });

        layoutGroupAvatar.setOnClickListener(view -> {
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
            Intent intent = new Intent(BaseActivity.Instance, RoomUserInformationActivity.class);
            intent.putIntegerArrayListExtra("userNos", uNos);
            intent.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo);
            intent.putExtra("roomTitle", roomTitle);
            BaseActivity.Instance.startActivity(intent);
        });

        imgAvatar.setOnClickListener(view -> {
            if (dto.getListTreeUser() != null && dto.getListTreeUser().size() > 0) {
                Intent intent = new Intent(BaseActivity.Instance, ProfileUserActivity.class);
                intent.putExtra(Constant.KEY_INTENT_USER_NO, dto.getListTreeUser().get(0).getUserNo());
                BaseActivity.Instance.startActivity(intent);
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
                        new Thread(() -> ChatRoomDBHelper.updateChatRoomNotification(roomNo, true)).start();
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

                        new Thread(() -> ChatRoomDBHelper.updateChatRoomNotification(roomNo, false)).start();
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