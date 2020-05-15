package com.dazone.crewchatoff.test;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.TestMultiLevelListview.MultilLevelListviewFragment;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.Tree.Org_tree;
import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.activity.ProfileUserActivity;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.utils.Statics;
import com.dazone.crewchatoff.database.DepartmentDBHelper;
import com.dazone.crewchatoff.database.FavoriteGroupDBHelper;
import com.dazone.crewchatoff.database.FavoriteUserDBHelper;
import com.dazone.crewchatoff.dto.BelongDepartmentDTO;
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
import com.dazone.crewchatoff.utils.DialogUtils;
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

public class OrganizationView2 implements View.OnClickListener {
    String TAG = "OrganizationView2";
    private ArrayList<TreeUserDTO> mPersonList = new ArrayList<>();
    private ArrayList<TreeUserDTO> temp = new ArrayList<>();
    private Context mContext;
    private int displayType = 0; // 0 folder structure , 1
//    private ArrayList<TreeUserDTOTemp> listTemp = AllUserDBHelper.getUser();
private ArrayList<TreeUserDTOTemp> listTemp = new ArrayList<>();
    private ArrayList<TreeUserDTO> selectedPersonList;
    private HashMap<Integer, ArrayList<StatusViewDto>> statusList = new HashMap<>();
    private ProgressBar mProgressBar;
    private CompanyFragment companyFragment;

    // to build tree
    private ArrayList<TreeUserDTO> mDepartmentList;
    private ViewGroup mViewGroup;

    private static int CODE_BUILD_TREE_OFFLINE = 5;

