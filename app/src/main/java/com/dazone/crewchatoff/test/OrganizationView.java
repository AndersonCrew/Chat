package com.dazone.crewchatoff.test;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.Tree.Org_tree;
import com.dazone.crewchatoff.adapter.AdapterOrganizationChartFragment;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.database.DepartmentDBHelper;
import com.dazone.crewchatoff.dto.BelongDepartmentDTO;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Sherry on 12/31/15.
 */
public class OrganizationView extends FrameLayout {
    private String TAG = "OrganizationView";
    private ArrayList<TreeUserDTO> mPersonList = new ArrayList<>();
    private ArrayList<TreeUserDTO> mSelectedPersonList;
    private ArrayList<TreeUserDTO> temp = new ArrayList<>();
    private Context mContext;
    private int displayType = 0; // 0 folder structure , 1
    private OnOrganizationSelectedEvent mSelectedEvent;
    private ArrayList<TreeUserDTOTemp> listTemp;
    private int myId = Utils.getCurrentId();
    private boolean mIsDisableSelected = false;
    private ArrayList<TreeUserDTO> mDepartmentList;
    private ArrayList<TreeUserDTO> originalList;
    private ViewGroup rootView;
    private TreeUserDTO originalTree;
    private TreeUserDTO currentTree;
    private boolean firstTime = true;

    public OrganizationView(Context context, ArrayList<TreeUserDTO> selectedPersonList, boolean isDisableSelected, ViewGroup viewGroup) {
        super(context);
        Log.d(TAG, "selectedPersonList:" + selectedPersonList.size());
        this.mContext = context;

        this.mIsDisableSelected = isDisableSelected;
        if (selectedPersonList != null)
            this.mSelectedPersonList = selectedPersonList;
        else
            this.mSelectedPersonList = new ArrayList<>();

        rootView = viewGroup;
        initWholeOrganization(viewGroup);
    }

    public void convertData(List<TreeUserDTO> treeUserDTOs) {
        if (treeUserDTOs != null && treeUserDTOs.size() != 0) {
            for (TreeUserDTO dto : treeUserDTOs) {
                if (dto.getSubordinates() != null && dto.getSubordinates().size() > 0) {
                    temp.add(dto);
                    convertData(dto.getSubordinates());
                } else {
                    temp.add(dto);
                }
            }
        }
    }

    private void initWholeOrganization(final ViewGroup viewGroup) {
        // build offline version
        // Get offline data
        mDepartmentList = new ArrayList<>();
        listTemp = new ArrayList<>();

        if (CompanyFragment.instance != null) {
            listTemp = CompanyFragment.instance.getUser();
            mDepartmentList = DepartmentDBHelper.getDepartments_v2();
        }
        if (mDepartmentList == null) mDepartmentList = new ArrayList<>();
        if (listTemp == null) listTemp = new ArrayList<>();
        if (mDepartmentList != null && mDepartmentList.size() > 0) {
            buildTree(mDepartmentList, viewGroup, false);
        }
    }

