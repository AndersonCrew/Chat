package com.dazone.crewchatoff.constant;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;

import com.dazone.crewchatoff.BuildConfig;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.utils.Constant;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.dazone.crewchatoff.customs.RoundedOneCorner;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

public interface Statics {

    int REQUEST_CODE =112;
    int RC_HANDLE_CAMERA_PERM = 770;

    // 자리비움 시간(초)
    int USER_STATUS_AWAY_TIME = 60 * 3;
    // version code
    String VERSION_CODE = "crewchat_version_code";
    // setting notification default time
    int DEFAULT_START_NOTIFICATION_TIME = 8;
    int DEFAULT_END_NOTIFICATION_TIME = 18;
    // setting notification key
    String ENABLE_NOTIFICATION = "enable_notification";
    String ENABLE_SOUND = "enable_notification_sound";
    String ENABLE_VIBRATE = "enable_notification_vibrate";
    String ENABLE_TIME = "enable_notification_time";
    String ENABLE_NOTIFICATION_WHEN_USING_PC_VERSION = "enable_notification_when_using_pc_version";
    String MessageNo = "MessageNo";

    String START_NOTIFICATION_HOUR = "start_notification_time_hour";
    String START_NOTIFICATION_MINUTES = "start_notification_time_minutes";
    String IS_FIRST_SHARE = "is_first_share";
    String VALUE_CODE_SHARE = "value_code_share";
    String END_NOTIFICATION_HOUR = "end_notification_time_hour";
    String END_NOTIFICATION_MINUTES = "end_notification_time_minutes";

    String FIRST_LOGIN = "first_login";
    String IS_NEW_CHAT = "is_new_chat";
    String IS_ENABLE_ENTER_KEY = "is_enable_enter_key";
    String IS_ENABLE_ENTER_VIEW_DUTY_KEY = "is_enable_enter_view_duty_key";
    String SCREEN_ROTATION = "creen_rotation";

//    preft key

    String ORGANIZATION_TREE = "organization_tree";
    String KEY_DATA_CURRENT_CHAT_LIST = "key_data_current_chat_list";

    String IMAGE_SIZE_MODE = "image_size_mode";
    String ID_MESS = "id_mess";
    int MODE_ORIGINAL = 0;
    int MODE_DEFAULT = 1;

    int NEW_GROUP_CHAT_TITLE = 1;
    int NEW_GROUP_CHAT_NO_TITLE = 0;

    long MYROOM_DEFAULT = -55;

//    end preft key

    int TYPE_DEPART = 0;
    int TYPE_USER = 2;

    // Room action type
    int ROOM_RENAME = 1;
    int ROOM_OPEN = 2;
    int ROOM_ADD_TO_FAVORITE = 3;
    int ROOM_REMOVE_FROM_FAVORITE = 6;
    int ROOM_ALARM_ON = 4;
    int ROOM_ALARM_OFF = 7;
    int ROOM_LEFT = 5;

    // Image menu conext
    int MENU_OPEN = 0;
    int MENU_COPY = 1;
    int MENU_DOWNLOAD = 2;
    int MENU_DELETE = 3;
    int MENU_SHARE = 4;
    int MENU_RELAY = 111;
    int MENU_TO_ME = 112;
    int MENU_UNREAD_MSG = 113;


    int GET_USER_LIMIT = 10;

    int MENU_REGISTERED_USERS = 5;
    int MENU_MODIFYING_GROUP = 6;
    int MENU_DELETE_GROUP = 7;
    int MENU_REMOVE_FROM_FAVORITE = 8;
    int MENU_OPEN_CHAT_ROOM = 9;
    int MENU_OPEN_CHAT_GROUP = 10;
    int ID_GROUP = 0;
    // Chat room Key
    String ROOM_TITLE = "room_title";
    String ROOM_NO = "room_no";
    int RENAME_ROOM = 1001;

    //    bundle key
    String IV_STATUS = "IV_STATUS";
    String CONTAINS_CHAT = "CONTAINS_CHAT";
    String TREE_USER_PC = "tree_user_pc";
    //    end bundle key
    String CHATTING_DTO = "chatting_dto";
    String CHATTING_DTO_FOR_GROUP_LIST = "chatting_dto_for_group_list";
    String CHATTING_DTO_GALLERY = "chatting_dto_gallery";

