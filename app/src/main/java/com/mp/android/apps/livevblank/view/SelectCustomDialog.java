package com.mp.android.apps.livevblank.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mp.android.apps.R;

public class SelectCustomDialog extends Dialog {
    private TextView ablumSelect;
    private TextView cameraSelect;

    public SelectCustomDialog(Context context) {
        super(context);

    }

    public SelectCustomDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected SelectCustomDialog(Context context, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.select_custom_dialog);
        initView();
    }

    private void initView() {
        ablumSelect = findViewById(R.id.album_select);
        cameraSelect = findViewById(R.id.camera_select);
    }

    public void setAblumSelectClickListener(View.OnClickListener onClickListener) {
        ablumSelect.setOnClickListener(onClickListener);
    }

    public void setCameraSelectCliclListener(View.OnClickListener onClickListener) {
        cameraSelect.setOnClickListener(onClickListener);
    }
}
