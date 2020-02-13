package com.dazone.crewchatoff.dto;

import com.google.gson.annotations.SerializedName;

public class CheckUpdateDto {
    @SerializedName("success")
    private int success;
    @SerializedName("version")
    private String version;
    @SerializedName("packageUrl")
    private String packageUrl;

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackageUrl() {
        return packageUrl;
    }

    public void setPackageUrl(String packageUrl) {
        this.packageUrl = packageUrl;
    }
}