package com.dazone.crewchatoff.HTTPs;

import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.activity.MainActivity;
import com.dazone.crewchatoff.constant.Constants;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.*;
import com.dazone.crewchatoff.interfaces.*;
import com.dazone.crewchatoff.utils.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HttpRequest {
    public static String TAG = ">>>HttpRequest";
    private static HttpRequest mInstance;
    private static String root_link;
    private static Prefs prefs;

    public static HttpRequest getInstance() {
        if (null == mInstance) {
            mInstance = new HttpRequest();
        }
        root_link = CrewChatApplication.getInstance().getPrefs().getServerSite();
        prefs = CrewChatApplication.getInstance().getPrefs();
        return mInstance;
    }


    public class ExportUserList extends AsyncTask<String, String, ArrayList<TreeUserDTOTemp>> {
        String response;
        IGetListOrganization callBack;

        public ExportUserList(String response, IGetListOrganization callBack) {
            this.response = response;
            this.callBack = callBack;
        }

        @Override
        protected ArrayList<TreeUserDTOTemp> doInBackground(String... params) {
            Type listType = new TypeToken<ArrayList<TreeUserDTOTemp>>() {
            }.getType();
            ArrayList<TreeUserDTOTemp> list = new Gson().fromJson(response, listType);
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<TreeUserDTOTemp> list) {
            super.onPostExecute(list);
            if (callBack != null) callBack.onGetListSuccess(list);
        }
    }

    public void GetListOrganize(final IGetListOrganization iGetListOrganization) {
        String url = root_link + Urls.URL_GET_ALL_USER_BE_LONGS;
        Map<String, String> params = new HashMap<>();
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(final String response) {
                new ExportUserList(response, iGetListOrganization).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (iGetListOrganization != null)
                    iGetListOrganization.onGetListFail(error);
            }
        });
    }

    public void GetListOrganize_Mod(String moddate, final IGetListOrganization iGetListOrganization) {
        String url = root_link + Urls.URL_GET_ALL_USER_BE_LONGS_MOD;

        Map<String, String> params = new HashMap<>();
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        params.put("moddate", moddate);
        WebServiceManager webServiceManager = new WebServiceManager();

        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(final String response) {
                new ExportUserList(response, iGetListOrganization).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (iGetListOrganization != null)
                    iGetListOrganization.onGetListFail(error);
            }
        });
    }

    public class ExportDepartmentList extends AsyncTask<String, String, ArrayList<TreeUserDTO>> {
        String response;
        IGetListDepart callBack;

        public ExportDepartmentList(String response, IGetListDepart callBack) {
            this.response = response;
            this.callBack = callBack;
        }

        @Override
        protected ArrayList<TreeUserDTO> doInBackground(String... params) {
            Type listType = new TypeToken<List<TreeUserDTO>>() {
            }.getType();
            ArrayList<TreeUserDTO> list = new Gson().fromJson(response, listType);
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<TreeUserDTO> list) {
            super.onPostExecute(list);
            if (callBack != null)
                callBack.onGetListDepartSuccess(list);
        }
    }

    public void GetListDepart_Mod(final String moddate, final IGetListDepart iGetListDepart) {
        String url = root_link + Urls.URL_GET_DEPARTMENT_MOD;
        Map<String, String> params = new HashMap<>();
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        params.put("moddate", moddate);

        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(final String response) {
                new ExportDepartmentList(response, iGetListDepart).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            @Override
            public void onFailure(ErrorDto error) {
                Log.d(TAG, "getListDepartment_Mod");
                if (iGetListDepart != null)
                    iGetListDepart.onGetListDepartFail(error);
            }
        });
    }

    public void GetListDepart(final IGetListDepart iGetListDepart) {
        String url = root_link + Urls.URL_GET_DEPARTMENT;
        Map<String, String> params = new HashMap<>();
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(final String response) {
                new ExportDepartmentList(response, iGetListDepart).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (iGetListDepart != null)
                    iGetListDepart.onGetListDepartFail(error);
            }
        });
    }

    public void CreateOneUserChatRoom(int UserNo, final ICreateOneUserChatRom iCreateOneUserChatRom) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();
        Map<String, Object> params2 = new HashMap<>();

        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        params2.put("joinNo", UserNo);
        Gson gson = new Gson();
        String js = gson.toJson(params2);

        if (UserNo != Utils.getCurrentId()) {
            params.put("command", "" + Urls.URL_CREATE_ONE_USER_CHAT);
            params.put("reqJson", js);
        } else {
            params.put("command", "" + Urls.URL_CREATE_MY_CHAT_ROOM);
            params.put("reqJson", "");
        }

        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                ChattingDto chattingDto = new Gson().fromJson(response, ChattingDto.class);
                if (iCreateOneUserChatRom != null)
                    iCreateOneUserChatRom.onICreateOneUserChatRomSuccess(chattingDto);
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (iCreateOneUserChatRom != null)
                    iCreateOneUserChatRom.onICreateOneUserChatRomFail(error);
            }
        });
    }

    class RestoreUser {
        public RestoreUser(int roomNo, List<Integer> userNos) {
            this.roomNo = roomNo;
            this.userNos = userNos;
        }

        int roomNo;
        List<Integer> userNos;

        public int getRoomNo() {
            return roomNo;
        }

        public void setRoomNo(int roomNo) {
            this.roomNo = roomNo;
        }

        public List<Integer> getUserNos() {
            return userNos;
        }

        public void setUserNos(List<Integer> userNos) {
            this.userNos = userNos;
        }
    }

    class ForwardMsg {
        long messageNo;
        String userNos;

        public ForwardMsg() {
        }

        public ForwardMsg(long messageNo, String userNos) {
            this.messageNo = messageNo;
            this.userNos = userNos;
        }

        public long getMessageNo() {
            return messageNo;
        }

        public void setMessageNo(long messageNo) {
            this.messageNo = messageNo;
        }

        public String getUserNos() {
            return userNos;
        }

        public void setUserNos(String userNos) {
            this.userNos = userNos;
        }
    }

    class ForwardMsgRoom {
        long messageNo;
        String roomNos;

        public ForwardMsgRoom() {
        }

        public ForwardMsgRoom(long messageNo, String roomNos) {
            this.messageNo = messageNo;
            this.roomNos = roomNos;
        }

        public String getRoomNos() {
            return roomNos;
        }

        public void setRoomNos(String roomNos) {
            this.roomNos = roomNos;
        }

        public long getMessageNo() {
            return messageNo;
        }

        public void setMessageNo(long messageNo) {
            this.messageNo = messageNo;
        }
    }

    public void ForwardChatMsgChatRoom(long messageNo, List<String> userNos, final IF_Relay callback) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();

        params.put("command", "" + Urls.URL_FORWARD_CHAT_MSG_ROOM);
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        String s = "";
        for (int i = 0; i < userNos.size(); i++) {
            if (i == userNos.size() - 1) {
                s = s + userNos.get(i).trim();
            } else {
                s = s + userNos.get(i).trim() + ",";
            }
        }

        ForwardMsgRoom forwardMsg = new ForwardMsgRoom(messageNo, s);
        params.put("reqJson", new Gson().toJson(forwardMsg));

        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                callback.onSuccess();
            }

            @Override
            public void onFailure(ErrorDto error) {
                callback.onFail();
            }
        });
    }

    public void ForwardChatMsgUser(long messageNo, List<String> userNos, final IF_Relay callback) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();

        params.put("command", "" + Urls.URL_FORWARD_CHAT_MSG_USER);
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        String s = "";
        for (int i = 0; i < userNos.size(); i++) {
            if (i == userNos.size() - 1) {
                s = s + userNos.get(i).trim();
            } else {
                s = s + userNos.get(i).trim() + ",";
            }
        }

        ForwardMsg forwardMsg = new ForwardMsg(messageNo, s);
        params.put("reqJson", new Gson().toJson(forwardMsg));
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                callback.onSuccess();
            }

            @Override
            public void onFailure(ErrorDto error) {
                callback.onFail();
            }
        });
    }

    public void getAttachFileList(final GetIvFileBox callback) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();

        params.put("command", "" + Urls.URL_GET_ATTACH_FILE_LIST);
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("reqJson", "");

        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                List<AttachImageList> lst = null;
                Type listType = new TypeToken<ArrayList<AttachImageList>>() {
                }.getType();
                lst = new Gson().fromJson(response, listType);
                if (lst == null) {
                    lst = new ArrayList<>();
                }

                callback.onSuccess(lst);
            }

            @Override
            public void onFailure(ErrorDto error) {
                callback.onFail();
            }
        });
    }

    public void UserRestore(List<Integer> userNos, int RoomNo, final IF_RestoreUser callback) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();

        params.put("command", "" + Urls.URL_ADD_CHAT_ROOM_USER_RESTORE);
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());

        RestoreUser obj = new RestoreUser(RoomNo, userNos);
        params.put("reqJson", new Gson().toJson(obj));

        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                callback.onSuccess();
            }

            @Override
            public void onFailure(ErrorDto error) {
                Log.d(TAG, "UserRestore ErrorDto:");
            }
        });
    }

    public void CreateGroupChatRoom(List<Integer> userNos, final ICreateOneUserChatRom iCreateOneUserChatRom) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();
        Map<String, Object> params2 = new HashMap<>();
        params.put("command", "" + Urls.URL_CREATE_GROUP_USER_CHAT);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        String user = "";
        userNos.toString();
        for (int i : userNos) {
            user += i + ",";
        }
        params2.put("userNos", user.substring(0, user.length() - 1));
        params2.put("roomTitle", "");
        params2.put("roomGroupType", 0);
        Gson gson = new Gson();
        String js = gson.toJson(params2);
        params.put("reqJson", js);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                ChattingDto chattingDto = new Gson().fromJson(response, ChattingDto.class);
                if (iCreateOneUserChatRom != null)
                    iCreateOneUserChatRom.onICreateOneUserChatRomSuccess(chattingDto);
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (iCreateOneUserChatRom != null)
                    iCreateOneUserChatRom.onICreateOneUserChatRomFail(error);
            }
        });
    }

    public void CreateGroupChatRoom(ArrayList<TreeUserDTO> list, final ICreateOneUserChatRom iCreateOneUserChatRom, String roomTitle) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();
        Map<String, Object> params2 = new HashMap<>();
        params.put("command", "" + Urls.URL_CREATE_GROUP_USER_CHAT);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        String user = "";
        for (TreeUserDTO treeUserDTO : list) {
            if (treeUserDTO.getType() == 2)
                user += treeUserDTO.getId() + ",";
        }
        params2.put("userNos", user.substring(0, user.length() - 1));
        params2.put("roomTitle", roomTitle);
        params2.put("roomGroupType", 0);
        Gson gson = new Gson();
        String js = gson.toJson(params2);
        params.put("reqJson", js);

        Log.d(TAG, "CreateGroupChatRoomNew:" + new Gson().toJson(params));
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                ChattingDto chattingDto = new Gson().fromJson(response, ChattingDto.class);
                if (iCreateOneUserChatRom != null)
                    iCreateOneUserChatRom.onICreateOneUserChatRomSuccess(chattingDto);
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (iCreateOneUserChatRom != null)
                    iCreateOneUserChatRom.onICreateOneUserChatRomFail(error);
            }
        });
    }

    class CreateRoomTitle {
        String userNos;
        String roomTitle;
        int roomGroupType;

        public CreateRoomTitle(String userNos, String roomTitle, int roomGroupType) {
            this.userNos = userNos;
            this.roomTitle = roomTitle;
            this.roomGroupType = roomGroupType;
        }

        public String getUserNos() {
            return userNos;
        }

        public void setUserNos(String userNos) {
            this.userNos = userNos;
        }

        public String getRoomTitle() {
            return roomTitle;
        }

        public void setRoomTitle(String roomTitle) {
            this.roomTitle = roomTitle;
        }

        public int getRoomGroupType() {
            return roomGroupType;
        }

        public void setRoomGroupType(int roomGroupType) {
            this.roomGroupType = roomGroupType;
        }
    }

    public void CreateGroupChatRoomWithRoomTitle(ArrayList<TreeUserDTO> list, final ICreateOneUserChatRom iCreateOneUserChatRom, String titleRoom, int type) {
        // type = 1: when check create new room else type = 0
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();
        params.put("command", "" + Urls.URL_CREATE_GROUP_USER_CHAT);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        String user = "";
        for (TreeUserDTO treeUserDTO : list) {
            if (treeUserDTO.getType() == 2)
                user += treeUserDTO.getId() + ",";
        }

        String lstUser = user.substring(0, user.length() - 1);
        CreateRoomTitle obj = new CreateRoomTitle(lstUser, titleRoom, type);
        String js = new Gson().toJson(obj);
        params.put("reqJson", js);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                ChattingDto chattingDto = new Gson().fromJson(response, ChattingDto.class);
                if (iCreateOneUserChatRom != null)
                    iCreateOneUserChatRom.onICreateOneUserChatRomSuccess(chattingDto);
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (iCreateOneUserChatRom != null)
                    iCreateOneUserChatRom.onICreateOneUserChatRomFail(error);
            }
        });
    }

    public void SendChatMsg(long RoomNo, String message, final SendChatMessage sendChatMessage) {
        final String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();
        Map<String, Object> params2 = new HashMap<>();
        params.put("command", "" + Urls.URL_SEND_CHAT_TIME);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        params2.put("roomNo", RoomNo);
        params2.put("message", message);
        params2.put("regDate", CrewChatApplication.getInstance().getTimeServer());
        Gson gson = new Gson();
        String js = gson.toJson(params2);
        params.put("reqJson", js);
        WebServiceManager webServiceManager = new WebServiceManager();

        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                ChattingDto chattingDto = new Gson().fromJson(response, ChattingDto.class);
                if (sendChatMessage != null)
                    sendChatMessage.onSendChatMessageSuccess(chattingDto);
            }

            @Override
            public void onFailure(ErrorDto error) {
                Log.d(TAG, "SendChatMsg error");
                if (sendChatMessage != null)
                    sendChatMessage.onSendChatMessageFail(error, url);
            }
        });
    }

    public void getAllUserInfo(final OnGetUserInfo callback) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();
        Map<Object, Object> params2 = new HashMap<>();
        params.put("command", "" + Urls.URL_GET_USERS_STATUS);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        Gson gson = new Gson();
        String js = gson.toJson(params2);
        params.put("reqJson", js);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                Type listType = new TypeToken<ArrayList<UserInfoDto>>() {
                }.getType();
                ArrayList<UserInfoDto> list = new Gson().fromJson(response, listType);
                callback.OnSuccess(list);
            }

            @Override
            public void onFailure(ErrorDto error) {
                callback.OnFail(error);
            }
        });
    }

    public void GetUser(int userNo, final OnGetUserCallBack callBack) {
        String url = root_link + Urls.URL_GET_USER;
        Map<String, String> params = new HashMap<>();
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        params.put("userNo", userNo + "");
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                ProfileUserDTO profileUserDTO = new Gson().fromJson(response, ProfileUserDTO.class);
                if (callBack != null)
                    callBack.onHTTPSuccess(profileUserDTO);
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (callBack != null)
                    callBack.onHTTPFail(error);
            }
        });
    }

    public void GetChatRoom(long roomNo, final OnGetChatRoom callBack) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();
        Map<String, Object> params2 = new HashMap<>();
        params.put("command", "" + Urls.URL_GET_CHAT_ROOM);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        params2.put("roomNo", roomNo);
        Gson gson = new Gson();
        String js = gson.toJson(params2);
        params.put("reqJson", js);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                Type listType = new TypeToken<ChatRoomDTO>() {
                }.getType();
                ChatRoomDTO chatRoomDTO = new Gson().fromJson(response, listType);
                if (callBack != null) {
                    callBack.OnGetChatRoomSuccess(chatRoomDTO);
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (callBack != null) {
                    callBack.OnGetChatRoomFail(error);
                }
            }
        });
    }

    public void GetChatList(final OnGetChatList onGetChatList) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();
        params.put("command", "" + Urls.URL_GET_CHAT_LIST);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        params.put("reqJson", "");
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                new Prefs().putStringValue(Statics.KEY_DATA_CURRENT_CHAT_LIST, response);
                Type listType = new TypeToken<List<ChattingDto>>() {
                }.getType();
                try {
                    List<ChattingDto> list = new Gson().fromJson(response, listType);
                    if (list != null) {
                        if (list.size() > 0) {
                            for (ChattingDto dto : list) {
                                if (dto.getRoomType() == 1) {
                                    MainActivity.myRoom = dto.getRoomNo();
                                    break;
                                }
                            }
                        }
                    }

                    if (onGetChatList != null)
                        onGetChatList.OnGetChatListSuccess(list);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (onGetChatList != null)
                    onGetChatList.OnGetChatListFail(error);
            }
        });
    }

    public void setNotification(String command, String deviceId, Map<String, Object> notificationParams, final OnSetNotification callback) {
        String url = root_link + Urls.URL_ROOT_2;

        Map<String, Object> params = new HashMap<>();
        Map<String, Object> jsonParam = new HashMap<>();
        jsonParam.put("DeviceType", Statics.DEVICE_TYPE);
        jsonParam.put("DeviceID", deviceId);

        Gson gson = new Gson();
        jsonParam.put("NotifcationOptions", notificationParams);

        params.put("command", command);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());

        //Gson gson = new Gson();
        String js = gson.toJson(jsonParam);
        params.put("reqJson", js);

        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                callback.OnSuccess();
            }

            @Override
            public void onFailure(ErrorDto error) {
                callback.OnFail(error);
            }
        });
    }

    public void DeleteChatRoomUser(long RoomNo, long UserNo, final BaseHTTPCallBack baseHTTPCallBack) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();
        Map<String, Object> params2 = new HashMap<>();
        params.put("command", "" + Urls.URL_DELETE_LIST);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        params2.put("roomNo", RoomNo);
        params2.put("userNo", UserNo);
        Gson gson = new Gson();
        String js = gson.toJson(params2);
        params.put("reqJson", js);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                if (baseHTTPCallBack != null)
                    baseHTTPCallBack.onHTTPSuccess();
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (baseHTTPCallBack != null)
                    baseHTTPCallBack.onHTTPFail(error);
            }
        });
    }

    public void insertFavoriteUser(long groupNo, long UserNo, final BaseHTTPCallbackWithJson baseHTTPCallBack) {
        String url = root_link + Urls.URL_ROOT_2;

        Map<String, String> params = new HashMap<>();
        Map<String, Object> params2 = new HashMap<>();

        params.put("command", "" + Urls.URL_INSERT_FAVORITE);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());

        params2.put("groupNo", groupNo);
        params2.put("userNo", UserNo);

        Gson gson = new Gson();
        String js = gson.toJson(params2);

        params.put("reqJson", js);

        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                if (baseHTTPCallBack != null)
                    baseHTTPCallBack.onHTTPSuccess(response);
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (baseHTTPCallBack != null)
                    baseHTTPCallBack.onHTTPFail(error);
            }
        });
    }

    public void deleteFavoriteUser(long groupNo, long UserNo, final BaseHTTPCallbackWithJson baseHTTPCallBack) {
        String url = root_link + Urls.URL_ROOT_2;

        Map<String, String> params = new HashMap<>();
        Map<String, Object> params2 = new HashMap<>();

        params.put("command", "" + Urls.URL_DELETE_FAVORITE);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());

        params2.put("groupNo", groupNo);
        params2.put("userNo", UserNo);

        Gson gson = new Gson();
        String js = gson.toJson(params2);

        params.put("reqJson", js);

        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                if (baseHTTPCallBack != null)
                    baseHTTPCallBack.onHTTPSuccess(response);
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (baseHTTPCallBack != null)
                    baseHTTPCallBack.onHTTPFail(error);
            }
        });
    }

    public void getFavotiteGroupAndData(final BaseHTTPCallbackWithJson baseHTTPCallBack) {

        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();

        params.put("command", "" + Urls.URL_GET_FAVORITE_GROUP_AND_DATA);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());

        params.put("reqJson", "");
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                if (baseHTTPCallBack != null)
                    baseHTTPCallBack.onHTTPSuccess(response);
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (baseHTTPCallBack != null)
                    baseHTTPCallBack.onHTTPFail(error);
            }
        });
    }

    public void getTopFavotiteGroupAndData(final BaseHTTPCallbackWithJson baseHTTPCallBack) {
        String url = root_link + Urls.URL_ROOT_2;

        Map<String, String> params = new HashMap<>();

        params.put("command", "" + Urls.URL_GET_TOP_FAVORITE_GROUP_AND_DATA);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        params.put("reqJson", "");

        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                if (baseHTTPCallBack != null)
                    baseHTTPCallBack.onHTTPSuccess(response);
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (baseHTTPCallBack != null)
                    baseHTTPCallBack.onHTTPFail(error);
            }
        });
    }

    public void insertFavoriteGroup(String groupName, final BaseHTTPCallbackWithJson baseHTTPCallBack) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();
        Map<String, Object> params2 = new HashMap<>();
        params.put("command", "" + Urls.URL_INSERT_FAVORITE_GROUP);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());

        params2.put("groupName", groupName);

        Gson gson = new Gson();
        String js = gson.toJson(params2);
        params.put("reqJson", js);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPSuccess(response);
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPFail(error);
                }
            }
        });
    }

    public void updateFavoriteGroup(long groupNo, String groupName, int sortNo, final BaseHTTPCallbackWithJson baseHTTPCallBack) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();
        Map<String, Object> params2 = new HashMap<>();
        params.put("command", "" + Urls.URL_UPDATE_FAVORITE_GROUP);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());

        params2.put("groupNo", groupNo);
        params2.put("groupName", groupName);
        params2.put("sortNo", sortNo);

        Gson gson = new Gson();
        String js = gson.toJson(params2);
        params.put("reqJson", js);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPSuccess(response);
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPFail(error);
                }
            }
        });
    }

    public void deleteFavoriteGroup(long groupNo, final BaseHTTPCallBack baseHTTPCallBack) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();
        Map<String, Object> params2 = new HashMap<>();
        params.put("command", "" + Urls.URL_DELETE_FAVORITE_GROUP);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());

        params2.put("groupNo", groupNo);

        Gson gson = new Gson();
        String js = gson.toJson(params2);
        params.put("reqJson", js);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPSuccess();
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPFail(error);
                }
            }
        });
    }

    public void updateChatRoomNotification(long roomNo, boolean notification, final BaseHTTPCallBack baseHTTPCallBack) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();
        Map<String, Object> params2 = new HashMap<>();
        params.put("command", "" + Urls.URL_UPDATE_CHAT_ROOM_NOTIFICATION);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());

        params2.put("roomNo", roomNo);
        params2.put("notification", notification);

        Gson gson = new Gson();
        String js = gson.toJson(params2);
        params.put("reqJson", js);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPSuccess();
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPFail(error);
                }
            }
        });
    }

    public void signUp(final BaseHTTPCallBackWithString baseHTTPCallBack, final String email) {
        final String url = "http://www.crewcloud.net" + Urls.URL_SIGN_UP;
        Map<String, String> params = new HashMap<>();
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("mailAddress", "" + email);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                Gson gson = new Gson();
                MessageDto messageDto = gson.fromJson(response, MessageDto.class);

                if (baseHTTPCallBack != null && messageDto != null) {
                    String message = messageDto.getMessage();
                    baseHTTPCallBack.onHTTPSuccess(message);
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPFail(error);
                }
            }
        });
    }

    public void InsertDevice(String deviceId, final BaseHTTPCallBack baseHTTPCallBack) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();
        Map<String, Object> params2 = new HashMap<>();
        params.put("command", "" + Urls.URL_INSERT_DEVICE);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());

        params2.put("DeviceType", Statics.DEVICE_TYPE);
        params2.put("DeviceID", deviceId);

        boolean isEnableN = prefs.getBooleanValue(Statics.ENABLE_NOTIFICATION, true);
        boolean isEnableSound = prefs.getBooleanValue(Statics.ENABLE_SOUND, true);
        boolean isEnableVibrate = prefs.getBooleanValue(Statics.ENABLE_VIBRATE, true);
        boolean isEnableTime = prefs.getBooleanValue(Statics.ENABLE_TIME, false);

        boolean isEnableNotificationWhenUsingPcVersion = prefs.getBooleanValue(Statics.ENABLE_NOTIFICATION_WHEN_USING_PC_VERSION, true);

        int start_hour = prefs.getIntValue(Statics.START_NOTIFICATION_HOUR, Statics.DEFAULT_START_NOTIFICATION_TIME);
        int start_minutes = prefs.getIntValue(Statics.START_NOTIFICATION_MINUTES, 0);
        int end_hour = prefs.getIntValue(Statics.END_NOTIFICATION_HOUR, Statics.DEFAULT_END_NOTIFICATION_TIME);
        int end_minutes = prefs.getIntValue(Statics.END_NOTIFICATION_MINUTES, 0);

        Map<String, Object> notificationParams = new HashMap<>();
        notificationParams.put("enabled", isEnableN);
        notificationParams.put("sound", isEnableSound);
        notificationParams.put("vibrate", isEnableVibrate);
        notificationParams.put("notitime", isEnableTime);
        notificationParams.put("starttime", TimeUtils.timeToStringNotAMPM(start_hour, start_minutes));
        notificationParams.put("endtime", TimeUtils.timeToStringNotAMPM(end_hour, end_minutes));
        notificationParams.put("confirmonline", isEnableNotificationWhenUsingPcVersion);

        Gson gson = new Gson();
        params2.put("NotifcationOptions", notificationParams);
        String js = gson.toJson(params2);
        params.put("reqJson", js);

        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPSuccess();
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPFail(error);
                }
            }
        });
    }

    public void DeleteDevice(String deviceId, final BaseHTTPCallBack baseHTTPCallBack) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();
        Map<String, Object> params2 = new HashMap<>();
        params.put("command", "" + Urls.URL_DELETE_DEVICE);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        params2.put("DeviceType", "Android");
        params2.put("DeviceID", deviceId);
        Gson gson = new Gson();
        String js = gson.toJson(params2);
        params.put("reqJson", js);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPSuccess();
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPFail(error);
                }
            }
        });
    }

    public void updateChatRoomInfo(int roomNo, String roomTitle, final BaseHTTPCallBack baseHTTPCallBack) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> params2 = new HashMap<>();
        params.put("command", "" + Urls.URL_UPDATE_ROOM_NO);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());

        params2.put("roomNo", roomNo);
        params2.put("roomTitle", roomTitle);

        Gson gson = new Gson();
        String js = gson.toJson(params2);

        params.put("reqJson", js);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPSuccess();
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPFail(error);
                }
            }
        });
    }

    public void addRoomToFavorite(long roomNo, final BaseHTTPCallBack baseHTTPCallBack) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> params2 = new HashMap<>();
        params.put("command", "" + Urls.URL_INSERT_FAVORITE_CHAT_ROOM);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());

        params2.put("roomNo", roomNo);

        Gson gson = new Gson();
        String js = gson.toJson(params2);

        params.put("reqJson", js);

        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPSuccess();
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPFail(error);
                }
            }
        });
    }

    public void removeFromFavorite(long roomNo, final BaseHTTPCallBack baseHTTPCallBack) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> params2 = new HashMap<>();
        params.put("command", "" + Urls.URL_DELETE_FAVORITE_CHAT_ROOM);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());

        params2.put("roomNo", roomNo);

        Gson gson = new Gson();
        String js = gson.toJson(params2);

        params.put("reqJson", js);

        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPSuccess();
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPFail(error);
                }
            }
        });
    }

    public void AddChatRoomUser(ArrayList<TreeUserDTO> list, long roomNo, final BaseHTTPCallBack baseHTTPCallBack) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();
        Map<String, Object> params2 = new HashMap<>();
        params.put("command", "" + Urls.URL_ADD_USER_CHAT);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());

        List<Integer> temp = new ArrayList<>();
        for (TreeUserDTO treeUserDTO : list) {
            temp.add(treeUserDTO.getId());
        }

        params2.put("userNos", temp);
        params2.put("roomNo", roomNo);
        Gson gson = new Gson();
        String js = gson.toJson(params2);
        params.put("reqJson", js);

        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPSuccess();
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPFail(error);
                }
            }
        });
    }

    public class UserUnreadClass {
        public long roomNo;
        public long messageNo;

        public UserUnreadClass(long roomNo, long messageNo) {
            this.roomNo = roomNo;
            this.messageNo = messageNo;
        }
    }

    public void GetCheckMessageUserList(long messageNo, long roomNo, final UnreadCallBack callBack) {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();

        params.put("command", "" + Urls.URL_GET_USER_UNRED);
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        UserUnreadClass userUnreadClass = new UserUnreadClass(roomNo, messageNo);
        params.put("reqJson", new Gson().toJson(userUnreadClass));

        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                ArrayList<UnreadDto> list = null;
                try {
                    Type listType = new TypeToken<ArrayList<UnreadDto>>() {
                    }.getType();
                    list = new Gson().fromJson(response, listType);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (list != null && list.size() > 0)
                    callBack.onSuccess(list);
                else callBack.onFail();
            }

            @Override
            public void onFailure(ErrorDto error) {
                callBack.onFail();
            }
        });
    }

    public void checkVersionUpdate(final BaseHTTPCallBackWithString baseHTTPCallBack) {
        final String url = Urls.URL_CHECK_UPDATE;
        Map<String, String> params = new HashMap<>();
        params.put("Domain", prefs.getStringValue(Constants.COMPANY_NAME, ""));
        params.put("MobileType", "Android");
        params.put("Applications", "CrewChat");

        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPSuccess(response);
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPFail(error);
                }
            }
        });
    }

    public void getServerTime() {
        String url = root_link + Urls.URL_ROOT_2;
        Map<String, String> params = new HashMap<>();
        params.put("sessionId", "" + CrewChatApplication.getInstance().getPrefs().getaccesstoken());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        params.put("command", "GetServerDate");
        params.put("reqJson", "");
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    String strDateServer = object.getString("StrServerDate");
                    String strDateLocal = object.getString("StrServerConvetLocalDate");

                    SimpleDateFormat formatter = new SimpleDateFormat(Statics.yyyy_MM_dd_HH_mm_ss_SSS, Locale.getDefault());

                    Date serverDate = formatter.parse(strDateServer);
                    Date localDate = formatter.parse(strDateLocal);

                    long calTime = localDate.getTime() - serverDate.getTime();
                    long localCalTime = System.currentTimeMillis() - localDate.getTime();

                    CrewChatApplication.getInstance().getPrefs().putLongValue(Statics.TIME_SERVER_MILI, calTime);
                    CrewChatApplication.getInstance().getPrefs().putLongValue(Statics.TIME_LOCAL_MILI, localCalTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(ErrorDto error) {
                Log.d("A", error.message);
            }
        });
    }

    public void checkSSL(final ICheckSSL checkSSL) {
        final String url = Urls.URL_CHECK_SSL;
        Map<String, String> params = new HashMap<>();
        params.put("Domain", CrewChatApplication.getInstance().getPrefs().getStringValue(Constants.COMPANY_NAME, ""));
        params.put("Applications", "CrewMail");

        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean hasSSL = jsonObject.getBoolean("SSL");
                    CrewChatApplication.getInstance().getPrefs().putBooleanValue(Constants.HAS_SSL, hasSSL);
                    checkSSL.hasSSL(hasSSL);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                checkSSL.checkSSLError(error);
            }
        });
    }
}
