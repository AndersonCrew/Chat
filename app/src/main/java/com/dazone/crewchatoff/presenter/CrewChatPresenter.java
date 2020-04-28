package com.dazone.crewchatoff.presenter;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;

public class CrewChatPresenter implements CompoundButton.OnCheckedChangeListener {
    private Context context;
    private Dialog mDialog;
    private Prefs mPrefs;
    private RadioButton rd_automatic;
    private RadioButton rd_portrait;
    private RadioButton rd_landscape;
    private RotationInterface mRotationInterface;

    public CrewChatPresenter(Context context, RotationInterface mRotationInterface) {
        this.mRotationInterface = mRotationInterface;
        this.context = context;
        mPrefs = CrewChatApplication.getInstance().getPrefs();
    }

    public void showDialog(String msg) {
        mDialog = new Dialog(context);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(true);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setContentView(R.layout.dialog_group_radiobutton);
        rd_automatic = mDialog.findViewById(R.id.rd_automatic);
        rd_portrait = mDialog.findViewById(R.id.rd_portrait);
        rd_landscape = mDialog.findViewById(R.id.rd_landscape);
        rd_automatic.setOnCheckedChangeListener(this);
        rd_portrait.setOnCheckedChangeListener(this);
        rd_landscape.setOnCheckedChangeListener(this);
        setRotation();
        mDialog.show();

    }

    private void setRotationEvent() {
        int rotation = mPrefs.getIntValue(Statics.SCREEN_ROTATION, Constant.AUTOMATIC);
        switch (rotation) {
            case Constant.AUTOMATIC:
            case Constant.PORTRAIT:
                mRotationInterface.rotationScreen();
                break;
            case Constant.LANSCAPE:
                mRotationInterface.rotationScreen();
                break;
        }
    }

    private void setRotation() {
        int rotation = mPrefs.getIntValue(Statics.SCREEN_ROTATION, Constant.PORTRAIT);
        switch (rotation) {
            case Constant.AUTOMATIC:
                rd_automatic.setChecked(true);
                break;
            case Constant.PORTRAIT:
                rd_portrait.setChecked(true);
                break;
            case Constant.LANSCAPE:
                rd_landscape.setChecked(true);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked) {
            switch (buttonView.getId()) {
                case R.id.rd_automatic:
                    mPrefs.putIntValue(Statics.SCREEN_ROTATION, Constant.AUTOMATIC);
                    setRotationEvent();
                    mDialog.dismiss();
                    break;
                case R.id.rd_portrait:
                    mPrefs.putIntValue(Statics.SCREEN_ROTATION, Constant.PORTRAIT);
                    setRotationEvent();
                    mDialog.dismiss();
                    break;
                case R.id.rd_landscape:
                    mPrefs.putIntValue(Statics.SCREEN_ROTATION, Constant.LANSCAPE);
                    setRotationEvent();
                    mDialog.dismiss();
                    break;
            }
        } else {
            switch (buttonView.getId()) {
                case R.id.rd_automatic:
                    break;
                case R.id.rd_portrait:
                    break;
                case R.id.rd_landscape:
                    break;
            }
        }
    }

    /**
     * set value to Textview Rotation
     *
     * @param tv
     */
    public void rotationSettingValue(TextView tv) {
        int rotation = mPrefs.getIntValue(Statics.SCREEN_ROTATION, Constant.PORTRAIT);

        switch (rotation) {
            case Constant.AUTOMATIC:
                tv.setText(context.getString(R.string.automatic));
                break;
            case Constant.PORTRAIT:
                tv.setText(context.getString(R.string.portrait));
                break;
            case Constant.LANSCAPE:
                tv.setText(context.getString(R.string.landscape));
                break;
            default:
                tv.setText(context.getString(R.string.portrait));
        }
    }

    public interface RotationInterface {
        void rotationScreen();

    }
}
