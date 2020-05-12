/**
  * Copyright 2020 bejson.com 
  */
package com.mp.android.apps.explore.bean;
import java.util.List;

/**
 * Auto-generated: 2020-01-19 16:8:48
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class JsonRootBean {

    private String msg;
    private int code;
    private List<Data> data;
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

    public void setData(List<Data> data) {
         this.data = data;
     }
     public List<Data> getData() {
         return data;
     }

    public void setTime(long time) {
         this.time = time;
     }
     public long getTime() {
         return time;
     }

}