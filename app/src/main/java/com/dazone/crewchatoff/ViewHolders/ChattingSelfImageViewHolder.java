package com.dazone.crewchatoff.ViewHolders;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.dazone.crewchatoff.BuildConfig;
import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.activity.MainActivity;
import com.dazone.crewchatoff.activity.RelayActivity;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.AttachDTO;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ChattingImageDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.fragment.ChattingFragment;
import com.dazone.crewchatoff.interfaces.ICreateOneUserChatRom;
import com.dazone.crewchatoff.interfaces.IF_Relay;
import com.dazone.crewchatoff.interfaces.Urls;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.graphics.Bitmap.createScaledBitmap;
import static com.dazone.crewchatoff.utils.Utils.getString;
import static com.dazone.crewchatoff.utils.Utils.getTypeFile;

public class ChattingSelfImageViewHolder extends BaseChattingHolder implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
    private TextView date_tv;
    private TextView tvUnread;
    private ImageView chatting_imv;
    private LinearLayout lnSendFail;
    private ProgressBar progressBarSending;
    private ProgressBar progressBarImageLoading;
    private ChattingDto tempDto;
    private Activity mActivity;
    private float ratio = 1.8f;
    private Bitmap destBitmap = null;
    String TAG = "ChattingSelfImageViewHolder";

    public ChattingSelfImageViewHolder(Activity activity, View v) {
        super(v);
        mActivity = activity;
    }

    @Override
    protected void setup(View v) {
        progressBarImageLoading = v.findViewById(R.id.progressbar_image_loading);
        date_tv = v.findViewById(R.id.date_tv);
        chatting_imv = v.findViewById(R.id.chatting_imv);
        tvUnread = v.findViewById(R.id.text_unread);
        lnSendFail = v.findViewById(R.id.ln_send_failed);
        progressBarSending = v.findViewById(R.id.progressbar_sending);
        chatting_imv.setOnCreateContextMenuListener(this);
    }

    protected final android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Bundle bundle = msg.getData();
                ChattingImageDto dto = (ChattingImageDto) bundle.getSerializable("data");
                if (dto != null) {
                    dto.getIvChatting().setImageBitmap(dto.getBmpResource());
                }
            }
        }
    };
    int getUnReadCount;

    private byte[] bitmapToByte(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    @Override
    public void bindData(final ChattingDto dto) {
        tempDto = dto;
        MessageNo = dto.getMessageNo();
        try {
            getUnReadCount = dto.getUnReadCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "";

        chatting_imv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.showContextMenu();
                return true;
            }
        });

        // Calculate ratio
        try {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            WindowManager windowmanager = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
            windowmanager.getDefaultDisplay().getMetrics(displayMetrics);
            int deviceWidth = displayMetrics.widthPixels;
            if (deviceWidth > 1000) {
                ratio = 3.6f;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (dto.isHasSent()) {
            if (progressBarSending != null) progressBarSending.setVisibility(View.GONE);
            if (lnSendFail != null) lnSendFail.setVisibility(View.GONE);
        } else {
            if (lnSendFail != null) lnSendFail.setVisibility(View.VISIBLE);
        }

        switch (dto.getmType()) {
            case Statics.CHATTING_VIEW_TYPE_SELECT_IMAGE:
                chatting_imv.setImageBitmap(null);
                chatting_imv.destroyDrawingCache();
                String imagePath = dto.getAttachFilePath(); // photoFile is a File type.

                try {
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = false;
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap tempBitmap = BitmapFactory.decodeFile(imagePath, options);
                    int height = options.outHeight;
                    int width = options.outWidth;

                    int reqWidth, reqHeight;
                    if (width > 180) {
                        reqWidth = (int) (180 * ratio);
                    } else {
                        reqWidth = (int) (60 * ratio);
                    }
                    reqHeight = (reqWidth * height) / width;
                    try {
                        destBitmap = createScaledBitmap(tempBitmap, reqWidth / 2, reqHeight / 2, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    chatting_imv.setImageBitmap(destBitmap);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }

                // End scale image
                dto.setRoomNo(CrewChatApplication.currentRoomNo);
                progressBarImageLoading.setVisibility(View.VISIBLE);

                String oldPath = chatting_imv.getTag() != null ? chatting_imv.getTag().toString() : null;
                String newPath = dto.getAttachFilePath();

                if (oldPath == null || !oldPath.equals(newPath)) {
                    chatting_imv.setTag(dto.getAttachFilePath());
                }

                break;

            default:
                // Clear cache resource before load new image
                chatting_imv.setImageBitmap(null);
                chatting_imv.destroyDrawingCache();
                if (TextUtils.isEmpty(dto.getRegDate())) {
                    date_tv.setText(TimeUtils.showTimeWithoutTimeZone(dto.getTime(), Statics.DATE_FORMAT_YY_MM_DD_DD_H_M));
                } else {
                    date_tv.setText(TimeUtils.displayTimeWithoutOffset(CrewChatApplication.getInstance().getApplicationContext(), dto.getRegDate(), 0, TimeUtils.KEY_FROM_SERVER));
                }

                try {
                    if (dto.getAttachNo() != 0) {
                        final String urlT = String.format("/UI/CrewChat/MobileThumbnailImage.aspx?session=%s&no=%s",
                                new Prefs().getaccesstoken(), dto.getAttachNo());
                        final String fullUrl = new Prefs().getServerSite() + urlT;
                        Log.d("sssDebug2018", fullUrl);
                        Glide.with(CrewChatApplication.getInstance())
                                .load(fullUrl)
                                .asBitmap()
                                //.override(100, 130)
                                .placeholder(R.drawable.error_loading_image)
                                .listener(new RequestListener<String, Bitmap>() {
                                    @Override
                                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                        // call callback when loading error
                                        if (progressBarImageLoading != null)
                                            progressBarImageLoading.setVisibility(View.GONE);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        // call callback when loading success

                                        return false;
                                    }
                                })
                                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                .error(R.drawable.error_loading_image)
                                .fallback(R.drawable.error_loading_image)
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                        int srcWidth = resource.getWidth();
                                        int srcHeight = resource.getHeight();
                                        int dstWidth = (int) (srcWidth * ratio) / 2;
                                        int dstHeight = (int) (srcHeight * ratio) / 2;
                                        Bitmap bitmap = createScaledBitmap(resource, dstWidth, dstHeight, true);
                                        chatting_imv.setImageBitmap(bitmap);
                                        if (progressBarImageLoading != null)
                                            progressBarImageLoading.setVisibility(View.GONE);
                                    }
                                });
                    } else {
                        ImageUtils.showImage(url, chatting_imv);
                    }

                    chatting_imv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                ChattingFragment.instance.ViewImageFull(dto);
                            } catch (Exception e) {

                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    ImageUtils.showImage("", chatting_imv);
                }
                break;
        }

        String strUnReadCount = dto.getUnReadCount() + "";
        tvUnread.setText(strUnReadCount);
        date_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "tvUnread");
                actionUnread();
            }
        });
        if (dto.getUnReadCount() == 0) {
            tvUnread.setVisibility(View.GONE);
        } else {
            tvUnread.setVisibility(View.VISIBLE);
            tvUnread.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "tvUnread");
                    actionUnread();
                }
            });
        }
    }


    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }

        return 0;
    }

    long MessageNo;

    void actionRelay() {
        Intent intent = new Intent(BaseActivity.Instance, RelayActivity.class);
        intent.putExtra(Statics.MessageNo, MessageNo);
        BaseActivity.Instance.startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Resources res = CrewChatApplication.getInstance().getResources();
        MenuItem open = menu.add(0, Statics.MENU_OPEN, 0, res.getString(R.string.open));
        //MenuItem copy = menu.add(0,Statics.MENU_COPY, 0, res.getString(R.string.copy));
        MenuItem download = menu.add(0, Statics.MENU_DOWNLOAD, 0, res.getString(R.string.download));
        //MenuItem delete = menu.add(0, Statics.MENU_DELETE, 0, res.getString(R.string.delete));
        MenuItem share = menu.add(0, Statics.MENU_SHARE, 0, res.getString(R.string.share));

        open.setOnMenuItemClickListener(this);
        //copy.setOnMenuItemClickListener(this);
        download.setOnMenuItemClickListener(this);
        //delete.setOnMenuItemClickListener(this);
        share.setOnMenuItemClickListener(this);
        MenuItem mnRelay = menu.add(0, Statics.MENU_RELAY, 0, res.getString(R.string.relay));
        mnRelay.setOnMenuItemClickListener(this);
        MenuItem mnToMe = menu.add(0, Statics.MENU_TO_ME, 0, res.getString(R.string.to_me));
        mnToMe.setOnMenuItemClickListener(this);
        MenuItem mnUnread = menu.add(0, Statics.MENU_UNREAD_MSG, 0, Constant.getUnreadText(CrewChatApplication.getInstance(), getUnReadCount));
        mnUnread.setOnMenuItemClickListener(this);

    }

    private ProgressDialog mProgressDialog = null;

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case Statics.MENU_OPEN:
                ChattingFragment.instance.ViewImageFull(tempDto);
                break;

            case Statics.MENU_COPY:
                String urlShare2 = tempDto.getAttachInfo().getFullPath().replace("D:", "");
                urlShare2 = urlShare2.replaceAll("\\\\", File.separator);
                urlShare2 = new Prefs().getServerSite() + urlShare2;
                final String urlTemp = urlShare2;

                ImageLoader.getInstance().loadImage(urlShare2, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        ClipboardManager mClipboard = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                        ContentValues values = new ContentValues(2);
                        values.put(MediaStore.Images.Media.MIME_TYPE, "Image/jpg");
                        values.put(MediaStore.Images.Media.DATA, urlTemp);
                        ContentResolver theContent = mActivity.getContentResolver();
                        Uri imageUri2 = theContent.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        ClipData theClip = ClipData.newUri(mActivity.getContentResolver(), "Image", imageUri2);
                        mClipboard.setPrimaryClip(theClip);
                    }
                });
                break;

            case Statics.MENU_DOWNLOAD:
                if (tempDto != null) {
                    AttachDTO attachDTO = tempDto.getAttachInfo();
                    if (attachDTO != null) {
//                        String urlDownload = new Prefs().getServerSite() + Urls.URL_DOWNLOAD_THUMBNAIL + "session="
//                                + CrewChatApplication.getInstance().getPrefs().getaccesstoken() + "&no=" + attachDTO.getAttachNo();


                        String url = String.format("/UI/CrewChat/MobileAttachDownload.aspx?session=%s&no=%s",
                                new Prefs().getaccesstoken(), tempDto.getAttachNo());
                        String urlDownload = new Prefs().getServerSite() + url;
//                        Log.d(TAG,"urlDownload:"+urlDownload);

                        String path = Environment.getExternalStorageDirectory() + Constant.pathDownload + "/" + attachDTO.getFileName();
                        File file = new File(path);
//                        if (file.exists()) {
//                            mActivity.startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
//                        } else {
//                            Utils.displayDownloadFileDialog(mActivity, urlDownload, attachDTO.getFileName());
//                        }
                        Utils.displayDownloadFileDialog(mActivity, urlDownload, attachDTO.getFileName());
                    }
                }
                break;

            case Statics.MENU_DELETE:
                break;

            case Statics.MENU_SHARE:
                if (tempDto != null) {
                    AttachDTO attachDTO = tempDto.getAttachInfo();
                    String urlDownload1 = new Prefs().getServerSite() + Urls.URL_DOWNLOAD_THUMBNAIL +
                            "session=" + CrewChatApplication.getInstance().getPrefs().getaccesstoken() +
                            "&no=" + attachDTO.getAttachNo();

                    String path = Environment.getExternalStorageDirectory() + Constant.pathDownload + "/" + attachDTO.getFileName();
                    final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/jpg");
                    final File photoFile = new File(path);
                    if (!photoFile.exists()) {
                        if (checkPermissionsWandR()) {
                            mProgressDialog = new ProgressDialog(BaseActivity.Instance);
                            mProgressDialog.setMessage(getString(R.string.download));
                            mProgressDialog.setIndeterminate(true);
                            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            mProgressDialog.setCancelable(false);
                            mProgressDialog.show();
                            DownloadImage(mActivity, urlDownload1, attachDTO.getFileName(), shareIntent, photoFile);
                        } else {
                            setPermissionsRandW();
                        }

                    } else {
                        shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(CrewChatApplication.getInstance(), BuildConfig.APPLICATION_ID + ".provider", photoFile));
                        mActivity.startActivity(Intent.createChooser(shareIntent, "Share image using"));
                    }

                }