    /*
    * LASTED MESSAGE TYPE
    * */
    int MESSAGE_TYPE_NORMAL = 0;
    int MESSAGE_TYPE_SYSTEM = 1;
    int MESSAGE_TYPE_ATTACH = 2;

    /*
    * LASTED MESSAGE ATTACH TYPE
    * */
    int ATTACH_FILE = 2;
    int ATTACH_IMAGE = 1;
    int ATTACH_NONE = 0;

    /**
     * CHAT VIEW IMAGE
     */


    String CHATTING_DTO_GALLERY_LIST = "chatting_dto_gallery_list";
    String CHATTING_DTO_GALLERY_POSITION = "chatting_dto_gallery_position";
    String CHATTING_DTO_GALLERY_SHOW_FULL = "chatting_dto_gallery_show_full";
    String CHATTING_DTO_GALLERY_SINGLE = "chatting_dto_gallery_single";
    String CHATTING_DTO_REG_DATE = "chatting_dto_reg_date";
    String get_user_name_from_db = "get_user_name_from_db";

    String CHATTING_DTO_ADD_USER = "add_user";
    String CHATTING_DTO_ADD_USER_NEW = "add_user_new";
    String GCM_NOTIFY = "gcm_notify";

    int REQUEST_TIMEOUT_MS = 15000;

    String TAG = "CrewChatLogs";
    String TAG_DAVID = "David_tags";
    String PREFS_KEY_SESSION_ERROR = "session_error";
    int DATABASE_VERSION = 39;
    String DATABASE_NAME = "crewchatoffline.db";


    //main version
    int MAIN_ACTIVITY_TAB_COUNT = 4;

    //format date
    String DATE_FORMAT_YY_MM_DD_DD = "yy-MM-dd-EEEEEEE";
    String DATE_FORMAT_YY_MM_DD_DD_H_M = "yy-MM-dd-EEEEEEE hh:mm aa";
    String yy_MM_dd_hh_mm_aa = "yy_MM_dd_HH_mm_ss";
    String DATE_FORMAT_YY_MM_DD = "yy-MM-dd";
    String DATE_FORMAT_YY_MM_DD_H_M = "yy-MM-dd hh:mm aa";
    String DATE_FORMAT_HH_MM_AA = "hh:mm aa";
    String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";

    String DATE_FORMAT_YYYY_MM_DD_AM_PM_HH_MM = "yyyy-MM-dd aa hh:mm";

    String yyyy_MM_dd_HH_mm_ss_SSS = "yyyy-MM-dd HH:mm:ss.SSS";


    int CHATTING_VIEW_TYPE_PERSON = 0;
    int CHATTING_VIEW_TYPE_PERSON_NOT_SHOW = 9;
    int CHATTING_VIEW_TYPE_SELF = 1;
    int CHATTING_VIEW_TYPE_SELF_NOT_SHOW = 10;
    int CHATTING_VIEW_TYPE_DATE = 2;
    int CHATTING_VIEW_TYPE_GROUP = 3;
    int CHATTING_VIEW_TYPE_SELF_IMAGE = 4;
    int CHATTING_VIEW_TYPE_PERSON_IMAGE = 5;
    int CHATTING_VIEW_TYPE_PERSON_IMAGE_NOT_SHOW = 11;
    int CHATTING_VIEW_TYPE_SELF_FILE = 6;
    int CHATTING_VIEW_TYPE_PERSON_FILE = 7;
    int CHATTING_VIEW_TYPE_PERSON_FILE_NOT_SHOW = 12;
    int CHATTING_VIEW_TYPE_GROUP_NEW = 8;
    int CHATTING_VIEW_TYPE_SELECT_IMAGE = 13;
    int CHATTING_VIEW_TYPE_SELECT_FILE = 14;
    int CHATTING_VIEW_TYPE_CONTACT = 15;
    int CHATTING_VIEW_TYPE_EMPTY = 16;
    int CHATTING_VIEW_TYPE_SELF_VIDEO = 17;
    int CHATTING_VIEW_TYPE_SELECT_VIDEO = 18;
    int CHATTING_VIEW_TYPE_PERSON_VIDEO_NOT_SHOW = 19;
    int CHATTING_VIEW_TYPE_4 = 20;
    int CHATTING_VIEW_TYPE_5 = 21;
    int CHATTING_VIEW_TYPE_6 = 22;
    int CHATTING_VIEW_TYPE_AUDIO_ME = 23;
    int CHATTING_VIEW_TYPE_AUDIO_USER = 24;

