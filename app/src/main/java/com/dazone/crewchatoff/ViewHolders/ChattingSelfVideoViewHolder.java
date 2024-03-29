package com.dazone.crewchatoff.ViewHolders;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.fragment.ChattingFragment;
import com.dazone.crewchatoff.interfaces.ICreateOneUserChatRom;
import com.dazone.crewchatoff.interfaces.IF_Relay;
import com.dazone.crewchatoff.interfaces.Urls;
import com.dazone.crewchatoff.utils.*;

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.dazone.crewchatoff.utils.Utils.getString;
import static com.dazone.crewchatoff.utils.Utils.getTypeFile;

public class ChattingSelfVideoViewHolder extends BaseChattingHolder implements View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
    public static String TAG = "ChattingSelfVideoViewHolder";
    private TextView date_tv;
    private TextView tvUnread, tvDuration;
    private ImageView chatting_imv, ivPlayBtn;
    private View overLayView;
    public ProgressBar progressBar, progressDownloading;
    private ChattingDto tempDto;
    private Activity mActivity;
    private String url;
    private String fileName = "";
    private String timeStrPublic = "";
    private Bitmap bitmapPublic = null;

    public ChattingSelfVideoViewHolder(Activity activity, View v) {
        super(v);
        mActivity = activity;
    }

    @Override
    protected void setup(View v) {
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        progressDownloading = (ProgressBar) v.findViewById(R.id.progress_downloading);
        date_tv = (TextView) v.findViewById(R.id.date_tv);
        chatting_imv = (ImageView) v.findViewById(R.id.chatting_imv);
        tvUnread = (TextView) v.findViewById(R.id.text_unread);
        ivPlayBtn = (ImageView) v.findViewById(R.id.iv_play_btn);
        tvDuration = (TextView) v.findViewById(R.id.tv_duration);
        overLayView = v.findViewById(R.id.overlay_movie);
        overLayView.setOnClickListener(this);
        overLayView.setOnCreateContextMenuListener(this);
    }

    boolean isLoaded =  false;

    protected final android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {

                Bundle args = msg.getData();
                if (args != null) {
                    try {
                        String timeStr = args.getString("duration");
                        Bitmap bitmap = args.getParcelable("bitmap");
                        tvDuration.setText(timeStr);
                        chatting_imv.setImageBitmap(bitmap);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };
    int getUnReadCount;

    @Override
    public void bindData(final ChattingDto dto) {
        MessageNo = dto.getMessageNo();
        tempDto = dto;

        try {
            getUnReadCount = dto.getUnReadCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
        AttachDTO attachDTO = dto.getAttachInfo();

        if (attachDTO != null) {

            url = new Prefs().getServerSite() + Urls.URL_DOWNLOAD + "session=" + CrewChatApplication.getInstance().getPrefs().getaccesstoken() + "&no=" + attachDTO.getAttachNo();
            fileName = attachDTO.getFileName();
            if (fileName == null) {
                fileName = "";
            }
            Log.d(TAG, "fileName:" + fileName);

            // Check local file is exist and get thumbnail image
            final File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constant.pathDownload, fileName);

            if (file.exists()) {
                // Thread to get meta data
                new Async_Get_Bitmap(file).execute();
                isLoaded = true;
            }
        }

        if (dto.getmType() == Statics.CHATTING_VIEW_TYPE_SELECT_VIDEO) {
            /*file_name_tv.setText(dto.getAttachFileName());
            file_size_tv.setText(Utils.readableFileSize(new Long(dto.getAttachFileSize())));
            file_receive_tv.setVisibility(View.GONE);
            *//** Set IMAGE FILE TYPE *//*
            String fileType = Utils.getFileType(dto.getAttachFileName());
            ImageUtils.imageFileType(file_thumb, fileType);*/
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            ChattingFragment.instance.SendTo(dto, progressBar, getAdapterPosition(), null);
        } else {
            if (TextUtils.isEmpty(dto.getRegDate())) {
                date_tv.setText(TimeUtils.showTimeWithoutTimeZone(dto.getTime(), Statics.DATE_FORMAT_YY_MM_DD_DD_H_M));
            } else {
                date_tv.setText(TimeUtils.displayTimeWithoutOffset(CrewChatApplication.getInstance().getApplicationContext(), dto.getRegDate(), 0, TimeUtils.KEY_FROM_SERVER));
            }
        }

        String strUnReadCount = dto.getUnReadCount() + "";
        tvUnread.setText(strUnReadCount);
//        tvUnread.setVisibility(dto.getUnReadCount() == 0 ? View.GONE : View.VISIBLE);
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
        overLayView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                Log.d(TAG,"onLongClick:"+new Gson().toJson(dto));
//                if (!TextUtils.isEmpty(url)) {
//                    Utils.displayDownloadFileDialog(BaseActivity.Instance, url, fileName);
//                }
                MessageNo = dto.getMessageNo();
                v.showContextMenu();
                return true;
            }
        });
    }

    private class Async_Get_Bitmap extends AsyncTask<Void, Void, Void> {
        private File file;
        private Message message;
        String timeStr = "";
        Bitmap bitmap = null;

        public Async_Get_Bitmap(File file) {
            this.file = file;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bitmapPublic = null;
            timeStrPublic = "";
            chatting_imv.setImageBitmap(null);

        }

        @Override
        protected Void doInBackground(Void... params) {
            // Get video thumbnail
            bitmap = ThumbnailUtils.createVideoThumbnail(file.getPath(), MediaStore.Video.Thumbnails.MICRO_KIND);
            // Get duration
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            //use one of overloaded setDataSource() functions to set your data source

            try {
                Uri uri = Uri.fromFile(file);
                retriever.setDataSource(mActivity, uri);
                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                long timeInMillisec = Long.parseLong(time);
                long duration = timeInMillisec / 1000;
                long hours = duration / 3600;
                long minutes = (duration - hours * 3600) / 60;
                long seconds = duration - (hours * 3600 + minutes * 60);


                if (minutes < 10) {
                    timeStr += "0";
                }

                timeStr += minutes + ":";

                if (seconds < 10) {
                    timeStr += "0";
                }
                timeStr += seconds;

                message = Message.obtain();
                message.what = 1;

                Bundle args = new Bundle();
                args.putString("duration", timeStr);
                args.putParcelable("bitmap", bitmap);
                message.setData(args);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // handler.sendMessage(message);

            mHandler.removeCallbacks(mFilterTask_right);
            mHandler.postDelayed(mFilterTask_right, 1300);
            timeStrPublic = timeStr;
            bitmapPublic = bitmap;
        }
    }

    Runnable mFilterTask_right = new Runnable() {
        @Override
        public void run() {
            tvDuration.setText(timeStrPublic);
            chatting_imv.setImageBitmap(bitmapPublic);
        }
    };

    private void getVideoMeta(File file) {
        // Get video thumbnail
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(file.getPath(), MediaStore.Video.Thumbnails.MICRO_KIND);
        // Get duration
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //use one of overloaded setDataSource() functions to set your data source

        try {
            Uri uri = Uri.fromFile(file);
            retriever.setDataSource(mActivity, uri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            long timeInMillisec = Long.parseLong(time);
            long duration = timeInMillisec / 1000;
            long hours = duration / 3600;
            long minutes = (duration - hours * 3600) / 60;
            long seconds = duration - (hours * 3600 + minutes * 60);

            String timeStr = "";

            if (minutes < 10) {
                timeStr += "0";
            }

            timeStr += minutes + ":";

            if (seconds < 10) {
                timeStr += "0";
            }
            timeStr += seconds;

            Message message = Message.obtain();
            message.what = 1;

            Bundle args = new Bundle();
            args.putString("duration", timeStr);
            args.putParcelable("bitmap", bitmap);
            message.setData(args);

            mHandler.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.overlay_movie:
                playVideoStreaming();
                break;
        }
    }

    /*  private void playVideoStreaming() {
          if (ChattingActivity.instance != null) {
              if (ChattingActivity.instance.checkPermissionsReadExternalStorage()) {
                  Log.d(TAG, "playVideoStreaming");
                  if (tempDto != null) {
                      AttachDTO attachDTO = tempDto.getAttachInfo();
                      if (attachDTO != null) {
                          String url = new Prefs().getServerSite() + Urls.URL_DOWNLOAD + "session=" + CrewChatApplication.getInstance().getPrefs().getaccesstoken() + "&no=" + attachDTO.getAttachNo();

                          Log.d(TAG, "url:" + url);
                          new WebClientAsyncTask(mActivity, progressDownloading, url, fileName, new OnDownloadFinish() {
                              @Override
                              public void onFinish(File file) {
                                  if (file == null || file.length() <= 0) {
                                      Toast.makeText(CrewChatApplication.getInstance(), "download fail", Toast.LENGTH_SHORT).show();
                                  } else {
                                      Log.d(TAG, "onFinish file:" + file.length());
                                      try {
                                          galleryAddPic(file.getPath());
                                      } catch (Exception e) {
                                          e.printStackTrace();
                                      }

                                      try {
                                          getVideoMeta(file);
                                      } catch (Exception e) {
                                          e.printStackTrace();
                                      }

                                  }
                              }
                          }).execute();

                      }
                  }

              } else {
                  ChattingActivity.instance.setPermissionsReadExternalStorage();
              }
          } else {
              Toast.makeText(CrewChatApplication.getInstance(), CrewChatApplication.getInstance().getResources().getString(R.string.can_not_check_permission), Toast.LENGTH_SHORT).show();
          }

      }*/
    private void playVideoStreaming() {
        if (ChattingActivity.instance != null) {
            if (ChattingActivity.instance.checkPermissionsReadExternalStorage()) {
                if (tempDto != null) {
                    AttachDTO attachDTO = tempDto.getAttachInfo();
                    if (attachDTO != null) {
                        String fileNameLocal = "/crewChat/" + attachDTO.getFileName();
                        File file1 = new File(Environment.getExternalStorageDirectory() + fileNameLocal);
                        if (file1.exists()) {
                            if (mActivity != null && file1 != null && file1.length() > 0) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    Uri apkUri = FileProvider.getUriForFile(BaseActivity.Instance, BuildConfig.APPLICATION_ID + ".provider", file1);
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(apkUri, "video/*");
                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    mActivity.startActivity(intent);
                                } else {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.fromFile(file1), "video/*");
                                    mActivity.startActivity(intent);
                                }
                                try {
                                    galleryAddPic(file1.getPath());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    getVideoMeta(file1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            String url = new Prefs().getServerSite() + Urls.URL_DOWNLOAD + "session=" + CrewChatApplication.getInstance().getPrefs().getaccesstoken() + "&no=" + attachDTO.getAttachNo();
                            Log.d("playVideoStreaming", "url:" + url);
                            // Check local data
                            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constant.pathDownload, fileName);
                            new WebClientAsyncTask(mActivity, progressDownloading, url, fileName, new OnDownloadFinish() {
                                @Override
                                public void onFinish(File file) {

                                    if (file != null && file.length() > 0) {

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            Uri apkUri = FileProvider.getUriForFile(BaseActivity.Instance, BuildConfig.APPLICATION_ID + ".provider", file);
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setDataAndType(apkUri, "video/*");
                                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                            BaseActivity.Instance.startActivity(intent);
                                        } else {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setDataAndType(Uri.fromFile(file), "video/*");
                                            BaseActivity.Instance.startActivity(intent);
                                        }

                                    }
                                    Log.d("playVideoStreaming", TAG);
                                    try {
                                        galleryAddPic(file.getPath());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        getVideoMeta(file);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).execute();

                        }
                    }
                }
            } else {
                ChattingActivity.instance.setPermissionsReadExternalStorage();
            }

        } else {
            Toast.makeText(CrewChatApplication.getInstance(), CrewChatApplication.getInstance().getResources().getString(R.string.can_not_check_permission), Toast.LENGTH_SHORT).show();
        }
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

    void actionOpen() {
        playVideoStreaming();
    }


    void actionRelay() {
        Intent intent = new Intent(BaseActivity.Instance, RelayActivity.class);
        intent.putExtra(Statics.MessageNo, MessageNo);
        BaseActivity.Instance.startActivity(intent);
    }

    void actionDownload() {
        if (!TextUtils.isEmpty(url)) {
            String path = Environment.getExternalStorageDirectory() + Constant.pathDownload + "/" + fileName;
            File file = new File(path);
//            if (file.isFile()) {
////                        Toast.makeText(mActivity, "file exist", Toast.LENGTH_LONG).show();
//                mActivity.startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
//            } else {
//                Utils.displayDownloadFileDialog(BaseActivity.Instance, url, fileName);
//            }
            Utils.displayDownloadFileDialog(BaseActivity.Instance, url, fileName);
        }
    }

    int RandW_PERMISSIONS_REQUEST_CODE = 1;
    private ProgressDialog mProgressDialog = null;
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
        if (ContextCompat.checkSelfPermission(BaseActivity.Instance, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        return true;
    }
    public void DownloadImage(final Context context, final String url, final String name, final Intent shareIntent, final File file) {
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
                            shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(CrewChatApplication.getInstance(), BuildConfig.APPLICATION_ID + ".provider", file));
                            mActivity.startActivity(Intent.createChooser(shareIntent, "Share video using"));
                        }
                    }
                }
            }
        };

        context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
    void actionShare() {

        if (!TextUtils.isEmpty(url)) {
            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("*/*");
            String path = Environment.getExternalStorageDirectory() + Constant.pathDownload + "/" + fileName;
            File file = new File(path);
            if (file.isFile()) {
//                        Toast.makeText(mActivity, "file exist", Toast.LENGTH_LONG).show();
//                mActivity.startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
                shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(CrewChatApplication.getInstance(), BuildConfig.APPLICATION_ID + ".provider", file));
                mActivity.startActivity(Intent.createChooser(shareIntent, "Share video using"));
            } else {
                if (checkPermissionsWandR()) {  mProgressDialog = new ProgressDialog(BaseActivity.Instance);
                    mProgressDialog.setMessage(getString(R.string.download));
                    mProgressDialog.setIndeterminate(true);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();

                    DownloadImage(BaseActivity.Instance, url, fileName,shareIntent,file);
                } else {
                    setPermissionsRandW();
                }

            }

        }
    }

    long MessageNo;

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case Statics.MENU_DOWNLOAD:
                actionDownload();
                break;
            case Statics.MENU_SHARE:
                actionShare();
                break;
            case Statics.MENU_RELAY:
                actionRelay();
                break;
            case Statics.MENU_TO_ME:
                actionToMe(MessageNo);
                break;
            case Statics.MENU_OPEN:
                actionOpen();
                break;
            case Statics.MENU_UNREAD_MSG:
                actionUnread();
                break;
        }
        return false;
    }

    private void actionUnread() {
        Intent intent = new Intent(Constant.INTENT_GOTO_UNREAD_ACTIVITY);
        intent.putExtra(Statics.MessageNo, tempDto.getMessageNo());
        BaseActivity.Instance.sendBroadcast(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        Resources res = CrewChatApplication.getInstance().getResources();
        MenuItem mnOpen = contextMenu.add(0, Statics.MENU_OPEN, 0, res.getString(R.string.open));
        mnOpen.setOnMenuItemClickListener(this);
        MenuItem mnDownload = contextMenu.add(0, Statics.MENU_DOWNLOAD, 0, res.getString(R.string.download));
        mnDownload.setOnMenuItemClickListener(this);
        MenuItem mnShare = contextMenu.add(0, Statics.MENU_SHARE, 0, res.getString(R.string.share));
        mnShare.setOnMenuItemClickListener(this);
        MenuItem mnRelay = contextMenu.add(0, Statics.MENU_RELAY, 0, res.getString(R.string.relay));
        mnRelay.setOnMenuItemClickListener(this);
        MenuItem mnToMe = contextMenu.add(0, Statics.MENU_TO_ME, 0, res.getString(R.string.to_me));
        mnToMe.setOnMenuItemClickListener(this);
        MenuItem mnUnread = contextMenu.add(0, Statics.MENU_UNREAD_MSG, 0, Constant.getUnreadText(CrewChatApplication.getInstance(), getUnReadCount));
        mnUnread.setOnMenuItemClickListener(this);

    }

    public interface OnDownloadFinish {
        void onFinish(File file);
    }

    // Download video first, then play it

    private static class WebClientAsyncTask extends AsyncTask<Void, Void, Void> {
        private final WeakReference<Activity> mWeakActivity;
        private String mUrl = "";
        private String mName = "";
        private File outputFile;
        private ProgressBar mProgressBar;
        private OnDownloadFinish mDownloadCallback;

        public WebClientAsyncTask(Activity activity, ProgressBar progressBar, String url, String fileName, OnDownloadFinish callback) {
            mWeakActivity = new WeakReference<>(activity);
            this.mUrl = url;
            this.mName = fileName;
            this.mProgressBar = progressBar;
            this.mDownloadCallback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Activity activity = mWeakActivity.get();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            BufferedInputStream bufferedInputStream = null;
            FileOutputStream fileOutputStream = null;

            try {
                URL apkUrl = new URL(this.mUrl);
                urlConnection = (HttpURLConnection) apkUrl.openConnection();
                inputStream = urlConnection.getInputStream();
                bufferedInputStream = new BufferedInputStream(inputStream);
//                Log.d(TAG, "mName:" + mName);
                outputFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constant.pathDownload, this.mName);

                if (outputFile.exists()) outputFile.delete();
                outputFile.createNewFile();

                fileOutputStream = new FileOutputStream(outputFile);
                byte[] buffer = new byte[4096];
                int readCount;
                while (true) {
                    readCount = bufferedInputStream.read(buffer);
                    if (readCount == -1) {
                        break;
                    }
                    fileOutputStream.write(buffer, 0, readCount);
                    fileOutputStream.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (urlConnection != null) {
                    try {
                        urlConnection.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressBar.setVisibility(View.GONE);
            Activity activity = mWeakActivity.get();
            mDownloadCallback.onFinish(outputFile);

        }
    }

    private void galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        mActivity.sendBroadcast(mediaScanIntent);
    }
}