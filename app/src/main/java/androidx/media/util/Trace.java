package androidx.media.util;

import android.annotation.SuppressLint;
import android.os.Build.VERSION;

public final class Trace {
    private static boolean ENABLED = (VERSION.SDK_INT >= 18);

    @SuppressLint({"NewApi"})
    public static void beginSection(String sectionName) {
        if (ENABLED) {
            if (sectionName != null && sectionName.length() > 127) {
                sectionName = sectionName.substring(0, 127);
            }
            android.os.Trace.beginSection(sectionName);
        }
    }

    @SuppressLint({"NewApi"})
    public static void endSection() {
        if (ENABLED) {
            android.os.Trace.endSection();
        }
    }
}
