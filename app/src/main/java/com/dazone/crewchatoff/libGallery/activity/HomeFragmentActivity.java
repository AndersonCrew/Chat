/*
 * Copyright 2013 - learnNcode (learnncode@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.dazone.crewchatoff.libGallery.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.libGallery.MediaChooser;
import com.dazone.crewchatoff.libGallery.MediaChooserConstants;
import com.dazone.crewchatoff.libGallery.fragment.ImageFragment;
import com.dazone.crewchatoff.libGallery.fragment.VideoFragment;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.dazone.crewchatoff.libGallery.MediaChooserConstants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE;


public class HomeFragmentActivity extends FragmentActivity implements ImageFragment.OnImageSelectedListener,
        VideoFragment.OnVideoSelectedListener, OnClickListener {


    private FragmentTabHost mTabHost;
    private TextView headerBarTitle;
    private ImageView headerBarCamera;
    private ImageView headerBarBack;
    private TextView headerBarDone;

    private static Uri fileUri;

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_home_media_chooser);

        headerBarTitle = (TextView) findViewById(R.id.titleTextViewFromMediaChooserHeaderBar);
        headerBarCamera = (ImageView) findViewById(R.id.cameraImageViewFromMediaChooserHeaderBar);
        headerBarBack = (ImageView) findViewById(R.id.backArrowImageViewFromMediaChooserHeaderView);
        headerBarDone = (TextView) findViewById(R.id.doneTextViewViewFromMediaChooserHeaderView);
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);


        mTabHost.setup(this, getSupportFragmentManager(), R.id.realTabcontent);
        headerBarBack.setOnClickListener(this);
        headerBarCamera.setOnClickListener(this);
        headerBarDone.setOnClickListener(this);

        if (!MediaChooserConstants.showCameraVideo) {
            headerBarCamera.setVisibility(View.GONE);
        }


        if (getIntent() != null && (getIntent().getBooleanExtra("isFromBucket", false))) {

            if (getIntent().getBooleanExtra("image", false)) {
                headerBarTitle.setText(getResources().getString(R.string.image));
                setHeaderBarCameraBackground(getResources().getDrawable(R.drawable.selector_camera_button));

                headerBarCamera.setTag(getResources().getString(R.string.image));

                Bundle bundle = new Bundle();
                bundle.putString("name", getIntent().getStringExtra("name"));
                mTabHost.addTab(mTabHost.newTabSpec("tab1").setIndicator(getResources().getString(R.string.images_tab) + "     "), ImageFragment.class, bundle);

            } else {
                headerBarTitle.setText(getResources().getString(R.string.video));
                setHeaderBarCameraBackground(getResources().getDrawable(R.drawable.selector_video_button));
                headerBarCamera.setTag(getResources().getString(R.string.video));

                Bundle bundle = new Bundle();
                bundle.putString("name", getIntent().getStringExtra("name"));
                mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator(getResources().getString(R.string.videos_tab) + "      "), VideoFragment.class, bundle);
            }
        } else {

            if (MediaChooserConstants.showVideo) {
                mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator(getResources().getString(R.string.videos_tab) + "      "), VideoFragment.class, null);
            }

            if (MediaChooserConstants.showImage) {
                headerBarTitle.setText(getResources().getString(R.string.image));
                setHeaderBarCameraBackground(getResources().getDrawable(R.drawable.selector_camera_button));
                headerBarCamera.setTag(getResources().getString(R.string.image));

                mTabHost.addTab(mTabHost.newTabSpec("tab1").setIndicator(getResources().getString(R.string.images_tab) + "      "), ImageFragment.class, null);
            }

            if (MediaChooserConstants.showVideo) {
                headerBarTitle.setText(getResources().getString(R.string.video));
                setHeaderBarCameraBackground(getResources().getDrawable(R.drawable.selector_video_button));
                headerBarCamera.setTag(getResources().getString(R.string.video));
            }
        }

        mTabHost.getTabWidget().getChildAt(0).setVisibility(View.GONE);

        for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {

            TextView textView = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            if (textView.getLayoutParams() instanceof RelativeLayout.LayoutParams) {

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) textView.getLayoutParams();
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                params.addRule(RelativeLayout.CENTER_VERTICAL);
                params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                params.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                textView.setLayoutParams(params);

            } else if (textView.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textView.getLayoutParams();
                params.gravity = Gravity.CENTER;
                textView.setLayoutParams(params);
            }
            textView.setTextColor(getResources().getColor(R.color.tabs_title_color));
            textView.setTextSize(Utils.convertDipToPixels(this, 10));

        }

        if ((mTabHost.getTabWidget().getChildAt(0) != null)) {
            ((TextView) (mTabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title))).setTextColor(Color.WHITE);
        }

        if ((mTabHost.getTabWidget().getChildAt(1) != null)) {
            ((TextView) (mTabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title))).setTextColor(getResources().getColor(R.color.headerbar_selected_tab_color));
        }

        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {

                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                ImageFragment imageFragment = (ImageFragment) fragmentManager.findFragmentByTag("tab1");
                VideoFragment videoFragment = (VideoFragment) fragmentManager.findFragmentByTag("tab2");
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


                if (tabId.equalsIgnoreCase("tab1")) {

                    headerBarTitle.setText(getResources().getString(R.string.image));
                    setHeaderBarCameraBackground(getResources().getDrawable(R.drawable.selector_camera_button));
                    headerBarCamera.setTag(getResources().getString(R.string.image));

                    if (imageFragment != null) {

                        if (videoFragment != null) {
                            fragmentTransaction.hide(videoFragment);
                        }
                        fragmentTransaction.show(imageFragment);
                    }
                    ((TextView) (mTabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title))).setTextColor(getResources().getColor(R.color.headerbar_selected_tab_color));
                    ((TextView) (mTabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title))).setTextColor(Color.WHITE);

                } else {
                    headerBarTitle.setText(getResources().getString(R.string.video));
                    setHeaderBarCameraBackground(getResources().getDrawable(R.drawable.selector_video_button));
                    headerBarCamera.setTag(getResources().getString(R.string.video));

                    if (videoFragment != null) {

                        if (imageFragment != null) {
                            fragmentTransaction.hide(imageFragment);
                        }

                        fragmentTransaction.show(videoFragment);
                        if (videoFragment.getAdapter() != null) {
                            videoFragment.getAdapter().notifyDataSetChanged();
                        }
                    }
                    ((TextView) (mTabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title))).setTextColor(Color.WHITE);
                    ((TextView) (mTabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title))).setTextColor(getResources().getColor(R.color.headerbar_selected_tab_color));
                }

                fragmentTransaction.commit();
            }
        });

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) headerBarCamera.getLayoutParams();
        params.height = Utils.convertDipToPixels(this, 40);
        params.width = Utils.convertDipToPixels(this, 40);
        headerBarCamera.setLayoutParams(params);
        headerBarCamera.setScaleType(ScaleType.CENTER_INSIDE);
        headerBarCamera.setPadding(Utils.convertDipToPixels(this, 15), Utils.convertDipToPixels(this, 15), Utils.convertDipToPixels(this, 15), Utils.convertDipToPixels(this, 15));

    }

    /**
     * Create a file Uri for saving an image or video
     */
    private Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), MediaChooserConstants.folderName);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MediaChooserConstants.MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else if (type == MediaChooserConstants.MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MediaChooserConstants.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, fileUri));
                final AlertDialog alertDialog = MediaChooserConstants.getDialog(HomeFragmentActivity.this).create();
                alertDialog.show();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 5000ms
                        String fileUriString = fileUri.toString().replaceFirst("file:///", "/").trim();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        VideoFragment videoFragment = (VideoFragment) fragmentManager.findFragmentByTag("tab2");
                        //
                        if (videoFragment == null) {
                            VideoFragment newVideoFragment = new VideoFragment();
                            newVideoFragment.addItem(fileUriString);

                        } else {
                            videoFragment.addItem(fileUriString);
                        }
                        alertDialog.cancel();
                    }
                }, 5000);


            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the video capture
            } else {
                // Video capture failed, advise user
            }
        } else if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, fileUri));

                final AlertDialog alertDialog = MediaChooserConstants.getDialog(HomeFragmentActivity.this).create();
                alertDialog.show();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 5000ms
                        String fileUriString = fileUri.toString().replaceFirst("file:///", "/").trim();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        ImageFragment imageFragment = (ImageFragment) fragmentManager.findFragmentByTag("tab1");
                        if (imageFragment == null) {
                            ImageFragment newImageFragment = new ImageFragment();
                            newImageFragment.addItem(fileUriString);


                        } else {
                            imageFragment.addItem(fileUriString);
                        }

                        alertDialog.cancel();
                    }
                }, 5000);
            }
        }
    }

    @Override
    public void onImageSelected(int count) {
        if (mTabHost.getTabWidget().getChildAt(1) != null) {
            if (count != 0) {
                ((TextView) mTabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title)).setText(getResources().getString(R.string.images_tab) + "  " + count);

            } else {
                ((TextView) mTabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title)).setText(getResources().getString(R.string.image));
            }
        } else {
            if (count != 0) {
                ((TextView) mTabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title)).setText(getResources().getString(R.string.images_tab) + "  " + count);

            } else {
                ((TextView) mTabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title)).setText(getResources().getString(R.string.image));
            }
        }
    }


    @Override
    public void onVideoSelected(int count) {
        if (count != 0) {
            ((TextView) mTabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title)).setText(getResources().getString(R.string.videos_tab) + "  " + count);

        } else {
            ((TextView) mTabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title)).setText(getResources().getString(R.string.video));
        }
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private void setHeaderBarCameraBackground(Drawable drawable) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            headerBarCamera.setBackgroundDrawable(drawable);
        } else {
            headerBarCamera.setBackground(drawable);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cameraImageViewFromMediaChooserHeaderBar:
                if (v.getTag().toString().equals(getResources().getString(R.string.video))) {
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    fileUri = getOutputMediaFileUri(MediaChooserConstants.MEDIA_TYPE_VIDEO); // create a file to save the image
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                    // start the image capture Intent
                    startActivityForResult(intent, MediaChooserConstants.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);

                } else {
                   /* Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    fileUri = getOutputMediaFileUri(MediaChooserConstants.MEDIA_TYPE_IMAGE); // create a file to save the image
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                    // start the image capture Intent
                    startActivityForResult(intent, MediaChooserConstants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);*/
                    if (ChattingActivity.instance.checkPermissionsCamera()) {
                        try {
                            captureImage();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        ChattingActivity.instance.setPermissionsCamera();
                    }
                }
                break;
            case R.id.doneTextViewViewFromMediaChooserHeaderView:
                FragmentManager fragmentManager = getSupportFragmentManager();
                ImageFragment imageFragment = (ImageFragment) fragmentManager.findFragmentByTag("tab1");
                VideoFragment videoFragment = (VideoFragment) fragmentManager.findFragmentByTag("tab2");

                if (videoFragment != null || imageFragment != null) {

                    if (videoFragment != null) {
                        if (videoFragment.getSelectedVideoList() != null && videoFragment.getSelectedVideoList().size() > 0) {
                            Intent videoIntent = new Intent();
                            videoIntent.setAction(MediaChooser.VIDEO_SELECTED_ACTION_FROM_MEDIA_CHOOSER);
                            videoIntent.putStringArrayListExtra("list", videoFragment.getSelectedVideoList());
                            sendBroadcast(videoIntent);
                        } else {
                            Toast.makeText(HomeFragmentActivity.this, getString(R.string.please_select_file), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    if (imageFragment != null) {
                        if (imageFragment.getSelectedImageList() != null && imageFragment.getSelectedImageList().size() > 0) {
                            Intent imageIntent = new Intent();
                            imageIntent.setAction(MediaChooser.IMAGE_SELECTED_ACTION_FROM_MEDIA_CHOOSER);
                            imageIntent.putStringArrayListExtra("list", imageFragment.getSelectedImageList());
                            sendBroadcast(imageIntent);
                        } else {
                            Toast.makeText(HomeFragmentActivity.this, getString(R.string.please_select_file), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    if (BucketHomeFragmentActivity.Instance != null) {
                        BucketHomeFragmentActivity.Instance.finish();
                    }
                    finish();
                } else {
                    Toast.makeText(HomeFragmentActivity.this, getString(R.string.please_select_file), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.backArrowImageViewFromMediaChooserHeaderView:
                finish();
                break;
            default:
                break;
        }
    }




    //Capture camera
    private void captureImage() {
        if (Build.VERSION.SDK_INT > 23) {
            // for android >= 7
            Uri mPhotoUri = CrewChatApplication.getInstance().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new ContentValues());
            fileUri = Constant.convertUri(CrewChatApplication.getInstance(), mPhotoUri);
            ChattingActivity.setOutputMediaFileUri_v7(fileUri);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

        } else {
            // for android < 7
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            fileUri = getOutputMediaFileUri(MediaChooserConstants.MEDIA_TYPE_IMAGE); // create a file to save the image
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
            // start the image capture Intent
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }
}
