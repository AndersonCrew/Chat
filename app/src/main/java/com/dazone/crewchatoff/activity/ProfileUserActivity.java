package com.dazone.crewchatoff.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.BelongDepartmentDTO;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.ProfileUserDTO;
import com.dazone.crewchatoff.interfaces.ICreateOneUserChatRom;
import com.dazone.crewchatoff.interfaces.OnGetUserCallBack;
import com.dazone.crewchatoff.utils.*;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class ProfileUserActivity extends AppCompatActivity implements View.OnClickListener {
    String TAG = "ProfileUserActivity";
    private ImageView btnBack;
    private ImageView btnCall;
    private ImageView btnEmail;
    private ImageView ivAvatar;
    private TextView tvFullName;
    private TextView tvPositionName;
    private TextView tvUserID;
    private TextView tvName;
    private TextView tvMailAddress;
    private TextView tvSex;
    private TextView tvPhoneNumber;
    private TextView tvCompanyNumber;
    private TextView tvExtensionNumber;
    private TextView tvEntranceDate;
    private LinearLayout tvEntranceDate_Label;
    private TextView tvBirthday;
    private LinearLayout tvBirthday_Label;
    private TextView tvBelongToDepartment;
    private TextView tv_pass;
    private TextView btnChangePass;
    private RelativeLayout btnChat;

    private int userNo = 0;
    private ImageView ivEmailEmail, ivPhoneCall, ivExPhoneCall, ivPhoneEmail, ivExPhoneEmail;
    private LinearLayout lnExPhone, lnPhone;
    public static ProfileUserActivity instance = null;
    private FrameLayout pwLayout;
    String emailAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        setContentView(R.layout.activity_profile_user);
        initView();
        receiveBundle();
        getDataFromServer();
    }

    private void initView() {
        pwLayout = findViewById(R.id.pwLayout);
        btnChat = findViewById(R.id.btnChat);
        btnChat.setOnClickListener(this);
        btnBack = findViewById(R.id.btn_back);
        btnCall = findViewById(R.id.btn_call);
        btnEmail = findViewById(R.id.btn_email);

        ivEmailEmail = findViewById(R.id.iv_email_email);
        ivEmailEmail.setOnClickListener(this);

        ivPhoneCall = findViewById(R.id.iv_phone_call);
        ivPhoneCall.setOnClickListener(this);
        ivPhoneEmail = findViewById(R.id.iv_phone_email);
        ivPhoneEmail.setOnClickListener(this);

        ivExPhoneCall = findViewById(R.id.iv_ex_phone_call);
        ivExPhoneCall.setOnClickListener(this);
        ivExPhoneEmail = findViewById(R.id.iv_ex_phone_email);
        ivExPhoneEmail.setOnClickListener(this);

        lnExPhone = findViewById(R.id.ln_ex_phone);
        lnPhone = findViewById(R.id.ln_phone);
        /** ROW AVATAR*/
        ivAvatar = findViewById(R.id.iv_avatar);
        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (urlAv.length() > 0) {
                    showDetailsImage(urlAv);
                } else {
                    Toast.makeText(getApplicationContext(), "can not get url image", Toast.LENGTH_SHORT).show();
                }
            }
        });
        tvFullName = findViewById(R.id.tv_full_name);
        tvPositionName = findViewById(R.id.tv_position_name);


        tvUserID = findViewById(R.id.tv_user_id);
        tvName = findViewById(R.id.tv_name);
        tvMailAddress = findViewById(R.id.tv_mail_address);
        tvSex = findViewById(R.id.tv_sex);
        tvPhoneNumber = findViewById(R.id.tv_phone_number);
        tvCompanyNumber = findViewById(R.id.tv_company_number);
        tvExtensionNumber = findViewById(R.id.tv_extension_number);
        tvEntranceDate = findViewById(R.id.tv_entrance_date);
        tvEntranceDate_Label = findViewById(R.id.tv_entrance_date_label);
        tvBirthday = findViewById(R.id.tv_birthday);
        tvBirthday_Label = findViewById(R.id.tv_birthday_label);
        tvBelongToDepartment = findViewById(R.id.tv_belong_to_department);

        btnBack.setOnClickListener(this);
        btnCall.setOnClickListener(this);
        btnEmail.setOnClickListener(this);

        tv_pass = findViewById(R.id.tv_pass);
        String pass = CrewChatApplication.getInstance().getPrefs().getPass();
        Log.d(TAG, "pass:" + pass);

        btnChangePass = findViewById(R.id.btnChangePass);
        btnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileUserActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void receiveBundle() {


        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey(Constant.KEY_INTENT_USER_NO)) {
            userNo = bundle.getInt(Constant.KEY_INTENT_USER_NO);
            if (userNo == 0) {
                Prefs preferenceUtilities = CrewChatApplication.getInstance().getPrefs();
                userNo = preferenceUtilities.getUserNo();
            }
        }
        if (Utils.getCurrentId() != userNo) {
            pwLayout.setVisibility(View.GONE);
            ivEmailEmail.setVisibility(View.VISIBLE);
            ivExPhoneCall.setVisibility(View.VISIBLE);
            ivExPhoneEmail.setVisibility(View.VISIBLE);
            ivPhoneEmail.setVisibility(View.VISIBLE);
            ivPhoneCall.setVisibility(View.VISIBLE);
        } else {
            pwLayout.setVisibility(View.VISIBLE);
            ivEmailEmail.setVisibility(View.GONE);
            ivExPhoneCall.setVisibility(View.GONE);
            ivExPhoneEmail.setVisibility(View.GONE);
            ivPhoneEmail.setVisibility(View.GONE);
            ivPhoneCall.setVisibility(View.GONE);


        }
    }

    private void getDataFromServer() {

        HttpRequest.getInstance().GetUser(userNo, new OnGetUserCallBack() {
            @Override
            public void onHTTPSuccess(ProfileUserDTO profileUserDTO) {
                fillData(profileUserDTO);
            }

            @Override
            public void onHTTPFail(ErrorDto errorDto) {
            }
        });
    }

    String urlAv = "";

    void showDetailsImage(String url) {
        Intent intent = new Intent(this, DetailsMyImageActivity.class);
        intent.putExtra(Statics.CHATTING_DTO_GALLERY_SHOW_FULL, url);
        startActivity(intent);
    }

    ProfileUserDTO mProfileUserDTO;
    private void fillData(ProfileUserDTO profileUserDTO) {
        mProfileUserDTO = profileUserDTO;
        String url = new Prefs().getServerSite() + profileUserDTO.getAvatarUrl();
        urlAv = url;
        ImageUtils.showCycle(url, ivAvatar, R.dimen.button_height);

        tvFullName.setText(profileUserDTO.getName());
        String strPositionName = "";
        String belongToDepartment = "";
        ArrayList<BelongDepartmentDTO> listBelong = profileUserDTO.getBelongs();

        for (BelongDepartmentDTO belongDepartmentDTOs : listBelong) {
            belongToDepartment += listBelong.indexOf(belongDepartmentDTOs) == listBelong.size() - 1 ?
                    belongDepartmentDTOs.getDepartName() + " / " + belongDepartmentDTOs.getPositionName() + " / " + belongDepartmentDTOs.getDutyName() :
                    belongDepartmentDTOs.getDepartName() + " / " + belongDepartmentDTOs.getPositionName() + " / " + belongDepartmentDTOs.getDutyName() + "<br>";
            if (belongDepartmentDTOs.isDefault()) {
                strPositionName = belongDepartmentDTOs.getDepartName() + " / " + belongDepartmentDTOs.getPositionName() + " / " + belongDepartmentDTOs.getDutyName();
            }
        }

        tvPositionName.setText(strPositionName);
        tvUserID.setText(profileUserDTO.getUserID());
        tvName.setText(profileUserDTO.getName());
        emailAddress = profileUserDTO.getMailAddress();
        tvMailAddress.setText(emailAddress);
        ivEmailEmail.setTag(emailAddress);

        tvSex.setText(profileUserDTO.getSex() == 0 ? "Female" : "Male");

        String cellPhone = profileUserDTO.getCellPhone();
        Log.d(TAG, "cellPhone:" + cellPhone);
        if (TextUtils.isEmpty(cellPhone)) {
            lnPhone.setVisibility(View.GONE);
        } else {
            tvPhoneNumber.setText(cellPhone);
            ivPhoneCall.setTag(cellPhone);
            ivPhoneEmail.setTag(cellPhone);

        }

        tvCompanyNumber.setText(profileUserDTO.getCompanyPhone());

        String exPhone = profileUserDTO.getExtensionNumber();
        Log.d(TAG, "exPhone:" + exPhone);
        if (TextUtils.isEmpty(exPhone)) {
            lnExPhone.setVisibility(View.GONE);
        } else {
            tvExtensionNumber.setText(exPhone);
            ivExPhoneCall.setTag(exPhone);
            ivExPhoneEmail.setTag(exPhone);
        }

        if (Utils.getCurrentUser().EntranceDateDisplay) {
            tvEntranceDate.setText(TimeUtils.displayTimeWithoutOffset(profileUserDTO.getEntranceDate()));
        } else {
            tvEntranceDate_Label.setVisibility(View.INVISIBLE);
        }
        if (Utils.getCurrentUser().BirthDateDisplay) {
            tvBirthday.setText(TimeUtils.displayTimeWithoutOffset(profileUserDTO.getBirthDate()));
        } else {
            tvBirthday_Label.setVisibility(View.INVISIBLE);
        }
        tvBelongToDepartment.setText(Html.fromHtml(belongToDepartment));

        String phoneNumber = !TextUtils.isEmpty(profileUserDTO.getCellPhone().trim()) ?
                profileUserDTO.getCellPhone() :
                !TextUtils.isEmpty(profileUserDTO.getCompanyPhone().trim()) ?
                        profileUserDTO.getCompanyPhone() :
                        "";
        btnCall.setTag(phoneNumber);
    }

    @Override
    public void onClick(View v) {
        String phoneNumber;
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_call:

            case R.id.iv_phone_call:
                phoneNumber = mProfileUserDTO.getCellPhone();
                if (!TextUtils.isEmpty(phoneNumber.trim())) {
                    Utils.CallPhone(ProfileUserActivity.this, phoneNumber);
                }
                break;
            case R.id.iv_ex_phone_call:
                phoneNumber = mProfileUserDTO.getCompanyPhone();
                if (!TextUtils.isEmpty(phoneNumber.trim())) {
                    Utils.CallPhone(ProfileUserActivity.this, phoneNumber);
                }
                break;
            case R.id.iv_email_email:
                phoneNumber = emailAddress;
                if (!TextUtils.isEmpty(phoneNumber + "")) {
                    Utils.sendMail(ProfileUserActivity.this, phoneNumber);
                }
                break;

            case R.id.iv_phone_email:

            case R.id.iv_ex_phone_email:
                phoneNumber = (String) v.getTag();
                if (!TextUtils.isEmpty(phoneNumber.trim())) {
                    Utils.sendSMS(ProfileUserActivity.this, phoneNumber);
                }
                break;
            case R.id.btnChat:

                btnChat.setEnabled(false);
                ArrayList<TreeUserDTO> selectedPersonList = new ArrayList<>();
                TreeUserDTO obj = new TreeUserDTO("", "", "", "", "", 1, 3, userNo, 0);
                selectedPersonList.add(obj);
                createChatRoom(selectedPersonList);
                break;

        }
    }


    private void createChatRoom(final ArrayList<TreeUserDTO> selectedPersonList) {
        HttpRequest.getInstance().CreateOneUserChatRoom(selectedPersonList.get(0).getId(), new ICreateOneUserChatRom() {
            @Override
            public void onICreateOneUserChatRomSuccess(ChattingDto chattingDto) {

                Intent intent = new Intent(BaseActivity.Instance, ChattingActivity.class);
                intent.putExtra(Statics.TREE_USER_PC, selectedPersonList.get(0));
                intent.putExtra(Statics.CHATTING_DTO, chattingDto);
                intent.putExtra(Constant.KEY_INTENT_ROOM_NO, chattingDto.getRoomNo());
                BaseActivity.Instance.startActivity(intent);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        btnChat.setEnabled(true);
                    }
                }, 2000);
            }

            @Override
            public void onICreateOneUserChatRomFail(ErrorDto errorDto) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        btnChat.setEnabled(true);
                    }
                }, 2000);
                Utils.showMessageShort("Fail");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }
}