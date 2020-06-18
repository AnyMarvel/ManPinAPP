/**
 * Copyright 2020 bejson.com
 */
package com.mp.android.apps.main.bean;

import java.util.List;

/**
 * 主页内容JavaBean解析类
 */
public class HomeDesignBean {

    private List<SourceListContent> sourceListContent;
    private String kind;

    public String getCardColor() {
        return cardColor;
    }

    public void setCardColor(String cardColor) {
        this.cardColor = cardColor;
    }

    private String cardColor;



    public void setSourceListContent(List<SourceListContent> sourceListContent) {
        this.sourceListContent = sourceListContent;
    }

    public List<SourceListContent> getSourceListContent() {
        return sourceListContent;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getKind() {
        return kind;
    }

}