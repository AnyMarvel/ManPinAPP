package com.mp.android.apps.livevblank.bean;

import java.io.Serializable;

public class ImageDesBean implements Serializable {
    private String imageDes;
    private String toPeople;
    private String byPeople;
    private String selectTime;
    private String imageCachePath;
    private String templateID;

    public String getTemplateID() {
        return templateID;
    }

    public void setTemplateID(String templateID) {
        this.templateID = templateID;
    }

    public String getImageCachePath() {
        return imageCachePath;
    }

    public void setImageCachePath(String imageCachePath) {
        this.imageCachePath = imageCachePath;
    }

    public String getImageDes() {
        return imageDes;
    }

    public void setImageDes(String imageDes) {
        this.imageDes = imageDes;
    }

    public String getToPeople() {
        return toPeople;
    }

    public void setToPeople(String toPeople) {
        this.toPeople = toPeople;
    }

    public String getByPeople() {
        return byPeople;
    }

    public void setByPeople(String byPeople) {
        this.byPeople = byPeople;
    }

    public String getSelectTime() {
        return selectTime;
    }

    public void setSelectTime(String selectTime) {
        this.selectTime = selectTime;
    }
}
