package com.dazone.crewchatoff.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.activity.InviteUserActivity;
import com.dazone.crewchatoff.activity.NewOrganizationChart;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by maidinh on 29/12/2016.
 */

public class AdapterOrganizationChart extends RecyclerView.Adapter<AdapterOrganizationChart.MyViewHolder> {
    private String TAG = "OrganizationChart";
    private List<TreeUserDTO> list = new ArrayList<>();
    private List<TreeUserDTO> listTemp = new ArrayList<>();
    private List<TreeUserDTO> listTemp_2 = new ArrayList<>();
    private List<TreeUserDTO> listTemp_3 = new ArrayList<>();
    private ArrayList<Integer> userNos;
    private int isSearch = 0; // 0 -> normal : 1 -> search
    private int myId;
    private NewOrganizationChart instance;
    private InviteUserActivity instance_2;
    private int mg = 0;

    public List<TreeUserDTO> getCurrentList() {
        return list;
    }

    public void updateIsSearch(int a) {
        isSearch = a;
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

    public void updateListSearch(List<TreeUserDTO> lst) {
        this.list = lst;
        this.notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout iconWrapper, item_org_wrapper, layout_one, layout_two;
        public ImageView avatar;
        public ImageView folderIcon;
        public ImageView ivStatus;
        public TextView name, position, nameTwo, positionTwo, name_department;
        public CheckBox row_check;
        public RelativeLayout relAvatar;

        public MyViewHolder(View view) {
            super(view);

            layout_one = view.findViewById(R.id.layout_one);
            layout_two = view.findViewById(R.id.layout_two);

            item_org_wrapper = view.findViewById(R.id.item_org_wrapper);

            avatar = view.findViewById(R.id.avatar);
            folderIcon = view.findViewById(R.id.ic_folder);
            relAvatar = view.findViewById(R.id.relAvatar);
            iconWrapper = view.findViewById(R.id.icon_wrapper);
            ivStatus = view.findViewById(R.id.status_imv);
            name = view.findViewById(R.id.name);
            position = view.findViewById(R.id.position);
            row_check = view.findViewById(R.id.row_check);

            name_department = view.findViewById(R.id.name_department);
            nameTwo = view.findViewById(R.id.nameTwo);
            positionTwo = view.findViewById(R.id.positionTwo);
        }

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
            String nameString = treeUserDTO.getName();
            String namePosition = "";
            String nameDuty = "";
            try {
                namePosition = treeUserDTO.getPosition();
                nameDuty = treeUserDTO.getDutyName();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (treeUserDTO.getType() == 2) {
                String url = new Prefs().getServerSite() + treeUserDTO.getAvatarUrl();

                avatar.setImageResource(R.drawable.avatar_l);
                ImageLoader.getInstance().displayImage(url, avatar, Statics.options2);

                position.setVisibility(View.VISIBLE);
                setDutyOrPosition(position, nameDuty, namePosition);
                positionTwo.setVisibility(View.VISIBLE);
                setDutyOrPosition(positionTwo, nameDuty, namePosition);

                folderIcon.setVisibility(View.GONE);
                relAvatar.setVisibility(View.VISIBLE);

                int status = treeUserDTO.getStatus();
                if (status == Statics.USER_LOGIN) {
                    ivStatus.setImageResource(R.drawable.home_big_status_01);
                } else if (status == Statics.USER_AWAY) {
                    ivStatus.setImageResource(R.drawable.home_big_status_02);
                } else { // Logout state
                    ivStatus.setImageResource(R.drawable.home_big_status_03);
                }
                if (treeUserDTO.getId() == Utils.getCurrentId()) {
                    ivStatus.setImageResource(R.drawable.home_status_me);
                }
            } else {
                position.setVisibility(View.GONE);

                positionTwo.setVisibility(View.GONE);

                relAvatar.setVisibility(View.GONE);
                folderIcon.setVisibility(View.VISIBLE);
            }
            name_department.setText("" + treeUserDTO.getName_parent());
            name.setText(nameString);
            nameTwo.setText(nameString);
            item_org_wrapper.setOnClickListener(view -> {
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
                } else {
                    boolean flag_2 = treeUserDTO.isCheck();
                    boolean flag;
                    if (flag_2) {
                        flag = false;
                        row_check.setChecked(false);
                        treeUserDTO.setIsCheck(false);
                    } else {
                        flag = true;
                        row_check.setChecked(true);
                        treeUserDTO.setIsCheck(true);
                    }

                    int LEVEL = treeUserDTO.getLevel();
                    int LEVEL_TEMP = treeUserDTO.getLevel();

                    if (flag) {
                        for (int i = 0; i < listTemp.size(); i++) {
                            TreeUserDTO obj = listTemp.get(i);

                            if (treeUserDTO.getId() == obj.getId()
                                    && treeUserDTO.getDBId() == obj.getDBId()
                                    && treeUserDTO.getParent() == obj.getParent()
                                    && treeUserDTO.getType() == obj.getType()
                                    && treeUserDTO.getPositionSortNo() == obj.getPositionSortNo()
                                    && treeUserDTO.getmSortNo() == obj.getmSortNo()
                                    && treeUserDTO.getName().equals(obj.getName())) {

                                listTemp.get(i).setIsCheck(flag);

                                break;
                            }
                        }
                    } else {
                        list.get(index).setIsCheck(false);
                        for (int i = index; i >= 0; i--) {
                            TreeUserDTO obj = list.get(i);
                            if (LEVEL > obj.getLevel()) {
                                list.get(i).setIsCheck(false);
                                LEVEL = obj.getLevel();

                            }
                        }

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
                                listTemp.get(i).setIsCheck(false);
                                break;
                            }
                        }
                        if (k >= 0) {

                            for (int i = k; i >= 0; i--) {
                                TreeUserDTO obj = listTemp.get(i);
                                if (LEVEL_TEMP > obj.getLevel()) {

                                    listTemp.get(i).setIsCheck(false);
                                    LEVEL_TEMP = obj.getLevel();
                                }
                            }
                        }
                    }
                    notifyDataSetChanged();
                }
            });

