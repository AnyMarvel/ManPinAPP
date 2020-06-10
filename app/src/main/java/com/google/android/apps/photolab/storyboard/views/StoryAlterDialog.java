package com.google.android.apps.photolab.storyboard.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.mp.android.apps.R;

import java.util.Objects;

public class StoryAlterDialog extends Dialog {

    TextView cancleButton;
    TextView confirmButton;

    public StoryAlterDialog(@NonNull Context context) {
        super(context);
    }

    public StoryAlterDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected StoryAlterDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.story_so_load_dialog);
        cancleButton = findViewById(R.id.cancle_button);
        confirmButton = findViewById(R.id.confirm_button);
    }

    public void setCancleButtonOnclik(View.OnClickListener onclik) {
        cancleButton.setOnClickListener(onclik);
    }

    public void setConfirmButtonOnclick(View.OnClickListener onclick) {
        confirmButton.setOnClickListener(onclick);
    }

}