//                ImageLoader.getInstance().loadImage(url, new SimpleImageLoadingListener() {
//                    @Override
//                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//
//                        /** SAVE FILE */
//                        String path = Utils.saveFile(loadedImage);
//                        Uri screenshotUri = Uri.parse("file:///" + path);
//                        try {
//                            final Intent intent = ShareCompat.IntentBuilder.from(mActivity)
//                                    .setType("image/*")
//                                    .setText("....")
//                                    .setSubject("Share image via...")
//                                    .setStream(screenshotUri)
//                                    .setChooserTitle("Share.")
//                                    .createChooserIntent()
//                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
//                                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//                            mActivity.startActivity(intent);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
//                        super.onLoadingFailed(imageUri, view, failReason);
//                    }
//                });

                break;
            case Statics.MENU_RELAY:
                actionRelay();
                break;
            case Statics.MENU_TO_ME:
                actionToMe(MessageNo);
                break;
            case Statics.MENU_UNREAD_MSG:
                actionUnread();
                break;
        }

        return false;
    }

    public void DownloadImage(final Context context, final String url, final String name, final Intent shareIntent, final File photoFile) {
        String mimeType;
        String serviceString = Context.DOWNLOAD_SERVICE;
        String fileType = name.substring(name.lastIndexOf(".")).toLowerCase();
        final DownloadManager downloadmanager;
        downloadmanager = (DownloadManager) context.getSystemService(serviceString);
        Uri uri = Uri
                .parse(url);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Constant.pathDownload, name);
        //request.setTitle(name);
        int type = getTypeFile(fileType);
        switch (type) {
            case 1:
                request.setMimeType(Statics.MIME_TYPE_IMAGE);
                break;
            case 2:
                request.setMimeType(Statics.MIME_TYPE_VIDEO);
                break;
            case 3:
                request.setMimeType(Statics.MIME_TYPE_AUDIO);
                break;
            default:
                try {
                    mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
                } catch (Exception e) {
                    e.printStackTrace();
                    mimeType = Statics.MIME_TYPE_ALL;
                }
                if (TextUtils.isEmpty(mimeType)) {
                    request.setMimeType(Statics.MIME_TYPE_ALL);
                } else {
                    request.setMimeType(mimeType);
                }
                break;
        }

        final Long reference = downloadmanager.enqueue(request);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(reference);
                    Cursor c = downloadmanager.query(query);

                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            if (mProgressDialog != null) {
                                mProgressDialog.dismiss();
                            }
                            shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(CrewChatApplication.getInstance(), BuildConfig.APPLICATION_ID + ".provider", photoFile));
                            mActivity.startActivity(Intent.createChooser(shareIntent, "Share image using"));
                        }
                    }
                }
            }
        };

        context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }


    private void actionUnread() {
        Intent intent = new Intent(Constant.INTENT_GOTO_UNREAD_ACTIVITY);
        intent.putExtra(Statics.MessageNo, tempDto.getMessageNo());
        BaseActivity.Instance.sendBroadcast(intent);
    }

    void sendMsgToMe(long MessageNo) {
        List<String> lstRoom = new ArrayList<>();
        lstRoom.add("" + MainActivity.myRoom);
        HttpRequest.getInstance().ForwardChatMsgChatRoom(MessageNo, lstRoom, new IF_Relay() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFail() {
                Toast.makeText(CrewChatApplication.getInstance(), "Send Msg to room Fail", Toast.LENGTH_SHORT).show();
            }
        });
    }

    int RandW_PERMISSIONS_REQUEST_CODE = 1;

    public void setPermissionsRandW() {
        String[] requestPermission;
        requestPermission = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(BaseActivity.Instance, requestPermission, RandW_PERMISSIONS_REQUEST_CODE);
    }

    public boolean checkPermissionsWandR() {
        if (ContextCompat.checkSelfPermission(BaseActivity.Instance, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return ContextCompat.checkSelfPermission(BaseActivity.Instance, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    void actionToMe(final long MessageNo) {
        if (MainActivity.myRoom != Statics.MYROOM_DEFAULT) {
            sendMsgToMe(MessageNo);
        } else {
            // create roomNo
            HttpRequest.getInstance().CreateOneUserChatRoom(Utils.getCurrentId(), new ICreateOneUserChatRom() {
                @Override
                public void onICreateOneUserChatRomSuccess(ChattingDto chattingDto) {
                    if (chattingDto != null) {
                        long roomNo = chattingDto.getRoomNo();
                        MainActivity.myRoom = roomNo;
                        sendMsgToMe(MessageNo);
                    }
                }

                @Override
                public void onICreateOneUserChatRomFail(ErrorDto errorDto) {
                    Utils.showMessageShort("Fail");
                }
            });
        }
    }
    // Define function show menu context here
}