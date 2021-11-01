package com.mp.android.apps.book.bean;

/**
 * 网络请求基础bean类
 */
public class BaseResponseBean {

    private String msg;
    private int code;
    private String data;
    private long time;
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public String getMsg() {
        return msg;
    }

    public void setCode(int code) {
        this.code = code;
    }
    public int getCode() {
        return code;
    }

    public void setData(String data) {
        this.data = data;
    }
    public String getData() {
        return data;
    }

    public void setTime(long time) {
        this.time = time;
    }
    public long getTime() {
        return time;
    }

}
