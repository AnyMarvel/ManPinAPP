package com.mp.android.apps.downloadUtils.bean;

public class CheckUpdateBean {

    private String apkName;
    private int versionCode;
    private String versionName;
    private String apkDescription;
    private String lanzouDownloadUrl;
    private String apkMd5;
    private String ApkSize;
    private String lanzouSignUrl;

    public String getLanzouSignUrl() {
        return lanzouSignUrl;
    }

    public void setLanzouSignUrl(String lanzouSignUrl) {
        this.lanzouSignUrl = lanzouSignUrl;
    }

    public void setApkName(String apkName) {
        this.apkName = apkName;
    }
    public String getApkName() {
        return apkName;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }
    public int getVersionCode() {
        return versionCode;
    }

    public void setApkDescription(String apkDescription) {
        this.apkDescription = apkDescription;
    }
    public String getApkDescription() {
        return apkDescription;
    }

    public void setLanzouDownloadUrl(String lanzouDownloadUrl) {
        this.lanzouDownloadUrl = lanzouDownloadUrl;
    }
    public String getLanzouDownloadUrl() {
        return lanzouDownloadUrl;
    }

    public void setApkMd5(String apkMd5) {
        this.apkMd5 = apkMd5;
    }
    public String getApkMd5() {
        return apkMd5;
    }

    public void setApkSize(String ApkSize) {
        this.ApkSize = ApkSize;
    }
    public String getApkSize() {
        return ApkSize;
    }
    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

}