package com.dazone.crewchatoff.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.dazone.crewchatoff.BuildConfig;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.interfaces.AudioGetDuration;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Constant {
    public final static String ROOT_URL_ANDROID = "http://www.crewcloud.net/android";
    public final static String PACKGE = "/Package/";
    public static final int ACTIVITY_HANDLER_NEXT_ACTIVITY = 1111;
    public static final int ACTIVITY_HANDLER_START_UPDATE = 1112;
    public static String TAG = "Constant";
    public static String CALL_CHAT_FRAGMENT = "CALL_CHAT_FRAGMENT";
    /**
     * URLS
     */
//    public static final String ROOT_URL_UPDATE = "http://www.crewcloud.net";
    public static final String ROOT_URL_UPDATE = "http://www.bizsw.co.kr:8080";


    /**
     * PATH SAVE DOWNLOAD
     */
    public static final String pathDownload = "/CrewChat/";
    public static final String pathDownload_no = "/CrewChat";

    /**
     * URI IMAGE DEFAULT
     */
    public static final String UriDefaultAvatar = "drawable://" + R.drawable.avatar_l;


    /**
     * INTENT FILTER
     */
    public static final String INTENT_FILTER = "INTENT_FILTER";
    public static final String INTENT_FILTER_SEARCH = "INTENT_FILTER_SEARCH";
    public static final String INTENT_FILTER_GET_MESSAGE_UNREAD_COUNT = "INTENT_FILTER_GET_MESSAGE_UNREAD_COUNT";
    public static final String INTENT_FILTER_ADD_USER = "INTENT_FILTER_ADD_USER";
    public static final String INTENT_FILTER_CHAT_DELETE_USER = "INTENT_FILTER_CHAT_DELETE_USER";
    public static final String INTENT_FILTER_UPDATE_ROOM_NAME = "INTENT_FILTER_UPDATE_ROOM_NAME";
    public static final String INTENT_GOTO_UNREAD_ACTIVITY = "INTENT_GOTO_UNREAD_ACTIVITY";


    public static final String INTENT_FILTER_NOTIFY_ADAPTER = "INTENT_FILTER_NOTIFY_ADAPTER";
    /**
     * INTENT RESULT
     */
    public static final int INTENT_RESULT_CREATE_NEW_ROOM = 800;


    /**
     * KEY INTENT
     */
    public static final String KEY_INTENT_TEXT_SEARCH = "KEY_INTENT_TEXT_SEARCH";
    public static final String KEY_INTENT_BASE_DATE = "KEY_INTENT_BASE_DATE";
    public static final String KEY_INTENT_ROOM_NO = "KEY_INTENT_ROOM_NO";
    public static final String KEY_INTENT_ROOM_DTO = "KEY_INTENT_ROOM_DTO";
    public static final String KEY_INTENT_GROUP_NO = "KEY_INTENT_GROUP_NO";
    public static final String KEY_INTENT_USER_NO = "KEY_INTENT_USER_NO";
    public static final String KEY_INTENT_USER_NO_ARRAY = "KEY_INTENT_USER_NO_ARRAY";
    public static final String KEY_INTENT_USER_DB = "KEY_INTENT_USER_DB";
    public static final String KEY_INTENT_COUNT_MEMBER = "KEY_INTENT_COUNT_MEMBER";
    public static final String KEY_INTENT_CHATTING_DTO = "KEY_INTENT_CHATTING_DTO";
    public static final String KEY_INTENT_UNREAD_TOTAL_COUNT = "KEY_INTENT_UNREAD_TOTAL_COUNT";
    public static final String KEY_INTENT_USER_STATUS_DTO = "KEY_INTENT_USER_STATUS_DTO";
    public static final String KEY_INTENT_ROOM_TITLE = "KEY_INTENT_ROOM_TITLE";
    public static final String KEY_INTENT_SELECT_USER_RESULT = "KEY_INTENT_SELECT_USER_RESULT";

    public static final String Favorites = "Favorites";

    /**
     * DISPLAY IMAGE OPTION
     */
    public static final DisplayImageOptions optionsProfileAvatar = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.loading)
            .showImageOnFail(R.drawable.loading)
            .cacheOnDisk(true).cacheInMemory(true)
            .imageScaleType(ImageScaleType.NONE_SAFE)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .considerExifParams(false)
            .displayer(new RoundedBitmapDisplayer(180))
            .showImageOnLoading(R.drawable.loading)
            .build();


    /**
     * DISPLAY IMAGE OPTION SETTING PROFILE
     */
    public static final DisplayImageOptions optionsProfileAvatarSetting = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.loading)
            .showImageOnFail(R.drawable.loading)
            .cacheOnDisk(true)
            .cacheInMemory(true)
            .imageScaleType(ImageScaleType.NONE_SAFE)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .considerExifParams(false)
            .displayer(new RoundedBitmapDisplayer(10))
            .showImageOnLoading(R.drawable.loading)
            .build();

    /*
    * TYPE ACTION
    * */
    public static final int TYPE_ACTION_FAVORITE = 1009;
    public static final int TYPE_ACTION_ALARM_ON = 1010;
    public static final int TYPE_ACTION_ALARM_OFF = 1011;

    /**
     * TYPE ROUNDED
     */
    public static final int TYPE_ROUNDED_TOP_RIGHT = 1001;
    public static final int TYPE_ROUNDED_TOP_LEFT = 1002;
    public static final int TYPE_ROUNDED_BOTTOM_RIGHT = 1003;
    public static final int TYPE_ROUNDED_BOTTOM_LEFT = 1004;
    public static final int TYPE_ROUNDED_LEFT_SIDE = 1005;
    public static final int TYPE_ROUNDED_TOP = 1007;
    public static final int TYPE_ROUNDED_RIGHT_SIDE = 1006;


    /**
     * FILE TYPES
     */
    public static final String IMAGE_JPG = ".jpg";
    public static final String IMAGE_JPEG = ".jpeg";
    public static final String IMAGE_PNG = ".png";
    public static final String IMAGE_GIF = ".gif";
    public static final String AUDIO_MP3 = ".mp3";
    public static final String AUDIO_WMA = ".wma";
    public static final String AUDIO_AMR = ".amr";
    public static final String VIDEO_MP4 = ".mp4";
    public static final String FILE_PDF = ".pdf";
    public static final String FILE_DOCX = ".docx";
    public static final String FILE_DOC = ".doc";
    public static final String FILE_XLS = ".xls";
    public static final String FILE_XLSX = ".xlsx";
    public static final String FILE_PPTX = ".pptx";
    public static final String FILE_PPT = ".ppt";
    public static final String FILE_ZIP = ".zip";
    public static final String FILE_RAR = ".rar";
    public static final String FILE_APK = ".apk";
