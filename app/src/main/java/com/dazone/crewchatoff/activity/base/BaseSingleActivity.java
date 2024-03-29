package com.dazone.crewchatoff.activity.base;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import com.dazone.crewchatoff.R;

public abstract class BaseSingleActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        addFragment(savedInstanceState);
    }

    protected FloatingActionButton fab;
    protected TextView toolbar_title, toolbar_status;
    protected ImageView ivMore;
    protected ImageView ivCall;
    protected ImageView ivSearch;
    protected SearchView mSearchView;

    protected void init() {
        setContentView(R.layout.activity_base_single);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ivMore = (ImageView) findViewById(R.id.more_menu);
        ivCall = (ImageView) findViewById(R.id.call_menu);
        ivSearch = (ImageView) findViewById(R.id.search_menu);
        mSearchView = (SearchView) findViewById(R.id.searchView);
        mSearchView.setMaxWidth( Integer.MAX_VALUE );
        mSearchView.setImeOptions(mSearchView.getImeOptions() | EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_status = (TextView) findViewById(R.id.toolbar_status);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void setUPToolBar(String title, String status) {
        if (TextUtils.isEmpty(title)) {
            toolbar_title.setText(mContext.getResources().getString(R.string.unknown));
            toolbar_title.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
            toolbar_status.setText("");
        } else {
            toolbar_title.setText(title);
            toolbar_title.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            toolbar_status.setText(status);
        }

    }


    protected abstract void addFragment(Bundle bundle);

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
        }
        return false;
    }


    public void showSave() {
        ivCall.setVisibility(View.GONE);
        ivMore.setImageResource(R.drawable.add_check);
    }

    public void HiddenTitle() {
        toolbar_title.setVisibility(View.GONE);
        toolbar_status.setVisibility(View.GONE);
    }

    public void HideStatus() {
        toolbar_status.setVisibility(View.GONE);
    }

    public void HideBtnMore() {
        ivMore.setVisibility(View.GONE);
    }

    public void setTitle(String title) {
        toolbar_title.setText(title);
    }

    public void setStatus(String status) {
        toolbar_status.setText(status);
    }

}
