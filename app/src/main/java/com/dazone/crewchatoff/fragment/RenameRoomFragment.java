package com.dazone.crewchatoff.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.RenameRoomActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallBack;
import com.dazone.crewchatoff.interfaces.OnTickCallback;
import com.dazone.crewchatoff.interfaces.OnTickCallbackSuccess;
import com.dazone.crewchatoff.utils.CrewChatApplication;

public class RenameRoomFragment extends BaseFragment {
    String TAG="RenameRoomFragment";
    private View rootView;
    private TextView tvRemainCharacterCount;
    private EditText et_title;
    private Button btnClear;

    private String roomTitle;
    private int roomNo;

    private OnTickCallbackSuccess mTickSuccessCallback;

    public void setTickSuccessCallback(OnTickCallbackSuccess mTickSuccessCallback) {
        this.mTickSuccessCallback = mTickSuccessCallback;
    }

    public RenameRoomFragment() {
        // Required empty public constructor
    }

    public static RenameRoomFragment newInstance(int roomNo, String roomTitle) {
        Log.d("RenameRoomFragment","roomNo:"+roomNo);
        RenameRoomFragment myFragment = new RenameRoomFragment();
        Bundle args = new Bundle();
        args.putInt(Statics.ROOM_NO, roomNo);
        args.putString(Statics.ROOM_TITLE, roomTitle);
        myFragment.setArguments(args);
        return myFragment;
    }

    OnTickCallback callback = new OnTickCallback() {
        @Override
        public void onTick() {
            // request to API
            final String titleChanged = et_title.getText().toString().trim();
            if (roomTitle.compareTo(titleChanged) == 0) {
                Toast.makeText(mContext, CrewChatApplication.getInstance().getResources().getString(R.string.warning_change_title), Toast.LENGTH_LONG).show();
                return;
            }

            HttpRequest.getInstance().updateChatRoomInfo(roomNo, titleChanged, new BaseHTTPCallBack() {
                @Override
                public void onHTTPSuccess() {

                    if (mTickSuccessCallback != null) {
                        mTickSuccessCallback.onTickSuccess(roomNo, titleChanged);
                    }
                }

                @Override
                public void onHTTPFail(ErrorDto errorDto) {

                }
            });
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((RenameRoomActivity) getActivity()).setCallback(callback);

        // Get all bundle is parsed to here
        Bundle args = getArguments();
        if (args != null) {
            roomTitle = args.getString(Statics.ROOM_TITLE, "");
            roomNo = args.getInt(Statics.ROOM_NO, -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_rename_room, container, false);
        initView();
        initData();

        return rootView;
    }

    private void initView() {
        tvRemainCharacterCount = (TextView) rootView.findViewById(R.id.tv_remain_character_count);
        et_title = (EditText) rootView.findViewById(R.id.et_title);
        btnClear = (Button) rootView.findViewById(R.id.btn_clear);
    }

    private void initData() {
        et_title.setText("");
        et_title.append(roomTitle);
        et_title.requestFocus();

        if (!TextUtils.isEmpty(et_title.getText().toString().trim())) {
            btnClear.setVisibility(View.VISIBLE);
        }

        //set on text change listener for edittext
        et_title.addTextChangedListener(textWatcher());
        //set event for clear button
        btnClear.setOnClickListener(onClickListener());
    }

    private View.OnClickListener onClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_title.setText("");
            }
        };
    }

    private TextWatcher textWatcher() {
        return new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!et_title.getText().toString().equals("")) { //if edittext include text
                    btnClear.setVisibility(View.VISIBLE);
                    int remain = 50 - et_title.getText().length();
                    String remainText = remain + "/50";
                    tvRemainCharacterCount.setText(remainText);
                } else { //not include text
                    btnClear.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }
}