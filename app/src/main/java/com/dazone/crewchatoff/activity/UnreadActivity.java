package com.dazone.crewchatoff.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.adapter.UnreadAdapter;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.database.AllUserDBHelper;
import com.dazone.crewchatoff.dto.BelongDepartmentDTO;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.dto.UnreadDto;
import com.dazone.crewchatoff.eventbus.NotifyAdapterOgr;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.interfaces.UnreadCallBack;
import com.dazone.crewchatoff.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maidinh on 31-Aug-17.
 */

public class UnreadActivity extends AppCompatActivity {
    private String TAG = "UnreadActivity";
    private Context context;
    private ArrayList<Integer> userNos;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private UnreadAdapter adapter;
    private ArrayList<TreeUserDTOTemp> users;
    private int myId;
    private ProgressBar progressBar;
    private long MessageNo;
    private long ROOM_NO;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unread_layout);
        context = this;
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        init();
        initDb();
    }

    void init() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        MessageNo = intent.getLongExtra(Statics.MessageNo, 0);
        Log.d(TAG,"MessageNo:"+MessageNo);
        ROOM_NO = intent.getLongExtra(Statics.ROOM_NO, 0);
        userNos = intent.getIntegerArrayListExtra("userNos");
        if (userNos == null) return;
        myId = Utils.getCurrentId();
        progressBar = findViewById(R.id.progressBar);
        adapter = new UnreadAdapter(context, new ArrayList<TreeUserDTO>(), myId);
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

    }

    @Subscribe
    public void notifyAdapter(NotifyAdapterOgr notifyAdapterOgr) {
        adapter.notifyDataSetChanged();
    }

    void initDb() {
        new HttpRequest().GetCheckMessageUserList(MessageNo, ROOM_NO, new UnreadCallBack() {
            @Override
            public void onSuccess(List<UnreadDto> list) {
                handler(list);
            }

            @Override
            public void onFail() {

            }
        });

    }

    void handler(List<UnreadDto> list) {
        if (CompanyFragment.instance != null) users = CompanyFragment.instance.getUser();
        if (users == null) users = AllUserDBHelper.getUser_v2();
        if (users == null) users = new ArrayList<>();

        if (users.size() > 0) {
            List<TreeUserDTO> temp = getLst(myId, users, userNos);
            if (temp != null && temp.size() > 0) {
                for (TreeUserDTO obj : temp) {
                    int userId = obj.getId();
                    for (UnreadDto dto : list) {
                        if (userId == dto.UserNo) {
                            if (dto.IsRead) {
                                obj.IsRead = true;
                                obj.ModDate = dto.ModDate;
                                obj.strModDate = dto.strModDate;
                            }
                            break;
                        }
                    }
                }


                adapter.update(temp);
                progressBar.setVisibility(View.GONE);
            }
        }
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
                        treeUserDTO.setCompanyNumber(treeUserDTOTemp.getCompanyPhone());

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return false;
    }
}
