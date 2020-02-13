package com.dazone.crewchatoff.ViewHolders;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
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
import com.dazone.crewchatoff.activity.MainActivity;
import com.dazone.crewchatoff.activity.RelayActivity;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.adapter.ChattingAdapter;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.database.ChatMessageDBHelper;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.eventbus.ReloadListMessage;
import com.dazone.crewchatoff.fragment.ChattingFragment;
import com.dazone.crewchatoff.interfaces.ICreateOneUserChatRom;
import com.dazone.crewchatoff.interfaces.IF_Relay;
import com.dazone.crewchatoff.interfaces.SendChatMessage;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import static com.dazone.crewchatoff.fragment.ChattingFragment.sendComplete;

public class ChattingSelfViewHolder extends BaseChattingHolder {
    private TextView date_tv, content_tv;
    private TextView tvUnread;
    private RelativeLayout layoutMain;
    private ProgressBar progressBarSending;
    private LinearLayout lnSendFailed;
    private ImageView btnResend, btnDelete;
    private ChattingAdapter mAdapter;
    String TAG = "ChattingSelfViewHolder";
    public static boolean isReSend;
    final Handler handler = new Handler();

    public void setAdapter(ChattingAdapter adapter) {
        this.mAdapter = adapter;
    }

    public ChattingSelfViewHolder(View v) {
        super(v);
    }

    @Override
    protected void setup(View v) {
        layoutMain = (RelativeLayout) v.findViewById(R.id.layout_main);
        date_tv = (TextView) v.findViewById(R.id.date_tv);

        content_tv = (TextView) v.findViewById(R.id.content_tv);
        tvUnread = (TextView) v.findViewById(R.id.text_unread);


        progressBarSending = (ProgressBar) v.findViewById(R.id.progressbar_sending);
        lnSendFailed = (LinearLayout) v.findViewById(R.id.ln_send_failed);


        btnResend = (ImageView) v.findViewById(R.id.btn_resend);
        btnDelete = (ImageView) v.findViewById(R.id.btn_delete);
    }

    void reLay(long MessageNo) {
        Intent intent = new Intent(BaseActivity.Instance, RelayActivity.class);
        intent.putExtra(Statics.MessageNo, MessageNo);
        BaseActivity.Instance.startActivity(intent);
    }

    void sendMsgToMe(long MessageNo) {
        List<String> lstRoom = new ArrayList<>();
        lstRoom.add("" + MainActivity.myRoom);
        HttpRequest.getInstance().ForwardChatMsgChatRoom(MessageNo, lstRoom, new IF_Relay() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFail() {
                Toast.makeText(CrewChatApplication.getInstance(), "Send Msg to room Fail", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void toMe(final long MessageNo) {
        if (MainActivity.myRoom != Statics.MYROOM_DEFAULT) {
            sendMsgToMe(MessageNo);
        } else {
            // create roomNo
            HttpRequest.getInstance().CreateOneUserChatRoom(Utils.getCurrentId(), new ICreateOneUserChatRom() {
                @Override
                public void onICreateOneUserChatRomSuccess(ChattingDto chattingDto) {
                    if (chattingDto != null) {
                        long roomNo = chattingDto.getRoomNo();
                        MainActivity.myRoom = roomNo;
                        sendMsgToMe(MessageNo);
                    }
                }

                @Override
                public void onICreateOneUserChatRomFail(ErrorDto errorDto) {
                    Utils.showMessageShort("Fail");
                }
            });
        }
    }

    public void showDialogChat(final String content, final long MessageNo) {
        android.support.v7.app.AlertDialog.Builder builderSingle = new android.support.v7.app.AlertDialog.Builder(BaseActivity.Instance);
        builderSingle.setTitle(Utils.getString(R.string.app_name));

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                CrewChatApplication.getInstance(),
                R.layout.row_chatting_call);
        arrayAdapter.add(CrewChatApplication.getInstance().getResources().getString(R.string.copy));
        arrayAdapter.add(CrewChatApplication.getInstance().getResources().getString(R.string.relay));
        arrayAdapter.add(CrewChatApplication.getInstance().getResources().getString(R.string.to_me));

        arrayAdapter.add(Constant.getUnreadText(CrewChatApplication.getInstance(), getUnReadCount));

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                enableText();
                                int sdk = android.os.Build.VERSION.SDK_INT;
                                if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) CrewChatApplication.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
                                    clipboard.setText(content);
                                } else {
                                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) CrewChatApplication.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
                                    android.content.ClipData clip = android.content.ClipData.newPlainText("Copy", content);
                                    clipboard.setPrimaryClip(clip);
                                }
                                break;
                            case 1:
                                enableText();
                                reLay(MessageNo);
                                Log.d(TAG, "reLay");
                                break;
                            case 2:
                                enableText();
                                toMe(MessageNo);
                                Log.d(TAG, "toMe");
                                break;
                            case 3:
                                enableText();
                                actionUnread();
                                Log.d(TAG, "actionUnread");
                                break;
                        }
                    }
                });
        AlertDialog dialog = builderSingle.create();
        if (arrayAdapter.getCount() > 0) {
            dialog.show();
        }
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                enableText();
                Log.d(TAG, "onDismiss");
            }
        });
