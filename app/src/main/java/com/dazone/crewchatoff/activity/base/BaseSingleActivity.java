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
        Toolbar toolbar = findViewById(R.id.toolbar);
        ivMore = findViewById(R.id.more_menu);
        ivCall = findViewById(R.id.call_menu);
        ivSearch = findViewById(R.id.search_menu);
        mSearchView = findViewById(R.id.searchView);
        mSearchView.setMaxWidth( Integer.MAX_VALUE );
        mSearchView.setImeOptions(mSearchView.getImeOptions() | EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fab = findViewById(R.id.fab);
        toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_status = findViewById(R.id.toolbar_status);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
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

    public void setTitle(String title) {
        toolbar_title.setText(title);
    }

    public void setStatus(String status) {
        toolbar_status.setText(status);
    }

}
