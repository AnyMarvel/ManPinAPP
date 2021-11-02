
package com.mp.android.apps.book.common;

public class RxBusTag {
    /**
     * 添加本地书架图书消息
     */
    public final static String HAD_ADD_BOOK = "rxbus_add_book";
    /**
     * 删除本地书架图书消息
     */
    public final static String HAD_REMOVE_BOOK = "rxbus_remove_book";

    /**
     * 更新书架图书消息
     */
    public final static String UPDATE_BOOK_PROGRESS = "rxbus_update_book_progress";

    /**
     * 登陆成功通知
     */
    public final static String LOGIN_SUCCESS = "rxbus_login_success";


    /**
     * 书架界面展示loading动画
     */
    public final static String SHOW_COLLECTION_RLLODING = "rxbus_show_collection_rlloding";

    /**
     * 书架界面隐藏loading动画
     */
    public final static String HIDE_COLLECTION_RLLODING = "rxbus_hide_collection_rlloding";
}
