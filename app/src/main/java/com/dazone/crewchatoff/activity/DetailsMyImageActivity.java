package com.dazone.crewchatoff.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.customs.ImageViewZoomSupport;

/**
 * Created by maidinh on 7/2/2017.
 */

public class DetailsMyImageActivity extends AppCompatActivity {
    ImageViewZoomSupport imageView;
    String url = "";
    String TAG = "DetailsMyImageActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.details_my_image_layout);
        imageView = (ImageViewZoomSupport) findViewById(R.id.imageView);
        url = getIntent().getStringExtra(Statics.CHATTING_DTO_GALLERY_SHOW_FULL);
        Log.d(TAG, "url:" + url);

        Glide.with(this)
                .load(url)
                .asBitmap()
                .placeholder(R.drawable.loading)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.loading)
                .fallback(R.drawable.loading)
                .into(imageView);
    }
}