    public void syncStatus() {
        // Get all of user again
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<TreeUserDTOTemp> currentUserList =null;

                if (CompanyFragment.instance != null) currentUserList = CompanyFragment.instance.getUser();
                if (currentUserList == null) currentUserList = new ArrayList<>();



                Message message = Message.obtain();
                message.what = 4;

                Bundle args = new Bundle();
                args.putParcelableArrayList("listUsers", currentUserList);
                message.setData(args);
                mHandler.sendMessage(message);
            }
        }).start();
    }

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
                mProgressBar.setVisibility(View.GONE);
                buildTree(mDepartmentList, mViewGroup, false);
            }
        }
    };

    public OrganizationView2(Context context, ArrayList<TreeUserDTO> selectedPersonList, boolean isDisplaySelectedOnly, final ViewGroup viewGroup, ProgressBar progressBar, CompanyFragment companyFragment) {
        this.mContext = context;
        this.mProgressBar = progressBar;
        this.mViewGroup = viewGroup;
        this.companyFragment = companyFragment;

        new Thread(new Runnable() {
            @Override
            public void run() {
                initWholeOrganization(viewGroup);
            }
        }).start();

        // Get Favorite Group
        new Thread(new Runnable() {
            @Override
            public void run() {
                getGroupFromServer();
            }
        }).start();
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

    private void initWholeOrganization(ViewGroup viewGroup) {
        mViewGroup = viewGroup;

        if (listTemp != null && listTemp.size() > 0) {
            // Check network is connected
            mDepartmentList = CrewChatApplication.listDeparts;

            // Get from database
            if (mDepartmentList == null) {
                Log.d(TAG, "mDepartmentList == null");
                // Get Online
                if (Utils.isNetworkAvailable()) {
                    getListDepartment(viewGroup);
                }
            } else { // mDepartment is not null, build it
                // ok, let's build the tree
                // send to handler to build it
                Log.d(TAG, "mDepartmentList not null");
                mHandler.obtainMessage(CODE_BUILD_TREE_OFFLINE).sendToTarget();
            }

        } else {
            getListAllUser(viewGroup);
        }
    }

    private void getListAllUser(final ViewGroup viewGroup) {
        Log.d(TAG,"URL_GET_ALL_USER_BE_LONGS 7");
//        HttpRequest.getInstance().GetListOrganize(new IGetListOrganization() {
//            @Override
//            public void onGetListSuccess(ArrayList<TreeUserDTOTemp> treeUserDTOs) {
//                AllUserDBHelper.addUser(treeUserDTOs);
//                Log.d(TAG,"addUser 7");
//                listTemp = treeUserDTOs;
//                getListDepartment(viewGroup);
//            }
//
//            @Override
//            public void onGetListFail(ErrorDto dto) {
//            }
//        });
    }

    private void getListDepartment(final ViewGroup viewGroup) {
        Log.d(TAG, "URL_GET_DEPARTMENT 8");
//        HttpRequest.getInstance().GetListDepart(new IGetListDepart() {
//            @Override
//            public void onGetListDepartSuccess(final ArrayList<TreeUserDTO> treeUserDTOs) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        DepartmentDBHelper.addDepartment(treeUserDTOs);
//                    }
//                }).start();
//
//                // Call function to build tree
//                buildTree(treeUserDTOs, viewGroup, true);
//
//                // Finish draw view, hide progressBar here
//                mProgressBar.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onGetListDepartFail(ErrorDto dto) {
//                mProgressBar.setVisibility(View.GONE);
//            }
//        });
    }

    private void buildTree(final ArrayList<TreeUserDTO> treeUserDTOs, ViewGroup viewGroup, boolean isFromServer) {
        // If Department ok, let's build tree, else do nothing
        if (treeUserDTOs != null) {

            // is from server data is not processing before build tree
            // isFromServer = true;
            if (isFromServer) {
                convertData(treeUserDTOs);
            } else {
                // add data offline to temp
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

                        treeUserDTO.setCompanyNumber(treeUserDTOTemp.getCompanyPhone());
                        temp.add(treeUserDTO);
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

            if (dto != null) {
//                Log.d(TAG, "dto.getSubordinates():" + dto.getSubordinates().size());
//                companyFragment.updateListCompany(dto.getSubordinates());
                for (TreeUserDTO treeUserDTO : dto.getSubordinates()) {
                    treeUserDTO.setParent(dto.getId());
                    draw(treeUserDTO, viewGroup, false, 0);
                }
            }
        }
    }


    private boolean checkDepartExist(ArrayList<TreeUserDTO> listDepart, long departNo) {
        for (TreeUserDTO depart : listDepart) {
            if (depart.getId() == departNo) {
                return true;
            }
        }
        return false;
    }

    private void draw(final TreeUserDTO treeUserDTO, final ViewGroup layout, final boolean checked, final int iconMargin) {
//        Log.d(TAG, treeUserDTO.getName());
        final LinearLayout layoutMain, lnDepartment;
        final LinearLayout child_list;
        final LinearLayout iconWrapper;
        final LinearLayout lnPhone;
        final ImageView avatar;
        final ImageView folderIcon;
        final ImageView ivUserStatus;
        final TextView name, position, tvPhone1, tvPhone2;
        final RelativeLayout relAvatar;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.row_organization2, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(view);

        layoutMain = (LinearLayout) view.findViewById(R.id.item_org_main_wrapper);
        lnDepartment = (LinearLayout) view.findViewById(R.id.item_org_wrapper);
        child_list = (LinearLayout) view.findViewById(R.id.child_list);
        avatar = (ImageView) view.findViewById(R.id.avatar);
        folderIcon = (ImageView) view.findViewById(R.id.ic_folder);
        relAvatar = (RelativeLayout) view.findViewById(R.id.relAvatar);
        iconWrapper = (LinearLayout) view.findViewById(R.id.icon_wrapper);
        ivUserStatus = (ImageView) view.findViewById(R.id.status_imv);
        tvPhone1 = (TextView) view.findViewById(R.id.tv_phone_1);
        tvPhone2 = (TextView) view.findViewById(R.id.tv_phone_2);
        lnPhone = (LinearLayout) view.findViewById(R.id.ln_phone);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) iconWrapper.getLayoutParams();

        if (displayType == 0) // set margin for icon if it's company type
        {
            params.leftMargin = iconMargin;
        }

        iconWrapper.setLayoutParams(params);
        name = (TextView) view.findViewById(R.id.name);
        position = (TextView) view.findViewById(R.id.position);
        String nameString = treeUserDTO.getName();
        String namePosition = treeUserDTO.getPosition();

        if (treeUserDTO.getType() == 2) {
            int status = treeUserDTO.getStatus();

            if (status == Statics.USER_LOGIN) {
                ivUserStatus.setImageResource(R.drawable.home_big_status_01);
            } else if (status == Statics.USER_AWAY) {
                ivUserStatus.setImageResource(R.drawable.home_big_status_02);
            } else { // Logout state
                ivUserStatus.setImageResource(R.drawable.home_big_status_03);
            }

            if (TextUtils.isEmpty(treeUserDTO.getPhoneNumber())) {
                tvPhone1.setVisibility(View.GONE);
            } else {
                tvPhone1.setText(treeUserDTO.getPhoneNumber());
            }

            if (TextUtils.isEmpty(treeUserDTO.getCompanyNumber())) {
                tvPhone2.setVisibility(View.GONE);
            } else {
                tvPhone2.setText(treeUserDTO.getCompanyNumber());
            }


            String url = new Prefs().getServerSite() + treeUserDTO.getAvatarUrl();
            ImageUtils.showCycleImageFromLink(url, avatar, R.dimen.button_height);

            position.setVisibility(View.VISIBLE);
            position.setText(namePosition);
            folderIcon.setVisibility(View.GONE);
            relAvatar.setVisibility(View.VISIBLE);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(BaseActivity.Instance, ProfileUserActivity.class);
                    intent.putExtra(Constant.KEY_INTENT_USER_NO, treeUserDTO.getId());
                    BaseActivity.Instance.startActivity(intent);
                }
            });

            layoutMain.setTag(treeUserDTO);
            layoutMain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    TreeUserDTO userInfo = (TreeUserDTO) v.getTag();
                    String strName = userInfo.getName();
                    String strPhoneNumber = userInfo.getPhoneNumber();
                    String strCompanyNumber = userInfo.getCompanyNumber();
                    int userNo = userInfo.getId();
                    DialogUtils.showDialogActionUser(
                            mContext,
                            strName,
                            strPhoneNumber,
                            strCompanyNumber,
                            userNo);
                    //String strCompanyNumber = u
                    //DialogUtils.showDialogUser();
                    return false;
                }
            });

