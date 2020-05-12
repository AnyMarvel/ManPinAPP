package com.mp.android.apps.login.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginUtils {
    /**
     * 不能包含空格,汉字等内容
     *
     * @param password
     * @return
     */
    public static boolean isValidPassword(String password) {

        if (password.length() > 0) {
            //判断是否有空格字符串
            for (int t = 0; t < password.length(); t++) {
                String b = password.substring(t, t + 1);
                if (b.equals(" ")) {
                    return false;
                }
            }


            //判断是否有汉字
            int count = 0;
            String regEx = "[\\u4e00-\\u9fa5]";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(password);
            while (m.find()) {
                for (int i = 0; i <= m.groupCount(); i++) {
                    count = count + 1;
                }
            }

            if (count > 0) {
                return false;
            }


            //判断是否是字母和数字
            int numberCounter = 0;
            for (int i = 0; i < password.length(); i++) {
                char c = password.charAt(i);
                if (!Character.isLetterOrDigit(c)) {
                    return false;
                }
                if (Character.isDigit(c)) {
                    numberCounter++;
                }
            }

        } else {
            return false;
        }
        return true;
    }

    /**
     * 用户名成都需要大于3且不能包含空格
     *
     * @param username
     * @return
     */
    public static boolean isValidUsername(String username) {

        if (username.length() >= 3) {
            //判断是否有空格字符串
            for (int t = 0; t < username.length(); t++) {
                String b = username.substring(t, t + 1);
                if (b.equals(" ")) {
                    return false;
                }
            }

        } else {
            return false;
        }
        return true;
    }

}
