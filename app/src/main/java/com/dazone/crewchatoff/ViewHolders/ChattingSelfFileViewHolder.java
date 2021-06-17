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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
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
import android.widget.LinearLayout;
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
import com.dazone.crewchatoff.constant.Constants;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.customs.AudioPlayer;
import com.dazone.crewchatoff.dto.AttachDTO;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.fragment.ChattingFragment;
import com.dazone.crewchatoff.interfaces.ICreateOneUserChatRom;
import com.dazone.crewchatoff.interfaces.IF_Relay;
import com.dazone.crewchatoff.interfaces.Urls;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.PermissionUtil;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;

import org.greenrobot.eventbus.EventBus;

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

import static com.dazone.crewchatoff.utils.Utils.getString;
import static com.dazone.crewchatoff.utils.Utils.getTypeFile;

public class ChattingSelfFileViewHolder extends BaseChattingHolder implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
    private TextView tvFileDate, tvFileName, tvFileSize, fileReceiveTv;
    private TextView tvUnread, tvDuration;
    protected ImageView imgFileThumb;
    private LinearLayout linearLayout, layoutNotAudio, layoutAudio;
    private ProgressBar progressBar, pBar;
    String TAG = "ChattingSelfFileViewHolder";
    private long MessageNo;
    private LinearLayout llDate;
    private TextView tvDate;
    private String fileType = "";
    private String fileName = "";
    private ChattingDto tempDto;
    int getUnReadCount;

    public ChattingSelfFileViewHolder(View v) {
        super(v);
    }

    @Override
    protected void setup(View v) {
        tvFileDate = v.findViewById(R.id.date_tv);
        tvFileName = v.findViewById(R.id.file_name_tv);
        tvFileSize = v.findViewById(R.id.file_size_tv);
        fileReceiveTv = v.findViewById(R.id.file_receive_tv);
        imgFileThumb = v.findViewById(R.id.file_thumb);
        linearLayout = v.findViewById(R.id.main_attach);
        progressBar = v.findViewById(R.id.progressBar);
        pBar = v.findViewById(R.id.pBar);
        tvUnread = v.findViewById(R.id.text_unread);
        layoutNotAudio = v.findViewById(R.id.layoutNotAudio);
        layoutAudio = v.findViewById(R.id.layoutAudio);
        tvDuration = v.findViewById(R.id.tvDuration);
        linearLayout.setOnCreateContextMenuListener(this);

        llDate = v.findViewById(R.id.llDate);
        tvDate = v.findViewById(R.id.time);
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    @Override
    public void bindData(final ChattingDto dto) {
        tempDto = dto;
        MessageNo = dto.getMessageNo();
        llDate.setVisibility(dto.isHeader()? View.VISIBLE : View.GONE);
        tvDate.setText(Utils.getStrDate(dto));
        getUnReadCount = dto.getUnReadCount();
        long regDate = new Date(TimeUtils.getTime(dto.getRegDate())).getTime();
        tvFileDate.setText(TimeUtils.displayTimeWithoutOffset(CrewChatApplication.getInstance().getApplicationContext(), regDate, 0));
        attachDTOTemp = dto.getAttachInfo();

        if (dto.getmType() == Statics.CHATTING_VIEW_TYPE_SELECT_FILE) {
            fileName = dto.getAttachFileName();
            tvFileName.setText(fileName);
            tvFileSize.setText(Utils.readableFileSize(dto.getAttachFileSize()));
            fileReceiveTv.setVisibility(View.GONE);


            fileType = Utils.getFileType(dto.getAttachFileName());
            ImageUtils.imageFileType(imgFileThumb, fileType);

            if (!dto.isSendTemp()) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
                ChattingFragment.instance.SendTo(dto, progressBar);
            } else progressBar.setVisibility(View.GONE);

        } else {
            fileName = dto.getAttachInfo().getFileName();
            if (fileName == null || fileName.trim().length() == 0)
                fileName = dto.getAttachFileName();
            if (fileName == null) fileName = "";

            fileType = Utils.getFileType(fileName);
            ImageUtils.imageFileType(imgFileThumb, fileType);

            tvFileName.setText(fileName);
            tvFileSize.setText(Utils.readableFileSize(dto.getAttachInfo().getSize()));
            fileReceiveTv.setVisibility(View.GONE);

            linearLayout.setOnClickListener(v -> {
                AttachDTO attachDTO = dto.getAttachInfo();
                touchOnView(attachDTO);
            });

            linearLayout.setOnLongClickListener(view -> {
                view.showContextMenu();
                return true;
            });
        }

        String strUnReadCount = String.valueOf(dto.getUnReadCount());
        tvUnread.setText(strUnReadCount);
        tvFileDate.setOnClickListener(v -> actionUnread());
        tvUnread.setVisibility(dto.getUnReadCount() == 0 ? View.GONE : View.VISIBLE);

        tvUnread.setOnClickListener(v -> actionUnread());
        if (Utils.isAudio(fileType)) {
            if (layoutNotAudio != null) layoutNotAudio.setVisibility(View.GONE);
            if (layoutAudio != null) layoutAudio.setVisibility(View.VISIBLE);

            if (tvDuration != null) {
                AttachDTO attachDTO = dto.getAttachInfo();
                if (attachDTO != null) {
                    String fileName = attachDTO.getFileName();
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Statics.AUDIO_RECORDER_FOLDER + "/" + fileName;
                    File file = new File(path);
                    if (!file.exists()) {
                        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Statics.AUDIO_RECORDER_FOLDER_ROOT + "/" + fileName;
                    }

                    new Constant.audioGetDuration(BaseActivity.Instance, path, duration -> tvDuration.setText(duration)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    tvDuration.setText("");
                }
            }
        } else {
            if (layoutNotAudio != null) layoutNotAudio.setVisibility(View.VISIBLE);
            if (layoutAudio != null) layoutAudio.setVisibility(View.GONE);
        }

        tvUnread.setVisibility(CrewChatApplication.getInstance().getPrefs().getBooleanValue(Constants.HAS_CALL_UNREAD_COUNT, false) || dto.getUnReadCount() == 0? View.GONE : View.VISIBLE);
    }
    void touchOnView(AttachDTO attachDTO) {
        if (ChattingActivity.instance != null) {
            if (checkPermissionsWandR()) {
                if (attachDTO != null) {

                    String url = new Prefs().getServerSite() + Urls.URL_DOWNLOAD + "session=" + CrewChatApplication.getInstance().getPrefs().getaccesstoken() + "&no=" + attachDTO.getAttachNo();
                    Log.d(TAG, "url:" + url);

                    pBar.setVisibility(View.VISIBLE);
                    new WebClientAsyncTask(BaseActivity.Instance, pBar, url, fileName, new OnDownloadFinish() {
                        @Override
                        public void onFinish(File file) {
                            Log.d(TAG, "onFinish download file");

                            if (Utils.isAudio(fileType)) {
                                String path = file.getAbsolutePath();
                                Log.d(TAG, "path:" + path);
                                new AudioPlayer(BaseActivity.Instance, path, fileName).show();
                            } else if (Utils.isVideo(fileType)) {
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
                            } else {
                                openFile(file);
                            }
                        }

                        @Override
                        public void onError() {
                            Toast.makeText(BaseActivity.Instance, "Can not open this file", Toast.LENGTH_SHORT).show();
                        }
                    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            } else {
                PermissionUtil.INSTANCE.requestPermissions(BaseActivity.Instance, RandW_PERMISSIONS_REQUEST_CODE, PermissionUtil.INSTANCE.getPermissionsStorage());
            }
        } else {
            Toast.makeText(CrewChatApplication.getInstance(), CrewChatApplication.getInstance().getResources().getString(R.string.can_not_check_permission), Toast.LENGTH_SHORT).show();
        }

    }

    void openFile(File file) {
        try {
            String type = Constant.getMimeType(file.getAbsolutePath());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri apkUri = FileProvider.getUriForFile(BaseActivity.Instance, BaseActivity.Instance.getApplicationContext().getPackageName() + ".provider", file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(apkUri, type);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                BaseActivity.Instance.startActivity(intent);
            } else {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), type);
                BaseActivity.Instance.startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(BaseActivity.Instance, "No Application available to view this file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    AttachDTO attachDTOTemp = null;

    void actionDownload() {
        if (attachDTOTemp != null) {
            String url = new Prefs().getServerSite() + Urls.URL_DOWNLOAD + "session=" + CrewChatApplication.getInstance().getPrefs().getaccesstoken() + "&no=" + attachDTOTemp.getAttachNo();
            Log.d(TAG, "url download:" + url);
            String path = Environment.getExternalStorageDirectory() + Constant.pathDownload + "/" + attachDTOTemp.getFileName();
            Utils.displayDownloadFileDialog(BaseActivity.Instance, url, attachDTOTemp.getFileName());
        }
    }

    public boolean checkPermissionsWandR() {
        return PermissionUtil.INSTANCE.checkPermissions(BaseActivity.Instance, PermissionUtil.INSTANCE.getPermissionsStorage());
    }

    int RandW_PERMISSIONS_REQUEST_CODE = 1;

    private ProgressDialog mProgressDialog = null;

    void actionShare() {
        if (attachDTOTemp != null) {
            String url = new Prefs().getServerSite() + Urls.URL_DOWNLOAD + "session=" + CrewChatApplication.getInstance().getPrefs().getaccesstoken() + "&no=" + attachDTOTemp.getAttachNo();
            String path = Environment.getExternalStorageDirectory() + Constant.pathDownload + "/" + attachDTOTemp.getFileName();
            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("*/*");
            File file = new File(path);
            if (file.exists()) {
                Uri uri = FileProvider.getUriForFile(CrewChatApplication.getInstance(), BuildConfig.APPLICATION_ID + ".provider", file);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                BaseActivity.Instance.startActivity(Intent.createChooser(shareIntent, "Share file using"));
            } else {
                if (checkPermissionsWandR()) {
                    mProgressDialog = new ProgressDialog(BaseActivity.Instance);
                    mProgressDialog.setMessage(getString(R.string.download));
                    mProgressDialog.setIndeterminate(true);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();
                    DownloadImage(BaseActivity.Instance, url, attachDTOTemp.getFileName(), shareIntent, file);
                } else {
                    PermissionUtil.INSTANCE.requestPermissions(BaseActivity.Instance, RandW_PERMISSIONS_REQUEST_CODE, PermissionUtil.INSTANCE.getPermissionsStorage());
                }
            }


        }
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
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(reference);
                    Cursor c = downloadmanager.query(query);

                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            if (mProgressDialog != null) {
                                mProgressDialog.dismiss();
                            }
                            Uri uri = FileProvider.getUriForFile(CrewChatApplication.getInstance(), BuildConfig.APPLICATION_ID + ".provider", file);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            BaseActivity.Instance.startActivity(Intent.createChooser(shareIntent, "Share file using"));
                        }
                    }
                }
            }
        };

        context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }


    void actionRelay() {
        Intent intent = new Intent(BaseActivity.Instance, RelayActivity.class);
        intent.putExtra(Statics.MessageNo, MessageNo);
        BaseActivity.Instance.startActivity(intent);
    }

    void actionOpen() {
        touchOnView(attachDTOTemp);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case Statics.MENU_OPEN:
                actionOpen();
                break;
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
        Log.d(TAG, "onCreateContextMenu");
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

        void onError();
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
                Log.d("doInBackground", "name:" + this.mName);
                outputFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constant.pathDownload, this.mName);

                if (outputFile.exists()) {
                    outputFile.delete();
                }

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
            if (outputFile != null) {
                if (outputFile.length() != 0) {
                    mDownloadCallback.onFinish(outputFile);

                } else {
                    mDownloadCallback.onError();
                }
            } else {
                mDownloadCallback.onError();
            }
            mProgressBar.setVisibility(View.GONE);

        }
    }
}