//            lnPhone.setVisibility(View.GONE);

            /*tvPhoneNumber.setText(treeUserDTO.getPhoneNumber());
            tvWorkPhone.setText(treeUserDTO.getCompanyNumber());*/

        } else {
            position.setVisibility(View.GONE);
            relAvatar.setVisibility(View.GONE);
            folderIcon.setVisibility(View.VISIBLE);
            tvPhone1.setVisibility(View.GONE);
            lnPhone.setVisibility(View.GONE);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    DepartmentDBHelper.addDepartment(treeUserDTO);
                }
            }).start();
        }

        name.setText(nameString);

        final int tempMargin = iconMargin + Utils.getDimenInPx(R.dimen.dimen_20_40);

        String temp = treeUserDTO.getId() + treeUserDTO.getName() + Utils.getCurrentId();

        if (!TextUtils.isEmpty(temp)) {
            if (new Prefs().getBooleanValue(temp, true)) {
                folderIcon.setImageResource(R.drawable.home_folder_open_ic);
                child_list.setVisibility(View.VISIBLE);
            } else {
                folderIcon.setImageResource(R.drawable.home_folder_close_ic);
                child_list.setVisibility(View.GONE);
            }
        }

        lnDepartment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "setOnClickListener");
                selectedPersonList = new ArrayList<>();
                getSelectedPersonList(treeUserDTO);
