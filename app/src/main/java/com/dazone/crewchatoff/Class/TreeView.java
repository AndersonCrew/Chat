package com.dazone.crewchatoff.Class;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.interfaces.ICreateOneUserChatRom;
import com.dazone.crewchatoff.interfaces.OnOrganizationSelectedEvent;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public abstract class TreeView extends BaseViewClass {
    protected TreeUserDTO dto;
    protected TextView title;
    protected CheckBox checkBox;
    protected RelativeLayout main;
    protected LinearLayout lnl_child;
    private OnOrganizationSelectedEvent mSelectedEvent;
    private ArrayList<TreeUserDTO> list = new ArrayList<>();

    public TreeView(Context context, TreeUserDTO dto) {
        super(context);
        this.dto = dto;
    }

    public void setOnSelectedEvent(OnOrganizationSelectedEvent selectedEvent) {
        this.mSelectedEvent = selectedEvent;
    }

    protected void handleItemClick(boolean task) {
        if (dto.getType() == 2) {
            if (task) {
                main.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dto.getId() != Utils.getCurrentId())
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
                        else {
                            Utils.showMessage(Utils.getString(R.string.can_not_chat));
                        }
                    }
                });
            }
        } else {
            if (task) {
                main.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dto.getSubordinates() != null && dto.getSubordinates().size() > 0) {
                            convertData(dto.getSubordinates());

                            if (list != null && list.size() > 0)
                                HttpRequest.getInstance().CreateGroupChatRoom(list, new ICreateOneUserChatRom() {
                                    @Override
                                    public void onICreateOneUserChatRomSuccess(ChattingDto chattingDto) {
                                        Intent intent = new Intent(BaseActivity.Instance, ChattingActivity.class);
                                        intent.putExtra(Constant.KEY_INTENT_ROOM_NO, chattingDto.getRoomNo());
                                        intent.putExtra(Statics.CHATTING_DTO, chattingDto);
                                        BaseActivity.Instance.startActivity(intent);
                                    }

                                    @Override
                                    public void onICreateOneUserChatRomFail(ErrorDto errorDto) {
                                        Utils.showMessageShort("Fail");
                                    }
                                },"");
                        }
                    }
                });
            }
        }

        if (checkBox != null)
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (dto.getType() != 2) {
                        if (buttonView.getTag() != null && !(Boolean) buttonView.getTag()) {
                            buttonView.setTag(true);
                        } else {
                            dto.setIsCheck(isChecked);

                            if (dto.getSubordinates() != null && dto.getSubordinates().size() != 0) {
                                int index = 0;

                                for (TreeUserDTO dto1 : dto.getSubordinates()) {
                                    dto1.setIsCheck(isChecked);
                                    View childView = lnl_child.getChildAt(index);
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
                    } else {
                        if (lnl_child != null) {
                            if (!isChecked) {
                                ViewGroup parent = ((ViewGroup) lnl_child.getParent());
                                unCheckBoxParent(parent);
                            }
                        }
                    }

                    if (mSelectedEvent != null) {
                        mSelectedEvent.onOrganizationCheck(isChecked, dto);
                    }
                }
            });
    }

    private void unCheckBoxParent(ViewGroup view) {
        if (view.getId() == R.id.mainParent) {
            CheckBox parentCheckBox = view.findViewById(R.id.row_check);

            if (parentCheckBox.isChecked()) {
                parentCheckBox.setTag(false);
                parentCheckBox.setChecked(false);
            }

            try {
                ViewGroup parent = (ViewGroup) (view.getParent()).getParent().getParent();
                unCheckBoxParent(parent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void convertData(List<TreeUserDTO> treeUserDTOs) {
        if (treeUserDTOs != null && treeUserDTOs.size() != 0) {
            for (TreeUserDTO dto : treeUserDTOs) {
                if (dto.getSubordinates() != null && dto.getSubordinates().size() > 0) {
                    if (dto.getType() == 2) {
                        list.add(dto);
                    }

                    convertData(dto.getSubordinates());
                } else {
                    if (dto.getType() == 2) {
                        list.add(dto);
                    }
                }
            }
        }
    }
}