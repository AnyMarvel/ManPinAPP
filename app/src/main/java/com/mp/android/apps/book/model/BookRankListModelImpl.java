package com.mp.android.apps.book.model;

import android.graphics.pdf.PdfDocument;

import com.mp.android.apps.book.base.MBaseModelImpl;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public class BookRankListModelImpl extends MBaseModelImpl {
    private final String TAG = "https://www.qidian.com";

    public static BookRankListModelImpl getInstance() {
        return new BookRankListModelImpl();
    }

    /**
     * 获得书城推荐页数据
     *
     * @return
     */
    public Observable<String> getBookRankList(String routePath,int PageNumber) {
        String path=routePath+"/page"+ PageNumber;
        return getRetrofitObject(TAG).create(IBookRFragmentAPI.class).getBookRankListData(path);
    }


    interface IBookRFragmentAPI {
        @GET
        Observable<String> getBookRankListData(@Url String url);
    }

}
