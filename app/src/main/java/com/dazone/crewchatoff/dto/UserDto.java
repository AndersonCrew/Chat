package com.dazone.crewchatoff.dto;

import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;

import java.io.Serializable;
import java.util.ArrayList;

public class UserDto extends DataDto implements Serializable {

    public int Id;
    public int CompanyNo;
    public int PermissionType;//0 normal, 1 admin
    public String userID;
    public String FullName = "";
    public String session;
    public String position;
    public String avatar;
    public String NameCompany = "";
    public String CrewDDSServer = "";
    public String CrewDDSServerIP = "";
    public boolean CrewChatLocalDatabase = false;
    public boolean BirthDateDisplay = false;
    public boolean EntranceDateDisplay = false;
    public int CrewDDSServerPort;
    public String CrewChatFileServerIP;
    public int CrewChatFileServerPort;

    public int status = 0;
    public Prefs prefs = null;


    private String phoneNumber;
    private String companyNumber;

    public String SessionID;
    public String MailAddress;
    public String Domain;

    public boolean isBirthDateDisplay() {
        return BirthDateDisplay;
    }

    public void setBirthDateDisplay(boolean birthDateDisplay) {
        BirthDateDisplay = birthDateDisplay;
    }

    public boolean isEntranceDateDisplay() {
        return EntranceDateDisplay;
    }

    public void setEntranceDateDisplay(boolean entranceDateDisplay) {
        EntranceDateDisplay = entranceDateDisplay;
    }

    public ArrayList<InformationCompany> informationcompany;

    @Override
    public int getId() {
        return Id;
    }

    @Override
    public void setId(int id) {
        Id = id;
    }

    public boolean isCrewChatLocalDatabase() {
        return CrewChatLocalDatabase;
    }

    public void setCrewChatLocalDatabase(boolean crewChatLocalDatabase) {
        CrewChatLocalDatabase = crewChatLocalDatabase;
    }

    public String getCrewDDSServerIP() {
        return CrewDDSServerIP;
    }

    public void setCrewDDSServerIP(String crewDDSServerIP) {
        CrewDDSServerIP = crewDDSServerIP;
    }

    public int getCrewDDSServerPort() {
        return CrewDDSServerPort;
    }

    public void setCrewDDSServerPort(int crewDDSServerPort) {
        CrewDDSServerPort = crewDDSServerPort;
    }

    public String getCrewChatFileServerIP() {
        return CrewChatFileServerIP;
    }

    public void setCrewChatFileServerIP(String crewChatFileServerIP) {
        CrewChatFileServerIP = crewChatFileServerIP;
    }

    public int getCrewChatFileServerPort() {
        return CrewChatFileServerPort;
    }

    public void setCrewChatFileServerPort(int crewChatFileServerPort) {
        CrewChatFileServerPort = crewChatFileServerPort;
    }

    public int getCompanyNo() {
        return CompanyNo;
    }

    public void setCompanyNo(int companyNo) {
        CompanyNo = companyNo;
    }

    public int getPermissionType() {
        return PermissionType;
    }

    public void setPermissionType(int permissionType) {
        PermissionType = permissionType;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getNameCompany() {
        return NameCompany;
    }

    public void setNameCompany(String nameCompany) {
        NameCompany = nameCompany;
    }

    public Prefs getPrefs() {
        return prefs;
    }

    public void setPrefs(Prefs prefs) {
        this.prefs = prefs;
    }

    public String getSessionID() {
        return SessionID;
    }

    public void setSessionID(String sessionID) {
        SessionID = sessionID;
    }

    public String getMailAddress() {
        return MailAddress;
    }

    public void setMailAddress(String mailAddress) {
        MailAddress = mailAddress;
    }

    public String getDomain() {
        return Domain;
    }

    public void setDomain(String domain) {
        Domain = domain;
    }

    public ArrayList<InformationCompany> getInformationcompany() {
        return informationcompany;
    }

    public void setInformationcompany(ArrayList<InformationCompany> informationcompany) {
        this.informationcompany = informationcompany;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public UserDto() {
        prefs = CrewChatApplication.getInstance().getPrefs();
    }

    public UserDto(String userID, String fullName, String avatar) {
        this.userID = userID;
        this.FullName = fullName;
        this.avatar = avatar;
    }

    public UserDto(int id, String userID, String fullName, String avatar) {
        Id = id;
        this.userID = userID;
        FullName = fullName;
        this.avatar = avatar;
    }

    public UserDto(int userNo, String userID, String fullName, String avatar, int status, String position) {
        this.Id = userNo;
        this.userID = userID;
        this.FullName = fullName;
        this.avatar = avatar;
        this.status = status;
        this.position = position;
    }

    public void setCrewDDSServer(String ip) {
        CrewDDSServer = ip;
    }

    public String getCrewDDSServer() {
        return CrewDDSServer;
    }
}
