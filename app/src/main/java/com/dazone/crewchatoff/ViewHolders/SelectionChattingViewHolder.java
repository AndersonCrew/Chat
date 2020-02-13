package com.dazone.crewchatoff.ViewHolders;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.SelectionPlusDto;
import com.dazone.crewchatoff.fragment.ChattingFragment;
import com.dazone.crewchatoff.libGallery.MediaChooser;
import com.dazone.crewchatoff.libGallery.activity.BucketHomeFragmentActivity;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Utils;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.onegravity.contactpicker.contact.ContactDescription;
import com.onegravity.contactpicker.contact.ContactSortOrder;
import com.onegravity.contactpicker.core.ContactPickerActivity;
import com.onegravity.contactpicker.picture.ContactPictureType;

public class SelectionChattingViewHolder extends ItemViewHolder<SelectionPlusDto> {
    public TextView title;
    public ImageView icon;
    public LinearLayout layout;
    private Uri uri;
    private String TAG = "SelectionChattingViewHolder";

    public SelectionChattingViewHolder(View v) {
        super(v);
    }

    @Override
    protected void setup(View v) {
        title = (TextView) v.findViewById(R.id.title);
        icon = (ImageView) v.findViewById(R.id.ic_folder);
        layout = (LinearLayout) v.findViewById(R.id.layout);
    }

