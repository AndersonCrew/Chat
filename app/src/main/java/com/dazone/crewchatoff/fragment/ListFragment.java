package com.dazone.crewchatoff.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.MainActivity;
import com.dazone.crewchatoff.adapter.PullUpLoadMoreRCVAdapter;
import com.dazone.crewchatoff.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public abstract class ListFragment<T> extends Fragment {
    String TAG = "ListFragment";
    public PullUpLoadMoreRCVAdapter adapterList;
    public List<T> dataSet;
    protected HttpRequest mHttpRequest;
    public RecyclerView rvMainList;
    public TextView tvUpdateTime;
    public RelativeLayout rlNewMessage, lnNoData, layoutSpeak;
    public TextView tvUserNameMessage;
    public ImageView ivScrollDown;
    protected LinearLayout progressBar;
    protected LinearLayout recycler_footer;
    protected RelativeLayout list_content_rl;
    protected TextView no_item_found;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected String str_lastID = "";
    protected int lastID = 0;
    public LinearLayoutManager layoutManager;
    protected Context mContext;
    protected FloatingActionButton fab;
    protected EditText mInputSearch;
    @SuppressLint("StaticFieldLeak")
    public static ListFragment instance = null;

    public void setContext(Context context) {
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        mHttpRequest = HttpRequest.getInstance();
        dataSet = new ArrayList<>();
    }

    public void showLnNodata() {
        lnNoData.setVisibility(View.VISIBLE);
        rvMainList.setVisibility(View.GONE);
    }

    public void hideLnNodata() {
        lnNoData.setVisibility(View.GONE);
        rvMainList.setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list, container, false);
        lnNoData = v.findViewById(R.id.lnNoData);
        progressBar = v.findViewById(R.id.progressBar);
        rvMainList = v.findViewById(R.id.rv_main);
        rlNewMessage = v.findViewById(R.id.rl_new_message);
        layoutSpeak = v.findViewById(R.id.layoutSpeak);
        tvUserNameMessage = v.findViewById(R.id.tv_user_message);
        ivScrollDown = v.findViewById(R.id.iv_scroll_down);
        recycler_footer = v.findViewById(R.id.recycler_footer);
        list_content_rl = v.findViewById(R.id.list_content_rl);
        no_item_found = v.findViewById(R.id.no_item_found);
        tvUpdateTime = v.findViewById(R.id.tvUpdateTime);

        fab = v.findViewById(R.id.fab);
        mInputSearch = v.findViewById(R.id.inputSearch);
        mInputSearch.setImeOptions(mInputSearch.getImeOptions() | EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);
        swipeRefreshLayout = v.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setEnabled(false);

        mInputSearch.addTextChangedListener(mWatcher);
        if (mInputSearch == null) Log.d(TAG, "init mInputSearch null");
        else Log.d(TAG, "init mInputSearch not null");
        setupRecyclerView();
        initList();
        return v;
    }

    public void setTimer(String timer) {
        if (tvUpdateTime != null) tvUpdateTime.setText(timer);
    }

    protected void scrollEndList(int position) {
        rvMainList.smoothScrollToPosition(position);
    }

    private TextWatcher mWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (adapterList != null) {
                adapterList.filterRecentFavorite(s.toString());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    public void justHide() {
        // Send broadcast to show search view input
        if (!isShowIcon) {
            Log.d(TAG, "! isShowIcon");
        } else {
            Log.d(TAG, "isShowIcon");
            hideSearchInput();
            isShowIcon = false;
            if (MainActivity.instance != null) MainActivity.instance.showPAB();
        }
    }


    boolean isShowIcon = false;

    public void searchAction(int type) {
        // Send broadcast to show search view input
        if (type == 1) {
            if (!isShowIcon) {
                Log.d(TAG, "! isShowIcon");
                showSearchInput();
                isShowIcon = true;
                if (MainActivity.instance != null) MainActivity.instance.hidePAB();
            } else {
                Log.d(TAG, "isShowIcon");
                hideSearchInput();
                isShowIcon = false;
                if (MainActivity.instance != null) MainActivity.instance.showPAB();
            }
        } else {
            // for tab chat list click
            if (isShowIcon) {
                Log.d(TAG, "isShowIcon");
                hideSearchInput();
                isShowIcon = false;
                if (MainActivity.instance != null) MainActivity.instance.showPAB();

            }
        }
    }

    public void showSearchInput() {
        if (this.mInputSearch != null) {
            this.mInputSearch.setVisibility(View.VISIBLE);
            this.mInputSearch.post(new Runnable() {
                @Override
                public void run() {
                    mInputSearch.requestFocus();
                    InputMethodManager img = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    img.showSoftInput(mInputSearch, InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }
    }

    public void hideSearchInput() {
        if (this.mInputSearch != null) {
            this.mInputSearch.setText("");
            this.mInputSearch.setVisibility(View.GONE);
            if (getActivity() != null) {
                Utils.hideKeyboard(getActivity());
            }
        }
    }

    protected void setupRecyclerView() {
        rvMainList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvMainList.setLayoutManager(layoutManager);
        initAdapter();
        rvMainList.setAdapter(adapterList);
    }

    public void disableSwipeRefresh() {
        swipeRefreshLayout.setEnabled(false);
    }

    protected abstract void initAdapter();

    //protected abstract void addMoreItem();
    protected abstract void initList();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
}