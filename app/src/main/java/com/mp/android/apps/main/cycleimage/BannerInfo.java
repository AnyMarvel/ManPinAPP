package com.mp.android.apps.main.cycleimage;

/**
 * Created by Gavin on 2016/8/31.
 */
public class BannerInfo {
    private String url;
    private String title;

    public BannerInfo(String title, String url) {
        this.url = url;
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
