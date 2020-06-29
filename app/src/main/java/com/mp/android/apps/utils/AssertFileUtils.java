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
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(am.open(fileName));
            BufferedReader br = new BufferedReader(inputStreamReader);
            String next = "";
            while (null != (next = br.readLine())) {
                sb.append(next);
            }
            br.close();
            inputStreamReader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            sb.delete(0, sb.length());
        }
        return sb.toString().trim();
    }

}