    // New user status
    int USER_LOGIN = 1;
    int USER_AWAY = 3;
    int USER_LOGOUT = 0;

    // Device Type String
    String DEVICE_TYPE = "Android";

    // End new user status

    int USER_STATUS_WORKING = 0;
    int USER_STATUS_AWAY = 1;
    int USER_STATUS_RESTING = 2;
    int USER_STATUS_WORKING_OUTSIDE = 3;
    int USER_STATUS_IN_A_CAL = 4;
    int USER_STATUS_METTING = 5;

    String NOTE_SUPPORT_URI_IMAGE = "content://media/external/images/media";
    String NOTE_SUPPORT_URI_VIDEO = "content://media/external/video/media";

    int MEDIA_TYPE_IMAGE = 1;
    int MEDIA_TYPE_VIDEO = 2;

    //CusTomGallery
    String TEMP_PATH_IMAGE = "/.temp_tmp";
    String FILE_NOTE_CUSTOM_GALLERY_ADAPTER = "file://";
    String ACTION_MULTIPLE_PICK = BuildConfig.APPLICATION_ID + ".ACTION_MULTIPLE_PICK";
    String KEY_DATA_PATH_INTENT = "key_path_intent";
    String KEY_DATA_NAME_INTENT = "key_name_intent";

    String IMAGE_DIRECTORY_NAME = "CrewChat";
    String DATE_FORMAT_PICTURE = "yyyyMMdd_HHmmss";

    int VIDEO_PICKER_SELECT = 300;
    int FILE_PICKER_SELECT = 400;
    int ADD_USER_SELECT = 500;
    int CONTACT_PICKER_SELECT = 600;
    int ADD_USER_TO_FAVORITE = 700;

    boolean WRITE_HTTP_REQUEST = true;


    int REQUESTCODE_UPDATE_CURRENT_CHAT_LIST = 999;

    //for intent
    int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    int CAMERA_VIDEO_REQUEST_CODE = 102;
    int IMAGE_PICKER_SELECT = 101;
    int IMAGE_ROTATE_CODE = 103;

    int ORGANIZATION_DISPLAY_SELECTED_ACTIVITY = 303;

    String ACTION_SHOW_SEARCH_INPUT = "receiver_show_search_input";
    String ACTION_HIDE_SEARCH_INPUT = "receiver_hide_search_input";

    String ACTION_SHOW_SEARCH_FAVORITE_INPUT = "receiver_show_search_favorite_input";
    String ACTION_HIDE_SEARCH_FAVORITE_INPUT = "receiver_hide_search_favorite_input";

    String ACTION_SHOW_SEARCH_INPUT_IN_CURRENT_CHAT = "receiver_show_search_input_in_current_chat";
    String ACTION_HIDE_SEARCH_INPUT_IN_CURRENT_CHAT = "receiver_hide_search_input_in_current_chat";

    //Google api
    String GOOGLE_SENDER_ID = "360611512660";//AIzaSyDSTPgQtGRDc1tvuWhY8z7h1PlH8jPdRsw
    //final public static String GOOGLE_SENDER_ID = "972801588344";//AIzaSyDoiBazJP1kKBVBTbYRjHyHLm8_2aC0MYs
    String ACTION_RECEIVER_NOTIFICATION = "receiver_notification";
    String GCM_DATA_NOTIFICATOON = "gcm_data_notificaiton";

    String ACTION_UPDATE_CURRENT_CHAT_LIST = "action_update_current_chat_list";
    String DATA_UPDATE_CURRENT_CHAT_LIST = "data_update_current_chat_list";
    public static String BROADCAST_ACTION = "com.dazone.crewcloud.account.receive";

