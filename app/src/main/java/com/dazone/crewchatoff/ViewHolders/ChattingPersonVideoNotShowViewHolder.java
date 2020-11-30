package com.dazone.crewchatoff.ViewHolders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
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
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.BuildConfig;
import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.activity.MainActivity;
import com.dazone.crewchatoff.activity.ProfileUserActivity;
import com.dazone.crewchatoff.activity.RelayActivity;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.constant.Constants;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.AttachDTO;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.fragment.ChattingFragment;
import com.dazone.crewchatoff.interfaces.ICreateOneUserChatRom;
import com.dazone.crewchatoff.interfaces.IF_Relay;
import com.dazone.crewchatoff.interfaces.Urls;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChattingPersonVideoNotShowViewHolder extends BaseChattingHolder implements View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
    String TAG = "ChattingPersonVideoNotShowViewHolder";
    private TextView date_tv;
    private TextView tvUnread, tvDuration;
    private ImageView chatting_imv, ivPlayBtn;
    private View overLayView;
    private ImageView avatar_imv;

    public ProgressBar progressBar, progressDownloading;
    private ChattingDto tempDto;
    private Activity mActivity;
    private String videoUrl;
    private String fileName = "";

    boolean isLoaded = false;
    private String timeStrPublic = "";
    private Bitmap bitmapPublic = null;
    private LinearLayout llDate;
    private TextView tvDate;

    public ChattingPersonVideoNotShowViewHolder(Activity activity, View v) {
        super(v);
        mActivity = activity;
    }

    @Override
    protected void setup(View v) {
        progressBar = v.findViewById(R.id.progressBar);
        progressDownloading = v.findViewById(R.id.progress_downloading);
        date_tv = v.findViewById(R.id.date_tv);
        chatting_imv = v.findViewById(R.id.chatting_imv);
        avatar_imv = v.findViewById(R.id.avatar_imv);
        tvDuration = v.findViewById(R.id.tv_duration);

        tvUnread = v.findViewById(R.id.text_unread);
        llDate = v.findViewById(R.id.llDate);
        tvDate = v.findViewById(R.id.time);


        ivPlayBtn = v.findViewById(R.id.iv_play_btn);
        ivPlayBtn.setOnClickListener(this);
        overLayView = v.findViewById(R.id.overlay_movie);
        overLayView.setOnClickListener(this);
        overLayView.setOnCreateContextMenuListener(this);
    }

    @SuppressLint("HandlerLeak")
    protected final android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {

                Bundle args = msg.getData();
                if (args != null) {
                    try {
                        String timeStr = args.getString("duration");
                        Bitmap bitmap = args.getParcelable("bitmap");
                        tvDuration.setText(timeStr);
                        if (bitmap != null) {
                            chatting_imv.setImageBitmap(bitmap);
                        } else {
                            chatting_imv.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.movie));
                        }
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
        tempDto = dto;
        MessageNo = dto.getMessageNo();
        AttachDTO attachDTO = dto.getAttachInfo();

        llDate.setVisibility(dto.isHeader()? View.VISIBLE : View.GONE);
        tvDate.setText(Utils.getStrDate(dto));

        if (attachDTO != null) {
            videoUrl = new Prefs().getServerSite() + Urls.URL_DOWNLOAD + "session=" + CrewChatApplication.getInstance().getPrefs().getaccesstoken() + "&no=" + attachDTO.getAttachNo();
            fileName = attachDTO.getFileName();

            // Check local file is exist and get thumbnail image
            final File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constant.pathDownload, fileName);
            if (file.exists()) {
                isLoaded = true;
                // Thread to get meta data
                new Async_Get_Bitmap(file).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

        }

        String url = "";

        try {
            if (dto.getImageLink() != null) {
                url = new Prefs().getServerSite() + dto.getImageLink();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ImageLoader.getInstance().displayImage(url, avatar_imv, Statics.options2);

        try {
            getUnReadCount = dto.getUnReadCount();
            long regDate = new Date(TimeUtils.getTime(dto.getRegDate())).getTime();
            date_tv.setText(TimeUtils.displayTimeWithoutOffset(CrewChatApplication.getInstance().getApplicationContext(), regDate, 0));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String strUnReadCount = String.valueOf(dto.getUnReadCount());

        tvUnread.setText(strUnReadCount);
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
        avatar_imv.setTag(dto.getUserNo());
        avatar_imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("avatar_imv onClick", TAG);
                try {
//                    int userNo = (int) v.getTag();
                    int userNo = dto.getUserNo();
                    Intent intent = new Intent(BaseActivity.Instance, ProfileUserActivity.class);
                    intent.putExtra(Constant.KEY_INTENT_USER_NO, userNo);
                    BaseActivity.Instance.startActivity(intent);
                    BaseActivity.Instance.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        overLayView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d("overLayView onLongClick", TAG);
                v.showContextMenu();
                return true;
            }
        });

        tvUnread.setVisibility(CrewChatApplication.getInstance().getPrefs().getBooleanValue(Constants.HAS_CALL_UNREAD_COUNT, false) || dto.getUnReadCount() == 0? View.GONE : View.VISIBLE);
    }

    private void actionUnread() {
        Intent intent = new Intent(Constant.INTENT_GOTO_UNREAD_ACTIVITY);
        intent.putExtra(Statics.MessageNo, tempDto.getMessageNo());
        BaseActivity.Instance.sendBroadcast(intent);
    }

    private void getVideoMeta(File file) {
        // Get video thumbnail
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(file.getPath(), MediaStore.Video.Thumbnails.MICRO_KIND);
        // Get duration
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(mActivity, Uri.fromFile(file));
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
    }

    private class Async_Get_Bitmap extends AsyncTask<Void, Void, Void> {
        private File file;

        String timeStr = "";
        private Message message;
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
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //   handler.sendMessage(message);
            timeStrPublic=timeStr;
            bitmapPublic=bitmap;
            mHandler.removeCallbacks(mFilterTask_right);
            mHandler.postDelayed(mFilterTask_right, 300);


        }
    }
    Runnable mFilterTask_right = new Runnable() {
        @Override
        public void run() {
            tvDuration.setText(timeStrPublic);
            chatting_imv.setImageBitmap(bitmapPublic);
        }
    };

    // Define function show menu context here
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.overlay_movie:
                Log.d("overlay_movie", TAG);
                playVideoStreaming();
                break;
        }
    }

    private void playVideoStreaming() {
        if (ChattingActivity.instance != null) {
            if (ChattingActivity.instance.checkPermissionsReadExternalStorage()) {
                if (tempDto != null) {
                    AttachDTO attachDTO = tempDto.getAttachInfo();
                    if (attachDTO != null) {
                        String fileNameLocal = "/crewChat/" + attachDTO.getFileName();
                        File file1 = new File(Environment.getExternalStorageDirectory() + fileNameLocal);
                        if (file1.exists()) {
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

                            }
                        } else {
                            String url = new Prefs().getServerSite() + Urls.URL_DOWNLOAD + "session=" + CrewChatApplication.getInstance().getPrefs().getaccesstoken() + "&no=" + attachDTO.getAttachNo();
                            Log.d("playVideoStreaming", "url:" + url);
                            // Check local data
                            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constant.pathDownload, fileName);
                            new WebClientAsyncTask(mActivity, progressDownloading, url, fileName, new OnDownloadFinish() {
                                @Override
                                public void onFinish(File file) {
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
                            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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

    long MessageNo;

    void actionToMe() {
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
        if (!TextUtils.isEmpty(videoUrl)) {
            String path = Environment.getExternalStorageDirectory() + Constant.pathDownload + "/" + fileName;
            File file = new File(path);
//            if (file.isFile()) {
////                        Toast.makeText(mActivity, "file exist", Toast.LENGTH_LONG).show();
//                mActivity.startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
//            } else {
//                Utils.displayDownloadFileDialog(BaseActivity.Instance, videoUrl, fileName);
//            }
            Utils.displayDownloadFileDialog(BaseActivity.Instance, videoUrl, fileName);
        }
    }

    void actionShare() {

        if (!TextUtils.isEmpty(videoUrl)) {
            String path = Environment.getExternalStorageDirectory() + Constant.pathDownload + "/" + fileName;
            File file = new File(path);
            if (file.isFile()) {
//                        Toast.makeText(mActivity, "file exist", Toast.LENGTH_LONG).show();
//                mActivity.startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
            } else {
                if (ChattingActivity.instance.checkPermissionsWandR()) {
                    Utils.DownloadImage(BaseActivity.Instance, videoUrl, fileName);
                } else {
                    ChattingActivity.instance.setPermissionsRandW();
                }

            }
            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("*/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            mActivity.startActivity(Intent.createChooser(shareIntent, "Share video using"));
        }
    }

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