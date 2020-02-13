package com.dazone.crewchatoff.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.HTTPs.HttpOauthRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.DetailsMyImageActivity;
import com.dazone.crewchatoff.activity.LoginActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.database.UserDBHelper;
import com.dazone.crewchatoff.dto.BelongDepartmentDTO;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.UserDetailDto;
import com.dazone.crewchatoff.dto.UserDto;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallBack;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallbackWithJson;
import com.dazone.crewchatoff.interfaces.OnBackCallBack;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;
import com.google.gson.Gson;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    // View var
    String TAG = "ProfileFragment";
    private View mView;
    private TextView tvPersonalID, tvEmail, tvCompanyName, tvName, tvPhoneNumber;
    private ImageView ivAvatar;
    private ProgressBar mProgressBar;
    private ImageView ivBack, ivLogout, ivTheme;
    private LinearLayout lnAbove;
    private RelativeLayout rl_phone_number;

    // Object var
    private UserDto userDBHelper;
    private int userNo = 0;
    private Context mContext;
    private OnBackCallBack mCallback;
    public Prefs prefs;

    public void setCallback(OnBackCallBack mCallback) {
        this.mCallback = mCallback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_profile, container, false);
        prefs = CrewChatApplication.getInstance().getPrefs();
        initView();
        if (Utils.isNetworkAvailable()) {
            getUserDetail(String.valueOf(prefs.getUserNo()));
        } else {
            dataOffline();
        }

        return mView;
    }

    private void dataOffline() {
        UserDto userDto = UserDBHelper.getUser();
        tvName.setText(userDto.getFullName());
        tvPersonalID.setText(userDto.getUserID());
        rl_phone_number.setVisibility(View.GONE);
        tvEmail.setText(prefs.getEmail());
        tvCompanyName.setText(userDto.getNameCompany());
        String url = prefs.getServerSite() + prefs.getAvatarUrl();
        ImageUtils.showCycleImageFromLink(url, ivAvatar, R.dimen.button_height);
    }

    private void initView() {
        lnAbove = (LinearLayout) mView.findViewById(R.id.lnabove);
        tvCompanyName = (TextView) mView.findViewById(R.id.txt_company_name);
        tvPersonalID = (TextView) mView.findViewById(R.id.txt_person_id);
        tvName = (TextView) mView.findViewById(R.id.txt_name);
        tvEmail = (TextView) mView.findViewById(R.id.txt_email);
        tvPhoneNumber = (TextView) mView.findViewById(R.id.tv_phone_number);
        ivAvatar = (ImageView) mView.findViewById(R.id.img_profile);
        ivBack = (ImageView) mView.findViewById(R.id.iv_back);
        //ivLogout = (ImageView) mView.findViewById(R.id.iv_logout);
        ivTheme = (ImageView) mView.findViewById(R.id.img_theme);
        mProgressBar = (ProgressBar) mView.findViewById(R.id.progressBar);
        rl_phone_number = (RelativeLayout) mView.findViewById(R.id.rl_phone_number);
        ivBack.setOnClickListener(this);
        //ivLogout.setOnClickListener(this);
    }

    private void backFunction() {
        if (mCallback != null) {
            mCallback.onBack();
        }
    }

    private void logout() {
        HttpOauthRequest.getInstance().logout(new BaseHTTPCallBack() {
            @Override
            public void onHTTPSuccess() {
                prefs.clearLogin();
                Intent intent = new Intent(CrewChatApplication.getInstance().getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void onHTTPFail(ErrorDto errorDto) {
            }
        });
    }

    private void getUserDetail(String userNo) {
        mProgressBar.setVisibility(View.VISIBLE);
        String languageCode = "EN";
        String timeZoneOffset = TimeUtils.getTimezoneOffsetInMinutes();
        String serverLink = prefs.getServerSite();
        HttpOauthRequest.getInstance().getUser(
                new BaseHTTPCallbackWithJson() {
                    @Override
                    public void onHTTPSuccess(String jsonData) {
                        mProgressBar.setVisibility(View.GONE);
                        Gson gson = new Gson();
                        UserDetailDto userDto = gson.fromJson(jsonData, UserDetailDto.class);
                        fillData(userDto);
                    }

                    @Override
                    public void onHTTPFail(ErrorDto errorDto) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                },
                userNo,
                languageCode,
                timeZoneOffset,
                serverLink
        );
    }

    private class AsyncBackGroundBlurLoader extends AsyncTask<Bitmap, Void, Drawable> {
        @Override
        protected Drawable doInBackground(Bitmap... loadedImage) {
            //Bitmap photo = Bitmap.createScaledBitmap(loadedImage[0], 768, 1024, true);
            Bitmap output = null;
            try {
                output = ImageUtils.fastblur(loadedImage[0], 2);
            } catch (Exception e) {
                // TODO: handle exception
            }
            return new BitmapDrawable(CrewChatApplication.getInstance().getApplicationContext().getResources(), output);
        }

        @Override
        protected void onPostExecute(Drawable result) {
            super.onPostExecute(result);
            ivTheme.setImageDrawable(result);
            lnAbove.setVisibility(View.VISIBLE);
        }
    }

    String urlSend = "";

    private void fillData(final UserDetailDto profileUserDTO) {
        String url = new Prefs().getServerSite() + profileUserDTO.getAvatarUrl();
        Log.d(TAG, "url:" + url);
        urlSend = url;
        // Change imageLoader by Picasso
        if (url.trim().length() > 0) {
            Picasso.with(getActivity())
                    .load(url)
                    .networkPolicy(Utils.isNetworkAvailable() ? NetworkPolicy.NO_CACHE : NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.loading)
                    .into(ivAvatar);
        }

        tvPersonalID.setText(profileUserDTO.getUserID());
        tvName.setText(profileUserDTO.getName());

        String company = "";
        ArrayList<BelongDepartmentDTO> belongs = profileUserDTO.getBelongs();
        if (belongs != null) {
            for (int i = 0; i < belongs.size(); i++) {
                if (i == 0) {
                    company += belongs.get(i).getDepartName();
                } else {
                    company += "," + belongs.get(i).getDepartName();
                }
            }
        }

        tvCompanyName.setText(prefs.getCompanyName());
        tvEmail.setText(profileUserDTO.getMailAddress());

        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "ivAvatar onClick");
//                ArrayList<ChattingDto> urls = new ArrayList<>();
//                ChattingDto dto = new ChattingDto();
//                UserDto u = new UserDto(profileUserDTO.getUserID(), profileUserDTO.getName(), profileUserDTO.getAvatarUrl());
//                dto.setUser(u);
//                AttachDTO attachDTO = new AttachDTO();
//                attachDTO.setFullPath(profileUserDTO.getAvatarUrl());
//                dto.setAttachInfo(attachDTO);
//                urls.add(dto);
//                Prefs prefs = CrewChatApplication.getInstance().getPrefs();
//                if (urls.size() > 0)
//                    prefs.setIMAGE_LIST(new Gson().toJson(urls));
//                else
//                    prefs.setIMAGE_LIST("");
//                Intent intent = new Intent(getActivity(), ChatViewImageActivity.class);
////                intent.putExtra(Statics.CHATTING_DTO_GALLERY_LIST, urls);
//                intent.putExtra(Statics.CHATTING_DTO_GALLERY_POSITION, 0);
//                intent.putExtra(Statics.CHATTING_DTO_GALLERY_SHOW_FULL, true);
//                getActivity().startActivity(intent);

                if (urlSend.length() > 0)
                    showDetailsImage(urlSend);
                else
                    Toast.makeText(getActivity(), "Can not get image from server", Toast.LENGTH_SHORT).show();
            }
        });

        String phone = "";
        if (!TextUtils.isEmpty(profileUserDTO.getCellPhone())) {
            phone = profileUserDTO.getCellPhone();
        }

        if (!TextUtils.isEmpty(phone)) {
            tvPhoneNumber.setText(phone);
            rl_phone_number.setVisibility(View.VISIBLE);
        } else {
            rl_phone_number.setVisibility(View.GONE);
        }
    }

    void showDetailsImage(String url) {
        Intent intent = new Intent(getActivity(), DetailsMyImageActivity.class);
        intent.putExtra(Statics.CHATTING_DTO_GALLERY_SHOW_FULL, url);
        getActivity().startActivity(intent);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                backFunction();
                break;
            /*case R.id.iv_logout:
                logout();
                break;*/
        }
    }
}