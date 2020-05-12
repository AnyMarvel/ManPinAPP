package androidx.media.util;

import android.util.Log;
import java.util.HashMap;

public class Timing {
    static HashMap<String, Stats> timings = new HashMap();
    String mName;

    public static class Stats {
        int count = 0;
        long latest = -1;
        long max = -1;
        long min = -1;
        long start_time = -1;
        long sum = 0;

        Stats() {
        }

        double getAverage() {
            if (this.count != 0) {
                return ((double) this.sum) / ((double) this.count);
            }
            return 0.0d;
        }

        long getMin() {
            return this.min;
        }

        long getMax() {
            return this.max;
        }

        int getCount() {
            return this.count;
        }
    }

    public Timing(String name) {
        this.mName = name;
    }

    public void begin() {
        Stats stats = (Stats) timings.get(this.mName);
        if (stats == null) {
            stats = new Stats();
            timings.put(this.mName, stats);
        }
        stats.start_time = System.currentTimeMillis();
    }

    public void clear() {
        timings.put(this.mName, new Stats());
    }

    public void end() {
        Stats stats = (Stats) timings.get(this.mName);
        if (stats == null || stats.start_time < 0) {
            Log.e("Timing", "No begin called.");
            return;
        }
        long t = System.currentTimeMillis() - stats.start_time;
        if (stats.count == 0) {
            stats.min = t;
            stats.max = t;
            stats.sum = t;
            stats.latest = t;
            stats.count = 1;
            return;
        }
        stats.count++;
        stats.sum += t;
        stats.min = Math.min(stats.min, t);
        stats.max = Math.max(stats.max, t);
        stats.latest = t;
    }

    public void log() {
        Stats stats = (Stats) timings.get(this.mName);
        if (stats != null) {
            String str = "Timing: ";
            String valueOf = String.valueOf(this.mName);
            valueOf = valueOf.length() != 0 ? str.concat(valueOf) : new String(str);
            str = String.format("%.2f", new Object[]{Double.valueOf(stats.getAverage())});
            long max = stats.getMax();
            Log.i(valueOf, new StringBuilder(String.valueOf(str).length() + 90).append("Average: ").append(str).append(", Maximum: ").append(max).append(", Latest: ").append(stats.latest).append(", Count: ").append(stats.getCount()).toString());
        }
    }
}
