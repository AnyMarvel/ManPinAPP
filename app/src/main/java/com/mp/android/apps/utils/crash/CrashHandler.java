package com.mp.android.apps.utils.crash;

import android.content.Context;
import android.os.Build;

import com.mp.android.apps.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * 全局崩溃捕获：拦截未处理异常，将现场信息写入本地文件，供用户在"设置 → 崩溃日志"中查看并分享给作者。
 * 不接入任何第三方上报，仅落本地。
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    /** 崩溃日志目录名（位于 App 专属外部目录下） */
    private static final String CRASH_DIR = "crash";
    private static final String FILE_PREFIX = "crash_";
    private static final String FILE_SUFFIX = ".log";

    private static volatile CrashHandler instance;

    private Context appContext;
    private Thread.UncaughtExceptionHandler defaultHandler;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (instance == null) {
            synchronized (CrashHandler.class) {
                if (instance == null) {
                    instance = new CrashHandler();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        if (context != null) {
            appContext = context.getApplicationContext();
        }
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            saveCrashLog(ex);
        } catch (Throwable t) {
            // 崩溃处理内部绝不能再抛异常
            android.util.Log.e("CrashHandler", "save crash log failed", t);
        }

        // 交回给之前的处理器（通常是系统默认，弹崩溃对话框并结束进程）
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, ex);
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    private void saveCrashLog(Throwable ex) {
        if (appContext == null || ex == null) {
            return;
        }
        File dir = getCrashDir(appContext);
        if (dir == null) {
            return;
        }

        String time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File file = new File(dir, FILE_PREFIX + time + FILE_SUFFIX);

        // 完整堆栈（含 cause 链）
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        pw.flush();

        StringBuilder sb = new StringBuilder();
        sb.append("崩溃时间: ")
                .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()))
                .append('\n');
        sb.append("应用版本: ").append(BuildConfig.VERSION_NAME)
                .append(" (").append(BuildConfig.VERSION_CODE).append(")\n");
        sb.append("设备品牌: ").append(Build.BRAND).append('\n');
        sb.append("设备型号: ").append(Build.MODEL).append('\n');
        sb.append("系统版本: Android ").append(Build.VERSION.RELEASE)
                .append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
        sb.append("--------- 堆栈信息 ---------\n");
        sb.append(sw.toString());

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
            writer.write(sb.toString());
            writer.flush();
        } catch (Exception e) {
            android.util.Log.e("CrashHandler", "write crash file failed", e);
        }
    }

    /**
     * 崩溃日志目录：App 专属外部目录下的 crash/；外部不可用时兜底到内部存储。
     */
    public static File getCrashDir(Context context) {
        if (context == null) {
            return null;
        }
        File dir = context.getExternalFilesDir(CRASH_DIR);
        if (dir == null) {
            dir = new File(context.getFilesDir(), CRASH_DIR);
        }
        if (!dir.exists() && !dir.mkdirs()) {
            return dir.exists() ? dir : null;
        }
        return dir;
    }

    /**
     * 列出全部崩溃日志文件，按修改时间倒序（最新在前）。
     */
    public static File[] listCrashFiles(Context context) {
        File dir = getCrashDir(context);
        if (dir == null) {
            return new File[0];
        }
        File[] files = dir.listFiles((f) -> f.isFile()
                && f.getName().startsWith(FILE_PREFIX) && f.getName().endsWith(FILE_SUFFIX));
        if (files == null) {
            return new File[0];
        }
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Long.compare(f2.lastModified(), f1.lastModified());
            }
        });
        return files;
    }

    /**
     * 清空全部崩溃日志。
     */
    public static void clearAll(Context context) {
        File[] files = listCrashFiles(context);
        for (File f : files) {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
    }
}
