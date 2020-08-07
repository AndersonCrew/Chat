package com.dazone.crewchatoff.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.Tree.Org_tree;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.adapter.AdapterOrganizationChart;
import com.dazone.crewchatoff.constant.Constants;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.BelongDepartmentDTO;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.eventbus.NotifyAdapterOgr;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.interfaces.ICreateOneUserChatRom;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NewOrganizationChart extends AppCompatActivity {
    private String TAG = "NewOrganizationChart";
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private AdapterOrganizationChart mAdapter;
    private List<TreeUserDTO> list = new ArrayList<>();
    private TextView tvCount;
    private EditText edRoomName;
    private CheckBox cbCreateNewRoom;
    private RelativeLayout layoutRoomName;
    private ImageView ivShare;
    private boolean isNewChat = false;
    private ArrayList<String> mSelectedImage = new ArrayList<>();
    private String typeShare;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_organization_chart_layout);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.create_chat_room));

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        try {
            typeShare = getIntent().getStringExtra(Constants.TYPE_SHARE);
            isNewChat = getIntent().getBooleanExtra(Statics.IS_NEW_CHAT, false);
            mSelectedImage = (ArrayList<String>) getIntent().getSerializableExtra(Constants.LIST_FILE_PATH_SHARE);
            list = (List<TreeUserDTO>) getIntent().getSerializableExtra(Constants.LIST_MEMBER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        isNewChat = true;
        Log.d(TAG, "isNewChat:" + isNewChat);

        setTitle("");
        initView();

        initDB();

    }

    @Subscribe
    public void notifyAdapter(NotifyAdapterOgr notifyAdapterOgr) {
        mAdapter.notifyDataSetChanged();
    }

    public void scrollToEndList(int size) {
        recyclerView.smoothScrollToPosition(size);
    }


    void settextForCount(int n) {
        int sum = 200;
        String s = "";
        int a = sum - n;
        s = a + "/" + sum;
        tvCount.setText(s);
    }

    void initView() {
        ivShare = findViewById(R.id.ivShare);
        layoutRoomName = findViewById(R.id.layoutRoomName);
        if (isNewChat) {
            layoutRoomName.setVisibility(View.VISIBLE);
            ivShare.setVisibility(View.GONE);
        } else {
            layoutRoomName.setVisibility(View.GONE);
            ivShare.setVisibility(View.VISIBLE);
        }

        tvCount = findViewById(R.id.tvCount);
        edRoomName = findViewById(R.id.edRoomName);
        cbCreateNewRoom = findViewById(R.id.cbCreateNewRoom);
        edRoomName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                settextForCount(charSequence.toString().length());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        recyclerView = findViewById(R.id.rv);
        NewOrganizationChart instance = this;
        mAdapter = new AdapterOrganizationChart(instance, null, null);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);


        EditText edSearch = findViewById(R.id.edSearch);
        edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String str = s.toString();
                if (str == null) str = "";
                if (str.length() == 0) {
                    lstCurrent = mAdapter.getCurrentList();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (str == null) str = "";
                if (str.length() == 0) {
                    mAdapter.updateIsSearch(0);
                    updateCurrentList();
                } else {
                    mAdapter.updateIsSearch(1);
                    mAdapter.actionSearch(str);
                }

            }
        });

    }

    void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            try {
                ivShare.setImageBitmap(Constant.getThumbnail(this, imageUri));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "imageUri:" + imageUri);
        }
    }


    void initDB() {
        if (list != null && list.size() > 0) {
            mAdapter.updateList(list);
        } else {
            Toast.makeText(getApplicationContext(), "Can not get list user, restart app please", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_organization, menu);
        return true;
    }

    void updateCurrentList() {
        if (lstCurrent != null && lstCurrent.size() > 0) {
            mAdapter.updateListSearch(lstCurrent);
        }
    }

    List<TreeUserDTO> lstCurrent = new ArrayList<>();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_add:
                if (isNewChat)
                    addToGroupChat();
                break;
        }
        return false;
    }

    ArrayList<TreeUserDTO> getListDTO(List<TreeUserDTO> lst) {
        ArrayList<TreeUserDTO> dtoList = new ArrayList<>();
        for (TreeUserDTO obj : lst) {
            if (obj.isCheck())
                dtoList.add(obj);
        }
        return dtoList;
    }

    ArrayList<TreeUserDTO> lst = new ArrayList<>();

    void addToGroupChat() {
        lst = getListDTO(mAdapter.getList());
        if (lst.size() == 0) {
        } else if (lst.size() == 1) {
            HttpRequest.getInstance().CreateOneUserChatRoom(lst.get(0).getId(), new ICreateOneUserChatRom() {
                @Override
                public void onICreateOneUserChatRomSuccess(ChattingDto chattingDto) {
                    Intent intent = new Intent(BaseActivity.Instance, ChattingActivity.class);
                    intent.putExtra(Statics.TREE_USER_PC, lst.get(0));
                    intent.putExtra(Statics.CHATTING_DTO, chattingDto);
                    intent.putExtra(Statics.IV_STATUS, lst.get(0).getStatus());
                    intent.putExtra(Constant.KEY_INTENT_ROOM_NO, chattingDto.getRoomNo());
                    intent.putExtra(Constants.TYPE_SHARE, typeShare);
                    intent.putExtra(Constants.LIST_FILE_PATH_SHARE, mSelectedImage);
                    BaseActivity.Instance.startActivity(intent);
                }

                @Override
                public void onICreateOneUserChatRomFail(ErrorDto errorDto) {
                    Utils.showMessageShort("Fail");
                }
            });
        } else if (lst.size() > 1) {
            int temp = -1;
            if (lst.size() == 2) {
                boolean flag = false;
                for (TreeUserDTO obj : lst) {
                    if (obj.getSubordinates() != null) {
                        if (obj.getSubordinates().size() > 0) {
                            // folder
                            flag = true;
                            break;
                        }
                    }
                }
                if (flag) {
                    temp = lst.get(1).getStatus();
                }
            }

            final int IV_STATUS = temp;

            boolean flag = cbCreateNewRoom.isChecked();
            String roomTitle = edRoomName.getText().toString().trim();
            if (roomTitle == null) roomTitle = "";
            if (flag) {
                HttpRequest.getInstance().CreateGroupChatRoomWithRoomTitle(lst, new ICreateOneUserChatRom() {
                    @Override
                    public void onICreateOneUserChatRomSuccess(ChattingDto chattingDto) {
                        Intent intent = new Intent(BaseActivity.Instance, ChattingActivity.class);
                        intent.putExtra(Constant.KEY_INTENT_ROOM_NO, chattingDto.getRoomNo());
                        intent.putExtra(Statics.CHATTING_DTO, chattingDto);
                        intent.putExtra(Constants.TYPE_SHARE, typeShare);
                        intent.putExtra(Constants.LIST_FILE_PATH_SHARE, mSelectedImage);
                        if (IV_STATUS != -1)
                            intent.putExtra(Statics.IV_STATUS, IV_STATUS);
                        BaseActivity.Instance.startActivity(intent);
                    }

                    @Override
                    public void onICreateOneUserChatRomFail(ErrorDto errorDto) {
                        Utils.showMessageShort("Fail");
                    }
                }, roomTitle, Statics.NEW_GROUP_CHAT_TITLE);
            } else {
                HttpRequest.getInstance().CreateGroupChatRoom(lst, new ICreateOneUserChatRom() {
                    @Override
                    public void onICreateOneUserChatRomSuccess(ChattingDto chattingDto) {
                        Intent intent = new Intent(BaseActivity.Instance, ChattingActivity.class);
                        intent.putExtra(Constant.KEY_INTENT_ROOM_NO, chattingDto.getRoomNo());
                        intent.putExtra(Statics.CHATTING_DTO, chattingDto);
                        intent.putExtra(Constants.TYPE_SHARE, typeShare);
                        intent.putExtra(Constants.LIST_FILE_PATH_SHARE, mSelectedImage);
                        if (IV_STATUS != -1)
                            intent.putExtra(Statics.IV_STATUS, IV_STATUS);
                        BaseActivity.Instance.startActivity(intent);
                    }

                    @Override
                    public void onICreateOneUserChatRomFail(ErrorDto errorDto) {
                        Utils.showMessageShort("Fail");
                    }
                }, roomTitle);
            }
        }
        finish();
    }
}