    String APPLICATION_ID = "com.dazone.crewchatoff";
    BitmapFactory.Options decodingOptions = new BitmapFactory.Options();

    DisplayImageOptions options3 = new DisplayImageOptions.Builder()
            .cacheOnDisk(true).cacheInMemory(true)
            .imageScaleType(ImageScaleType.NONE_SAFE)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .considerExifParams(false)
            .displayer(new RoundedBitmapDisplayer(90))
            .build();


    /**
     * DISPLAY OPTIONS AVATAR GROUP
     */
    // 그룹 프로필 사진 이미지(하단 왼쪽) 4인이상의 경우
    DisplayImageOptions avatarGroupBL = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.loading)
            .showImageOnFail(R.drawable.loading)
            .cacheOnDisk(true).cacheInMemory(true)
            .imageScaleType(ImageScaleType.NONE_SAFE)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .displayer(new RoundedOneCorner(90, Constant.TYPE_ROUNDED_BOTTOM_LEFT))
            .considerExifParams(false)
            .build();

    // 그룹 프로필 사진 이미지(하단 오른쪽) 4인이상의 경우
    DisplayImageOptions avatarGroupBR = new DisplayImageOptions.Builder()
            .cacheOnDisk(true).cacheInMemory(true)
            .imageScaleType(ImageScaleType.NONE_SAFE)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .displayer(new RoundedOneCorner(90, Constant.TYPE_ROUNDED_BOTTOM_RIGHT))
            .considerExifParams(false)
            .build();

    // 그룹 프로필 사진 이미지(상단 왼쪽) 4인이상의 경우
    DisplayImageOptions avatarGroupTL = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.loading)
            .showImageOnFail(R.drawable.loading)
            .cacheOnDisk(true)
            .cacheInMemory(true)
            .imageScaleType(ImageScaleType.NONE_SAFE)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .displayer(new RoundedOneCorner(90, Constant.TYPE_ROUNDED_TOP_LEFT))
            .considerExifParams(false)
            .build();

    // 그룹 프로필 사진 이미지(상단 오른쪽) 4인이상의 경우
    DisplayImageOptions avatarGroupTR = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.loading)
            .showImageOnFail(R.drawable.loading)
            .cacheOnDisk(true).cacheInMemory(true)
            .imageScaleType(ImageScaleType.NONE_SAFE)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .displayer(new RoundedOneCorner(90, Constant.TYPE_ROUNDED_TOP_RIGHT))
            .considerExifParams(false)
            .build();

    DisplayImageOptions avatarGroupLeftSide = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.loading)
            .showImageOnFail(R.drawable.loading)
            .cacheOnDisk(true).cacheInMemory(true)
            .imageScaleType(ImageScaleType.NONE_SAFE)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .displayer(new RoundedOneCorner(90, Constant.TYPE_ROUNDED_LEFT_SIDE))
            .considerExifParams(false)
            .build();

    DisplayImageOptions avatarGroupTOP = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.loading)
            .showImageOnFail(R.drawable.loading)
            .cacheOnDisk(true).cacheInMemory(true)
            .imageScaleType(ImageScaleType.NONE_SAFE)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .displayer(new RoundedOneCorner(90, Constant.TYPE_ROUNDED_TOP))
            .considerExifParams(false)
            .build();

    /***/
    DisplayImageOptions options2 = new DisplayImageOptions.Builder()
            .cacheOnDisk(true)
            .cacheInMemory(true)
            .showImageForEmptyUri(R.drawable.avatar_l)
            .showImageOnLoading(R.drawable.avatar_l)
            .showImageOnFail(R.drawable.avatar_l)
            .imageScaleType(ImageScaleType.NONE_SAFE)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .considerExifParams(false)
            .displayer(new RoundedBitmapDisplayer(90))
            .build();

    DisplayImageOptions options = new DisplayImageOptions.Builder()
            .cacheOnDisk(true).cacheInMemory(true)
            .imageScaleType(ImageScaleType.NONE_SAFE)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .considerExifParams(false)
            .displayer(new FadeInBitmapDisplayer(0))
            .considerExifParams(true)
            .decodingOptions(decodingOptions)
            .postProcessor(new BitmapProcessor() {
                @Override
                public Bitmap process(Bitmap bmp) {
                    // return Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getWidth(), false);
                    int width = bmp.getWidth();
                    int height = bmp.getHeight();
                    if (width > 1000 || height > 1000) {
                        return Bitmap.createScaledBitmap(bmp, width / 3, height / 3, false);
                    }
                    return bmp;
                }
            })
            .build();

    DisplayImageOptions optionsNoCache = new DisplayImageOptions.Builder()
            .cacheOnDisk(false)
            .cacheInMemory(true)
            .imageScaleType(ImageScaleType.NONE_SAFE)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .considerExifParams(false)
            .displayer(new FadeInBitmapDisplayer(0))
            .considerExifParams(true)
            .decodingOptions(decodingOptions)
            .build();

    DisplayImageOptions optionsViewAttach = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.loading)
            .showImageOnFail(R.drawable.loading)
            .cacheOnDisk(true).cacheInMemory(true)
            .imageScaleType(ImageScaleType.NONE_SAFE)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .considerExifParams(true)
            .displayer(new FadeInBitmapDisplayer(0))
            .considerExifParams(true)
            .showImageOnLoading(R.drawable.loading)
            .decodingOptions(decodingOptions)
            .build();

    //file type
    String IMAGE_JPG = ".jpg";
    String IMAGE_JPEG = ".jpeg";
    String IMAGE_PNG = ".png";
    String IMAGE_GIF = ".gif";
    String AUDIO_MP3 = ".mp3";
    String AUDIO_M4A = ".m4a";
    String AUDIO_WMA = ".wma";
    String AUDIO_AMR = ".amr";
    String VIDEO_MP4 = ".mp4";
    String VIDEO_MOV = ".mov";
    String FILE_PDF = ".pdf";
    String FILE_DOCX = ".docx";
    String FILE_DOC = ".doc";
    String FILE_XLS = ".xls";
    String FILE_XLSX = ".xlsx";
    String FILE_PPTX = ".pptx";
    String FILE_PPT = ".ppt";
    String FILE_ZIP = ".zip";
    String FILE_RAR = ".rar";
    String FILE_APK = ".apk";
    String MIME_TYPE_AUDIO = "audio/*";
    /*    public static final String MIME_TYPE_TEXT = "application/vnd.google-apps.file";*/
    String MIME_TYPE_IMAGE = "image/*";
    String MIME_TYPE_VIDEO = "video/*";
    String MIME_TYPE_APP = "file/*";
    String MIME_TYPE_TEXT = "text/*";
    String MIME_TYPE_ALL = "*/*";
    String MIME_TYPE_PDF = "application/pdf";
    String MIME_TYPE_ZIP = "application/zip";
    String MIME_TYPE_RAR = "application/x-rar-compressed";
    String MIME_TYPE_DOC = "application/doc";
    String MIME_TYPE_XLSX = "application/xls";
    String MIME_TYPE_PPTX = "application/ppt";
    String MIME_TYPE_APK = "application/vnd.android.package-archive";

    String ORANGE = "orange";

    String chat_jw_group_co_kr = "chat.jw-group.co.kr";
    int chat_jw_group_co_kr_limit_image = 5;

    String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    //    String AUDIO_RECORDER_FILE_EXT_MP3 = ".mp3";
    String AUDIO_RECORDER_FILE_EXT_MP3 = ".m4a";
    String AUDIO_RECORDER_FOLDER = "CrewChat/Audio";
    String AUDIO_RECORDER_FOLDER_ROOT = "CrewChat";
    int output_formats[] = {MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP};
    //    int output_formats[] = {MediaRecorder.OutputFormat.MPEG_4};
    String file_exts[] = {AUDIO_RECORDER_FILE_EXT_MP3, AUDIO_RECORDER_FILE_EXT_3GP};
    String CHOOSE_OPTION_IMAGE = "choose_option_image";
    //choose option image
    int STANDARD = 0;
    int HIGH = 1;
    int ORIGINAL = 2;
}