    private void buildTree(final ArrayList<TreeUserDTO> treeUserDTOs, ViewGroup viewGroup, boolean isFromServer) {
        if (treeUserDTOs != null) {
            if (isFromServer) {
                convertData(treeUserDTOs);
            } else {
                temp.clear();
                temp.addAll(treeUserDTOs);
            }

            for (TreeUserDTO treeUserDTO : temp) {
                if (treeUserDTO.getSubordinates() != null && treeUserDTO.getSubordinates().size() > 0) {
                    treeUserDTO.setSubordinates(null);
                }
            }

            // sort data by order
            Collections.sort(temp, new Comparator<TreeUserDTO>() {
                @Override
                public int compare(TreeUserDTO r1, TreeUserDTO r2) {
                    if (r1.getmSortNo() > r2.getmSortNo()) {
                        return 1;
                    } else if (r1.getmSortNo() == r2.getmSortNo()) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });

            for (TreeUserDTOTemp treeUserDTOTemp : listTemp) {
                if (treeUserDTOTemp.getBelongs() != null) {
                    for (BelongDepartmentDTO belong : treeUserDTOTemp.getBelongs()) {
                        TreeUserDTO treeUserDTO = new TreeUserDTO(
                                treeUserDTOTemp.getName(),
                                treeUserDTOTemp.getNameEN(),
                                treeUserDTOTemp.getCellPhone(),
                                treeUserDTOTemp.getAvatarUrl(),
                                belong.getPositionName(),
                                treeUserDTOTemp.getType(),
                                treeUserDTOTemp.getStatus(),
                                treeUserDTOTemp.getUserNo(),
                                belong.getDepartNo(),
                                treeUserDTOTemp.getUserStatusString(),
                                belong.getPositionSortNo()
                        );

                        treeUserDTO.DutyName = belong.getDutyName();
                        treeUserDTO.setCompanyNumber(treeUserDTOTemp.getCompanyPhone());

                        for (TreeUserDTO u : mSelectedPersonList) {
                            if (treeUserDTOTemp.getUserNo() == u.getId()) {
                                treeUserDTO.setIsCheck(true);
                                break;
                            }
                        }

                        if (isAdd(temp, treeUserDTO)) {
                            temp.add(treeUserDTO);
                        }
                    }
                }
            }

            mPersonList = new ArrayList<>(temp);
            TreeUserDTO dto = null;
            try {
                dto = Org_tree.buildTree(mPersonList);
            } catch (Exception e) {
                e.printStackTrace();
            }


            resortTree(dto);
            if (firstTime) {
                originalTree = dto;
                originalList = mPersonList;
                firstTime = false;
            }
            if (dto != null) {
                for (TreeUserDTO treeUserDTO : dto.getSubordinates()) {
                    draw(treeUserDTO, viewGroup, false, 0);
                }
            }
        }
    }

    private void resortTree(TreeUserDTO dto) {
        if (dto.getSubordinates() != null) {
            if (dto.getSubordinates().size() > 0) {
                boolean hasType2 = false;
                boolean hasType0 = false;

                for (TreeUserDTO tree : dto.getSubordinates()) {
                    if (tree.getType() == 2) {
                        hasType2 = true;
                    }
                    if (tree.getType() == 0) {
                        hasType0 = true;
                    }
                }
                if (hasType2 && hasType0) {
                    Collections.sort(dto.getSubordinates(), new Comparator<TreeUserDTO>() {
                        @Override
                        public int compare(TreeUserDTO r1, TreeUserDTO r2) {
                            return r1.getmSortNo() - r2.getmSortNo();
                        }
                    });
                }
                for (TreeUserDTO dto1 : dto.getSubordinates()) {
                    resortTree(dto1);
                }
            }
        }
    }

    boolean isAdd(List<TreeUserDTO> lst, TreeUserDTO obj) {
        for (TreeUserDTO treeUserDTO : lst) {
            if (obj.getPosition().equals(treeUserDTO.getPosition())
                    && obj.getPositionSortNo() == treeUserDTO.getPositionSortNo()
                    && obj.getParent() == treeUserDTO.getParent()
                    && obj.getId() == treeUserDTO.getId()) {
                return false;
            }
        }
        return true;
    }

    public void setOnSelectedEvent(OnOrganizationSelectedEvent selectedEvent) {
        this.mSelectedEvent = selectedEvent;
    }


    private void draw(final TreeUserDTO treeUserDTO, final ViewGroup layout, final boolean checked, final int iconMargin) {
        final LinearLayout child_list;
        final LinearLayout iconWrapper;
        final ImageView avatar;
        final ImageView folderIcon;
        final ImageView ivStatus;
        final TextView name, position;
        final CheckBox row_check;
        final RelativeLayout relAvatar;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.row_organization, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(view);
        child_list = view.findViewById(R.id.child_list);
        avatar = view.findViewById(R.id.avatar);
        folderIcon = view.findViewById(R.id.ic_folder);
        relAvatar = view.findViewById(R.id.relAvatar);
        iconWrapper = view.findViewById(R.id.icon_wrapper);
        ivStatus = view.findViewById(R.id.status_imv);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) iconWrapper.getLayoutParams();
        if (displayType == 0) // set margin for icon if it's company type
        {
            params.leftMargin = iconMargin;
        }
        iconWrapper.setLayoutParams(params);
        name = view.findViewById(R.id.name);
        position = view.findViewById(R.id.position);
        row_check = view.findViewById(R.id.row_check);

        row_check.setChecked(treeUserDTO.isCheck());

        if (treeUserDTO.isCheck()) {
            if (mIsDisableSelected) {
                row_check.setEnabled(false);
            } else {
                row_check.setEnabled(true);
            }
        }


        String nameString = treeUserDTO.getName();
        String namePosition = treeUserDTO.DutyName;
        if (namePosition.isEmpty())
            namePosition = treeUserDTO.getPosition();

        if (treeUserDTO.getType() == 2) {
            String url = new Prefs().getServerSite() + treeUserDTO.getAvatarUrl();

            //ImageUtils.showCycleImageFromLink(url, avatar, R.dimen.button_height);
//            Log.d(TAG, "url:" + url);
            ImageLoader.getInstance().displayImage(url, avatar, Statics.options2);

            position.setVisibility(View.VISIBLE);
            position.setText(namePosition);
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
            relAvatar.setVisibility(View.GONE);
            folderIcon.setVisibility(View.VISIBLE);
        }
        name.setText(nameString);

        if (treeUserDTO.getId() == myId) {
            row_check.setEnabled(false);
        } /*else {
            row_check.setEnabled(true);
        }*/

        final int tempMargin = iconMargin + Utils.getDimenInPx(R.dimen.dimen_20_40);

        row_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked && treeUserDTO.getType() == 2) {
//                    unCheckFather(dto);
                    ViewGroup parent = ((ViewGroup) layout.getParent());
                    unCheckBoxParent(parent);
                } else {
                    if (buttonView.getTag() != null && !(Boolean) buttonView.getTag()) {
                        buttonView.setTag(true);
                    } else {
                        treeUserDTO.setIsCheck(isChecked);
                        if (treeUserDTO.getSubordinates() != null && treeUserDTO.getSubordinates().size() != 0) {
                            int index = 0;
                            for (TreeUserDTO dto1 : treeUserDTO.getSubordinates()) {

                                dto1.setIsCheck(isChecked);
                                View childView = child_list.getChildAt(index);
                                CheckBox childCheckBox = childView.findViewById(R.id.row_check);
                                if (childCheckBox != null) {
                                    if (childCheckBox.isEnabled()) {
                                        childCheckBox.setChecked(dto1.isCheck());
                                    }

                                } else {
                                    break;
                                }
                                index++;
                            }
                        }
                    }
                }
                if (mSelectedEvent != null) {
                    mSelectedEvent.onOrganizationCheck(isChecked, treeUserDTO);
                }
            }
        });

        String temp = treeUserDTO.getId() + treeUserDTO.getName();
        if (!TextUtils.isEmpty(temp)) {
            if (new Prefs().getBooleanValue(temp, true)) {
                child_list.setVisibility(View.VISIBLE);
            } else {
                child_list.setVisibility(View.GONE);
            }
        }

        if (treeUserDTO.getSubordinates() != null && treeUserDTO.getSubordinates().size() != 0) {
            folderIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHideSubMenuView(child_list, folderIcon, treeUserDTO);
                }
            });
            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHideSubMenuView(child_list, folderIcon, treeUserDTO);
                }
            });

            if (treeUserDTO.getItemName().equalsIgnoreCase("Customer Business Div.")) {
                // sort data by order
                Collections.sort(treeUserDTO.getSubordinates(), new Comparator<TreeUserDTO>() {
                    @Override
                    public int compare(TreeUserDTO r1, TreeUserDTO r2) {
                        if (r1.getmSortNo() > r2.getmSortNo()) {
                            return 1;
                        } else if (r1.getmSortNo() == r2.getmSortNo()) {
                            return 0;
                        } else {
                            return -1;
                        }
                    }
                });
            }


            for (TreeUserDTO dto1 : treeUserDTO.getSubordinates()) {
                draw(dto1, child_list, false, tempMargin);
            }
        }
    }

    private void unCheckBoxParent(ViewGroup view) {
        if (view.getId() == R.id.item_org_main_wrapper || view.getId() == R.id.item_org_wrapper) {
            CheckBox parentCheckBox = view.findViewById(R.id.row_check);
            if (parentCheckBox.isChecked()) {
                parentCheckBox.setTag(false);
                parentCheckBox.setChecked(false);
            }
            if ((view.getParent()).getParent() instanceof ViewGroup) {
                try {
                    ViewGroup parent = (ViewGroup) (view.getParent()).getParent();
                    unCheckBoxParent(parent);
                } catch (Exception e) {
                }
            }
        }
    }

    private void showHideSubMenuView(LinearLayout child_list, ImageView icon, TreeUserDTO treeUserDTO) {
        String temp = treeUserDTO.getId() + treeUserDTO.getName();
        if (child_list.getVisibility() == View.VISIBLE) {
            child_list.setVisibility(View.GONE);
            icon.setImageResource(R.drawable.home_folder_close_ic);
            new Prefs().putBooleanValue(temp, false);

        } else {
            child_list.setVisibility(View.VISIBLE);
            icon.setImageResource(R.drawable.home_folder_open_ic);
            new Prefs().putBooleanValue(temp, true);

        }
    }

    private boolean drawing = false;
    private String currentValue = "";
    private String lastValue = "";

    public void search(String value) {
        currentValue = value;
        if (currentValue.equals(lastValue)) {
            return;
        }
        if (!drawing) {
            drawing = true;
            lastValue = value;
            ArrayList<TreeUserDTO> lst = new ArrayList<>();
            for (TreeUserDTO obj : originalList) {
                if (obj.getType() == 2) {
                    if ((obj.getName().toUpperCase().contains(value.toUpperCase()))
                            || (obj.getPhoneNumber() != null && obj.getPhoneNumber().toUpperCase().contains(value.toUpperCase()))
                            || (obj.getPhoneNumber() != null && obj.getPhoneNumber().toUpperCase().replace("-", "").contains(value.toUpperCase()))
                            || (obj.getCompanyNumber() != null && obj.getCompanyNumber().toUpperCase().contains(value.toUpperCase()))
                            || (obj.getCompanyNumber() != null && obj.getCompanyNumber().toUpperCase().replace("-", "").contains(value.toUpperCase()))) {
                        if (Constant.isAddSearch(lst, obj.getId())) {
                            lst.add(obj);
                        }
                    }
                }
            }

            if (lst == null || lst.size() == 0) {
                rootView.removeAllViews();
                drawing = false;
                return;
            }

            TreeUserDTO dto = null;
            try {
                dto = Org_tree.buildTree(lst);
            } catch (Exception e) {
                e.printStackTrace();
            }


            resortTree(dto);

            if (dto != null) {
                rootView.removeAllViews();
                for (TreeUserDTO treeUserDTO : dto.getSubordinates()) {
                    draw(treeUserDTO, rootView, false, 0);
                }
            }
            drawing = false;
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (drawing) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    search(currentValue);
                }
            }).start();
        }
    }

    public void clearSearch() {
        lastValue = "";
        drawing = false;
        rootView.removeAllViews();
        if (originalTree != null) {
            for (TreeUserDTO treeUserDTO : originalTree.getSubordinates()) {
                draw(treeUserDTO, rootView, false, 0);
            }
        }
    }


    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private AdapterOrganizationChartFragment mAdapter;

    void initView(ViewGroup v) {
        recyclerView = findViewById(R.id.rv_organization);
//        TabOrganizationChartFragment instance = this;
        mAdapter = new AdapterOrganizationChartFragment(mContext, new ArrayList<TreeUserDTO>(), true, null);
        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
//        v.addView(recyclerView);
    }
}
