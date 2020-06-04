package com.mp.android.apps.main.view.bar;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

public class BarManager {

    private BarManager() {
    }

    public static Bar with(Activity activity) {
        Window window = activity.getWindow();
        ViewGroup contentLayout = window.getDecorView().findViewById(Window.ID_ANDROID_CONTENT);
        if (contentLayout.getChildCount() > 0) {
            View contentView = contentLayout.getChildAt(0);
            if (contentView instanceof Bar) {
                return (Bar) contentView;
            }
        }
        return new HostLayout(activity);
    }
}
