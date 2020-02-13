package com.dazone.crewchatoff.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class TreeUserDTOTemp implements DrawImageItem, Serializable, Parcelable {

    @SerializedName("DepartNo")
    public int DepartNo;
    @SerializedName("UserNo")
    public int UserNo;
    public int DBId;
    public String UserID;
    public int status = 0;
    public String DutyName = "";

    public String getDutyName() {
        return DutyName;
    }

    public void setDutyName(String dutyName) {
        DutyName = dutyName;
    }

    // type = 1 category , type =2 : user
    public int Type = 2;
    public int isHide = 0;

    public boolean isCheck = false;
    @SerializedName("PositionName")
    public String Position = "";
    @SerializedName("AvatarUrl")
    public String AvatarUrl = "";
    @SerializedName("CellPhone")
    public String CellPhone = "";
    @SerializedName("Enabled")
    public boolean Enabled=true;
    @SerializedName("Name")
    public String Name = "";
    @SerializedName("Name_EN")
    public String NameEN = "";
    @SerializedName("CompanyPhone")
    public String CompanyPhone = "";

    @SerializedName("Belongs")
    public ArrayList<BelongDepartmentDTO> Belongs;

    public ArrayList<BelongDepartmentDTO> getBelongs() {
        return Belongs;
    }

    public void setBelongs(ArrayList<BelongDepartmentDTO> belongs) {
        Belongs = belongs;
    }

    protected TreeUserDTOTemp(Parcel in) {
        DepartNo = in.readInt();
        UserNo = in.readInt();
        DBId = in.readInt();
        UserID = in.readString();
        status = in.readInt();
        Type = in.readInt();
        isHide = in.readInt();
        isCheck = in.readByte() != 0;
        Position = in.readString();
        AvatarUrl = in.readString();
        CellPhone = in.readString();
        Name = in.readString();
        NameEN = in.readString();
        CompanyPhone = in.readString();
        userStatusString = in.readString();
        Enabled = in.readByte() != 0;
    }

    public boolean getEnabled() {
        return Enabled;
    }

    public void setEnabled(boolean enabled) {
        Enabled = enabled;
    }

    public static final Creator<TreeUserDTOTemp> CREATOR = new Creator<TreeUserDTOTemp>() {
        @Override
        public TreeUserDTOTemp createFromParcel(Parcel in) {
            return new TreeUserDTOTemp(in);
        }

        @Override
        public TreeUserDTOTemp[] newArray(int size) {
            return new TreeUserDTOTemp[size];
        }
    };

    public String getCompanyPhone() {
        return CompanyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        CompanyPhone = companyPhone;
    }

    private String userStatusString;

    public String getUserStatusString() {
        return userStatusString;
    }

    public void setUserStatusString(String userStatusString) {
        this.userStatusString = userStatusString;
    }


    @Override
    public String toString() {
        return "TreeUserDTOTemp{" +
                "DepartNo=" + DepartNo +
                ", UserNo=" + UserNo +
                ", DBId=" + DBId +
                ", UserID='" + UserID + '\'' +
                ", status=" + status +
                ", Enabled=" + Enabled +
                ", Type=" + Type +
                ", isHide=" + isHide +
                ", isCheck=" + isCheck +
                ", Position='" + Position + '\'' +
                ", AvatarUrl='" + AvatarUrl + '\'' +
                ", CellPhone='" + CellPhone + '\'' +
                ", Name='" + Name + '\'' +
                ", NameEN='" + NameEN + '\'' +
                ", CompanyPhone='" + CompanyPhone + '\'' +
                ", userStatusString='" + userStatusString + '\'' +

                '}';
    }

    public TreeUserDTOTemp() {
    }

    public int getDepartNo() {
        return DepartNo;
    }

    public void setDepartNo(int departNo) {
        DepartNo = departNo;
    }

    public int getUserNo() {
        return UserNo;
    }

    public void setUserNo(int userNo) {
        UserNo = userNo;
    }

    public int getDBId() {
        return DBId;
    }

    public void setDBId(int DBId) {
        this.DBId = DBId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public int getIsHide() {
        return isHide;
    }

    public void setIsHide(int isHide) {
        this.isHide = isHide;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public String getPosition() {
        return Position;
    }

    public void setPosition(String position) {
        Position = position;
    }

    public String getAvatarUrl() {
        return AvatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        AvatarUrl = avatarUrl;
    }

    public String getCellPhone() {
        return CellPhone;
    }

    public void setCellPhone(String cellPhone) {
        CellPhone = cellPhone;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getNameEN() {
        return NameEN;
    }

    public void setNameEN(String nameEN) {
        NameEN = nameEN;
    }

    @Override
    public String getImageLink() {
        return getAvatarUrl();
    }

    @Override
    public String getImageTitle() {
        return getName();
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(DepartNo);
        dest.writeInt(UserNo);
        dest.writeInt(DBId);
        dest.writeString(UserID);
        dest.writeInt(status);
        dest.writeInt(Type);
        dest.writeInt(isHide);
        dest.writeByte((byte) (isCheck ? 1 : 0));
        dest.writeString(Position);
        dest.writeString(AvatarUrl);
        dest.writeString(CellPhone);
        dest.writeString(Name);
        dest.writeString(NameEN);
        dest.writeString(CompanyPhone);
        dest.writeByte((byte) (Enabled ? 1 : 0));
        dest.writeString(userStatusString);
    }
}