package com.dazone.crewchatoff.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.adapter.AttachFileBoxAdapter;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.AttachImageList;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.interfaces.GetIvFileBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by maidinh on 7/2/2017.
 */

public class AttachFileBoxActivity extends AppCompatActivity {
    String TAG = "AttachFileBoxActivity";
    RecyclerView rvIvFileBox;
    LinearLayoutManager lLayout;
    AttachFileBoxAdapter mAdapter;
    ProgressBar progressBar;
    List<AttachImageList> imagesURL;
    TextView tvNodata;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attach_file_box_layout);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        init();
    }

    long roomNo = -1;

    void init() {
        roomNo = getIntent().getLongExtra(Statics.ROOM_NO, -1);
        Log.d(TAG, "roomNo:" + roomNo);
        tvNodata = (TextView) findViewById(R.id.tvNodata);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imagesURL = new ArrayList<>();

        List<TreeUserDTOTemp> allUser = null;
        if (CompanyFragment.instance != null) allUser = CompanyFragment.instance.getUser();
        if (allUser == null) allUser = new ArrayList<>();

        mAdapter = new AttachFileBoxAdapter(this, imagesURL, allUser);
        rvIvFileBox = (RecyclerView) findViewById(R.id.rvIvFileBox);
        lLayout = new LinearLayoutManager(AttachFileBoxActivity.this);


        rvIvFileBox.setLayoutManager(lLayout);
        rvIvFileBox.setAdapter(mAdapter);
        HttpRequest.getInstance().getAttachFileList(new GetIvFileBox() {
            @Override
            public void onSuccess(List<AttachImageList> lst) {
                progressBar.setVisibility(View.GONE);
                tvNodata.setVisibility(View.GONE);
                for (AttachImageList obj : lst) {
                    if (obj.getType() != 1 && roomNo == obj.getRoomNo()) {
//                    if (obj.getType() != 1) {
                        imagesURL.add(obj);
                    }
                }
                if (imagesURL.size() > 0) {
                    Collections.reverse(imagesURL);
                    mAdapter.updateList(imagesURL);
                } else {
                    tvNodata.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onFail() {
                progressBar.setVisibility(View.GONE);
                tvNodata.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return true;
    }
}
