package com.dazone.crewchatoff.dto;

import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.SimpleFormatter;

/**
 * Created by david on 12/24/15.
 */
public class ChattingDto extends DataDto implements DrawImageItem, Serializable, Cloneable {
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private UserDto user;

    @SerializedName("RoomNo")
    private long RoomNo;
    @SerializedName("MakeUserNo")
    private int MakeUserNo;
    @SerializedName("Moddate")
    private String Moddate;
    @SerializedName("IsOne")
    private boolean IsOne;
    @SerializedName("RoomTitle")
    private String RoomTitle;
    @SerializedName("LastedMsg")
    private String LastedMsg;
    @SerializedName("LastedMsgAttachType")
    private int LastedMsgAttachType;
    @SerializedName("LastedMsgType")
    private int LastedMsgType;
    @SerializedName("LastedMsgDate")
    private String LastedMsgDate;
    @SerializedName("UserNos")
    private ArrayList<Integer> UserNos;
    @SerializedName("WriterUser")
    private int WriterUser;
    @SerializedName("WriteUserNo")
    private int WriterUserNo;
    @SerializedName("MessageNo")
    private long MessageNo;
    @SerializedName("UserNo")
    private int UserNo;
    @SerializedName("MsgUserNo")
    private int MsgUserNo;
    @SerializedName("Message")
    private String Message;
    @SerializedName("Type")
    private int type = 0;
    private int mType = 0;
    @SerializedName("AttachNo")
    private int AttachNo;
    @SerializedName("RegDate")
    private String RegDate;
    @SerializedName("UnReadCount")
    private int UnReadCount;
    @SerializedName("AttachInfo")
    private AttachDTO AttachInfo;
    private List<UserDto> list = new ArrayList<>();
    private List<TreeUserDTOTemp> listTreeUser = new ArrayList<>();
    private boolean isCheckFromServer = false;
    @SerializedName("AttachFileName")
    private String AttachFileName;
    @SerializedName("AttachFileType")
    private int AttachFileType;
    @SerializedName("AttachFilePath")
    private String AttachFilePath;
    @SerializedName("AttachFileSize")
    private int AttachFileSize;
    @SerializedName("UnreadTotalCount")
    private int UnreadTotalCount;
    private int RoomType;
    public boolean isSendding = false;

    @SerializedName("Notification")
    private boolean Notification = true;

    @SerializedName("Favorite")
    private boolean Favorite = false;

    @SerializedName("strRegDate")
    private String strRegDate;

    private boolean hasSent = true;
    private boolean cbChoose = false;
    private int status = 0;
    private int positionUploadImage;
    private int temp = 0;
    private boolean isHeader;
    private boolean isSendTemp = false;

    public String getStrRegDate() {
        return strRegDate;
    }

