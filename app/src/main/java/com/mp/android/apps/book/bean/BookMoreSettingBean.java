package com.mp.android.apps.book.bean;


public class BookMoreSettingBean {
    public String settingNameStr;
    public boolean aSwitchBoolean;
    public String settingTag;


    public BookMoreSettingBean(String settingNameStr, boolean aSwitchBoolean, String settingTag) {
        this.settingNameStr = settingNameStr;
        this.aSwitchBoolean = aSwitchBoolean;
        this.settingTag = settingTag;
    }

    public String getSettingNameStr() {
        return settingNameStr;
    }

    public void setSettingNameStr(String settingNameStr) {
        this.settingNameStr = settingNameStr;
    }

    public boolean isaSwitchBoolean() {
        return aSwitchBoolean;
    }

    public void setaSwitchBoolean(boolean aSwitchBoolean) {
        this.aSwitchBoolean = aSwitchBoolean;
    }

    public String getSettingTag() {
        return settingTag;
    }

    public void setSettingTag(String settingTag) {
        this.settingTag = settingTag;
    }
}
