package com.dazone.crewchatoff.interfaces;

public class Urls {
    public static final String URL_ROOT_2 = "/UI/CrewChat/MobileWebService.asmx/RequestData";
    public static final String URL_CREATE_ONE_USER_CHAT = "CreateOneUserChatRoom";
    public static final String URL_CREATE_MY_CHAT_ROOM =  "CreateMyChatRoom";
    public static final String URL_CREATE_GROUP_USER_CHAT = "CreateGroupChatRoomNew";
    public static final String URL_ADD_CHAT_ROOM_USER_RESTORE = "AddChatRoomUserRestore";
    public static final String URL_GET_ATTACH_FILE_LIST = "GetAttachFileList";
    public static final String URL_FORWARD_CHAT_MSG_USER = "ForwardChatMsgUser";
    public static final String URL_FORWARD_CHAT_MSG_ROOM = "ForwardChatMsgChatRoom";

    public static final String URL_SEND_CHAT_TIME = "SendChatMsg_Time";
    public static final String URL_SEND_ATTACH_FILE_TIME = "SendChatAttachFile_Time";
    public static final String URL_GET_CHAT_MSG_SECTION_TIME = "GetChatMsgSection_Time";
    public static final String URL_UPDATE_MESSAGE_UNREAD_COUNT_TIME = "UpdateMessageUnreadCount_Time";
    public static final String URL_GET_MESSAGE_UNREAD_COUNT_TIME = "GetMessageUnreadCount_Time";
    public static final String URL_CHECK_HAS_CALL_UNREAD_COUNT = "GetUseReadCount";

    public static final String URL_GET_CHAT_ROOM = "GetChatRoom";
    public static final String URL_GET_CHAT_LIST = "GetChatListData";
    public static final String URL_DELETE_LIST = "DeleteChatRoomUser";
    public static final String URL_GET_USERS_STATUS = "GetAllUserInfo";

    public static final String URL_GET_FAVORITE_GROUP_AND_DATA = "GetFavotiteGroupAndData";
    public static final String URL_GET_TOP_FAVORITE_GROUP_AND_DATA = "GetFavotiteTopGroupData";
    public static final String URL_INSERT_FAVORITE_GROUP = "InsertFavoriteGroup";
    public static final String URL_UPDATE_FAVORITE_GROUP = "UpdateFavoriteGroup";
    public static final String URL_DELETE_FAVORITE_GROUP = "DeleteFavoriteGroup";
    public static final String URL_UPDATE_CHAT_ROOM_NOTIFICATION = "UpdateChatRoomNotification";
    public static final String URL_INSERT_FAVORITE = "InsertFavoriteUser";
    public static final String URL_DELETE_FAVORITE = "DeleteFavoriteUser";
    public static final String URL_INSERT_DEVICE  = "InsertDevice";
    public static final String URL_DELETE_DEVICE  = "DeleteDevice";
    public static final String URL_UPDATE_ROOM_NO  = "UpdateChatRoomInfo";
    public static final String URL_INSERT_FAVORITE_CHAT_ROOM  = "InsertFavoriteChatRoom";
    public static final String URL_DELETE_FAVORITE_CHAT_ROOM  = "DeleteFavoriteChatRoom";
    public static final String URL_ADD_USER_CHAT = "AddChatRoomUser";
    public static final String URL_GET_USER_UNRED = "GetCheckMessageUserList";

    public static final String URL_GET_USER = "/UI/WebService/WebServiceCenter.asmx/GetUser";
    public static final String URL_GET_ALL_USER_BE_LONGS = "/UI/WebService/WebServiceCenter.asmx/GetAllUsersWithBelongs";
    public static final String URL_GET_ALL_USER_BE_LONGS_MOD = "/UI/WebService/WebServiceCenter.asmx/GetAllUsersWithBelongs_Mod";
    public static final String URL_GET_DEPARTMENT = "/UI/WebService/WebServiceCenter.asmx/GetDepartments";
    public static final String URL_GET_DEPARTMENT_MOD = "/UI/WebService/WebServiceCenter.asmx/GetDepartments_Mod";
    public static final String URL_DOWNLOAD = "/UI/CrewChat/MobileAttachDownload.aspx?";
    public static final String URL_DOWNLOAD_THUMBNAIL = "/UI/CrewChat/MobileThumbnailImage.aspx?";
    public static final String URL_SIGN_UP = "/UI/Center/MobileService.asmx/SendConfirmEmail";
    public static final int FILE_SERVER_PORT = 9999;
    public static final int DDS_SERVER_PORT = 1118;
    public static String HOST_STATUS = "122.41.175.77";
    public static final String URL_CHECK_UPDATE = "http://mobileupdate.crewcloud.net/WebServiceMobile.asmx/Mobile_Version";
    public static final String URL_CHECK_SSL = "http://mobileupdate.crewcloud.net/WebServiceMobile.asmx/SSL_Check";
}