package com.mp.android.apps.livevblank.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NumberToCh {

    public static String parseNum(String str){
        String chineseDate = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date da = null;
        try {
            da = sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // String datestr = sdf.format(new Date());
        String datestr = sdf.format(da);
        System.out.println(datestr);
        String[] strs = datestr.split("-");
        // 年
        for (int i = 0; i < strs[0].length(); i++) {
            chineseDate += formatDigit(strs[0].charAt(i));
        }
        // chineseDate = chineseDate+"年";
        // 月
        char c1 = strs[1].charAt(0);
        char c2 = strs[1].charAt(1);
        String newmonth = "";
        if (c1 == '0') {
            newmonth = String.valueOf(formatDigit(c2));
        } else if (c1 == '1' && c2 == '0') {
            newmonth = "十";
        } else if (c1 == '1' && c2 != '0') {
            newmonth = "十" + formatDigit(c2);
        }
        chineseDate = chineseDate + "年" + newmonth + "月";
        // 日
        char d1 = strs[2].charAt(0);
        char d2 = strs[2].charAt(1);
        String newday = "";
        if (d1 == '0') {//单位数天
            newday = String.valueOf(formatDigit(d2));
        } else if (d1 != '1' && d2 == '0') {//几十
            newday = String.valueOf(formatDigit(d1)) + "十";
        } else if (d1 != '1' && d2 != '0') {//几十几
            newday = formatDigit(d1) + "十" + formatDigit(d2);
        } else if (d1 == '1' && d2 != '0') {//十几
            newday = "十" + formatDigit(d2);
        } else {//10
            newday = "十";
        }
        chineseDate = chineseDate + newday + "日";

        return chineseDate;
    }

    public static char formatDigit(char sign) {
        if (sign == '0')
            sign = '〇';
        if (sign == '1')
            sign = '一';
        if (sign == '2')
            sign = '二';
        if (sign == '3')
            sign = '三';
        if (sign == '4')
            sign = '四';
        if (sign == '5')
            sign = '五';
        if (sign == '6')
            sign = '六';
        if (sign == '7')
            sign = '七';
        if (sign == '8')
            sign = '八';
        if (sign == '9')
            sign = '九';
        return sign;
    }

}
