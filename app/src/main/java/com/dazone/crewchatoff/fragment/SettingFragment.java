package com.dazone.crewchatoff.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.BuildConfig;
import com.dazone.crewchatoff.HTTPs.HttpOauthRequest;
import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.CrewChatSettingActivity;
import com.dazone.crewchatoff.activity.LoginActivity;
import com.dazone.crewchatoff.activity.MainActivity;
import com.dazone.crewchatoff.activity.NotificationSettingActivity;
import com.dazone.crewchatoff.activity.ProfileUserActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.customs.AlertDialogView;
import com.dazone.crewchatoff.database.AllUserDBHelper;
import com.dazone.crewchatoff.database.BelongsToDBHelper;
import com.dazone.crewchatoff.database.ChatMessageDBHelper;
import com.dazone.crewchatoff.database.ChatRoomDBHelper;
import com.dazone.crewchatoff.database.DepartmentDBHelper;
import com.dazone.crewchatoff.database.FavoriteGroupDBHelper;
import com.dazone.crewchatoff.database.FavoriteUserDBHelper;
import com.dazone.crewchatoff.database.UserDBHelper;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.UserDto;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallBack;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.DialogUtils;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.Utils;

public class SettingFragment extends BaseFragment implements View.OnClickListener {
    private String TAG = "SettingFragment";
    private View mView;
    private UserDto userDBHelper;
    private Context mContext;
    public Prefs prefs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userDBHelper = UserDBHelper.getUser();
        prefs = CrewChatApplication.getInstance().getPrefs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.chat_setting, container, false);
        initSettingGroup();
        return mView;
    }

    private TextView tvGeneralSetting, tvLogout, tvUserName, tv_infor;
    private TextView tvNotificationSettings, tvCrewChatSettings;
    private ImageView mAvatar;

    private void initSettingGroup() {

        tvGeneralSetting = (TextView) mView.findViewById(R.id.tv_general_setting);
        tvGeneralSetting.setOnClickListener(this);
        tvLogout = (TextView) mView.findViewById(R.id.tv_logout);
        tvLogout.setOnClickListener(this);
        tvNotificationSettings = (TextView) mView.findViewById(R.id.tv_notification_settings);
        tvNotificationSettings.setOnClickListener(this);
        tvUserName = (TextView) mView.findViewById(R.id.tv_username);
        tvUserName.setOnClickListener(this);
        tvCrewChatSettings = (TextView) mView.findViewById(R.id.tv_crew_chat_settings);
        tvCrewChatSettings.setOnClickListener(this);
        mAvatar = (ImageView) mView.findViewById(R.id.iv_avatar);
        mAvatar.setOnClickListener(this);

        tv_infor = (TextView) mView.findViewById(R.id.tv_infor);
        tv_infor.setOnClickListener(this);


        String url = prefs.getServerSite() + prefs.getAvatarUrl();
        Log.d(TAG, "url:" + url);
//        ImageUtils.showCycleImageSquareFromLink(url, mAvatar, R.dimen.button_height);
        ImageUtils.showCycleImageFromLink(url, mAvatar, R.dimen.button_height);


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_username:
                goProfile();
                break;
            case R.id.iv_avatar:
                goProfile();
                break;

            case R.id.tv_notification_settings:
                Intent intent = new Intent(mContext, NotificationSettingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            case R.id.tv_general_setting:
                generalSetting();
                break;
            case R.id.tv_logout:
                logoutV2();
                //logout();
                break;
            case R.id.tv_infor:
                showInfoV2();
                //showInfor();
                break;
            case R.id.tv_crew_chat_settings:
                //TODO CHAT SETTING
                CrewChatSettingActivity.toActivity(mContext);
                break;
        }
    }


    void showInfor() {
        Prefs prefs = CrewChatApplication.getInstance().getPrefs();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.crewchat_version));

        String versionName = BuildConfig.VERSION_NAME;
        String user_version = getResources().getString(R.string.user_version) + " " + versionName;

        String lastest_version = getResources().getString(R.string.lastest_version) + " " + prefs.getSERVER_VERSION();