    public void setStrRegDate(String strRegDate) {
        this.strRegDate = strRegDate;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getPositionUploadImage() {
        return positionUploadImage;
    }

    public void setPositionUploadImage(int positionUploadImage) {
        this.positionUploadImage = positionUploadImage;
    }


    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    @Override
    public String toString() {
        return "ChattingDto{" +
                ", user=" + user +
                ", RoomNo=" + RoomNo +
                ", MakeUserNo=" + MakeUserNo +
                ", Moddate='" + Moddate + '\'' +
                ", IsOne=" + IsOne +
                ", RoomTitle='" + RoomTitle + '\'' +
                ", LastedMsg='" + LastedMsg + '\'' +
                ", LastedMsgAttachType=" + LastedMsgAttachType +
                ", LastedMsgType=" + LastedMsgType +
                ", LastedMsgDate='" + LastedMsgDate + '\'' +
                ", UserNos=" + UserNos +
                ", WriterUser=" + WriterUser +
                ", WriterUserNo=" + WriterUserNo +
                ", MessageNo=" + MessageNo +
                ", UserNo=" + UserNo +
                ", MsgUserNo=" + MsgUserNo +
                ", Message='" + Message + '\'' +
                ", type=" + type +
                ", mType=" + mType +
                ", AttachNo=" + AttachNo +
                ", RegDate='" + RegDate + '\'' +
                ", StrRegDate='" + strRegDate + '\'' +
                ", UnReadCount=" + UnReadCount +
                ", AttachInfo=" + AttachInfo +
                ", list=" + list +
                ", listTreeUser=" + listTreeUser +
                ", isCheckFromServer=" + isCheckFromServer +
                ", AttachFileName='" + AttachFileName + '\'' +
                ", AttachFileType=" + AttachFileType +
                ", AttachFilePath='" + AttachFilePath + '\'' +
                ", AttachFileSize=" + AttachFileSize +
                ", UnreadTotalCount=" + UnreadTotalCount +
                ", Notification=" + Notification +
                ", Favorite=" + Favorite +
                '}';
    }

    public boolean isSendTemp() {
        return isSendTemp;
    }

    public void setSendTemp(boolean sendTemp) {
        isSendTemp = sendTemp;
    }

    public int getRoomType() {
        return RoomType;
    }

    public void setRoomType(int roomType) {
        RoomType = roomType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isCbChoose() {
        return cbChoose;
    }

    public void setCbChoose(boolean cbChoose) {
        this.cbChoose = cbChoose;
    }

    public int getUnreadTotalCount() {
        return UnreadTotalCount;
    }

    public void setUnreadTotalCount(int unreadTotalCount) {
        UnreadTotalCount = unreadTotalCount;
    }

    public String getAvatar() {
        return user != null ? user.avatar : "";
    }

    public String getName() {

        return user != null ? user.FullName : "";
    }

    public int getmType() {
        return mType;
    }

    public void setmType(int mType) {
        this.mType = mType;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String getImageLink() {
        return getAvatar();
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    @Override
    public String getImageTitle() {
        return getName();
    }

    public long getRoomNo() {
        return RoomNo;
    }

    public void setRoomNo(long roomNo) {
        RoomNo = roomNo;
    }

    public int getMakeUserNo() {
        return MakeUserNo;
    }

    public void setMakeUserNo(int makeUserNo) {
        MakeUserNo = makeUserNo;
    }

    public String getModdate() {
        return Moddate;
    }

    public void setModdate(String moddate) {
        Moddate = moddate;
    }

    public boolean isOne() {
        return IsOne;
    }

    public void setOne(boolean one) {
        IsOne = one;
    }

    public String getRoomTitle() {
        return RoomTitle;
    }

    public void setRoomTitle(String roomTitle) {
        RoomTitle = roomTitle;
    }

    public String getLastedMsg() {
        return LastedMsg;
    }

    public void setLastedMsg(String lastedMsg) {
        LastedMsg = lastedMsg;
    }

    public String getLastedMsgDate() {
        return LastedMsgDate;
    }

    public void setLastedMsgDate(String lastedMsgDate) {
        LastedMsgDate = lastedMsgDate;
    }

    public int getUnReadCount() {
        return UnReadCount;
    }

    public void setUnReadCount(int unReadCount) {
        UnReadCount = unReadCount;
    }

    public ArrayList<Integer> getUserNos() {
        return UserNos;
    }

    public void setUserNos(ArrayList<Integer> userNos) {
        UserNos = userNos;
    }

    public int getWriterUser() {
        return WriterUser;
    }

    public void setWriterUser(int writerUser) {
        WriterUser = writerUser;
    }

    public long getMessageNo() {
        return MessageNo;
    }

    public void setMessageNo(long messageNo) {
        MessageNo = messageNo;
    }

    public int getUserNo() {
        return UserNo;
    }

    public void setUserNo(int userNo) {
        UserNo = userNo;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public int getAttachNo() {
        return AttachNo;
    }

    public void setAttachNo(int attachNo) {
        AttachNo = attachNo;
    }

    public String getRegDate() {
        return RegDate;
    }

    public void setRegDate(String regDate) {
        RegDate = regDate;
    }

    public AttachDTO getAttachInfo() {
        return AttachInfo;
    }

    public void setAttachInfo(AttachDTO attachInfo) {
        AttachInfo = attachInfo;
    }

    public int getWriterUserNo() {
        return WriterUserNo;
    }

    public void setWriterUserNo(int writerUserNo) {
        WriterUserNo = writerUserNo;
    }

    public List<UserDto> getList() {
        return list;
    }

    public void setList(List<UserDto> list) {
        this.list = list;
    }

    public List<TreeUserDTOTemp> getListTreeUser() {
        return listTreeUser;
    }

    public void setListTreeUser(List<TreeUserDTOTemp> listTreeUser) {
        this.listTreeUser = listTreeUser;
    }

    public boolean isCheckFromServer() {
        return isCheckFromServer;
    }

    public void setCheckFromServer(boolean checkFromServer) {
        isCheckFromServer = checkFromServer;
    }

    public String getAttachFileName() {
        return AttachFileName;
    }

    public void setAttachFileName(String attachFileName) {
        AttachFileName = attachFileName;
    }

    public int getAttachFileType() {
        return AttachFileType;
    }

    public void setAttachFileType(int attachFileType) {
        AttachFileType = attachFileType;
    }

    public String getAttachFilePath() {
        return AttachFilePath;
    }

    public void setAttachFilePath(String attachFilePath) {
        AttachFilePath = attachFilePath;
    }

    public int getAttachFileSize() {
        return AttachFileSize;
    }

    public void setAttachFileSize(int attachFileSize) {
        AttachFileSize = attachFileSize;
    }

    public int getLastedMsgAttachType() {
        return LastedMsgAttachType;
    }

    public void setLastedMsgAttachType(int lastedMsgAttachType) {
        LastedMsgAttachType = lastedMsgAttachType;
    }

    public int getLastedMsgType() {
        return LastedMsgType;
    }

    public void setLastedMsgType(int lastedMsgType) {
        LastedMsgType = lastedMsgType;
    }

    public boolean isNotification() {
        return Notification;
    }

    public void setNotification(boolean notification) {
        Notification = notification;
    }

    public boolean isFavorite() {
        return Favorite;
    }

    public void setFavorite(boolean favorite) {
        Favorite = favorite;
    }

    public int getMsgUserNo() {
        return MsgUserNo;
    }

    public void setMsgUserNo(int msgUserNo) {
        MsgUserNo = msgUserNo;
    }

    public boolean isHasSent() {
        return hasSent;
    }

    public void setHasSent(boolean hasSent) {
        this.hasSent = hasSent;
    }
}
