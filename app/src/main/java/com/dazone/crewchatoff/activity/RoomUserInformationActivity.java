package com.dazone.crewchatoff.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.adapter.RoomUserInfoAdapter;
import com.dazone.crewchatoff.constant.Constants;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.database.AllUserDBHelper;
import com.dazone.crewchatoff.dto.BelongDepartmentDTO;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.eventbus.NotifyAdapterOgr;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.fragment.CurrentChatListFragment;
import com.dazone.crewchatoff.fragment.RecentFavoriteFragment;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.Serializable;
import java.util.ArrayList;

public class RoomUserInformationActivity extends BaseActivity {
    protected TextView toolbar_title;
    protected ImageView ivBack;
    private ArrayList<Integer> userNos;
    private RoomUserInfoAdapter mAdapter;
    public RecyclerView rvMainList;
    public RecyclerView.LayoutManager layoutManager;
    private ArrayList<TreeUserDTO> temp = new ArrayList<>();
    String TAG = "RoomUserInformationActivity";
    private String roomTitle = "";
    long roomNo = -1;
    int myID;
    ArrayList<TreeUserDTOTemp> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_user_information);
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }

        init();

        // Get bundle data
        Intent intent = getIntent();

        if (intent != null) {
            roomTitle = intent.getStringExtra("roomTitle");
            userNos = intent.getIntegerArrayListExtra("userNos");

            try {
                roomNo = intent.getLongExtra(Constant.KEY_INTENT_ROOM_NO, -1);

            } catch (Exception e) {
                e.printStackTrace();
            }


            if (CompanyFragment.instance != null) users = CompanyFragment.instance.getUser();
            if (users == null) users = AllUserDBHelper.getUser_v2();
            if (users == null) users = new ArrayList<>();

            myID = Utils.getCurrentId();

            // init data
            if (userNos != null) {
                @SuppressLint("StringFormatMatches") String subtitle = CrewChatApplication.getInstance().getResources().getString(R.string.room_info_participant_count, userNos.size());
                toolbar_title.setText(subtitle);


                temp = getLst(myID, users, userNos);

                mAdapter = new RoomUserInfoAdapter(this, temp, rvMainList);
                rvMainList.setAdapter(mAdapter);
            }
        }
    }

    @Subscribe
    public void notifyAdapter(NotifyAdapterOgr notifyAdapterOgr) {
        mAdapter.notifyDataSetChanged();
    }

    ArrayList<TreeUserDTO> getLst(int myID, ArrayList<TreeUserDTOTemp> users, ArrayList<Integer> userNos) {
        ArrayList<TreeUserDTO> temp = new ArrayList<>();
        TreeUserDTO myUser = null;
        for (Integer userId : userNos) {
            for (TreeUserDTOTemp treeUserDTOTemp : users) {
                if (userId == treeUserDTOTemp.getUserNo()) {
                    if (treeUserDTOTemp.getBelongs() != null) {

                        String positionName = "";
                        String dutyName = "";
                        for (BelongDepartmentDTO belong : treeUserDTOTemp.getBelongs()) {
                            if (TextUtils.isEmpty(positionName)) {
                                positionName += belong.getPositionName();
                            } else {
                                positionName = "";
                                positionName += belong.getPositionName();
                            }
                            if (TextUtils.isEmpty(dutyName)) {
                                dutyName += belong.getDutyName();
                            } else {
                                dutyName = "";
                                dutyName += belong.getDutyName();
                            }
                        }

                        TreeUserDTO treeUserDTO = new TreeUserDTO(
                                dutyName,
                                treeUserDTOTemp.getName(),
                                treeUserDTOTemp.getNameEN(),
                                treeUserDTOTemp.getCellPhone(),
                                treeUserDTOTemp.getAvatarUrl(),
                                positionName,
                                treeUserDTOTemp.getType(),
                                treeUserDTOTemp.getStatus(),
                                treeUserDTOTemp.getUserNo(),
                                treeUserDTOTemp.getDepartNo(),
                                treeUserDTOTemp.getUserStatusString()
                        );


                        if (userId == myID) {
                            if (myUser != null) {
                                temp.add(myUser);
                                myUser = treeUserDTO;
                            } else {
                                myUser = treeUserDTO;
                            }
                        } else {
                            temp.add(treeUserDTO);
                        }
                    }
                }
            }
        }

        if (myUser != null) {
            temp.add(0, myUser);
        }
        if (temp == null) {
            temp = new ArrayList<>();
        }
        return temp;
    }

    private void init() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());
        toolbar_title = findViewById(R.id.toolbar_title);
        rvMainList = findViewById(R.id.rv_main);

        rvMainList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        rvMainList.setLayoutManager(layoutManager);
    }

    void actionAdd() {
        if (roomNo != -1) {
            final Intent intent = new Intent(this, InviteUserActivity.class);
            intent.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo);
            intent.putExtra(Constant.KEY_INTENT_COUNT_MEMBER, userNos);
            intent.putExtra(Constant.KEY_INTENT_ROOM_TITLE, roomTitle);
            intent.putExtra(Constants.LIST_MEMBER, (Serializable) CompanyFragment.instance.getSubordinates());
            startActivityForResult(intent, Statics.ADD_USER_SELECT);
        } else {
            Toast.makeText(getApplicationContext(), "Can not get roomNo", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_user_from_room_infor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_add:
                actionAdd();
                break;
        }

        return true;
    }


    private void activityResultAddUser(Intent data) {
        try {
            Bundle bc = data.getExtras();
            if (bc != null) {
                ArrayList<Integer> userNosAdded = bc.getIntegerArrayList(Constant.KEY_INTENT_USER_NO_ARRAY);

                ArrayList<Integer> lstNew = new ArrayList<>();
                if (userNosAdded != null) {
                    for (int i : userNosAdded) {
                        if (Constant.isAddUser(userNos, i)) {
                            userNos.add(i);
                            lstNew.add(i);
                        }
                    }
                    temp = getLst(myID, users, userNos);
                    if (temp != null) {
                        mAdapter.updateData(temp);
                    }
                    String subtitle = "";
                    try {
                        subtitle = CrewChatApplication.getInstance().getResources().getString(R.string.room_info_participant_count, userNos.size());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (subtitle.length() > 0 && toolbar_title != null) {
                        toolbar_title.setText(subtitle);
                    }

                    if (ChattingActivity.instance != null) {
                        ChattingActivity.instance.activityResultAddUser(data);
                    } else {
                        if (CurrentChatListFragment.fragment != null) {
                            CurrentChatListFragment.fragment.updateWhenAddUser(roomNo, lstNew);
                        }
                        if (RecentFavoriteFragment.instance != null) {
                            RecentFavoriteFragment.instance.updateWhenAddUser(roomNo, lstNew);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Statics.ADD_USER_SELECT:
                    activityResultAddUser(data);
                    break;
            }
        } else if (resultCode == Constant.INTENT_RESULT_CREATE_NEW_ROOM) {
            try {
                Bundle bc = data.getExtras();
                if (bc != null) {
                    finish();
                    if (ChattingActivity.instance != null) {
                        ChattingActivity.instance.newRoom(bc);
                    } else {
                        ChattingDto chattingDto = (ChattingDto) bc.getSerializable(Constant.KEY_INTENT_CHATTING_DTO);
                        Intent intent = new Intent(this, ChattingActivity.class);
                        intent.putExtra(Statics.CHATTING_DTO, chattingDto);
                        intent.putExtra(Constant.KEY_INTENT_ROOM_NO, chattingDto.getRoomNo());
                        intent.putExtra(Constant.KEY_INTENT_ROOM_TITLE, bc.getStringArrayList(Constant.KEY_INTENT_ROOM_TITLE));
                        startActivity(intent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}