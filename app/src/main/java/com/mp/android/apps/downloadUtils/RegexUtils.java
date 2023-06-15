package com.mp.android.apps.downloadUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class RegexUtils {
    /**
     * 获得匹配正则表达式的内容
     * @param str 字符串
     * @param reg 正则表达式
     * @param isCaseInsensitive 是否忽略大小写，true忽略大小写，false大小写敏感
     * @return 匹配正则表达式的字符串，组成的List
     */
    public static String getMatch(final String str, final String reg, final boolean isCaseInsensitive) {
        try {
            Pattern pattern = null;
            if (isCaseInsensitive) {
                //编译正则表达式,忽略大小写
                pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
            } else {
                //编译正则表达式,大小写敏感
                pattern = Pattern.compile(reg);
            }
            Matcher matcher = pattern.matcher(str);// 指定要匹配的字符串
            if (matcher.find()){
                return matcher.group(1);
            }else {
                return  null;
            }
        }catch (Exception e){
            return null;
        }
    }



    public static String getMatch0(final String str, final String reg, final boolean isCaseInsensitive) {
        try {
            Pattern pattern = null;
            if (isCaseInsensitive) {
                //编译正则表达式,忽略大小写
                pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
            } else {
                //编译正则表达式,大小写敏感
                pattern = Pattern.compile(reg);
            }
            Matcher matcher = pattern.matcher(str);// 指定要匹配的字符串
            if (matcher.find()){
                return matcher.group(0);
            }else {
                return  null;
            }
        }catch (Exception e){
            return null;
        }
    }

    public static List<String> getMatchList(final String str, final String reg, final boolean isCaseInsensitive) {
        ArrayList<String> result = new ArrayList<String>();
        Pattern pattern = null;
        if (isCaseInsensitive) {
            //编译正则表达式,忽略大小写
            pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        } else {
            //编译正则表达式,大小写敏感
            pattern = Pattern.compile(reg);
        }
        Matcher matcher = pattern.matcher(str);// 指定要匹配的字符串
        while (matcher.find()) { //此处find（）每次被调用后，会偏移到下一个匹配
            result.add(matcher.group(1));//获取当前匹配的值
        }
        result.trimToSize();
        return result;
    }

}
