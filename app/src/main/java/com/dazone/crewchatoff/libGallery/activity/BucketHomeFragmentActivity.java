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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.libGallery.MediaChooser;
import com.dazone.crewchatoff.libGallery.MediaChooserConstants;
import com.dazone.crewchatoff.libGallery.fragment.BucketImageFragment;
import com.dazone.crewchatoff.libGallery.fragment.BucketVideoFragment;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.dazone.crewchatoff.constant.Statics.CHOOSE_OPTION_IMAGE;
import static com.dazone.crewchatoff.libGallery.MediaChooserConstants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE;


public class BucketHomeFragmentActivity extends FragmentActivity {
    private String TAG="BucketHomeFragmentActivity";
    private FragmentTabHost mTabHost;
    private TextView headerBarTitle;
    private ImageView headerBarCamera;
    private ImageView headerBarBack;
    private ImageView image_option;
    private TextView headerBarDone;
    public static BucketHomeFragmentActivity Instance = null;
    private static Uri fileUri;
    private ArrayList<String> mSelectedVideo = new ArrayList<>();
    private ArrayList<String> mSelectedImage = new ArrayList<>();
    private final Handler handler = new Handler();
    private Prefs prefs;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Instance = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_home_media_chooser);

        prefs = CrewChatApplication.getInstance().getPrefs();
        headerBarTitle = (TextView) findViewById(R.id.titleTextViewFromMediaChooserHeaderBar);
        headerBarCamera = (ImageView) findViewById(R.id.cameraImageViewFromMediaChooserHeaderBar);
        headerBarBack = (ImageView) findViewById(R.id.backArrowImageViewFromMediaChooserHeaderView);
        image_option = (ImageView) findViewById(R.id.image_option);
        image_option.setVisibility(View.VISIBLE);
        image_option.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getApplicationContext(), v);

                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
                setCheckMenu(popup);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.Standard:
                                prefs.putIntValue(CHOOSE_OPTION_IMAGE, Statics.STANDARD);
                                return true;
                            case R.id.High:
                                prefs.putIntValue(CHOOSE_OPTION_IMAGE, Statics.HIGH);
                                return true;
                            case R.id.Original:
                                prefs.putIntValue(CHOOSE_OPTION_IMAGE, Statics.ORIGINAL);
                                return true;
                            default:
                                return false;

                        }

                    }

                });
                popup.show();//showing popup menu
            }
        });
        headerBarDone = (TextView) findViewById(R.id.doneTextViewViewFromMediaChooserHeaderView);
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);

        headerBarTitle.setText(getResources().getString(R.string.app_name));
        headerBarCamera.setBackgroundResource(R.drawable.ic_camera_unselect_from_media_chooser_header_bar);
        headerBarCamera.setTag(getResources().getString(R.string.image));

        headerBarBack.setOnClickListener(clickListener);
        headerBarCamera.setOnClickListener(clickListener);
        headerBarDone.setOnClickListener(clickListener);

        if (!MediaChooserConstants.showCameraVideo) {
            headerBarCamera.setVisibility(View.GONE);
        }

        mTabHost.setup(this, getSupportFragmentManager(), R.id.realTabcontent);


        if (MediaChooserConstants.showVideo) {
            image_option.setVisibility(View.GONE);
            mTabHost.addTab(
                    mTabHost.newTabSpec("tab2").setIndicator(getResources().getString(R.string.videos_tab) + "      "),
                    BucketVideoFragment.class, null);
        }

        if (MediaChooserConstants.showImage) {
            mTabHost.addTab(
                    mTabHost.newTabSpec("tab1").setIndicator(getResources().getString(R.string.images_tab) + "      "),
                    BucketImageFragment.class, null);
        }

        mTabHost.getTabWidget().setBackgroundColor(getResources().getColor(R.color.tabs_color));
        mTabHost.getTabWidget().getChildAt(0).setVisibility(View.GONE);

        for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {

            View childView = mTabHost.getTabWidget().getChildAt(i);
            TextView textView = (TextView) childView.findViewById(android.R.id.title);


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
            textView.setTextColor(ContextCompat.getColor(this, R.color.tabs_title_color));
            textView.setTextSize(convertDipToPixels(10));
        }

