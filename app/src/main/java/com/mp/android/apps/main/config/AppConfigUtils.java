package com.mp.android.apps.main.config;

import com.alibaba.fastjson.JSONObject;
import com.mp.android.apps.book.base.MBaseModelImpl;
import com.mp.android.apps.book.base.observer.SimpleObserver;
import com.mp.android.apps.utils.GeneralTools;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public class AppConfigUtils  extends MBaseModelImpl {

    public static boolean business;

    public void getManPinAppConfig(IAppConfig appConfig){
        getRetrofitObject("https://gitee.com").
                create(IAppConfigAPI.class).
                config()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onNext(String value) {
                        try {
                            List<Integer> jsonObject=JSONObject.parseArray(value,Integer.class);
                            if (jsonObject.contains(GeneralTools.APP_VERSIONCODE)){
                                business = true;
                            }else {
                                business=false;
                            }
                        }catch (Exception e){

                        }
                        appConfig.callback(business);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        appConfig.callback(business);
                    }
                });
    }


    public interface IAppConfig{
        void callback(boolean business);
    }
    public interface IAppConfigAPI {
        @GET("/dssljt/hgsdist/raw/master/manpinAppConfig.json")
        @Headers({"Accept:text/html,application/xhtml+xml,application/xml",
                "User-Agent:Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3",
                "Accept-Charset:UTF-8",
                "Connection:close",
                "Cache-Control:no-cache"})
        Observable<String> config();
    }

}