//        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
//
//                if (i == KeyEvent.KEYCODE_BACK) {
//                    enableText();
//                    Log.d(TAG, "OnKeyListener");
//                    return true;
//                }
//                return false;
//            }
//        });

        Button b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (b != null) {
            b.setTextColor(ContextCompat.getColor(CrewChatApplication.getInstance(), R.color.light_black));
        }


    }

    void enableText() {
        if (content_tv != null)
            content_tv.setEnabled(true);
    }

    private void actionUnread() {
        Intent intent = new Intent(Constant.INTENT_GOTO_UNREAD_ACTIVITY);
        intent.putExtra(Statics.MessageNo, tempDto.getMessageNo());
        BaseActivity.Instance.sendBroadcast(intent);
    }

    ChattingDto tempDto;
    int getUnReadCount;

    @Override
    public void bindData(final ChattingDto dto) {

        try {
            getUnReadCount = dto.getUnReadCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
        tempDto = dto;
        String strUnReadCount = dto.getUnReadCount() + "";
        tvUnread.setText(strUnReadCount);
        if (!TextUtils.isEmpty(dto.getLastedMsgDate())) {
            date_tv.setText(TimeUtils.displayTimeWithoutOffset(CrewChatApplication.getInstance().getApplicationContext(), dto.getLastedMsgDate(), 0, TimeUtils.KEY_FROM_SERVER));
        } else {
            date_tv.setText(TimeUtils.displayTimeWithoutOffset(CrewChatApplication.getInstance().getApplicationContext(), dto.getRegDate(), 0, TimeUtils.KEY_FROM_SERVER));
        }


        if (dto.getMessage() != null) {
            String message = dto.getMessage();
            //  String[] url = message.split("http");
            try {
                if (dto.getType() == Constant.APPROVAL) {
                    String[] fullUrl = message.split("\\|");
                    String msgText = "";
                    String linkUrl = "";
                    String linkTitle = "";
                    if (fullUrl.length >= 3) {
                        msgText = fullUrl[0];
                        linkUrl = fullUrl[1];
                        linkTitle = fullUrl[2];
                        Spanned Text = Html.fromHtml(msgText + " <br /><br />" +
                                "<a href='" + linkUrl + "'>" + linkTitle + "</a><br />");
                        //content_tv.setText(Html.fromHtml(text));

                        // content_tv
                        content_tv.setAutoLinkMask(0);
                        content_tv.setLinkTextColor(CrewChatApplication.getInstance().getResources().getColor(R.color.colorPrimary));
                        content_tv.setLinksClickable(true);
                        content_tv.setMovementMethod(LinkMovementMethod.getInstance());
                        content_tv.setText(Text);
                    } else {
                        content_tv.setAutoLinkMask(Linkify.ALL);
                        // Linkify.addLinks(content_tv, Linkify.WEB_URLS);
                        content_tv.setLinksClickable(true);
                        content_tv.setText(dto.getMessage());
                    }
                } else {
                    content_tv.setAutoLinkMask(Linkify.ALL);
                    // Linkify.addLinks(content_tv, Linkify.WEB_URLS);
                    content_tv.setLinksClickable(true);
                    content_tv.setText(dto.getMessage());
                }


            } catch (Exception e) {
            }
            // content_tv.setText(dto.getMessage());
          /*  Spanned Text = Html.fromHtml(url[0]+" <br />" +
                    "<a href='https://www.android-examples.com'>☞ 바로가기</a>");
*/


        }


//        tvUnread.setVisibility(dto.getUnReadCount() == 0 ? View.GONE : View.VISIBLE);
        date_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "tvUnread");
                actionUnread();
            }
        });
        if (dto.getUnReadCount() == 0) {
            tvUnread.setVisibility(View.INVISIBLE);
        } else {
            tvUnread.setVisibility(View.VISIBLE);
            tvUnread.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "tvUnread");
                    actionUnread();
                }
            });
        }
      /*  if (isReSend) {
            isReSend = false;
        }*/

        if (dto.isHasSent()) {
            if (progressBarSending != null) progressBarSending.setVisibility(View.GONE);
            if (lnSendFailed != null) lnSendFailed.setVisibility(View.GONE);
        } else {
            if (lnSendFailed != null) lnSendFailed.setVisibility(View.VISIBLE);
        }


        /** SHOW DIALOG */
        layoutMain.setTag(content_tv.getText().toString());

