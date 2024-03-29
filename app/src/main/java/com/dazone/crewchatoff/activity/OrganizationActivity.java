package com.dazone.crewchatoff.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;

import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.activity.base.BaseSingleActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.database.AllUserDBHelper;
import com.dazone.crewchatoff.database.UserDBHelper;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallBack;
import com.dazone.crewchatoff.interfaces.ICreateOneUserChatRom;
import com.dazone.crewchatoff.test.OrganizationFragment;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class OrganizationActivity extends BaseSingleActivity {
    String TAG = "OrganizationActivity";
    private OrganizationFragment fragment;
    private long task = -1;
    private ArrayList<Integer> userNos;
    private String oldTitle = "";
    private int currentUserNo = 0;

    static {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    protected void addFragment(Bundle bundle) {
        Bundle bundle1 = getIntent().getExtras();
        if (bundle1 != null) {
            try {
                task = bundle1.getLong(Constant.KEY_INTENT_ROOM_NO);
                userNos = bundle1.getIntegerArrayList(Constant.KEY_INTENT_COUNT_MEMBER);
                oldTitle = bundle1.getString(Constant.KEY_INTENT_ROOM_TITLE);
            } catch (Exception e) {
                task = -1;
                e.printStackTrace();
            }
        }

        fragment = OrganizationFragment.newInstance(userNos, false);

        if (bundle == null) {
            Utils.addFragmentToActivity(getSupportFragmentManager(), fragment, R.id.content_base_single_activity, false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        showSave();
        HiddenTitle();
        currentUserNo = new Prefs().getUserNo();

        if (currentUserNo == 0) {
            currentUserNo = UserDBHelper.getUser().Id;
        }

        ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "setOnClickListener");
                if (fragment != null) {
                    Log.d(TAG, "fragment != null");
                    if (task == -1) {
                        fragment.callChat();
                        finish();
                    } else {
                        Log.d(TAG, "fragment == null");
                        ArrayList<TreeUserDTO> list = fragment.getListUser();




                        if (list != null && list.size() > 0) {
                            if (userNos != null && userNos.size() == 2) {
                                Log.d(TAG, "1 userNos != null");
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
                                ivMore.setOnClickListener(null);
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
            }
        });
    }
}