package com.mp.android.apps.login.model;
import com.alibaba.fastjson.JSON;
import com.mp.android.apps.monke.monkeybook.base.MBaseModelImpl;
import java.util.HashMap;
import java.util.Map;
import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class ILoginFragmentModelImpl extends MBaseModelImpl {
    private final String TAG = "http://aimanpin.com";

    public static ILoginFragmentModelImpl getInstance() {
        return new ILoginFragmentModelImpl();
    }

    public Observable<String> verifyLoginUser(String contract) {
        Map<String, String> map = new HashMap<String, String>(16);
        map.put("clientId", "59dfbd1db8544992aa9f7e12f5c1a213");
        map.put("clientSecret", "");
        map.put("recipient", contract);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JSON.toJSONString(map));
        return getRetrofitObject("https://www.onlyid.net").create(ILoginFragmentAPI.class).verifyCodeByID(requestBody);
    }

    /**
     * 使用手机号或邮箱登录
     * 获取验证码登录接口
     *
     * @param str 手机号或邮箱
     * @return 返回用户信息
     */
    public Observable<String> loginByContractInfo(String str) {
        return getRetrofitObject(TAG).create(ILoginFragmentAPI.class).loginByContractInfo(str);
    }

    /**
     * 登陆
     *
     * @param userName  用户名
     * @param passworld 密码
     * @return 返回登陆基础信息
     */
    public Observable<String> loginByUserName(String userName, String passworld) {
        return getRetrofitObject(TAG).create(ILoginFragmentAPI.class).loginByUserName(userName, passworld);
    }


    interface ILoginFragmentAPI {
        @POST("/api/open/send-otp")
        Observable<String> verifyCodeByID(@Body RequestBody requestBody);

        @GET("/app/phoneOrEmailLogin")
        Observable<String> loginByContractInfo(@Query("constr") String constr);

        @POST("app/login")
        Observable<String> loginByUserName(@Query("userName") String userName, @Query("passworld") String passworld);

    }

}