    @Override
    public void bindData(SelectionPlusDto dto) {
        switch (dto.getType()) {
            case 1:
                icon.setImageResource(R.drawable.attach_ic_camera);
                title.setText(Utils.getString(R.string.camera));
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        try {
//                            captureImage(Statics.MEDIA_TYPE_IMAGE);
//                        } catch (Exception e) {
//
//                        }

                        if (ChattingActivity.instance != null) {
                            if (ChattingActivity.instance.checkPermissionsCamera()) {
                                try {
                                    captureImage(Statics.MEDIA_TYPE_IMAGE);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                ChattingActivity.instance.setPermissionsCamera();
                            }
                        } else {
                            Toast.makeText(CrewChatApplication.getInstance(), CrewChatApplication.getInstance().getResources().getString(R.string.can_not_check_permission), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                break;
            case 2:
                icon.setImageResource(R.drawable.attach_ic_video_record);
                title.setText(Utils.getString(R.string.video_record));
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        try {
//                            recordVideo();
//                        } catch (Exception e) {
//
//                        }

                        if (ChattingActivity.instance != null) {
                            if (ChattingActivity.instance.checkPermissionsCamera()) {
                                try {
                                    recordVideo();
                                } catch (Exception e) {

                                }
                            } else {
                                ChattingActivity.instance.setPermissionsCamera();
                            }
                        } else {
                            Toast.makeText(CrewChatApplication.getInstance(), CrewChatApplication.getInstance().getResources().getString(R.string.can_not_check_permission), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                break;
            case 3:
                icon.setImageResource(R.drawable.attach_ic_images);
                title.setText(Utils.getString(R.string.image));
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        try {
//                            selectImage();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }

                        if (ChattingActivity.instance != null) {
                            if (ChattingActivity.instance.checkPermissionsCamera()) {
                                try {
                                    selectImage();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                ChattingActivity.instance.setPermissionsCamera();
                            }
                        } else {
                            Toast.makeText(CrewChatApplication.getInstance(), CrewChatApplication.getInstance().getResources().getString(R.string.can_not_check_permission), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                break;
            case 4:
                icon.setImageResource(R.drawable.attach_ic_video);
                title.setText(Utils.getString(R.string.video));
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        try {
//                            Intent intentVideo = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
//                            intentVideo.setType(Statics.MIME_TYPE_VIDEO);
//                            ChattingActivity.Instance.startActivityForResult(intentVideo, Statics.VIDEO_PICKER_SELECT);
//                            ChattingActivity.Instance.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }


                        if (ChattingActivity.instance != null) {
                            if (ChattingActivity.instance.checkPermissionsCamera()) {
                                try {
                                    Intent intentVideo = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                                    intentVideo.setType(Statics.MIME_TYPE_VIDEO);
                                    ChattingActivity.Instance.startActivityForResult(intentVideo, Statics.VIDEO_PICKER_SELECT);
                                    ChattingActivity.Instance.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                ChattingActivity.instance.setPermissionsCamera();
                            }
                        } else {
                            Toast.makeText(CrewChatApplication.getInstance(), CrewChatApplication.getInstance().getResources().getString(R.string.can_not_check_permission), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                break;
            case 5:
                icon.setImageResource(R.drawable.attach_ic_file);
                title.setText(Utils.getString(R.string.file));
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Intent i = new Intent(ChattingActivity.Instance, FilePickerActivity.class);
//                        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
//                        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
//                        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
//                        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
//                        ChattingActivity.Instance.startActivityForResult(i, Statics.FILE_PICKER_SELECT);



                        if (ChattingActivity.instance != null) {
                            if (ChattingActivity.instance.checkPermissionsCamera()) {
                                Intent i = new Intent(ChattingActivity.Instance, FilePickerActivity.class);
                                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
                                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                                ChattingActivity.Instance.startActivityForResult(i, Statics.FILE_PICKER_SELECT);
                            } else {
                                ChattingActivity.instance.setPermissionsCamera();
                            }
                        } else {
                            Toast.makeText(CrewChatApplication.getInstance(), CrewChatApplication.getInstance().getResources().getString(R.string.can_not_check_permission), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                break;

            case 6:
                icon.setImageResource(R.drawable.attach_ic_contact);
                title.setText(Utils.getString(R.string.contact));
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        contactPicker();

                        if (ChattingActivity.instance != null) {
                            if (ChattingActivity.instance.checkPermissionsContacts()) {
                                contactPicker();
                            } else {
                                ChattingActivity.instance.setPermissionsCameraContacts();
                            }
                        } else {
                            Toast.makeText(CrewChatApplication.getInstance(), CrewChatApplication.getInstance().getResources().getString(R.string.can_not_check_permission), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                break;
            case 7:
                icon.setImageResource(R.drawable.recording_icon);
                title.setText(Utils.getString(R.string.recording));
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ChattingActivity.instance != null) {
                            if (ChattingActivity.instance.checkPermissionsAudio()) {
                                try {
                                    if(ChattingFragment.instance!=null)ChattingFragment.instance.recordDialog();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                ChattingActivity.instance.setPermissionsAudio();
                            }
                        } else {
                            Toast.makeText(CrewChatApplication.getInstance(), CrewChatApplication.getInstance().getResources().getString(R.string.can_not_check_permission), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                break;
        }
//        title.setText(TimeUtils.showTime(dto.getTime(), Statics.DATE_FORMAT_YY_MM_DD));
    }

    private void contactPicker() {


        //Intent intent = new Intent(ChattingActivity.Instance, ContactPickerMultiActivity.class);


        Intent intent = new Intent(ChattingActivity.Instance, ContactPickerActivity.class)
                //.putExtra(ContactPickerActivity.EXTRA_THEME, mDarkTheme ? R.style.Theme_Dark : R.style.Theme_Light)
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_BADGE_TYPE, ContactPictureType.ROUND.name())
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION, ContactDescription.ADDRESS.name())
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_SORT_ORDER, ContactSortOrder.AUTOMATIC.name());

        ChattingActivity.Instance.startActivityForResult(intent, Statics.CONTACT_PICKER_SELECT);
    }

    private void recordVideo() {

        if (Build.VERSION.SDK_INT > 23) {
            // for android >= 7

            ContentValues values = new ContentValues();
//            values.put(MediaStore.Video.Media.TITLE, "Title1");
//            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            values.put(MediaStore.Video.VideoColumns.MIME_TYPE, "video/mp4");
//            values.put(MediaStore.Video.Media.DATA, videoPath);
            Uri mVideoUri = CrewChatApplication.getInstance().getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            Uri uriTemp = Constant.convertUri(CrewChatApplication.getInstance(), mVideoUri);
            ChattingActivity.videoPath = uriTemp;
//            Log.d(TAG,"uriTemp:"+uriTemp.toString());
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mVideoUri);
            if (takeVideoIntent.resolveActivity(CrewChatApplication.getInstance().getPackageManager()) != null) {
                ChattingActivity.Instance.startActivityForResult(takeVideoIntent, Statics.CAMERA_VIDEO_REQUEST_CODE);
            }


        } else {
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            Uri videoPath = ChattingActivity.getOutputMediaFileUri(Statics.MEDIA_TYPE_VIDEO);
//            Log.d(TAG, "videoPath:" + videoPath.toString());
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoPath);
            ChattingActivity.videoPath = videoPath;
            if (takeVideoIntent.resolveActivity(CrewChatApplication.getInstance().getPackageManager()) != null) {
                ChattingActivity.Instance.startActivityForResult(takeVideoIntent, Statics.CAMERA_VIDEO_REQUEST_CODE);
            }
        }

    }

    //Capture camera
    private void captureImage(int task) {
        if (Build.VERSION.SDK_INT > 23) {
            // for android >= 7
            if (task == Statics.MEDIA_TYPE_IMAGE) {
                Uri mPhotoUri = CrewChatApplication.getInstance().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new ContentValues());
                uri = Constant.convertUri(CrewChatApplication.getInstance(), mPhotoUri);
                ChattingActivity.setOutputMediaFileUri_v7(uri);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                ChattingActivity.Instance.startActivityForResult(intent, Statics.CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
            }
        } else {
            // for android < 7
            if (task == Statics.MEDIA_TYPE_IMAGE) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                uri = ChattingActivity.getOutputMediaFileUri(Statics.MEDIA_TYPE_IMAGE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                ChattingActivity.Instance.startActivityForResult(intent, Statics.CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
            }
        /*if (task == Statics.MEDIA_TYPE_VIDEO) {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            uri = getOutputMediaFileUri(Statics.MEDIA_TYPE_VIDEO);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            ChattingActivity.Instance.startActivityForResult(intent, Statics.CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        }*/
        }
    }

    private void selectImage() {
        MediaChooser.showOnlyImageTab();
        //MediaChooser.showCameraVideoView(false);
        Intent intent = new Intent(ChattingActivity.Instance, BucketHomeFragmentActivity.class);
        ChattingActivity.Instance.startActivity(intent);
    }
}