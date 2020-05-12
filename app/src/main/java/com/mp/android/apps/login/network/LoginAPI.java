package com.mp.android.apps.login.network;
import com.mp.android.apps.login.bean.login.LoginRootBean;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LoginAPI {
    /**
     * 注册模块
     * @param userName 用户名
     * @param passworld 密码
     * @param nickname 昵称
     * @param sex 性别
     * @return 是否注册成功
     */
    @POST("app/register")
    Call<LoginRootBean> registAccount(@Query("userName") String userName, @Query("passworld") String passworld,
                                        @Query("nickname") String nickname, @Query("sex") String sex);

    /**
     * 快捷登陆
     * @param uniqueID 唯一ID
     * @param nickname 昵称
     * @param sex 性别
     * @param iconurl 用户头像
     * @return 返回登陆基础信息
     */
    @POST("app/quickLogin")
    Call<LoginRootBean> quickLogin(@Query("uniqueID") String uniqueID, @Query("nickname") String nickname,
                                  @Query("sex") String sex, @Query("iconurl") String iconurl);

    /**
     * 登陆
     * @param userName 用户名
     * @param passworld 密码
     * @return 返回登陆基础信息
     */
    @POST("app/login")
    Call<LoginRootBean> login(@Query("userName") String userName, @Query("passworld") String passworld);
}
