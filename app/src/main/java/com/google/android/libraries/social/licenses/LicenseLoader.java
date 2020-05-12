package com.google.android.libraries.social.licenses;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

final class LicenseLoader extends AsyncTaskLoader<List<License>> {
    private static final String LOG_TAG = LicenseLoader.class.getCanonicalName();
    private List<String> extraPluginPaths;
    private List<License> licenses;

    LicenseLoader(Context context) {
        super(context.getApplicationContext());
    }

    LicenseLoader(Context context, List<String> extraPluginPaths) {
        this(context);
        this.extraPluginPaths = extraPluginPaths;
    }

    public List<License> loadInBackground() {
        TreeSet<License> licenses = new TreeSet();
        licenses.addAll(Licenses.getLicenses(getContext()));
        if (this.extraPluginPaths != null) {
            for (String extraPluginPath : this.extraPluginPaths) {
                licenses.addAll(Licenses.getPluginLicenses(getContext(), extraPluginPath));
            }
        }
        return Collections.unmodifiableList(new ArrayList(licenses));
    }

    public void deliverResult(List<License> licenses) {
        this.licenses = licenses;
        super.deliverResult(licenses);
    }

    protected void onStartLoading() {
        if (this.licenses != null) {
            deliverResult(this.licenses);
        } else {
            forceLoad();
        }
    }

    protected void onStopLoading() {
        cancelLoad();
    }
}
