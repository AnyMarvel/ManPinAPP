package com.mp.android.apps.book.model;

import android.graphics.pdf.PdfDocument;

import com.mp.android.apps.book.base.MBaseModelImpl;

import java.util.UUID;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Url;

public class BookRankListModelImpl extends MBaseModelImpl {
    //改用起点移动站 majax JSON 接口：PC 站(www.qidian.com)榜单页有 JS 动态反爬(probe.js/buid)，
    //纯 HTTP 只能拿到 202 挑战页。移动站 /majax/rank/{type}list 返回干净 JSON 且支持分页(total 上千)。
    private final String TAG = "https://m.qidian.com";

    public static BookRankListModelImpl getInstance() {
        return new BookRankListModelImpl();
    }

    /**
     * 获得书城榜单数据（起点移动站 majax JSON 接口，支持分页）。
     *
     * @param routePath 形如 /majax/rank/yuepiaolist
     * @param PageNumber 页码，从 1 开始
     *
     * 鉴权是"双提交 cookie"模式：只校验 Cookie 里的 _csrfToken 与 query 参数 _csrfToken 一致，
     * 不校验来源——故这里自造一个 UUID 同时放进 Cookie 和参数即可，无需先请求页面拿真 token。
     */
    public Observable<String> getBookRankList(String routePath,int PageNumber) {
        String token = UUID.randomUUID().toString();
        String url = routePath + "?_csrfToken=" + token + "&gender=male&pageNum=" + PageNumber;
        return getRetrofitObject(TAG).create(IBookRFragmentAPI.class)
                .getBookRankListData(url, "_csrfToken=" + token);
    }


    interface IBookRFragmentAPI {
        @GET
        Observable<String> getBookRankListData(@Url String url, @Header("Cookie") String cookie);
    }

}
