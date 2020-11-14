package com.mp.android.apps.monke.monkeybook.widget.contentswitchview.contentAnimtion;

public enum ContentPageStatus {
    NONE(-1),//没有上一页 也没有下一页
    PREANDNEXT(0), //有上一页也有下一页
    ONLYPRE(1), //只有上一页
    ONLYNEXT(2);//只有下一页

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    private int status = -1;

    ContentPageStatus(int status) {
        this.status = status;
    }
}
