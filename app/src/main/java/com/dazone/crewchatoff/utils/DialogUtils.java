package com.dazone.crewchatoff.utils;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import com.dazone.crewchatoff.activity.ProfileUserActivity;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.R;

public class DialogUtils {
    public interface OnAlertDialogViewClickEvent {
        void onOkClick(DialogInterface alertDialog);

        void onCancelClick();
    }

    public static void showDialogUser(String name, String phoneNumber, String companyNumber, final int userNo) {
        android.support.v7.app.AlertDialog.Builder builderSingle = new android.support.v7.app.AlertDialog.Builder(BaseActivity.Instance);
        builderSingle.setTitle(name);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                CrewChatApplication.getInstance(),
                R.layout.row_chatting_call);

        arrayAdapter.add(CrewChatApplication.getInstance().getString(R.string.my_profile));

        final String phone = !TextUtils.isEmpty(phoneNumber.trim()) ?
                phoneNumber :
                !TextUtils.isEmpty(companyNumber.trim()) ?
                        companyNumber :
                        "";

        if (!TextUtils.isEmpty(phone.trim())) {
            arrayAdapter.add("Call (" + phone + ")");
        }

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intent = new Intent(BaseActivity.Instance, ProfileUserActivity.class);
                                intent.putExtra(Constant.KEY_INTENT_USER_NO, userNo);
                                BaseActivity.Instance.startActivity(intent);
                                BaseActivity.Instance.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                break;
                            case 1:
                                Utils.CallPhone(BaseActivity.Instance, phone);
                                break;
                        }
                    }
                });
        AlertDialog dialog = builderSingle.create();
        if (arrayAdapter.getCount() > 0) {
            dialog.show();
        }


        Button b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (b != null) {
            b.setTextColor(ContextCompat.getColor(CrewChatApplication.getInstance(), R.color.light_black));
        }
    }
}