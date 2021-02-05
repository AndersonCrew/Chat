package com.dazone.crewchatoff.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.TestMultiLevelListview.MultilLevelListviewFragment;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.activity.MainActivity;
import com.dazone.crewchatoff.activity.ProfileUserActivity;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.constant.Constants;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.database.FavoriteGroupDBHelper;
import com.dazone.crewchatoff.database.FavoriteUserDBHelper;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.StatusViewDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.dto.userfavorites.FavoriteGroupDto;
import com.dazone.crewchatoff.dto.userfavorites.FavoriteUserDto;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallbackWithJson;
import com.dazone.crewchatoff.interfaces.ICreateOneUserChatRom;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.Utils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by maidinh on 4/1/2017.
 */

public class AdapterOrganizationCompanyTab extends RecyclerView.Adapter<AdapterOrganizationCompanyTab.MyViewHolder> {
    private String TAG = "OrganizationChart";
    private List<TreeUserDTO> list;
    private List<TreeUserDTO> listTemp = new ArrayList<>();
    private List<TreeUserDTO> listTemp_2 = new ArrayList<>();
    private List<TreeUserDTO> listTemp_3 = new ArrayList<>();
    private List<TreeUserDTOTemp> lstStatus;
    private int mg = 0;
    private int isSearch = 0; // 0 -> normal : 1 -> search
    private Context mContext;
    private CompanyFragment instance;
    private ArrayList<String> mSelectedImage = new ArrayList<>();
    private String typeShare;