/*		((TextView)(mTabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title))).setTextColor(getResources().getColor(R.color.headerbar_selected_tab_color));
        ((TextView)(mTabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title))).setTextColor(Color.WHITE);*/


        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {

                FragmentManager fragmentManager = getSupportFragmentManager();
                BucketImageFragment imageFragment = (BucketImageFragment) fragmentManager.findFragmentByTag("tab1");
                BucketVideoFragment videoFragment = (BucketVideoFragment) fragmentManager.findFragmentByTag("tab2");
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                if (tabId.equalsIgnoreCase("tab1")) {

                    headerBarTitle.setText(getResources().getString(R.string.image));
                    headerBarCamera.setBackgroundResource(R.drawable.selector_camera_button);
                    headerBarCamera.setTag(getResources().getString(R.string.image));

                    if (imageFragment == null) {
                        BucketImageFragment newImageFragment = new BucketImageFragment();
                        fragmentTransaction.add(R.id.realTabcontent, newImageFragment, "tab1");

                    } else {

                        if (videoFragment != null) {
                            fragmentTransaction.hide(videoFragment);
                        }

                        fragmentTransaction.show(imageFragment);

                    }
                    ((TextView) (mTabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title))).setTextColor(getResources().getColor(R.color.headerbar_selected_tab_color));
                    ((TextView) (mTabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title))).setTextColor(Color.WHITE);

                } else {
                    headerBarTitle.setText(getResources().getString(R.string.video));
                    headerBarCamera.setBackgroundResource(R.drawable.selector_video_button);
                    headerBarCamera.setTag(getResources().getString(R.string.video));

                    if (videoFragment == null) {

                        final BucketVideoFragment newVideoFragment = new BucketVideoFragment();
                        fragmentTransaction.add(R.id.realTabcontent, newVideoFragment, "tab2");

                    } else {

                        if (imageFragment != null) {
                            fragmentTransaction.hide(imageFragment);
                        }

                        fragmentTransaction.show(videoFragment);
                    }

                    ((TextView) (mTabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title))).setTextColor(Color.WHITE);
                    ((TextView) (mTabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title))).setTextColor(getResources().getColor(R.color.headerbar_selected_tab_color));

                }

                fragmentTransaction.commit();
            }
        });

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) headerBarCamera.getLayoutParams();
        params.height = convertDipToPixels(40);
        params.width = convertDipToPixels(40);
        headerBarCamera.setLayoutParams(params);
        headerBarCamera.setScaleType(ScaleType.CENTER_INSIDE);
        headerBarCamera.setPadding(convertDipToPixels(15), convertDipToPixels(15), convertDipToPixels(15), convertDipToPixels(15));

    }

    OnClickListener clickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            if (view == headerBarCamera) {

                if (view.getTag().toString().equals(getResources().getString(R.string.video))) {
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    fileUri = getOutputMediaFileUri(MediaChooserConstants.MEDIA_TYPE_VIDEO); // create a file to save the image
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                    // start the image capture Intent
                    startActivityForResult(intent, MediaChooserConstants.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);

                } else {
                   /* Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    fileUri = getOutputMediaFileUri(MediaChooserConstants.MEDIA_TYPE_IMAGE); // create a file to save the image
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
*/
                    // start the image capture Intent
                    // startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
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

            } else if (view == headerBarDone) {

                if (mSelectedImage.size() == 0 && mSelectedVideo.size() == 0) {
                    Toast.makeText(BucketHomeFragmentActivity.this, getString(R.string.please_select_file), Toast.LENGTH_SHORT).show();

                } else {

                    if (mSelectedVideo.size() > 0) {
                        Intent videoIntent = new Intent();
                        videoIntent.setAction(MediaChooser.VIDEO_SELECTED_ACTION_FROM_MEDIA_CHOOSER);
                        videoIntent.putStringArrayListExtra("list", mSelectedVideo);
                        sendBroadcast(videoIntent);
                    }

                    if (mSelectedImage.size() > 0) {
                        Intent imageIntent = new Intent();
                        imageIntent.setAction(MediaChooser.IMAGE_SELECTED_ACTION_FROM_MEDIA_CHOOSER);
                        imageIntent.putStringArrayListExtra("list", mSelectedImage);
                        sendBroadcast(imageIntent);
                    }
                    finish();
                }

            } else if (view == headerBarBack) {
                finish();
            }
        }
    };

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

    /**
     * Create a file Uri for saving an image or video
     */
    private Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private void setCheckMenu(PopupMenu popupMenu) {
        int choose = prefs.getIntValue(CHOOSE_OPTION_IMAGE, Statics.ORIGINAL);
        if (choose == Statics.STANDARD) {
            popupMenu.getMenu().getItem(Statics.STANDARD).setChecked(true);
        } else if (choose == Statics.HIGH) {
            popupMenu.getMenu().getItem(Statics.HIGH).setChecked(true);
        } else if (choose == Statics.ORIGINAL) {
            popupMenu.getMenu().getItem(Statics.ORIGINAL).setChecked(true);
        }
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

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == MediaChooserConstants.BUCKET_SELECT_IMAGE_CODE) {

                addMedia(mSelectedImage, data.getStringArrayListExtra("list"));
//                Log.d(TAG,"BUCKET_SELECT_IMAGE_CODE: mSelectedImage: "+mSelectedImage.size());

            } else if (requestCode == MediaChooserConstants.BUCKET_SELECT_VIDEO_CODE) {
                addMedia(mSelectedVideo, data.getStringArrayListExtra("list"));

            } else if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {

                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, fileUri));
                final AlertDialog alertDialog = MediaChooserConstants.getDialog(BucketHomeFragmentActivity.this).create();
                alertDialog.show();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 2000ms
                        try {

                            String fileUriString = fileUri.toString().replaceFirst("file:///", "/").trim();
                            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                            BucketImageFragment bucketImageFragment = (BucketImageFragment) fragmentManager.findFragmentByTag("tab1");
                            if (bucketImageFragment != null) {
                                bucketImageFragment.getAdapter().addLatestEntry(fileUriString);
                                bucketImageFragment.getAdapter().notifyDataSetChanged();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        alertDialog.dismiss();
                    }
                }, 5000);

            } else if (requestCode == MediaChooserConstants.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {


                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, fileUri));

                final AlertDialog alertDialog = MediaChooserConstants.getDialog(BucketHomeFragmentActivity.this).create();
                alertDialog.show();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 2000ms
                        String fileUriString = fileUri.toString().replaceFirst("file:///", "/").trim();
                        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                        BucketVideoFragment bucketVideoFragment = (BucketVideoFragment) fragmentManager.findFragmentByTag("tab2");
                        if (bucketVideoFragment != null) {
                            bucketVideoFragment.getAdapter().addLatestEntry(fileUriString);
                            bucketVideoFragment.getAdapter().notifyDataSetChanged();

                        }
                        alertDialog.dismiss();
                    }
                }, 5000);
            }
        }
    }

    private void addMedia(ArrayList<String> list, ArrayList<String> input) {
        for (String string : input) {
            list.add(string);
        }
    }


    public int convertDipToPixels(float dips) {
        return (int) (dips * BucketHomeFragmentActivity.this.getResources().getDisplayMetrics().density + 0.5f);
    }

}
