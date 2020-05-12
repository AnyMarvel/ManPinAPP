package com.mp.android.apps.utils;

import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 使用setDebug()
 * 自定义log日志系统
 */
public class Logger {
    /**
     * tag值
     */
    private static String TAG = "StoryBoard";
    /**
     * debug开关值
     */
    private static boolean isDebug = true;

    /**
     * 封装Log方法
     */
    private static String buildLogMsg(String str, Map map) {
        if (str == null || map == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        StackTraceElement stackTrace = getStackTrace();
        String str2 = "";
        if (stackTrace != null) {
            str2 = stackTrace.getMethodName();
        }
        stringBuilder.append(String.format("[%s]", new Object[]{str2}));
        stringBuilder.append(" ").append(str);

        Iterator localIterator = map.entrySet().iterator();
        while (localIterator.hasNext()) {
            Entry localEntry = (Entry) localIterator.next();
            stringBuilder.append("\n\t");
            stringBuilder.append(String.valueOf(localEntry.getKey()) + " : " + String.valueOf(localEntry.getValue()));
        }
        return stringBuilder.toString();
    }

    /**
     * 封装Log方法
     */
    private static String buildLogMsg(String str, Object... objArr) {
        if (str == null && objArr == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        StackTraceElement stackTrace = getStackTrace();
        String str2 = "";
        if (stackTrace != null) {
            str2 = stackTrace.getMethodName();
        }
        stringBuilder.append(String.format("[%s]", new Object[]{str2}));
        if (str != null) {
            stringBuilder.append(" ").append(str);
        }
        if (objArr != null) {
            int i = 0;
            while (i + 1 < objArr.length) {
                stringBuilder.append("\n\t");
                Object obj = objArr[i];
                i++;
                stringBuilder.append(formatKv(obj, objArr[i]));
                i++;
            }
            if (i == objArr.length - 1) {
                stringBuilder.append("\n\t");
                stringBuilder.append(objArr[i]);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 封装Log方法 TAG为Coow:MethodName.pid:
     */
    private static String buildLogTag() {
        return buildLogTag(TAG);
    }

    /**
     * 封装Log方法
     */
    private static String buildLogTag(String str) {
        if (TextUtils.isEmpty(str)) {
            str = TAG;
        }
        StackTraceElement stackTrace = getStackTrace();
        String str2 = "";
        if (stackTrace != null) {
            String className = stackTrace.getClassName();
            if (!TextUtils.isEmpty(className)) {
                str2 = className.substring(className.lastIndexOf(46) + 1);
            }
        }
        return str + str2 + "." + String.valueOf(Process.myPid());
    }

    /**
     * log.d方法(打印当前执行的方法)
     */
    public static void d() {
        if (isDebug) {
            Log.d(buildLogTag(), buildLogMsg(null, new Object[0]));
        }
    }

    /**
     * log.d方法
     */
    public static void d(String str, Map map) {
        if (isDebug) {
            Log.d(buildLogTag(), buildLogMsg(str, map));
        }
    }

    /**
     * log.d方法
     */
    public static void d(String str, Object... objArr) {
        if (isDebug) {
            Log.d(buildLogTag(), buildLogMsg(str, objArr));
        }
    }

    /**
     * log.e方法
     */
    public static void e() {
        if (isDebug) {
            Log.e(buildLogTag(), buildLogMsg(null, new Object[0]));
        }
    }

    /**
     * log.e方法
     */
    public static void e(String str, Throwable th, Object... objArr) {
        if (isDebug) {
            Log.e(buildLogTag(), buildLogMsg(str, objArr), th);
        }
    }

    /**
     * log.e方法
     */
    public static void e(String str, Object... objArr) {
        if (isDebug) {
            Log.e(buildLogTag(), buildLogMsg(str, objArr));
        }
    }

    /**
     * 格式化数据格式
     *
     * @param obj
     * @param obj2
     * @return
     */
    private static String formatKv(Object obj, Object obj2) {
        if (!isDebug) {
            return "";
        }
        String str = "%s:%s";
        Object[] objArr = new Object[2];
        if (obj == null) {
            obj = "";
        }
        objArr[0] = obj;
        if (obj2 == null) {
            obj2 = "";
        }
        objArr[1] = obj2;
        return String.format(str, objArr);
    }

    /**
     * StackTrace(堆栈轨迹)存放的就是方法调用栈的信息，异常处理中常用的printStackTrace()实质就是打印异常调用的堆栈信息。
     * 获取方法调用栈的信息
     *
     * @return
     */
    private static StackTraceElement getStackTrace() {
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
            if (!stackTraceElement.isNativeMethod() && !stackTraceElement.getClassName().equals(Thread.class.getName()) && !stackTraceElement.getClassName().equals(Logger.class.getName())) {
                return stackTraceElement;
            }
        }
        return null;
    }

    /**
     * log.i
     */
    public static void i() {
        if (isDebug) {
            Log.i(buildLogTag(), buildLogMsg(null, new Object[0]));
        }
    }

    /**
     * log.i
     */
    public static void i(String str, Object... objArr) {
        if (isDebug) {
            Log.i(buildLogTag(), buildLogMsg(str, objArr));
        }
    }

    /**
     * 判断是否是debug环境
     *
     * @return
     */
    public static boolean isDebug() {
        return isDebug;
    }

    /**
     * 设置是否是debug环境
     *
     * @param z true or false
     */
    public static void setDebug(boolean z) {
        Log.i(TAG, "set environment =" + z);
        isDebug = z;
    }

    /**
     * log.w
     *
     * @param str
     * @param th
     * @param objArr
     */
    public static void w(String str, Throwable th, Object... objArr) {
        if (isDebug) {
            Log.w(buildLogTag(), buildLogMsg(str, objArr), th);
        }
    }

    /**
     * log.w
     *
     * @param str
     * @param objArr
     */
    public static void w(String str, Object... objArr) {
        if (isDebug) {
            Log.w(buildLogTag(), buildLogMsg(str, objArr));
        }
    }
}
