package com.mp.android.apps.login.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.mp.android.apps.login.bean.login.Data;


import static android.content.Context.MODE_PRIVATE;
import static com.mp.android.apps.StoryboardActivity.LOGIN_SP_STATE;

public class LoginManager {
    private String uniqueID;
    private String nickname;
    private String usericon;
    private String sex;
    private SharedPreferences sharedPreferences;

    private static LoginManager loginManager;

    private LoginManager() {
    }

    public static LoginManager getInstance() {
        if (loginManager == null) {
            synchronized (LoginManager.class) {
                if (loginManager == null) {
                    loginManager = new LoginManager();
                }
            }
        }
        return loginManager;
    }

    public LoginManager initSP(Activity activity) {
        sharedPreferences = activity.getSharedPreferences(LOGIN_SP_STATE, MODE_PRIVATE);
        return loginManager;
    }

    public void initData() {
        uniqueID = sharedPreferences.getString("uniqueID", null);
        nickname = sharedPreferences.getString("nickname", null);
        usericon = sharedPreferences.getString("usericon", null);
        sex = sharedPreferences.getString("sex", null);
    }

    public boolean checkLoginInfo() {
        initData();
        return !TextUtils.isEmpty(uniqueID) && !TextUtils.isEmpty(nickname);
    }

    public void editLoginInfo(Data data) {
        sharedPreferences.edit()
                .putString("uniqueID", data.getUniqueID())
                .putString("nickname", data.getNickname())
                .putString("usericon", data.getUsericon())
                .putString("sex", data.getSex())
                .apply();
    }

    public Data getLoginInfo() {
        initData();
        Data data = new Data();
        if (checkLoginInfo()) {
            data.setNickname(nickname);
            data.setSex(sex);
            data.setUsericon(usericon);
            data.setUniqueID(uniqueID);
        }
        return data;
    }

    public void editLogoutInfo() {
        sharedPreferences.edit()
                .putString("uniqueID", null)
                .putString("nickname", null)
                .putString("usericon", null)
                .putString("sex", null)
                .apply();

    }


}
