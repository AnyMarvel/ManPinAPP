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

    /**
     * 获取到验证码的进行本地存储
     *
     * @param number
     */
    public void editLoginCodeByCodeNumber(String contractInfo, String number) {
        sharedPreferences.edit()
                .putString("verificationContractInfo", contractInfo)
                .putString("verificationCode", number)
                .putString("verificationTime", String.valueOf(System.currentTimeMillis()))
                .apply();
    }


    /**
     * 判断验证码和账号是否匹配
     *
     * @param number   验证码
     * @param contract 账号信息
     * @return
     */
    public boolean checkLoginCodeByCodeNumber(String number, String contract) {
        String hqtime = sharedPreferences.getString("verificationTime", null);
        String contractNumber = sharedPreferences.getString("verificationCode", null);
        String contractInfo = sharedPreferences.getString("verificationContractInfo", null);
        assert contractNumber != null;
        assert contractInfo != null;
        if (hqtime != null && contractNumber.equals(number) && contract.equals(contractInfo)) {
            long s = (System.currentTimeMillis() - Long.parseLong(hqtime)) / (1000 * 60);
            return s <= 10;
        }
        return false;


    }


}