            row_check.setChecked(treeUserDTO.isCheck());
            row_check.setOnClickListener(view -> {
                Log.d(TAG, "row_check onClick");
                boolean flag = row_check.isChecked();
                treeUserDTO.setIsCheck(flag);
                int LEVEL = treeUserDTO.getLevel();
                int LEVEL_TEMP = treeUserDTO.getLevel();

                if (treeUserDTO.getType() == 2) {
                    if (flag) {
                        for (int i = 0; i < listTemp.size(); i++) {
                            TreeUserDTO obj = listTemp.get(i);

                            if (treeUserDTO.getId() == obj.getId()
                                    && treeUserDTO.getDBId() == obj.getDBId()
                                    && treeUserDTO.getParent() == obj.getParent()
                                    && treeUserDTO.getType() == obj.getType()
                                    && treeUserDTO.getPositionSortNo() == obj.getPositionSortNo()
                                    && treeUserDTO.getmSortNo() == obj.getmSortNo()
                                    && treeUserDTO.getName().equals(obj.getName())) {

                                listTemp.get(i).setIsCheck(flag);

                                break;
                            }
                        }
                    } else {
                        list.get(index).setIsCheck(false);
                        for (int i = index; i >= 0; i--) {
                            TreeUserDTO obj = list.get(i);
                            if (LEVEL > obj.getLevel()) {
                                list.get(i).setIsCheck(false);
                                LEVEL = obj.getLevel();

                            }
                        }

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
                                listTemp.get(i).setIsCheck(false);
                                break;
                            }
                        }
                        if (k >= 0) {

                            for (int i = k; i >= 0; i--) {
                                TreeUserDTO obj = listTemp.get(i);
                                if (LEVEL_TEMP > obj.getLevel()) {

                                    listTemp.get(i).setIsCheck(false);
                                    LEVEL_TEMP = obj.getLevel();
                                }
                            }
                        }
                    }

                } else {
                    if (flag) {
                        int a = index + 1;
                        if (a < list.size()) {
                            for (int i = a; i < list.size(); i++) {
                                TreeUserDTO obj = list.get(i);
                                if (LEVEL < obj.getLevel()) {
                                    list.get(i).setIsCheck(true);
                                } else {
                                    break;
                                }
                            }
                        }
                        int temp = 0;
                        for (int i = 0; i < listTemp.size(); i++) {
                            TreeUserDTO obj = listTemp.get(i);
                            if (treeUserDTO.getId() == obj.getId()
                                    && treeUserDTO.getDBId() == obj.getDBId()
                                    && treeUserDTO.getParent() == obj.getParent()
                                    && treeUserDTO.getType() == obj.getType()
                                    && treeUserDTO.getPositionSortNo() == obj.getPositionSortNo()
                                    && treeUserDTO.getmSortNo() == obj.getmSortNo()
                                    && treeUserDTO.getName().equals(obj.getName())) {
                                listTemp.get(i).setIsCheck(flag);
                                temp = i;
                                break;
                            }
                        }
                        int c = temp + 1;
                        if (c < listTemp.size()) {
                            for (int i = c; i < listTemp.size(); i++) {
                                TreeUserDTO obj = listTemp.get(i);
                                if (LEVEL < obj.getLevel()) {
                                    listTemp.get(i).setIsCheck(true);
                                } else {
                                    break;
                                }
                            }
                        }

                    } else {
                        if (LEVEL == 0) {
                            int a = index + 1;
                            if (a < list.size()) {
                                for (int i = a; i < list.size(); i++) {
                                    TreeUserDTO obj = list.get(i);
                                    if (LEVEL < obj.getLevel()) {
                                        list.get(i).setIsCheck(false);
                                    } else {
                                        break;
                                    }
                                }
                            }
                            int temp = 0;
                            for (int i = 0; i < listTemp.size(); i++) {
                                TreeUserDTO obj = listTemp.get(i);
                                if (treeUserDTO.getId() == obj.getId()
                                        && treeUserDTO.getDBId() == obj.getDBId()
                                        && treeUserDTO.getParent() == obj.getParent()
                                        && treeUserDTO.getType() == obj.getType()
                                        && treeUserDTO.getPositionSortNo() == obj.getPositionSortNo()
                                        && treeUserDTO.getmSortNo() == obj.getmSortNo()
                                        && treeUserDTO.getName().equals(obj.getName())) {
                                    listTemp.get(i).setIsCheck(flag);
                                    temp = i;
                                    break;
                                }
                            }
                            int c = temp + 1;
                            if (c < listTemp.size()) {
                                for (int i = c; i < listTemp.size(); i++) {
                                    TreeUserDTO obj = listTemp.get(i);
                                    if (LEVEL < obj.getLevel()) {
                                        listTemp.get(i).setIsCheck(false);
                                    } else {
                                        break;
                                    }
                                }
                            }
                        } else {

                            for (int i = index; i >= 0; i--) {
                                TreeUserDTO obj = list.get(i);
                                if (LEVEL > obj.getLevel()) {
                                    list.get(i).setIsCheck(false);
                                    LEVEL = obj.getLevel();
                                }
                            }

                            int temp = 0;
                            for (int i = 0; i < listTemp.size(); i++) {
                                TreeUserDTO obj = listTemp.get(i);
                                if (treeUserDTO.getId() == obj.getId()
                                        && treeUserDTO.getDBId() == obj.getDBId()
                                        && treeUserDTO.getParent() == obj.getParent()
                                        && treeUserDTO.getType() == obj.getType()
                                        && treeUserDTO.getPositionSortNo() == obj.getPositionSortNo()
                                        && treeUserDTO.getmSortNo() == obj.getmSortNo()
                                        && treeUserDTO.getName().equals(obj.getName())) {
                                    listTemp.get(i).setIsCheck(flag);
                                    temp = i;
                                    break;
                                }
                            }
                            for (int i = temp; i >= 0; i--) {
                                TreeUserDTO obj = listTemp.get(i);
                                if (LEVEL > obj.getLevel()) {
                                    listTemp.get(i).setIsCheck(false);
                                    LEVEL = obj.getLevel();
                                }
                            }
                            int level = treeUserDTO.getLevel();
                            for (int i = temp + 1; i < listTemp.size(); i++) {
                                TreeUserDTO obj = listTemp.get(i);
                                if (level < obj.getLevel()) {
                                    listTemp.get(i).setIsCheck(false);
                                } else {
                                    break;
                                }
                            }
                            // for child list
                            if (index + 1 < list.size()) {
                                for (int i = index + 1; i < list.size(); i++) {
                                    TreeUserDTO obj = list.get(i);
                                    if (level < obj.getLevel()) {
                                        list.get(i).setIsCheck(false);
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                notifyDataSetChanged();
            });
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

    boolean isAdd(List<TreeUserDTO> lst, TreeUserDTO treeUserDTO) {
        return true;
    }


    void addList(TreeUserDTO obj, int margin, int level) {
        margin += Utils.getDimenInPx(R.dimen.dimen_20_40);
        obj.setMargin(margin);

        level += 1;
        obj.setLevel(level);

        if (userNos != null) {
            for (int a : userNos) {
                if (a == obj.getId()) {
                    obj.setCheck(true);
                    break;
                }
            }
        }
        obj.setName_parent(Constant.get_department_name(obj, listTemp_3));
        this.listTemp.add(obj);

        String temp = obj.getId() + obj.getName() + Utils.getCurrentId();
        boolean flag = new Prefs().getBooleanValue(temp, false);
        obj.setFlag(flag);
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
                    Collections.sort(obj.getSubordinates(), (r1, r2) -> r1.getmSortNo() - r2.getmSortNo());
                }
                for (TreeUserDTO dto1 : obj.getSubordinates()) {
                    addList(dto1, margin, level);
                }
            }
        }
    }

    public void updateList(List<TreeUserDTO> listUpdate) {
        if (listUpdate != null && listUpdate.size() > 0) {
            list.clear();

            final int tempMargin = Utils.getDimenInPx(R.dimen.dimen_20_40) * -1;
            for (TreeUserDTO obj : listUpdate) {
                addList(obj, tempMargin, -1);
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
            int k = 0;
            for (int i = 0; i < this.list.size(); i++) {
                if (this.list.get(i).getId() == myId) {
                    k = i;
                    break;
                }
            }
            if (instance != null)
                instance.scrollToEndList(k);
            if (instance_2 != null)
                instance_2.scrollToEndList(k);
            Log.d(TAG, "notifyDataSetChanged");
        }

    }

    public List<TreeUserDTO> getList() {
        return listTemp;
    }

    public AdapterOrganizationChart( NewOrganizationChart instance, InviteUserActivity instance_2, ArrayList<Integer> userNos) {
        this.instance = instance;
        this.instance_2 = instance_2;
        this.mg = Utils.getDimenInPx(R.dimen.dimen_20_40);
        this.myId = Utils.getCurrentId();
        this.userNos = userNos;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_organization_chart_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        TreeUserDTO treeUserDTO = list.get(position);
        holder.handler(treeUserDTO, position);

    }

    void collapse(final int position, final TreeUserDTO treeUserDTO) {
        int levelCur = list.get(position).getLevel();
        Log.d(TAG, "levelCur:" + levelCur);
        int a = position + 1;
        if (a < list.size()) {
            for (int i = a; i < list.size(); i++) {
                TreeUserDTO obj = list.get(i);
                int level = obj.getLevel();
                if (levelCur < level) {
                    Log.d(TAG, "remove: " + obj.getName());
                    list.remove(i);
                    i--;
                } else {
                    break;
                }
            }
            notifyDataSetChanged();
        }


    }

    private void expand(int position, TreeUserDTO treeUserDTO, boolean flag) {
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
                    object.setFlag(true);
                    if (isAdd(this.list, object)) {
                        list.add(index, object);
                        index++;
                    }
                } else {
                    break;
                }
            }
        }

        notifyDataSetChanged();
        if (flag) {
            Log.d(TAG, "scrollToEndList");
            if (instance != null)
                instance.scrollToEndList(position + 1);
            if (instance_2 != null)
                instance_2.scrollToEndList(position + 1);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}