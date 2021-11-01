package com.mp.android.apps.main.bookR.view.popupwindow.bean;

import com.mp.android.apps.main.home.bean.SourceListContent;

import java.util.HashMap;
import java.util.List;


public class UserBookCorrespondenceBean {
    /**
     * 本地图书对应的record记录 个人图书阅读记录
     */
    private HashMap<String, Integer> userBookRelay;
    /**
     * 本地图书列表
     */
    private List<SourceListContent> bookList;
    /**
     * 用户唯一ID
     */
    private String uniqueID;

    public HashMap<String, Integer> getUserBookRelay() {
        return userBookRelay;
    }

    public void setUserBookRelay(HashMap<String, Integer> userBookRelay) {
        this.userBookRelay = userBookRelay;
    }

    public List<SourceListContent> getBookList() {
        return bookList;
    }

    public void setBookList(List<SourceListContent> bookList) {
        this.bookList = bookList;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }
}