//____________>SON edit
    /**
     * rotation screen
     */
    public static final int AUTOMATIC = 0;
    public static final int PORTRAIT = 1;
    public static final int LANSCAPE = 2;
    //approval message
    public static final int APPROVAL = 3;

    //end Son edit
    public static void cancelAllNotification(Context ctx, int id) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(id);
    }

    public static String getUserName(List<TreeUserDTOTemp> allUser, int userNo) {
        String s = "";
        for (TreeUserDTOTemp obj : allUser) {
            if (obj.getUserNo() == userNo) {
                s = obj.getName();
                break;
            }
        }
        return s;
    }

    public static boolean isAddUser(ArrayList<Integer> userNos, int uN) {
        for (int i : userNos) {
            if (i == uN) {
                return false;
            }
        }
        return true;
    }


    public static int getSTT(int status) {
        int iv;
        if (status == Statics.USER_LOGIN) {
            iv = R.drawable.home_big_status_01;
        } else if (status == Statics.USER_AWAY) {
            iv = R.drawable.home_big_status_02;
        } else {
            iv = R.drawable.home_big_status_03;
        }
        return iv;
    }


    public static List<ChattingDto> addIdUnknow(List<ChattingDto> list, int myId) {
        for (ChattingDto dto : list) {
            ArrayList<Integer> UserNos = dto.getUserNos();
            if (UserNos.size() == 1 && UserNos.get(0) == myId && dto.getRoomType() != 1) {
                ArrayList<Integer> lst = UserNos;
                lst.add(dto.getMakeUserNo());
                dto.setUserNos(lst);
            }
        }
        return list;
    }

    public static boolean isAddChattingDto(ChattingDto dto) {
        boolean flag = dto.isOne();
        if (!flag && dto.getUserNos().size() < 2 && dto.getRoomType() != 1)
            return false;
        return true;
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = url.substring(url.lastIndexOf(".") + 1);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    static int THUMBNAIL_SIZE = 300;

    public static Bitmap getThumbnail(Context con, Uri uri) throws FileNotFoundException, IOException {
        InputStream input = con.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            return null;
        }

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true; //optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//
        input = con.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    public static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0) return 1;
        else return k;
    }

    public static ArrayList<Integer> removeDuplicatePosition(ArrayList<Integer> lst) {
        for (int i = 0; i < lst.size(); i++) {
            for (int j = i + 1; j < lst.size(); j++) {
                if (lst.get(i) == lst.get(j)) {
                    lst.remove(i);
                    i--;
                    break;
                }
            }
        }
        return lst;
    }

    public static boolean isIp(String str) {
        if (str == null || str.length() == 0)
            return false;
        String[] arr = str.split("[.]");
        if (arr.length != 4)
            return false;
        for (String sub : arr) {
            try {
                int a = Integer.parseInt(sub);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public static Uri convertUri(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(context, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        Uri myUri = Uri.parse("file://" + result);
        return myUri;
    }

    public static boolean isAddSearch(List<TreeUserDTO> lst, int id) {
        for (TreeUserDTO obj : lst) {
            if (obj.getId() == id)
                return false;
        }
        return true;
    }

    public static String get_department_name(TreeUserDTO index, List<TreeUserDTO> listTemp_3) {
        int n = listTemp_3.size();
        if (n == 0) return "";
        for (int i = n - 1; i >= 0; i--) {
            TreeUserDTO obj = listTemp_3.get(i);
            if (obj != null) {
                if (obj.getLevel() < index.getLevel())
                    return obj.getName();
            } else {
                return "";
            }
        }
        return "";
    }

    public static String getFilename(int currentFormat, String fileName) {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, Statics.AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + fileName + Statics.file_exts[currentFormat]);
    }

    public static String audioFormatDuration(int second) {
        try {
            int m = second / 60;
            int s = second % 60;

            if (m == 0 && s == 0) s = 1;

            String MIN = "" + m;
            if (m < 10) MIN = "0" + m;
            String SEC = "" + s;
            if (s < 10) SEC = "0" + s;
            return MIN + ":" + SEC;
        } catch (Exception e) {
            return "";
        }
    }

    public static class audioGetDuration extends AsyncTask<String, String, String> {
        Context context;
        String path;
        AudioGetDuration callBack;

        public audioGetDuration(Context context, String path, AudioGetDuration callBack) {
            this.context = context;
            this.path = path;
            this.callBack = callBack;
        }

        @Override
        protected String doInBackground(String... strings) {
            final File file = new File(path);
            String duration = "";
            if (file.exists()) {
                try {
                    MediaPlayer mp = MediaPlayer.create(context, Uri.parse(path));
                    int d = mp.getDuration() / 1000;
//                Log.d(TAG, "duration:" + duration);
                    duration = audioFormatDuration(d);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return duration;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            callBack.onComplete(s);
        }
    }

    public static void openFile(Context context, File file) {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri apkUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(apkUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            } else {
                String type = Constant.getMimeType(file.getAbsolutePath());
                Log.d(TAG, "type:" + type);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), type);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception");
            Toast.makeText(context, "No Application available to view this file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

//    public static void audioPlayer(Activity context, String pathAudio) {
//        final Dialog dialog = new Dialog(context);
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.player);
//
//        // Handler
//        final Handler mHandler = new Handler();
//        // songProgressBar
//        final SeekBar songProgressBar = (SeekBar) dialog.findViewById(R.id.songProgressBar);
//
//
//        // MediaPlayer
//        final MediaPlayer mp = new MediaPlayer();
//        try {
//            mp.setDataSource(pathAudio);
//            mp.prepare();
//            mp.start();
//            // songProgressBar
//            songProgressBar.setProgress(0);
//            songProgressBar.setMax(mp.getDuration());
//            mHandler.postDelayed(mUpdateTimeTask, 1000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (mp == null) return;
//
//
//
//
//        // btnClose
//        FrameLayout btnClose = (FrameLayout) dialog.findViewById(R.id.btnClose);
//        btnClose.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mp != null) {
//                    mp.release();
//                }
//                dialog.dismiss();
//            }
//        });
//        // setOnKeyListener dialog
//        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    Log.d("audioPlayer", "KEYCODE_BACK");
//                }
//                return true;
//            }
//        });
//
//        Window window = dialog.getWindow();
//        window.setGravity(Gravity.CENTER);
//        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//        dialog.show();
//    }

    public static String getUnreadText(Context context, int count) {
        String s = "";
        if (count == 0) s = context.getResources().getString(R.string.read_msg);
        else s = context.getResources().getString(R.string.unread_msg);
        return s;
    }
}
