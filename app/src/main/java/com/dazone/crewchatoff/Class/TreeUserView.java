package com.dazone.crewchatoff.Class;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Message;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.database.AllUserDBHelper;
import com.dazone.crewchatoff.database.FavoriteUserDBHelper;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallbackWithJson;
import com.dazone.crewchatoff.interfaces.ICreateOneUserChatRom;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Utils;

import java.util.HashMap;

public class TreeUserView extends TreeView implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
    private ImageView avatar_imv;
    private ImageView status_imv;
    private TextView position, tvPhone1, tvPhone2;
    private LinearLayout lnItemWraper, lnPhone;
    private HashMap<Integer, ImageView> myMap;
    private int marginLeft = 0;
    private TextView tv_work_phone, tv_personal_phone;
    private TreeUserDTOTemp user;

    public TreeUserView(Context context, TreeUserDTO dto) {
        super(context, dto);
        setupView();
    }

    public TreeUserView(Context context, TreeUserDTO dto, HashMap<Integer, ImageView> statusViewMap, int marginLeft) {
        super(context, dto);
        this.myMap = statusViewMap;
        this.marginLeft = marginLeft;
        setupView();
    }

    @SuppressLint("HandlerLeak")
    protected final android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (user != null) {
                    dto.setName(user.getName());
                    dto.setNameEN(user.getNameEN());
                    dto.setCompanyNumber(user.getCellPhone());
                    dto.setAvatarUrl(user.getAvatarUrl());
                    dto.setPosition(user.getPosition());
                    dto.setType(user.getType());
                    dto.setId(user.getUserNo());
                    dto.setParent(user.getDepartNo());
                    dto.setCompanyNumber(user.getCompanyPhone());
                    dto.setPhoneNumber(user.getCellPhone());
                    dto.setStatus(user.getStatus());
                    dto.setStatusString(user.getUserStatusString());

                    // Update view
                    ImageUtils.showRoundImage(dto, avatar_imv);

                    title.setText(dto.getItemName());
                    position.setText(dto.getPosition());

                    setupStatusImage();
                    if (TextUtils.isEmpty(dto.getPhoneNumber())) {
                        tvPhone1.setVisibility(View.GONE);
                    } else {
                        tvPhone1.setText(dto.getPhoneNumber());

                    }
                    if (TextUtils.isEmpty(dto.getCompanyNumber())) {
                        tvPhone2.setVisibility(View.GONE);
                    } else {
                        tvPhone2.setText(dto.getCompanyNumber());
                    }
                }
            }
        }
    };

    @Override
    public void setupView() {
        currentView = inflater.inflate(R.layout.tree_user_row, null);
        avatar_imv = currentView.findViewById(R.id.avatar);
        status_imv = currentView.findViewById(R.id.status_imv);
        title = currentView.findViewById(R.id.name);
        position = currentView.findViewById(R.id.position);
        lnItemWraper = currentView.findViewById(R.id.item_org_wrapper);
        tv_work_phone = currentView.findViewById(R.id.tv_work_phone);
        tv_personal_phone = currentView.findViewById(R.id.tv_personal_phone);
        tvPhone1 = currentView.findViewById(R.id.tv_phone_1);
        tvPhone2 = currentView.findViewById(R.id.tv_phone_2);
        lnPhone = currentView.findViewById(R.id.ln_phone);
        main = currentView.findViewById(R.id.mainParent);

        currentView.setOnCreateContextMenuListener(this);
        currentView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.showContextMenu();
                return true;
            }
        });

        binData();
    }

    private void binData() {
        if (dto == null) {
            return;
        }

        user = AllUserDBHelper.getAUser(dto.getId());

        if (user != null) {
            dto.setName(user.getName());
            dto.setNameEN(user.getNameEN());
            dto.setCompanyNumber(user.getCellPhone());
            dto.setAvatarUrl(user.getAvatarUrl());
            dto.setPosition(user.getPosition());
            dto.setType(user.getType());
            dto.setId(user.getUserNo());
            dto.setCompanyNumber(user.getCompanyPhone());
            dto.setPhoneNumber(user.getCellPhone());
            dto.setStatus(user.getStatus());
            dto.setStatusString(user.getUserStatusString());

        }

        fillData();

    }

    private void fillData() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) lnItemWraper.getLayoutParams();
        params.leftMargin = marginLeft;
        ImageUtils.showRoundImage(dto, avatar_imv);

        title.setText(dto.getItemName());
        position.setText(dto.getPosition());

        setupStatusImage();
        handleItemClick(true);

        avatar_imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // hold status view
        if (myMap != null) {
            myMap.put(dto.getId(), status_imv);
        }
    }

    private void setupStatusImage() {
        switch (dto.getStatus()) {
            case Statics.USER_LOGIN:
                status_imv.setImageResource(R.drawable.home_big_status_01);
                break;
            case Statics.USER_AWAY:
                status_imv.setImageResource(R.drawable.home_big_status_02);
                break;
            case Statics.USER_LOGOUT:
            default:
                status_imv.setImageResource(R.drawable.home_big_status_03);
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (menu.size() == 0) {
            Resources res = CrewChatApplication.getInstance().getResources();
            MenuItem removeFavorite = menu.add(0, Statics.MENU_REMOVE_FROM_FAVORITE, 0, res.getString(R.string.remove_from_favorite));
            MenuItem openChatRoom = menu.add(0, Statics.MENU_OPEN_CHAT_ROOM, 0, res.getString(R.string.open_chat_room));

            removeFavorite.setOnMenuItemClickListener(this);
            openChatRoom.setOnMenuItemClickListener(this);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case Statics.MENU_REMOVE_FROM_FAVORITE:
                // Call API to remove an user from favorite list
                HttpRequest.getInstance().deleteFavoriteUser(dto.getParent(), dto.getId(), new BaseHTTPCallbackWithJson() {
                    @Override
                    public void onHTTPSuccess(String jsonData) {
                        FavoriteUserDBHelper.deleteFavoriteUser(dto.getParent(), dto.getId());
                        Toast.makeText(CrewChatApplication.getInstance(), "Has deleted", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onHTTPFail(ErrorDto errorDto) {
                        Toast.makeText(CrewChatApplication.getInstance(), "Has failed", Toast.LENGTH_LONG).show();
                    }
                });
                break;

            case Statics.MENU_OPEN_CHAT_ROOM:
                if (dto.getId() != Utils.getCurrentId()) {
                    HttpRequest.getInstance().CreateOneUserChatRoom(dto.getId(), new ICreateOneUserChatRom() {
                        @Override
                        public void onICreateOneUserChatRomSuccess(ChattingDto chattingDto) {
                            Intent intent = new Intent(BaseActivity.Instance, ChattingActivity.class);
                            intent.putExtra(Constant.KEY_INTENT_ROOM_NO, chattingDto.getRoomNo());
                            intent.putExtra(Statics.TREE_USER_PC, dto);
                            intent.putExtra(Statics.CHATTING_DTO, chattingDto);
                            BaseActivity.Instance.startActivity(intent);
                        }

                        @Override
                        public void onICreateOneUserChatRomFail(ErrorDto errorDto) {
                            Utils.showMessageShort("Fail");
                        }
                    });
                } else {
                    Utils.showMessage(Utils.getString(R.string.can_not_chat));
                }

                break;
        }

        return false;
    }
}