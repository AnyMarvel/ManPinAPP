package com.mp.android.apps.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AssertFileUtils {
    /**
     * 从asset路径下读取对应文件转String输出
     *
     * @param mContext
     * @return
     */
    public static String getJson(Context mContext, String fileName) {
        // TODO Auto-generated method stub
        StringBuilder sb = new StringBuilder();
        AssetManager am = mContext.getAssets();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(am.open(fileName)));
            String next;
            while (null != (next = br.readLine())) {
                sb.append(next);
            }
        } catch (IOException e) {
            android.util.Log.e("AssertFileUtils", "getJson failed: " + fileName, e);
            sb.delete(0, sb.length());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    android.util.Log.e("AssertFileUtils", "close reader failed", e);
                }
            }
        }
        return sb.toString().trim();
    }

}
