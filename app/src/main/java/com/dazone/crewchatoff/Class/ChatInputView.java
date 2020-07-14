package com.dazone.crewchatoff.Class;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.customs.EmojiView;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.Utils;

public class ChatInputView extends BaseViewClass implements View.OnClickListener {
    public String TAG = "ChatInputView";
    public ImageView plus_imv, btnEmotion, btnSend;
    public Button btnVoice;
    public EditText edt_comment;
    public EmojiView mEmojiView;

    public LinearLayout selection_lnl, linearEmoji;

    public ChatInputView(Context context) {
        super(context);
        setupView();
    }

    @Override
    protected void setupView() {
        currentView = inflater.inflate(R.layout.input_text_layout, null);
        initView(currentView);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView(View v) {
        if (v == null) {
            return;
        }

        btnVoice = v.findViewById(R.id.btnVoice);

        plus_imv = v.findViewById(R.id.plus_imv);
        plus_imv.setOnClickListener(this);
        btnEmotion = v.findViewById(R.id.btnEmotion);
        btnEmotion.setOnClickListener(this);
        btnSend = v.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        edt_comment = v.findViewById(R.id.edt_comment);
        selection_lnl = v.findViewById(R.id.selection_lnl);
        selection_lnl.setVisibility(View.GONE);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) selection_lnl.getLayoutParams();
        if (CrewChatApplication.getInstance().getPrefs().getDDSServer().contains(Statics.chat_jw_group_co_kr)) {
            params.height = Utils.getDimenInPx(R.dimen.dimen_70_140);
        } else {
            params.height = Utils.getDimenInPx(R.dimen.dimen_140_280);
        }

        selection_lnl.setLayoutParams(params);
        linearEmoji = v.findViewById(R.id.linearEmoj);
        linearEmoji.setVisibility(View.GONE);
        mEmojiView = v.findViewById(R.id.emojicons);

        edt_comment.setOnTouchListener((v1, event) -> {
            if (linearEmoji.getVisibility() == View.VISIBLE) {
                linearEmoji.setVisibility(View.GONE);
            }

            if (selection_lnl.getVisibility() == View.VISIBLE) {
                selection_lnl.setVisibility(View.GONE);
            }

            return false;
        });
    }




    @Override
    public void onClick(View v) {
        int viewID = v.getId();
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        switch (viewID) {
            case R.id.plus_imv:
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                selection_lnl.removeAllViews();

                if (linearEmoji.getVisibility() == View.VISIBLE)
                    linearEmoji.setVisibility(View.GONE);

                if (selection_lnl.getVisibility() == View.GONE) {
                    selection_lnl.setVisibility(View.VISIBLE);
                    GridSelectionChatting grid = new GridSelectionChatting(selection_lnl.getContext());
                    grid.addToView(selection_lnl);
                    Log.d(TAG, "addToView");
                } else {
                    Log.d(TAG, "removeAllViews");
                    selection_lnl.removeAllViews();
                    selection_lnl.setVisibility(View.GONE);
                }
                break;

            case R.id.btnEmotion:
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                if (selection_lnl.getVisibility() == View.VISIBLE) {
                    selection_lnl.setVisibility(View.GONE);
                }
                if (linearEmoji.getVisibility() == View.GONE) {
                    linearEmoji.setVisibility(View.VISIBLE);
                } else {
                    linearEmoji.setVisibility(View.GONE);
                }
                break;
        }
    }


}