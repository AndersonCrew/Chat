package com.dazone.crewchatoff.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.AttachDTO;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.UserDto;
import com.dazone.crewchatoff.interfaces.Urls;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;

import java.io.File;

public class ChatViewAttachActivity extends BaseActivity {
    private ChattingDto chattingDto = null;
    protected LinearLayout view_header, view_footer, linearOk;
    private ImageView back_imv, avatar_imv, imv_btn_down_load, imv_btn_delete, img_main;
    private TextView userName_tv, day_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Instance = this;
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            chattingDto = (ChattingDto) bundle.getSerializable(Statics.CHATTING_DTO_GALLERY);
        }

        setContentView(R.layout.chat_view_attach_activity);
        init();
    }

    private void init() {
        setUpHeader();
        setUpFooter();
        initAdapter();
        //handleClick();
    }

    private void setUpPage() {
        setImageUser();
        setUpButton();
    }

    protected void initAdapter() {
        setUpPage();
    }

    private void setUpHeader() {
        img_main = (ImageView) findViewById(R.id.main_vpg_main);
        view_header = (LinearLayout) findViewById(R.id.view_header);
        back_imv = (ImageView) findViewById(R.id.back_imv);
        avatar_imv = (ImageView) findViewById(R.id.avatar_imv);
        ImageUtils.drawCycleImage(avatar_imv, R.drawable.avatar_l, Utils.getDimenInPx(R.dimen.button_height));
        userName_tv = (TextView) findViewById(R.id.userName_tv);
        day_tv = (TextView) findViewById(R.id.day_tv);
        back_imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setImageUser() {
        if (chattingDto != null) {
            UserDto userDto = chattingDto.getUser();
            if (userDto != null) {
                ImageUtils.showCycleImageFromLink(prefs.getServerSite() + chattingDto.getUser().avatar, avatar_imv, R.dimen.button_height);
                userName_tv.setText(userDto.FullName);
                day_tv.setText(TimeUtils.displayTimeWithoutOffset(this, chattingDto.getRegDate(), 0, TimeUtils.KEY_FROM_SERVER));
            }

            String url = chattingDto.getAttachInfo().getFullPath();
            if (!TextUtils.isEmpty(url)) {
                String temp = url.replace("D:", "");
                url = temp.replaceAll("\\\\", File.separator);
                ImageUtils.showImageFull(this, url, img_main);
            } else {
                ImageUtils.showImage(url, img_main);
            }
        } /*else {
            UserDto userDto = UserDBHelper.getUser();
            showCycleImageFromLink(prefs.getServerSite() + userDto.avatar, avatar_imv, R.dimen.button_height);
            userName_tv.setText(userDto.FullName);
            day_tv.setText(Util.parseMili2Date(dto.DayCreate, Statics.DATE_FORMAT_DETAIL).toLowerCase());
        }*/
    }

    private void setUpFooter() {
        view_footer = (LinearLayout) findViewById(R.id.view_footer);
        imv_btn_down_load = (ImageView) findViewById(R.id.imv_btn_down_load);
        imv_btn_delete = (ImageView) findViewById(R.id.imv_btn_delete);
        linearOk = (LinearLayout) findViewById(R.id.linearOk);
    }

    private void setUpButton() {
        imv_btn_down_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chattingDto != null) {
                    AttachDTO attachDTO = chattingDto.getAttachInfo();
                    if (attachDTO != null) {
                        String url = new Prefs().getServerSite() + Urls.URL_DOWNLOAD + "session=" + CrewChatApplication.getInstance().getPrefs().getaccesstoken() + "&no=" + attachDTO.getAttachNo();
                        Utils.displayDownloadFileDialog(ChatViewAttachActivity.this, url, attachDTO.getFileName());
                    }
                }
            }
        });
        /*imv_btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDelete(detailDTO.dsAttactment.get(main_vpg_main.getCurrentItem()));
            }
        });*/
    }
}