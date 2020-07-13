package com.dazone.crewchatoff.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.Tree.Org_tree;
import com.dazone.crewchatoff.adapter.AdapterOrganizationChart;
import com.dazone.crewchatoff.constant.Constants;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.database.AllUserDBHelper;
import com.dazone.crewchatoff.database.UserDBHelper;
import com.dazone.crewchatoff.dto.BelongDepartmentDTO;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.eventbus.RotationAction;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallBack;
import com.dazone.crewchatoff.interfaces.ICreateOneUserChatRom;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Integer.MAX_VALUE;

/**
 * Created by maidinh on 4/12/2017.
 */

public class InviteUserActivity extends AppCompatActivity {
    private String TAG = "InviteUserActivity";
    private RecyclerView recyclerView;
    private ArrayList<TreeUserDTO> temp = new ArrayList<>();
    private AdapterOrganizationChart mAdapter;
    private LinearLayoutManager mLayoutManager;
    private List<TreeUserDTO> list = new ArrayList<>();

    private long task = -1;
    private ArrayList<Integer> userNos;
    private String oldTitle = "";
    private int currentUserNo = 0;
    public Prefs prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_user_layout);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        getDataFromBundler();
        initView();
        initDB();
        prefs = CrewChatApplication.getInstance().getPrefs();
        rotationSetting();
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    void getDataFromBundler() {
        Bundle bundle1 = getIntent().getExtras();
        if (bundle1 != null) {
            try {
                task = bundle1.getLong(Constant.KEY_INTENT_ROOM_NO);
                userNos = bundle1.getIntegerArrayList(Constant.KEY_INTENT_COUNT_MEMBER);
                oldTitle = bundle1.getString(Constant.KEY_INTENT_ROOM_TITLE);
                list = (List<TreeUserDTO>) getIntent().getSerializableExtra(Constants.LIST_MEMBER);
            } catch (Exception e) {
                task = -1;
                e.printStackTrace();
            }
        }

        if (userNos == null) userNos = new ArrayList<>();

        currentUserNo = new Prefs().getUserNo();

        if (currentUserNo == 0) {
            currentUserNo = UserDBHelper.getUser().Id;
        }
    }

    void initView() {
        recyclerView = findViewById(R.id.rv);
        InviteUserActivity instance = this;
        mAdapter = new AdapterOrganizationChart(null, instance, userNos);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
    }

    public void scrollToEndList(int size) {
        recyclerView.smoothScrollToPosition(size);
    }

    void initDB() {
        if (list != null && list.size() > 0) {
            mAdapter.updateList(list);
        } else {
            Toast.makeText(getApplicationContext(), "Can not get list user, restart app please", Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe
    public void rotationActionRc(RotationAction rotationAction) {
        rotationSetting();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_invite_user, menu);

        MenuItem myActionMenuItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setMaxWidth(MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.length() == 0) {
                    mAdapter.updateIsSearch(0);
                    updateCurrentList();
                } else {
                    mAdapter.updateIsSearch(1);
                    Log.d(TAG, "onQueryTextChange:" + s);
                    mAdapter.actionSearch(s);
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_add:
                addToGroupChat();
                Log.d(TAG, "menu_add");
                break;
            case R.id.menu_search:
                lstCurrent = mAdapter.getCurrentList();
                Log.d(TAG, "menu_search");
                break;
        }
        return false;
    }

    ArrayList<TreeUserDTO> getListDTO(List<TreeUserDTO> lst) {
        ArrayList<TreeUserDTO> dtoList = new ArrayList<>();
        for (TreeUserDTO obj : lst) {
            if (obj.isCheck() && obj.getType() == 2)
                dtoList.add(obj);
        }
        return dtoList;
    }

    void addToGroupChat() {
        if (task == -1) {
            finish();
        } else {
            ArrayList<TreeUserDTO> list = getListDTO(mAdapter.getList());
            if (list != null && list.size() > 0) {
                if (userNos != null && userNos.size() == 2) {
                    List<Integer> listUserNos = new ArrayList<>();
                    for (int i : userNos) {
                        if (i != currentUserNo) {
                            listUserNos.add(i);
                        }
                    }
                    for (TreeUserDTO treeUserDTO : list) {
                        boolean idAdd = true;
                        for (int i : listUserNos) {
                            if (i == treeUserDTO.getId()) {
                                idAdd = false;
                                break;
                            }
                        }
                        if (idAdd) {
                            listUserNos.add(treeUserDTO.getId());
                            // Combine title for new user added to group
                            TreeUserDTOTemp temp = AllUserDBHelper.getAUser(treeUserDTO.getId());
                            if (temp != null && treeUserDTO.getId() != currentUserNo) {
                                oldTitle += "," + temp.getName();
                            }
                        }
                    }

                    if (listUserNos.size() == 1) {
                        Utils.showMessageShort("User has been added");
                        finish();
                    } else {
                        HttpRequest.getInstance().CreateGroupChatRoom(listUserNos, new ICreateOneUserChatRom() {
                            @Override
                            public void onICreateOneUserChatRomSuccess(ChattingDto chattingDto) {

                                // Update room title here
                                HttpRequest.getInstance().updateChatRoomInfo((int) chattingDto.getRoomNo(), oldTitle, new BaseHTTPCallBack() {
                                    @Override
                                    public void onHTTPSuccess() {
                                    }

                                    @Override
                                    public void onHTTPFail(ErrorDto errorDto) {
                                    }
                                });

                                // Start new activity
                                Intent intent = new Intent();
                                intent.putExtra(Constant.KEY_INTENT_CHATTING_DTO, chattingDto);
                                intent.putExtra(Constant.KEY_INTENT_ROOM_TITLE, oldTitle);
                                setResult(Constant.INTENT_RESULT_CREATE_NEW_ROOM, intent);
                                finish();
                            }

                            @Override
                            public void onICreateOneUserChatRomFail(ErrorDto errorDto) {
                                Utils.showMessageShort("Fail");
                            }
                        });
                    }
                } else {
                    Log.d(TAG, "2 userNos != null");
                    if (userNos != null) {
                        for (int i : userNos) {
                            for (TreeUserDTO treeUserDTO : list) {
                                if (treeUserDTO.getId() == i) {
                                    list.remove(treeUserDTO);
                                    break;
                                }
                            }
                        }


                        if (list.size() > 0) {
                            final ArrayList<Integer> test = new ArrayList<>();

                            for (TreeUserDTO treeUserDTO : list) {
                                test.add(treeUserDTO.getId());
                            }

                            HttpRequest.getInstance().AddChatRoomUser(list, task, new BaseHTTPCallBack() {
                                @Override
                                public void onHTTPSuccess() {
                                    Bundle conData = new Bundle();
                                    conData.putInt(Statics.CHATTING_DTO_ADD_USER_NEW, 1);
                                    conData.putIntegerArrayList(Constant.KEY_INTENT_USER_NO_ARRAY, test);
                                    Intent intent = new Intent();
                                    intent.putExtras(conData);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }

                                @Override
                                public void onHTTPFail(ErrorDto errorDto) {
                                    Utils.showMessage("Now, Only apply for group!");
                                }
                            });
                        } else {
                            Utils.showMessage("Added.");
                            finish();
                        }
                    } else {
                        Utils.showMessage("Now, Only apply for group!");
                        finish();
                    }
                }
            }
        }
    }

    List<TreeUserDTO> lstCurrent = new ArrayList<>();

    void updateCurrentList() {
        if (lstCurrent != null && lstCurrent.size() > 0) {
            mAdapter.updateListSearch(lstCurrent);
        }
    }

    public void rotationSetting() {
        int rotation = prefs.getIntValue(Statics.SCREEN_ROTATION, Constant.PORTRAIT);

        switch (rotation) {
            case Constant.AUTOMATIC:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                break;
            case Constant.PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case Constant.LANSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
    }
}
