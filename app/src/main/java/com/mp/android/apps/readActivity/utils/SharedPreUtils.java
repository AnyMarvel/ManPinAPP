package com.mp.android.apps.readActivity.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.mp.android.apps.MyApplication;


public class SharedPreUtils {
    private static final String SHARED_NAME = "IReader_pref";
    private static SharedPreUtils sInstance;
    private SharedPreferences sharedReadable;
    private SharedPreferences.Editor sharedWritable;

    private SharedPreUtils() {
        sharedReadable = MyApplication.getInstance()
                .getSharedPreferences(SHARED_NAME, Context.MODE_MULTI_PROCESS);
        sharedWritable = sharedReadable.edit();
    }

    public static SharedPreUtils getInstance() {
        if (sInstance == null) {
            synchronized (SharedPreUtils.class) {
                if (sInstance == null) {
                    sInstance = new SharedPreUtils();
                }
            }
        }
        return sInstance;
    }

    public String getString(String key) {
        return sharedReadable.getString(key, "");
    }

    public void putString(String key, String value) {
        sharedWritable.putString(key, value);
        sharedWritable.apply(); // 使用apply()替代commit()，避免阻塞主线程
    }

    public void putInt(String key, int value) {
        sharedWritable.putInt(key, value);
        sharedWritable.apply(); // 使用apply()替代commit()，避免阻塞主线程
    }

    public void putBoolean(String key, boolean value) {
        sharedWritable.putBoolean(key, value);
        sharedWritable.apply(); // 使用apply()替代commit()，避免阻塞主线程
    }

    public int getInt(String key, int def) {
        return sharedReadable.getInt(key, def);
    }

    public boolean getBoolean(String key, boolean def) {
        return sharedReadable.getBoolean(key, def);
    }

}
