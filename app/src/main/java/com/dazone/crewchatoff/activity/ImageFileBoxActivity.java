package com.dazone.crewchatoff.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.adapter.ImageFileBoxAdapter;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.AttachImageList;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.interfaces.GetIvFileBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by maidinh on 6/2/2017.
 */

public class ImageFileBoxActivity extends AppCompatActivity {
    String TAG = "ImageFileBoxActivity";
    RecyclerView rvIvFileBox;
    GridLayoutManager lLayout;
    ImageFileBoxAdapter mAdapter;
    ProgressBar progressBar;
    List<AttachImageList> imagesURL;
    TextView tvNodata;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_file_box_layout);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        init();
    }

    long roomNo = -1;

    void init() {
        roomNo = getIntent().getLongExtra(Statics.ROOM_NO, -1);
        tvNodata = findViewById(R.id.tvNodata);
        progressBar = findViewById(R.id.progressBar);
        imagesURL = new ArrayList<>();

        List<TreeUserDTOTemp> allUser = null;

        if (CompanyFragment.instance != null) allUser = CompanyFragment.instance.getUser();
        if (allUser == null) allUser = new ArrayList<>();


        mAdapter = new ImageFileBoxAdapter(this, imagesURL, allUser);
        rvIvFileBox = findViewById(R.id.rvIvFileBox);
        lLayout = new GridLayoutManager(ImageFileBoxActivity.this, 3);
        rvIvFileBox.setLayoutManager(lLayout);
        rvIvFileBox.setAdapter(mAdapter);
        HttpRequest.getInstance().getAttachFileList(new GetIvFileBox() {
            @Override
            public void onSuccess(List<AttachImageList> lst) {
                progressBar.setVisibility(View.GONE);
                tvNodata.setVisibility(View.GONE);

                for (AttachImageList obj : lst) {
                    if (obj.getType() == 1 && roomNo == obj.getRoomNo()) {
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
