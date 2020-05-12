package androidx.media.filterfw;

import android.util.Log;
import java.util.HashSet;
import java.util.Iterator;

public class FilterFactory {
    private static final String TAG = "FilterFactory";
    private static Object mClassLoaderGuard = new Object();
    private static ClassLoader mCurrentClassLoader = FilterFactory.class.getClassLoader();
    private static HashSet<String> mLibraries = new HashSet();
    private static boolean mLogVerbose = Log.isLoggable(TAG, Log.VERBOSE);
    private static FilterFactory mSharedFactory;
    private HashSet<String> mPackages = new HashSet();

    public static FilterFactory sharedFactory() {
        if (mSharedFactory == null) {
            mSharedFactory = new FilterFactory();
        }
        return mSharedFactory;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void addFilterLibrary(java.lang.String r4) {
        /*
        r0 = mLogVerbose;
        if (r0 == 0) goto L_0x0019;
    L_0x0004:
        r1 = "FilterFactory";
        r2 = "Adding filter library ";
        r0 = java.lang.String.valueOf(r4);
        r3 = r0.length();
        if (r3 == 0) goto L_0x0031;
    L_0x0012:
        r0 = r2.concat(r0);
    L_0x0016:
        android.util.Log.v(r1, r0);
    L_0x0019:
        r1 = mClassLoaderGuard;
        monitor-enter(r1);
        r0 = mLibraries;	 Catch:{ all -> 0x0047 }
        r0 = r0.contains(r4);	 Catch:{ all -> 0x0047 }
        if (r0 == 0) goto L_0x0037;
    L_0x0024:
        r0 = mLogVerbose;	 Catch:{ all -> 0x0047 }
        if (r0 == 0) goto L_0x002f;
    L_0x0028:
        r0 = "FilterFactory";
        r2 = "Library already added";
        android.util.Log.v(r0, r2);	 Catch:{ all -> 0x0047 }
    L_0x002f:
        monitor-exit(r1);	 Catch:{ all -> 0x0047 }
    L_0x0030:
        return;
    L_0x0031:
        r0 = new java.lang.String;
        r0.<init>(r2);
        goto L_0x0016;
    L_0x0037:
        r0 = mLibraries;	 Catch:{ all -> 0x0047 }
        r0.add(r4);	 Catch:{ all -> 0x0047 }
        r0 = new dalvik.system.PathClassLoader;	 Catch:{ all -> 0x0047 }
        r2 = mCurrentClassLoader;	 Catch:{ all -> 0x0047 }
        r0.<init>(r4, r2);	 Catch:{ all -> 0x0047 }
        mCurrentClassLoader = r0;	 Catch:{ all -> 0x0047 }
        monitor-exit(r1);	 Catch:{ all -> 0x0047 }
        goto L_0x0030;
    L_0x0047:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0047 }
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.media.filterfw.FilterFactory.addFilterLibrary(java.lang.String):void");
    }

    public void addPackage(String packageName) {
        if (mLogVerbose) {
            String str = TAG;
            String str2 = "Adding package ";
            String valueOf = String.valueOf(packageName);
            Log.v(str, valueOf.length() != 0 ? str2.concat(valueOf) : new String(str2));
        }
        this.mPackages.add(packageName);
    }

    public boolean isFilterAvailable(String className) {
        return getFilterClass(className) != null;
    }

    public Filter createFilterByClassName(String className, String filterName, MffContext context) {
        if (mLogVerbose) {
            String str = TAG;
            String str2 = "Looking up class ";
            String valueOf = String.valueOf(className);
            Log.v(str, valueOf.length() != 0 ? str2.concat(valueOf) : new String(str2));
        }
        Class<? extends Filter> filterClass = getFilterClass(className);
        if (filterClass != null) {
            return createFilterByClass(filterClass, filterName, context);
        }
        throw new IllegalArgumentException(new StringBuilder(String.valueOf(className).length() + 24).append("Unknown filter class '").append(className).append("'!").toString());
    }

    public Filter createFilterByClass(Class<? extends Filter> filterClass, String filterName, MffContext context) {
        Filter filter = null;
        try {
            try {
                 filter = (Filter) filterClass.getConstructor(new Class[]{MffContext.class, String.class}).newInstance(new Object[]{context, filterName});
                if (filter != null) {
                    return filter;
                }
                throw new IllegalArgumentException(new StringBuilder(String.valueOf(filterName).length() + 34).append("Could not construct the filter '").append(filterName).append("'!").toString());
            } catch (Throwable t) {
                RuntimeException runtimeException = new RuntimeException(new StringBuilder(String.valueOf(filterName).length() + 23).append("Error creating filter ").append(filterName).append("!").toString(), t);
            }
        } catch (Exception e) {
            String valueOf = String.valueOf(filterClass);
            throw new IllegalArgumentException(new StringBuilder(String.valueOf(valueOf).length() + 87).append("The filter class '").append(valueOf).append("' does not have a constructor of the form <init>(MffContext, String)!").toString());
        }
        return filter;
    }

    private Class<? extends Filter> getFilterClass(String name) {
        Class<?> filterClass = null;
        Iterator it = this.mPackages.iterator();
        while (it.hasNext()) {
            String packageName = (String) it.next();
            try {
                if (mLogVerbose) {
                    Log.v(TAG, new StringBuilder((String.valueOf(packageName).length() + 8) + String.valueOf(name).length()).append("Trying ").append(packageName).append(".").append(name).toString());
                }
                synchronized (mClassLoaderGuard) {
                    filterClass = mCurrentClassLoader.loadClass(new StringBuilder((String.valueOf(packageName).length() + 1) + String.valueOf(name).length()).append(packageName).append(".").append(name).toString());
                }
                if (filterClass != null) {
                    break;
                }
            } catch (ClassNotFoundException e) {
            }
        }
        Class<? extends Filter> result = null;
        if (filterClass != null) {
            try {
                result = filterClass.asSubclass(Filter.class);
            } catch (ClassCastException e2) {
            }
        }
        return result;
    }
}