//        content_tv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                content_tv.setLinksClickable(true);
//            }
//        });


//        content_tv.setLinksClickable(true);
//        content_tv.setMovementMethod(LinkMovementMethod.getInstance());


        content_tv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                long MessageNo = dto.getMessageNo();
                String content = content_tv.getText().toString();
                showDialogChat(content, MessageNo);
                Log.d(TAG, "onLongClick:");
                content_tv.setEnabled(false);
                return true;
            }
        });


        layoutMain.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                Log.d(TAG,"layoutMain onLongClick:"+new Gson().toJson(dto));
                long MessageNo = dto.getMessageNo();
                String content = (String) v.getTag();
//                DialogUtils.showDialogChat(content);
                showDialogChat(content, MessageNo);
                return true;
            }
        });
        // Set event listener for failed message
        if (btnResend != null) {
            btnResend.setTag(dto.getId());
            btnResend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // sendComplete=true;
                    boolean flag = ChattingFragment.isSend;
                    if (flag && !dto.isSendding) {
                        btnResend.setImageDrawable(CrewChatApplication.getInstance().getResources().getDrawable(R.drawable.icon_loadding));
                        dto.isSendding = true;
                        Log.d(TAG, "btnResend:" + dto.getMessage());
                        final Integer localId = (Integer) v.getTag();
                        final long localMessageNo = dto.getMessageNo();
                        for (int i = 0; i <= mAdapter.getData().size() - 1; i++) {
                            if (dto.getId() == mAdapter.getData().get(i).getId()) {
                                HttpRequest.getInstance().SendChatMsg(dto.getRoomNo(), mAdapter.getData().get(i).getMessage(), new SendChatMessage() {
                                    @Override
                                    public void onSendChatMessageSuccess(final ChattingDto chattingDto) {
                                        dto.setHasSent(true);
                                        dto.setMessageNo(chattingDto.getMessageNo());
                                        dto.setUnReadCount(chattingDto.getUnReadCount());
                                        String time = TimeUtils.convertTimeDeviceToTimeServerDefault(chattingDto.getRegDate());
                                        dto.setRegDate(time);
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ChatMessageDBHelper.updateMessage(dto, localId);
                                            }
                                        }).start();
                                        EventBus.getDefault().post(new ReloadListMessage());
                                        dto.isSendding = false;
                                        btnResend.setImageDrawable(CrewChatApplication.getInstance().getResources().getDrawable(R.drawable.chat_ic_refresh));
                                        //  mAdapter.notifyDataSetChanged();
                                        // sendComplete=false;
                                    }

                                    @Override
                                    public void onSendChatMessageFail(ErrorDto errorDto, String url) {
                                        EventBus.getDefault().post(new ReloadListMessage());
                                        dto.isSendding = false;
                                        btnResend.setImageDrawable(CrewChatApplication.getInstance().getResources().getDrawable(R.drawable.chat_ic_refresh));
                                        // sendComplete=false;
                                     /*   newDto.isSendding = false;
                                        isSend = true;
                                        ///Toast.makeText(mActivity, "Send message failed !", Toast.LENGTH_SHORT).show();
                                        if (isNetWork) {
                                            ChatMessageDBHelper.deleteByIdTmp(newDto, finalLastId);
                                        }*/

                                    }
                                });
                            }
                        }
                       /* HttpRequest.getInstance().SendChatMsg(dto.getRoomNo(), dto.getMessage(), new SendChatMessage() {
                            @Override
                            public void onSendChatMessageSuccess(final ChattingDto chattingDto) {
                               *//* dto.setHasSent(true);
                                dto.setMessageNo(chattingDto.getMessageNo());
                                dto.setUnReadCount(chattingDto.getUnReadCount());
                                String time = TimeUtils.convertTimeDeviceToTimeServerDefault(chattingDto.getRegDate());
                                dto.setRegDate(time);*//*

                                Log.d(TAG, "onSendChatMessageSuccess:" + dto.getMessage());
                                // update old chat message model --> messageNo from server
                                // perform update when send message success

                                // Notify current adapter
                          *//*      if (mAdapter != null) {
                                    for (int i = mAdapter.getData().size() - 1; i > -1; i--) {
                                        if (mAdapter.getData().get(i).getId() == localId) {
                                            mAdapter.getData().get(i).setHasSent(true);
                                            mAdapter.getData().get(i).setMessageNo(chattingDto.getMessageNo());
                                            mAdapter.getData().get(i).setUnReadCount(chattingDto.getUnReadCount());
                                            String time = TimeUtils.convertTimeDeviceToTimeServerDefault(chattingDto.getRegDate());
                                            mAdapter.getData().get(i).setRegDate(time);
                                            mAdapter.notifyItemChanged(i);
                                            }

                                    }

                                }*//*
                         *//* new Thread(new Runnable() {
                                    @Override
                                    public void run() {
//                                        dto.setHasSent(true);
//                                        Log.d(TAG,"finalLastId adapter:"+localId);
                                        dto.setRegDate(TimeUtils.convertTimeDeviceToTimeServerDefault(chattingDto.getRegDate()));
                                        dto.setHasSent(true);
                                        dto.setUnReadCount(chattingDto.getUnReadCount());
                                        ChatMessageDBHelper.updateMessage(dto, localId);


                                    }
                                }).start();*//*

                                    if (chattingDto.getId() == dto.getId()) {
                                        if (!dto.isHasSent()) {
                                            dto.setUnReadCount(chattingDto.getUnReadCount());
                                            dto.setHasSent(true);
                                            dto.setMessage(chattingDto.getMessage());
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    *//*ChatMessageDBHelper.updateMessageHasSend(chattingDto.getMessageNo() , true,chattingDto.getUnReadCount());*//*
                                                    ChatMessageDBHelper.updateMessage(dto, dto.getId());
                                                }
                                            }).start();
                                            //  if (!isReSend) {

                                            mAdapter.notifyDataSetChanged();
                                            //  }
                                            // adapterList.notifyDataSetChanged();
                                            Log.d(TAG, "adapterList.notifyItemChanged(i);");
                                    }
                                }
                                // mAdapter.notifyDataSetChanged();

                                dto.isSendding = false;

                                *//* mAdapter.notifyDataSetChanged();*//*
                            }

                            @Override
                            public void onSendChatMessageFail(ErrorDto errorDto, String url) {
                                dto.isSendding = false;

                                Log.d(TAG, "onSendChatMessageFail btnResend:" + dto.getMessage());
                            }
                        });*/
                    } else {
                        Log.d(TAG, "wait finish send: dto.isSendding:" + dto.isSendding + " msg:" + dto.getMessage());
                    }
                }
            });
        }


        if (btnDelete != null) {
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // delete or call callback
                    if (ChatMessageDBHelper.deleteMessage(dto.getMessageNo())) {
                        if (mAdapter != null && mAdapter.getData() != null) {
                            mAdapter.getData().remove(dto);
                            // check remove line date
//                            Log.d(TAG, "position:" + getAdapterPosition());
                            int before = getAdapterPosition() - 1;
                            if (before >= 0) {
                                Log.d(TAG, "msg before:" + mAdapter.getData().get(before).getMessage());
                                if (mAdapter.getData().get(before).getmType() == Statics.CHATTING_VIEW_TYPE_DATE) {
                                    int after = getAdapterPosition();
                                    boolean isRemove = false;
                                    ChattingDto obj = null;
                                    try {
                                        obj = mAdapter.getData().get(after);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    if (obj == null) {
                                        // remove
                                        Log.d(TAG, "obj = null");
                                        isRemove = true;
                                    } else {
                                        Log.d(TAG, "obj != null -> mType: " + obj.getmType() + " msg:" + obj.getMessage());
                                        if (obj.getmType() == Statics.CHATTING_VIEW_TYPE_DATE) {
                                            // remove
                                            isRemove = true;
                                        }
                                    }
                                    if (isRemove) mAdapter.getData().remove(before);
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    }

}