//        String msg = user_version + "\n\n" + lastest_version;
        String msg = user_version;
        builder.setMessage(msg);

        builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        Button b = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (b != null) {
            b.setTextColor(ContextCompat.getColor(mContext, R.color.light_black));
        }

    }

    private void showInfoV2() {
        String versionName = BuildConfig.VERSION_NAME;
        String user_version = getResources().getString(R.string.user_version) + " " + versionName;
        Utils.oneButtonAlertDialog(getActivity(), getResources().getString(R.string.about_crewchat), user_version, getResources().getString(R.string.confirm));
    }

    private void goProfile() {
//        Intent intent = new Intent(mContext, ProfileActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        Bundle bundle = new Bundle();
//        bundle.putInt(Constant.KEY_INTENT_USER_NO, userDBHelper.getId());
//        intent.putExtras(bundle);
//        startActivity(intent);


        try {
            Intent intent = new Intent(mContext, ProfileUserActivity.class);
            int userNo = 0;
            if (userDBHelper.getId() == 0) {
                userNo = CrewChatApplication.currentId;
            } else {
                userNo = userDBHelper.getId();
            }
            intent.putExtra(Constant.KEY_INTENT_USER_NO, userNo);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generalSetting() {
    }


    private void logout() {
        // Show logout confirm
        AlertDialogView.normalAlertDialogWithCancel(mContext, Utils.getString(R.string.logout_confirm_title), Utils.getString(R.string.logout_confirm), Utils.getString(R.string.no), Utils.getString(R.string.yes), new AlertDialogView.OnAlertDialogViewClickEvent() {

            @Override
            public void onOkClick(DialogInterface alertDialog) {
                doLogout();
            }

            @Override
            public void onCancelClick() {

            }
        });
    }

    private void logoutV2() {
        Utils.customAlertDialog(getActivity(), getResources().getString(R.string.app_name),Utils.getString(R.string.logout_confirm_title), Utils.getString(R.string.yes), Utils.getString(R.string.no), new DialogUtils.OnAlertDialogViewClickEvent() {
            @Override
            public void onOkClick(DialogInterface alertDialog) {
                doLogout();
            }

            @Override
            public void onCancelClick() {

            }
        });
    }

    final int LOGOUT_COMPLETE = 100;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == LOGOUT_COMPLETE) {
                Prefs prefs1 = CrewChatApplication.getInstance().getPrefs();
                final String userId = prefs1.getUserID();
                final String dm = prefs1.getDDSServer();
                final String pw = prefs1.getPass();
                prefs1.clear();
                prefs1.putUserID(userId);
                prefs1.setDDSServer(dm);
                prefs1.setPass(pw);
                prefs1.putBooleanValue(Statics.FIRST_LOGIN, false);
                prefs1.set_login_install_app(false);
                prefs1.setDataComplete(false);
                Log.d(TAG, "CLEAR ALL");
                // Finish current activity to start new activity
                ((MainActivity) getActivity()).destroyFragment();
                getActivity().finish();

//                            ShortcutBadger.applyCount(getActivity(),0);
//                            Log.d(TAG,"Log out");

                Intent intent = new Intent(mContext, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
            }
        }
    };

    private void doLogout() {
        new Prefs().putIntValue("PAGE", 0);
        String ids = new Prefs().getGCMregistrationid();
        if (!TextUtils.isEmpty(ids)) {
            HttpRequest.getInstance().DeleteDevice(ids, new BaseHTTPCallBack() {
                @Override
                public void onHTTPSuccess() {

                    HttpOauthRequest.getInstance().logout(new BaseHTTPCallBack() {
                        @Override
                        public void onHTTPSuccess() {
                            // New thread to clear all cache
                            CrewChatApplication.isAddUser = false;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    BelongsToDBHelper.clearBelong();
//                                    Log.d(TAG,"before delete AllUserDBHelper.getUser():"+AllUserDBHelper.getUser().size());
                                    AllUserDBHelper.clearUser();
//                                    Log.d(TAG,"after delete AllUserDBHelper.getUser():"+AllUserDBHelper.getUser().size());
                                    ChatRoomDBHelper.clearChatRooms();
                                    ChatMessageDBHelper.clearMessages();
                                    DepartmentDBHelper.clearDepartment();
                                    UserDBHelper.clearUser();
                                    FavoriteGroupDBHelper.clearGroups();
                                    FavoriteUserDBHelper.clearFavorites();
                                    // CrewChatApplication.getInstance().getPrefs().clear();
                                    CrewChatApplication.resetValue();
                                    CrewChatApplication.isLoggedIn = false;
                                    handler.obtainMessage(LOGOUT_COMPLETE).sendToTarget();
                                }
                            }).start();
                        }

                        @Override
                        public void onHTTPFail(ErrorDto errorDto) {
                            Log.d(TAG, "onHTTPFail 1");
                            Toast.makeText(mContext, "Logout failed !", Toast.LENGTH_LONG).show();
                        }
                    });

                }

                @Override
                public void onHTTPFail(ErrorDto errorDto) {
                    Log.d(TAG, "onHTTPFail 2");
                    Toast.makeText(mContext, "Logout failed !", Toast.LENGTH_LONG).show();
                }
            });
        }


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
}