//                Log.d(TAG, new Gson().toJson(treeUserDTO));
                createChatRoom(selectedPersonList);
            }
        });


        lnDepartment.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG, "setOnLongClickListener");
                if (treeUserDTO.getType() == Statics.TYPE_DEPART) {
                    showMenuDepartment(child_list, folderIcon, treeUserDTO);
                } else {
                    int myId = Utils.getCurrentId();

                    if (treeUserDTO.getId() != myId) {
                        showMenu(treeUserDTO);
                    }
                }

                return true;
            }
        });

        if (treeUserDTO.getSubordinates() != null && treeUserDTO.getSubordinates().size() != 0) {
            folderIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHideSubMenuView(child_list, folderIcon, treeUserDTO);
                }
            });

            // sort data by order
            boolean hasType2 = false;
            boolean hasType0 = false;

            for (TreeUserDTO dto : treeUserDTO.getSubordinates()) {
                if (dto.getType() == 2) {
                    hasType2 = true;
                }

                if (dto.getType() == 0) {
                    hasType0 = true;
                }
            }

            if (hasType2 && hasType0) {
                Collections.sort(treeUserDTO.getSubordinates(), new Comparator<TreeUserDTO>() {
                    @Override
                    public int compare(TreeUserDTO r1, TreeUserDTO r2) {
                        return r1.getmSortNo() - r2.getmSortNo();
                    }
                });
            }

            long startTime = System.currentTimeMillis();

            for (TreeUserDTO dto1 : treeUserDTO.getSubordinates()) {
                dto1.setParent(treeUserDTO.getId());
                draw(dto1, child_list, false, tempMargin);
            }

            long estimatedTime = System.currentTimeMillis() - startTime;
            Log.d("lchTest", "draw(): " + estimatedTime + " ms");
        } else {
            folderIcon.setImageResource(R.drawable.home_folder_close_ic);
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

    private void showMenuDepartment(final LinearLayout childList, final ImageView folderIcon, final TreeUserDTO userInfo) {
        Resources res = mContext.getResources();
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(mContext);
        builderSingle.setTitle(userInfo.getName());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mContext, R.layout.row_chatting_call);

        // final FavoriteUserDto user = FavoriteUserDBHelper.isFavoriteUser(userInfo.getId());
        // final boolean isFavorite = (user != null);

        // Check is hide or visible by check childList is visible
        if (childList.getVisibility() != View.VISIBLE) {
            arrayAdapter.add(res.getString(R.string.organization_expand_all));
        } else {
            arrayAdapter.add(res.getString(R.string.organization_collapse_all));
        }

        arrayAdapter.add(res.getString(R.string.organization_group_chat));

        /*if (isFavorite){
            arrayAdapter.add(res.getString(R.string.organization_remove_favorite));
        }else{
            arrayAdapter.add(res.getString(R.string.organization_add_favorite));
        }*/

        arrayAdapter.add(res.getString(R.string.organization_add_favorite));

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Check close app/ expand all
                            showHideSubMenuView(childList, folderIcon, userInfo);
                        } else if (which == 1) {
                            // Go to chatting1
                            selectedPersonList = new ArrayList<>();
                            getSelectedPersonList(userInfo);
                            createChatRoom(selectedPersonList);
                        } else if (which == 2) {
                            // Get selected person list
                            selectedPersonList = new ArrayList<>();
                            getSelectedPersonList(userInfo);

                            // Show choose group
                            chooseGroup();
                            // Add to favorite
                            // Refresh tab favorite if tab visible
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

    private void showMenu(final TreeUserDTO userInfo) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(mContext);
        builderSingle.setTitle(userInfo.getName());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                mContext,
                R.layout.row_chatting_call);

        final FavoriteUserDto user = FavoriteUserDBHelper.isFavoriteUser(userInfo.getId());
        final boolean isFavorite = (user != null);

        Resources res = mContext.getResources();
        arrayAdapter.add(res.getString(R.string.chatting));

        /*if (isFavorite){
            arrayAdapter.add(res.getString(R.string.favorite_remove));
        }else{
            arrayAdapter.add(res.getString(R.string.favorite_add));
        }*/

        arrayAdapter.add(res.getString(R.string.favorite_add));
        arrayAdapter.add(res.getString(R.string.view_profile));

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Go to chatting1
                            selectedPersonList = new ArrayList<>();
                            getSelectedPersonList(userInfo);
                            createChatRoom(selectedPersonList);
                        } else if (which == 1) {
                            /*if (isFavorite){ // remove from favorite
                                HttpRequest.getInstance().deleteFavoriteUser(user.getGroupNo(), user.getUserNo(), new BaseHTTPCallbackWithJson() {
                                    @Override
                                    public void onHTTPSuccess(String jsonData) {
                                        // update local data, delete user from favorite table
                                        Utils.printLogs("Remove user from favorite successfully JSON = "+jsonData);
                                        FavoriteUserDBHelper.deleteFavoriteUser(user.getGroupNo(), user.getUserNo());

                                        // Show message
                                        String msg = CrewChatApplication.getInstance().getResources().getString(R.string.favorite_remove_success);
                                        Toast.makeText(CrewChatApplication.getInstance(), msg, Toast.LENGTH_LONG).show();
                                        // Send broadcast to favorite tab to delete
                                        MultilLevelListviewFragment instance = MultilLevelListviewFragment.instance;
                                        if (instance != null && instance.isVisible()){
                                            instance.removeFavoriteUser(user.getUserNo());
                                        }
                                    }

                                    @Override
                                    public void onHTTPFail(ErrorDto errorDto) {

                                    }
                                });
                            } else { // Add to favorite
                                chooseGroup(userInfo.getId());
                            }*/

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

    private void createChatRoom(final ArrayList<TreeUserDTO> selectedPersonList) {
        if (selectedPersonList.size() == 1) {
            if (selectedPersonList.get(0).getId() == Utils.getCurrentId()) {
                Utils.showMessage(Utils.getString(R.string.can_not_chat));
            } else {
                HttpRequest.getInstance().CreateOneUserChatRoom(selectedPersonList.get(0).getId(), new ICreateOneUserChatRom() {
                    @Override
                    public void onICreateOneUserChatRomSuccess(ChattingDto chattingDto) {
                        Intent intent = new Intent(BaseActivity.Instance, ChattingActivity.class);
                        intent.putExtra(Statics.TREE_USER_PC, selectedPersonList.get(0));
                        intent.putExtra(Statics.CHATTING_DTO, chattingDto);
                        intent.putExtra(Constant.KEY_INTENT_ROOM_NO, chattingDto.getRoomNo());
                        BaseActivity.Instance.startActivity(intent);
                    }

                    @Override
                    public void onICreateOneUserChatRomFail(ErrorDto errorDto) {
                        Utils.showMessageShort("Fail");
                    }
                });
            }
        } else if (selectedPersonList.size() > 1) {
            HttpRequest.getInstance().CreateGroupChatRoom(selectedPersonList, new ICreateOneUserChatRom() {
                @Override
                public void onICreateOneUserChatRomSuccess(ChattingDto chattingDto) {
                    Intent intent = new Intent(BaseActivity.Instance, ChattingActivity.class);
                    intent.putExtra(Statics.CHATTING_DTO, chattingDto);
                    intent.putExtra(Constant.KEY_INTENT_ROOM_NO, chattingDto.getRoomNo());
                    BaseActivity.Instance.startActivity(intent);
                }

                @Override
                public void onICreateOneUserChatRomFail(ErrorDto errorDto) {
                    Utils.showMessageShort("Fail");
                }
            },"");
        }
    }

    private void showHideSubMenuView(LinearLayout child_list, ImageView icon, TreeUserDTO treeUserDTO) {
        String temp = treeUserDTO.getId() + treeUserDTO.getName() + Utils.getCurrentId();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ic_folder:
                break;
        }
    }

    private void chooseGroup() {
        // get group from client
        // request to server to get favorite group on new thread
        getGroupFromClient();
        //getGroupFromServer();
    }

    private void createDialog(final ArrayList<FavoriteGroupDto> groups) {
        String[] AlertDialogItems = new String[groups.size()];

        for (int i = 0; i < groups.size(); i++) {
            AlertDialogItems[i] = groups.get(i).getName();
        }

        AlertDialog popup;
        final ArrayList<FavoriteGroupDto> selectedItems = new ArrayList<>();
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        //mContext.getString(R.string.alert_selectbook_message)
        builder.setTitle(mContext.getResources().getString(R.string.choose_group));

        builder.setMultiChoiceItems(AlertDialogItems, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        if (isChecked) {
                            selectedItems.add(groups.get(indexSelected));
                        } else if (selectedItems.contains(indexSelected)) {
                            selectedItems.remove(indexSelected);
                        }
                    }
                });

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (selectedItems.size() == 0) {
                    String msg = mContext.getResources().getString(R.string.msg_select_item);
                    Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
                    // Show popup again
                } else {
                    // Send user to server
                    insertFavoriteUser(selectedItems);
                    dialog.dismiss();
                }
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        popup = builder.create();
        popup.show();
    }

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
                                FavoriteUserDBHelper.addFavoriteUser(user);
                            }
                        }).start();

                        // Find favorite tab if it is visible
                        MultilLevelListviewFragment instance = MultilLevelListviewFragment.instance;
                        if (instance != null && instance.isVisible()) {
                            instance.addNewFavorite(user);
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

    private void getGroupFromServer() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                HttpRequest.getInstance().getFavotiteGroupAndData(new BaseHTTPCallbackWithJson() {
//                    @Override
//                    public void onHTTPSuccess(String json) {
//                        Type listType = new TypeToken<ArrayList<FavoriteGroupDto>>() {
//                        }.getType();
//                        ArrayList<FavoriteGroupDto> list = new Gson().fromJson(json, listType);
//
//                        Message message = Message.obtain();
//                        message.what = 3;
//
//                        Bundle args = new Bundle();
//                        args.putParcelableArrayList("groupList", list);
//                        message.setData(args);
//                        mHandler.sendMessage(message);
//                    }
//
//                    @Override
//                    public void onHTTPFail(ErrorDto errorDto) {
//                    }
//                });
//            }
//        }).start();

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
}