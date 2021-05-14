package com.mp.android.apps.book.bean;

public class BookSourceBean {
    private String bookTitle;
    private String bookSourceScore;
    private String bookSourceAddress;
    private boolean bookSourceSwitch;


    public boolean isBookSourceSwitch() {
        return bookSourceSwitch;
    }

    public void setBookSourceSwitch(boolean bookSourceSwitch) {
        this.bookSourceSwitch = bookSourceSwitch;
    }



    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookSourceScore(String bookSourceScore) {
        this.bookSourceScore = bookSourceScore;
    }

    public String getBookSourceScore() {
        return bookSourceScore;
    }

    public void setBookSourceAddress(String bookSourceAddress) {
        this.bookSourceAddress = bookSourceAddress;
    }

    public String getBookSourceAddress() {
        return bookSourceAddress;
    }


}