    public void updateListStatus(List<TreeUserDTOTemp> lstStatus) {
        if (lstStatus != null && list != null && list.size() > 0) {
            if (lstStatus.size() > 0) {
                this.lstStatus = lstStatus;
                for (TreeUserDTOTemp obj : this.lstStatus) {
                    int status = obj.getStatus();
                    int userNo = obj.getUserNo();
                    for (int i = 0; i < this.list.size(); i++) {
                        TreeUserDTO temp = this.list.get(i);
                        if (userNo == temp.getId() && temp.getType() == 2) {
                            this.list.get(i).setStatus(status);
                            break;
                        }
                    }
                }
                Log.d(TAG, "updateListStatus");
                this.notifyDataSetChanged();
            }
        } else {
            Log.d(TAG, "dont updateListStatus");
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout item_org_wrapper, layout_one, layout_two;
        public LinearLayout child_list;
        public LinearLayout lnPhone;
        public ImageView avatar;
        public ImageView folderIcon;
        public ImageView ivUserStatus;
        public TextView name, position, tvPhone1, tvPhone2, nameTwo, positionTwo, name_department;
        public RelativeLayout relAvatar;

        public MyViewHolder(View view) {
            super(view);
            layout_one = view.findViewById(R.id.layout_one);
            layout_two = view.findViewById(R.id.layout_two);
            item_org_wrapper = view.findViewById(R.id.item_org_wrapper);
            child_list = view.findViewById(R.id.child_list);
            avatar = view.findViewById(R.id.avatar);
            folderIcon = view.findViewById(R.id.ic_folder);
            relAvatar = view.findViewById(R.id.relAvatar);
            ivUserStatus = view.findViewById(R.id.status_imv);
            tvPhone1 = view.findViewById(R.id.tv_phone_1);
            tvPhone2 = view.findViewById(R.id.tv_phone_2);
            lnPhone = view.findViewById(R.id.ln_phone);
            name = view.findViewById(R.id.name);
            position = view.findViewById(R.id.position);

            nameTwo = view.findViewById(R.id.nameTwo);
            positionTwo = view.findViewById(R.id.positionTwo);
            name_department = view.findViewById(R.id.name_department);
        }

        @SuppressLint("SetTextI18n")
        public void handler(final TreeUserDTO treeUserDTO, final int index) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            int margin;
            if (isSearch == 0) {
                margin = treeUserDTO.getMargin();
                layout_one.setVisibility(View.VISIBLE);
                layout_two.setVisibility(View.GONE);
            } else {
                margin = mg;
                layout_one.setVisibility(View.GONE);
                layout_two.setVisibility(View.VISIBLE);
            }
            params.setMargins(margin, 0, 0, 0);
            item_org_wrapper.setLayoutParams(params);

            folderIcon.setImageResource(treeUserDTO.isFlag() ? R.drawable.home_folder_open_ic : R.drawable.home_folder_close_ic);
            item_org_wrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "item_org_wrapper onClick");
                    if (treeUserDTO.getType() != 2) {
                        if (treeUserDTO.isFlag()) {
                            Log.d(TAG, "collapse");
                            collapse(index, treeUserDTO);
                            treeUserDTO.setFlag(false);

                        } else {
                            Log.d(TAG, "expand");
                            boolean flag = false;
                            if (index == list.size() - 1) {
                                flag = true;
                            }
                            expand(index, treeUserDTO, flag);
                            treeUserDTO.setFlag(true);

                        }
                        updateListStatus(lstStatus);
                    } else {
                        item_org_wrapper.setEnabled(false);
                        ArrayList<TreeUserDTO> selectedPersonList = new ArrayList<>();
                        if (treeUserDTO.getType() == 2) {
                            selectedPersonList.add(treeUserDTO);
                        } else {
                            int k = -10;
                            for (int i = 0; i < listTemp.size(); i++) {
                                TreeUserDTO obj = listTemp.get(i);
                                if (treeUserDTO.getId() == obj.getId()
                                        && treeUserDTO.getDBId() == obj.getDBId()
                                        && treeUserDTO.getParent() == obj.getParent()
                                        && treeUserDTO.getType() == obj.getType()
                                        && treeUserDTO.getPositionSortNo() == obj.getPositionSortNo()
                                        && treeUserDTO.getmSortNo() == obj.getmSortNo()
                                        && treeUserDTO.getName().equals(obj.getName())) {
                                    k = i;
                                    selectedPersonList.add(listTemp.get(i));
                                    break;
                                }
                            }
                            if (k != -10) {
                                int level = treeUserDTO.getLevel();
                                for (int i = k + 1; i < listTemp.size(); i++) {
                                    TreeUserDTO obj = listTemp.get(i);
                                    if (level < obj.getLevel()) {
                                        selectedPersonList.add(obj);
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                        createChatRoom(selectedPersonList, item_org_wrapper);
                    }
                }
            });
            item_org_wrapper.setOnLongClickListener(view -> {
                Log.d(TAG, "setOnLongClickListener");
                if (treeUserDTO.getType() == 2) {
                    int myId = Utils.getCurrentId();

                    if (treeUserDTO.getId() != myId) {
                        showMenu(treeUserDTO);
                    }
                } else {
                    showMenuDepartment(treeUserDTO, index);
                }
                return true;
            });

            String nameString = treeUserDTO.getName();

            String namePosition = "";
            String namePositionCurrent = "";
            try {
                namePosition = treeUserDTO.DutyName;
                namePositionCurrent = treeUserDTO.getPosition();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (namePosition == null) namePosition = "";
            if (namePositionCurrent == null) namePositionCurrent = "";

            name.setText("" + nameString);
            nameTwo.setText("" + nameString);

            name_department.setText(treeUserDTO.getName_parent());
            if (treeUserDTO.getType() == 2) {

                tvPhone1.setVisibility(View.VISIBLE);
                lnPhone.setVisibility(View.VISIBLE);
                int status = treeUserDTO.getStatus();

                if (TextUtils.isEmpty(treeUserDTO.getPhoneNumber())) {
                    tvPhone1.setVisibility(View.GONE);
                } else {
                    tvPhone1.setVisibility(View.VISIBLE);
                    tvPhone1.setText(treeUserDTO.getPhoneNumber());
                }
                if (TextUtils.isEmpty(treeUserDTO.getCompanyNumber())) {
                    tvPhone2.setVisibility(View.GONE);
                } else {
                    tvPhone2.setVisibility(View.VISIBLE);
                    tvPhone2.setText(treeUserDTO.getCompanyNumber());
                }

                String url = new Prefs().getServerSite() + treeUserDTO.getAvatarUrl();
                ImageUtils.showCycleImageFromLink(url, avatar, R.dimen.button_height);
                position.setVisibility(View.VISIBLE);
                //set duty or position by key
                setDutyOrPosition(position, namePosition, namePositionCurrent);

                //  position.setText(namePosition);
                if (status == Statics.USER_LOGIN) {
                    ivUserStatus.setImageResource(R.drawable.home_big_status_01);
                } else if (status == Statics.USER_AWAY) {
                    /*   position.setText(namePositionCurrent);*/
                    ivUserStatus.setImageResource(R.drawable.home_big_status_02);
                } else { // Logout state
                    ivUserStatus.setImageResource(R.drawable.home_big_status_03);
                }
                if (treeUserDTO.getId() == Utils.getCurrentId()) {
                    //position.setText(namePositionCurrent);
                    ivUserStatus.setImageResource(R.drawable.home_status_me);
                }
                positionTwo.setVisibility(View.VISIBLE);
                setDutyOrPosition(positionTwo, namePosition, namePositionCurrent);
               // positionTwo.setText(" " + namePosition);

                folderIcon.setVisibility(View.GONE);
                relAvatar.setVisibility(View.VISIBLE);
                avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(BaseActivity.Instance, ProfileUserActivity.class);
                        intent.putExtra(Constant.KEY_INTENT_USER_NO, treeUserDTO.getId());
                        BaseActivity.Instance.startActivity(intent);
                    }
                });

            } else {
                positionTwo.setVisibility(View.GONE);
                position.setVisibility(View.GONE);

                relAvatar.setVisibility(View.GONE);
                folderIcon.setVisibility(View.VISIBLE);
                tvPhone1.setVisibility(View.GONE);
                lnPhone.setVisibility(View.GONE);
            }

            folderIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (treeUserDTO.getType() != 2) {
                        if (treeUserDTO.isFlag()) {
                            Log.d(TAG, "collapse");
                            collapse(index, treeUserDTO);
                            treeUserDTO.setFlag(false);

                        } else {
                            Log.d(TAG, "expand");
                            boolean flag = false;
                            if (index == list.size() - 1) {
                                flag = true;
                            }
                            expand(index, treeUserDTO, flag);
                            treeUserDTO.setFlag(true);

                        }
                    }
                }
            });
        }

    }

    private void showMenu(final TreeUserDTO userInfo) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(mContext);
        builderSingle.setTitle(userInfo.getName());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                mContext,
                R.layout.row_chatting_call);

        final FavoriteUserDto user = FavoriteUserDBHelper.isFavoriteUser(userInfo.getId());

        Resources res = mContext.getResources();
        arrayAdapter.add(res.getString(R.string.chatting));

        arrayAdapter.add(res.getString(R.string.favorite_add));
        arrayAdapter.add(res.getString(R.string.view_profile));

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Go to chatting1
                            ArrayList<TreeUserDTO> selectedPersonList = new ArrayList<>();
                            selectedPersonList.add(userInfo);
                            createChatRoom(selectedPersonList, null);
                        } else if (which == 1) {

                            selectedPersonList = new ArrayList<>();
                            getSelectedPersonList(userInfo);
                            chooseGroup();

                        } else if (which == 2) {

                            Intent intent = new Intent(BaseActivity.Instance, ProfileUserActivity.class);
                            intent.putExtra(Constant.KEY_INTENT_USER_NO, userInfo.getId());
                            BaseActivity.Instance.startActivity(intent);
                            BaseActivity.Instance.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
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

    private void getSelectedPersonList(TreeUserDTO treeUserDTO) {
        if (treeUserDTO.getType() == 2) {
            selectedPersonList.add(treeUserDTO);
        } else {
            ArrayList<TreeUserDTO> subordinates = treeUserDTO.getSubordinates();
            if (subordinates != null) {
                for (TreeUserDTO treeUserDTO1 : subordinates) {
                    getSelectedPersonList(treeUserDTO1);
                }
            }
        }
    }

    private void chooseGroup() {
        getGroupFromClient();
    }

    private void getGroupFromClient() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<FavoriteGroupDto> groupArr = FavoriteGroupDBHelper.getFavoriteGroup();
                Message message = Message.obtain();
                message.what = 2;
                Bundle args = new Bundle();
                args.putParcelableArrayList("groupList", groupArr);
                message.setData(args);
                mHandler.sendMessage(message);

            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    protected final android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                // initDepartment();
            } else if (msg.what == 2) {
                Bundle args = msg.getData();
                ArrayList<FavoriteGroupDto> groups = args.getParcelableArrayList("groupList");

                if (groups == null) {
                    groups = new ArrayList<>();
                }

                groups.add(0, new FavoriteGroupDto("Favorite", 0));


                createDialog(groups);
            } else if (msg.what == 3) {
                Bundle args = msg.getData();
                ArrayList<FavoriteGroupDto> groups = args.getParcelableArrayList("groupList");

                if (groups != null) {
                    // Just get data from server and store to local data, not show dialog
                    saveDataToLocal(groups);
                }
            } else if (msg.what == 4) { // update status
                Bundle args = msg.getData();
                ArrayList<TreeUserDTOTemp> users = args.getParcelableArrayList("listUsers");
                updateStatus(users);
            } else if (msg.what == CODE_BUILD_TREE_OFFLINE) {


            }
        }
    };
    private static int CODE_BUILD_TREE_OFFLINE = 5;
    private HashMap<Integer, ArrayList<StatusViewDto>> statusList = new HashMap<>();

    private void updateStatus(ArrayList<TreeUserDTOTemp> users) {
        // Compare status and update view
        for (TreeUserDTOTemp user : users) {
            for (Map.Entry<Integer, ArrayList<StatusViewDto>> u : statusList.entrySet()) {
                if (user.getUserNo() == u.getKey()) {
                    // set image resource for this view
                    int status = user.getStatus();
                    String status_text = user.getUserStatusString();

                    for (StatusViewDto row : u.getValue()) {
                        if (TextUtils.isEmpty(status_text)) {
                            row.status_text.setVisibility(View.GONE);
                        } else {
                            row.status_text.setText(status_text);

                            if (!row.status_text.isShown()) {
                                row.status_text.setVisibility(View.VISIBLE);
                            }
                        }

                        if (status == Statics.USER_LOGIN) {
                            row.status_icon.setImageResource(R.drawable.home_big_status_01);
                        } else if (status == Statics.USER_AWAY) {
                            row.status_icon.setImageResource(R.drawable.home_big_status_02);
                        } else {
                            row.status_icon.setImageResource(R.drawable.home_big_status_03);
                        }
                    }
                }
            }
        }
    }

    private void saveDataToLocal(final List<FavoriteGroupDto> groups) {
        // Save data to local
        // sync data and store to local database
        new Thread(new Runnable() {
            @Override
            public void run() {
                // just test, not run now
                FavoriteGroupDBHelper.addGroups(groups);
            }
        }).start();
    }

    private void createDialog(final ArrayList<FavoriteGroupDto> groups) {
        String[] AlertDialogItems = new String[groups.size()];

        for (int i = 0; i < groups.size(); i++) {
            AlertDialogItems[i] = groups.get(i).getName();
        }

        AlertDialog popup;
        final ArrayList<FavoriteGroupDto> selectedItems = new ArrayList<>();
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.choose_group));

        builder.setMultiChoiceItems(AlertDialogItems, null,
                (dialog, indexSelected, isChecked) -> {
                    if (isChecked) {
                        selectedItems.add(groups.get(indexSelected));
                    } else if (selectedItems.contains(indexSelected)) {
                        selectedItems.remove(indexSelected);
                    }
                });

        builder.setPositiveButton(R.string.yes, (dialog, id) -> {
            if (selectedItems.size() == 0) {
                String msg = mContext.getResources().getString(R.string.msg_select_item);
                Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
            } else {
                // Send user to server
                insertFavoriteUser(selectedItems);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.no, (dialog, id) -> dialog.dismiss());

        popup = builder.create();
        popup.show();
        Button b = popup.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (b != null) {
            b.setTextColor(Color.BLACK);
        }

        Button b2 = popup.getButton(DialogInterface.BUTTON_POSITIVE);
        if (b2 != null) {
            b2.setTextColor(Color.BLACK);
        }
    }

    private ArrayList<TreeUserDTO> selectedPersonList;

    private void insertFavoriteUser(ArrayList<FavoriteGroupDto> groups) {
        for (FavoriteGroupDto group : groups) {

            for (TreeUserDTO user : selectedPersonList) {
                HttpRequest.getInstance().insertFavoriteUser(group.getGroupNo(), user.getId(), new BaseHTTPCallbackWithJson() {
                    @Override
                    public void onHTTPSuccess(String jsonData) {
                        Toast.makeText(CrewChatApplication.getInstance(), "Insert to favorite successfully", Toast.LENGTH_LONG).show();

                        // Ok, if tab multi level user is visible, let's add current user to it
                        final FavoriteUserDto user = new Gson().fromJson(jsonData, FavoriteUserDto.class);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "user.getGroupNo():" + user.getGroupNo());
                                if (user.getGroupNo() == 0) {
                                    user.setIsTop(1);
                                }
                                FavoriteUserDBHelper.addFavoriteUser(user);
                            }
                        }).start();

                        if (MultilLevelListviewFragment.instanceNew != null && MultilLevelListviewFragment.instanceNew.isVisible()) {
                            MultilLevelListviewFragment.instanceNew.addNewFavorite(user);
                        }
                    }

                    @Override
                    public void onHTTPFail(ErrorDto errorDto) {
                        Toast.makeText(CrewChatApplication.getInstance(), "Insert to favorite failed", Toast.LENGTH_LONG).show();
                    }
                });
            }

        }
    }

    private void showMenuDepartment(final TreeUserDTO userInfo, final int index) {
        Resources res = mContext.getResources();
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(mContext);
        builderSingle.setTitle(userInfo.getName());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mContext, R.layout.row_chatting_call);
        arrayAdapter.add(res.getString(R.string.organization_group_chat));
        arrayAdapter.add(res.getString(R.string.organization_add_favorite));

        builderSingle.setAdapter(
                arrayAdapter,
                (dialog, which) -> {
                    if (which == 0) {
                        // Go to chatting1
                        selectedPersonList = new ArrayList<>();
                        getSelectedPersonList(userInfo);
                        createChatRoom(selectedPersonList, null);
                    } else if (which == 1) {
                        // Get selected person list
                        selectedPersonList = new ArrayList<>();
                        getSelectedPersonList(userInfo);

                        // Show choose group
                        chooseGroup();
                        // Add to favorite
                        // Refresh tab favorite if tab visible
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

    boolean isAdd(List<TreeUserDTO> lst, TreeUserDTO treeUserDTO) {
        return true;
    }

    void addListTemp(TreeUserDTO obj, int margin, int level) {
        margin += Utils.getDimenInPx(R.dimen.dimen_20_40);
        obj.setMargin(margin);

        level += 1;
        obj.setLevel(level);

        String temp = obj.getId() + obj.getName() + Utils.getCurrentId();
        boolean flag = new Prefs().getBooleanValue(temp, false);
        obj.setFlag(flag);
        obj.setName_parent(Constant.get_department_name(obj, listTemp_3));

        this.listTemp.add(obj);
        this.listTemp_2.add(obj);
        this.listTemp_3.add(obj);

        if (obj.getSubordinates() != null) {
            if (obj.getSubordinates().size() > 0) {
                boolean hasType2 = false;
                boolean hasType0 = false;

                for (TreeUserDTO dto : obj.getSubordinates()) {
                    if (dto.getType() == 2) {
                        hasType2 = true;
                    }
                    if (dto.getType() == 0) {
                        hasType0 = true;
                    }
                }
                if (hasType2 && hasType0) {
                    Collections.sort(obj.getSubordinates(), new Comparator<TreeUserDTO>() {
                        @Override
                        public int compare(TreeUserDTO r1, TreeUserDTO r2) {
                            return r1.getmSortNo() - r2.getmSortNo();
                        }
                    });
                }
                for (TreeUserDTO dto1 : obj.getSubordinates()) {
                    addListTemp(dto1, margin, level);
                }
            }
        }
    }

    public void updateList(List<TreeUserDTO> list) {
        if (list != null && list.size() > 0) {
            Log.d(TAG, "start updateList");
            this.list.clear();
            final int tempMargin = Utils.getDimenInPx(R.dimen.dimen_20_40) * -1;
            for (TreeUserDTO obj : list) {
                addListTemp(obj, tempMargin, -1);
            }

            Log.d(TAG, "finish addListTemp:" + listTemp.size());
            String first_login = Statics.FIRST_LOGIN;
            boolean isLogin = new Prefs().getBooleanValue(first_login, false);
            if (!isLogin) {
                new Prefs().putBooleanValue(first_login, true);
                List<Integer> lstId = new ArrayList<>();
                int levelFirst = 0;
                for (int i = 0; i < this.listTemp.size(); i++) {
                    TreeUserDTO obj = this.listTemp.get(i);
                    if (obj.getId() == Utils.getCurrentId() && obj.getType() == 2) {
                        lstId.add(i);
                        String temp = obj.getId() + obj.getName() + Utils.getCurrentId();
                        new Prefs().putBooleanValue(temp, true);
                        this.listTemp.get(i).setFlag(true);
                    }
                }
                for (int i = 0; i < lstId.size(); i++) {
                    levelFirst = this.listTemp.get(lstId.get(i)).getLevel();
                    for (int j = lstId.get(i); j >= 0; j--) {
                        TreeUserDTO obj = this.listTemp.get(j);
                        if (levelFirst > obj.getLevel()) {
                            levelFirst = obj.getLevel();
                            String temp = obj.getId() + obj.getName() + Utils.getCurrentId();
                            new Prefs().putBooleanValue(temp, true);
                            this.listTemp.get(j).setFlag(true);
                        }
                    }
                }
            } else {
            }
            List<TreeUserDTO> lst = this.listTemp_2;
            for (int i = 0; i < lst.size(); i++) {
                TreeUserDTO obj = lst.get(i);
                int level = obj.getLevel();
                boolean flag = obj.isFlag();
                if (!flag) {
                    if (i + 1 < lst.size()) {
                        for (int j = i + 1; j < lst.size(); j++) {
                            TreeUserDTO nextObj = lst.get(j);
                            if (level < nextObj.getLevel()) {
                                lst.remove(j);
                                j--;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
            this.list = lst;
            this.notifyDataSetChanged();
            Log.d(TAG, "notifyDataSetChanged");
        }

    }

    public List<TreeUserDTO> getList() {
        return listTemp;
    }

    public List<TreeUserDTO> getCurrentList() {
        return list;
    }

    public void updateIsSearch(int a) {
        isSearch = a;
    }

    public void updateListSearch(List<TreeUserDTO> lst) {
        Log.d(TAG, "updateListSearch");
        this.list = lst;
        this.notifyDataSetChanged();
    }

    boolean isAddSearch(List<TreeUserDTO> lst, int id) {
        for (TreeUserDTO obj : lst) {
            if (obj.getId() == id)
                return false;
        }
        return true;
    }

    public void actionSearch(String key) {
        List<TreeUserDTO> lst = new ArrayList<>();
        for (TreeUserDTO obj : listTemp_3) {
            if (obj.getType() == 2) {
                if ((obj.getName().toUpperCase().contains(key.toUpperCase()))
                        || (obj.getPhoneNumber() != null && obj.getPhoneNumber().toUpperCase().contains(key.toUpperCase()))
                        || (obj.getPhoneNumber() != null && obj.getPhoneNumber().toUpperCase().replace("-", "").contains(key.toUpperCase()))
                        || (obj.getCompanyNumber() != null && obj.getCompanyNumber().toUpperCase().contains(key.toUpperCase()))
                        || (obj.getCompanyNumber() != null && obj.getCompanyNumber().toUpperCase().replace("-", "").contains(key.toUpperCase()))) {
                    if (isAddSearch(lst, obj.getId())) {
                        lst.add(obj);
                    }
                }
            }
        }
        this.list = lst;
        this.notifyDataSetChanged();
    }

    public AdapterOrganizationCompanyTab(Context context, List<TreeUserDTO> list, boolean mIsDisableSelected, CompanyFragment instance) {
        this.mContext = context;
        this.list = list;
        this.instance = instance;
        this.mg = Utils.getDimenInPx(R.dimen.dimen_20_40);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_organization_company_tab, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        TreeUserDTO treeUserDTO = list.get(position);
        holder.handler(treeUserDTO, position);

    }

    void collapse(final int position, final TreeUserDTO treeUserDTO) {
        String temp = treeUserDTO.getId() + treeUserDTO.getName() + Utils.getCurrentId();
        Log.d(TAG, "collapse:" + temp);
        new Prefs().putBooleanValue(temp, false);
        int levelCur = list.get(position).getLevel();

        int a = position + 1;
        if (a < list.size()) {
            for (int i = a; i < list.size(); i++) {
                TreeUserDTO obj = list.get(i);
                int level = obj.getLevel();
                if (levelCur < level) {
                    list.remove(i);
                    i--;
                } else {
                    break;
                }
            }
            notifyDataSetChanged();
        }

    }

    boolean isContains(TreeUserDTO obj) {
        for (TreeUserDTO treeUserDTO : this.list) {
            if (treeUserDTO.getId() == obj.getId()
                    && treeUserDTO.getDBId() == obj.getDBId()
                    && treeUserDTO.getParent() == obj.getParent()
                    && treeUserDTO.getType() == obj.getType()
                    && treeUserDTO.getPositionSortNo() == obj.getPositionSortNo()
                    && treeUserDTO.getmSortNo() == obj.getmSortNo()
                    && treeUserDTO.getName().equals(obj.getName()))
                return true;
        }
        return false;
    }

    private void expand(int position, TreeUserDTO treeUserDTO, boolean flag) {
        String temp = treeUserDTO.getId() + treeUserDTO.getName() + Utils.getCurrentId();
        new Prefs().putBooleanValue(temp, true);
        int levelCur = treeUserDTO.getLevel();
        int index = position + 1;

        // get index of list
        int indexListTemp = 0;
        for (int i = 0; i < listTemp.size(); i++) {
            TreeUserDTO obj = listTemp.get(i);
            if (obj.getType() != 2) {
                if (treeUserDTO.getId() == obj.getId()
                        && treeUserDTO.getDBId() == obj.getDBId()
                        && treeUserDTO.getParent() == obj.getParent()
                        && treeUserDTO.getType() == obj.getType()
                        && treeUserDTO.getPositionSortNo() == obj.getPositionSortNo()
                        && treeUserDTO.getmSortNo() == obj.getmSortNo()
                        && treeUserDTO.getName().equals(obj.getName())) {
                    indexListTemp = i;
                    break;
                }
            }
        }

        int a = indexListTemp + 1;
        if (a < listTemp.size()) {
            for (int i = a; i < listTemp.size(); i++) {
                TreeUserDTO object = listTemp.get(i);
                if (levelCur < object.getLevel()) {
                    int k = 1;
                    TreeUserDTO preObj = listTemp.get(i - k);
                    String tE = "";
                    boolean fL = false;
                    while (preObj.getLevel() >= object.getLevel()) {
                        k++;
                        preObj = listTemp.get(i - k);
                    }
                    tE = preObj.getId() + preObj.getName() + Utils.getCurrentId();
                    fL = new Prefs().getBooleanValue(tE, false);

                    if (fL && isContains(preObj)) {
                        tE = object.getId() + object.getName() + Utils.getCurrentId();
                        fL = new Prefs().getBooleanValue(tE, false);
                        object.setFlag(fL);
                        if (isAdd(list, object)) {
                            list.add(index, object);
                            index++;
                        }
                    }
                } else {
                    break;
                }
            }
        }

        notifyDataSetChanged();
        if (flag) {
            Log.d(TAG, "position:" + position);
            instance.scrollToEndList(position + 1);
        }
    }


    private void createChatRoom(final ArrayList<TreeUserDTO> selectedPersonList, final LinearLayout layout) {
        if (selectedPersonList.size() == 1) {
            HttpRequest.getInstance().CreateOneUserChatRoom(selectedPersonList.get(0).getId(), new ICreateOneUserChatRom() {
                @Override
                public void onICreateOneUserChatRomSuccess(ChattingDto chattingDto) {
                    Intent intent = new Intent(BaseActivity.Instance, ChattingActivity.class);
                    intent.putExtra(Statics.TREE_USER_PC, selectedPersonList.get(0));
                    intent.putExtra(Statics.CHATTING_DTO, chattingDto);
                    intent.putExtra(Statics.IV_STATUS, selectedPersonList.get(0).getStatus());
                    intent.putExtra(Constant.KEY_INTENT_ROOM_NO, chattingDto.getRoomNo());

                    if(MainActivity.type != null && MainActivity.mSelectedImage.size() > 0) {
                        intent.putExtra(Constants.TYPE_SHARE, MainActivity.type);
                        intent.putExtra(Constants.LIST_FILE_PATH_SHARE, MainActivity.mSelectedImage);
                    }

                    BaseActivity.Instance.startActivity(intent);
                    if (layout != null) layout.setEnabled(true);
                }

                @Override
                public void onICreateOneUserChatRomFail(ErrorDto errorDto) {
                    if (layout != null) layout.setEnabled(true);
                    Utils.showMessageShort("Fail");
                }
            });
        } else if (selectedPersonList.size() > 1) {
            int temp = -1;
            if (selectedPersonList.size() == 2) {
                boolean flag = false;
                for (TreeUserDTO obj : selectedPersonList) {
                    if (obj.getSubordinates() != null) {
                        if (obj.getSubordinates().size() > 0) {
                            flag = true;
                            break;
                        }
                    }
                }
                if (flag) {
                    temp = selectedPersonList.get(1).getStatus();
                }
            }

            final int IV_STATUS = temp;
            HttpRequest.getInstance().CreateGroupChatRoom(selectedPersonList, new ICreateOneUserChatRom() {
                @Override
                public void onICreateOneUserChatRomSuccess(ChattingDto chattingDto) {
                    Intent intent = new Intent(BaseActivity.Instance, ChattingActivity.class);
                    intent.putExtra(Statics.CHATTING_DTO, chattingDto);
                    intent.putExtra(Constant.KEY_INTENT_ROOM_NO, chattingDto.getRoomNo());
                    if (IV_STATUS != -1)
                        intent.putExtra(Statics.IV_STATUS, IV_STATUS);

                    if(MainActivity.type != null && MainActivity.mSelectedImage.size() > 0) {
                        intent.putExtra(Constants.TYPE_SHARE, MainActivity.type);
                        intent.putExtra(Constants.LIST_FILE_PATH_SHARE, MainActivity.mSelectedImage);
                    }

                    BaseActivity.Instance.startActivity(intent);
                    if (layout != null) layout.setEnabled(true);
                }

                @Override
                public void onICreateOneUserChatRomFail(ErrorDto errorDto) {
                    if (layout != null) layout.setEnabled(true);
                    Utils.showMessageShort("Fail");
                }
            }, "");
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
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
}