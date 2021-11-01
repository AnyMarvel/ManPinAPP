package com.mp.android.apps.book.view.popupwindow;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.mp.android.apps.R;

/**
 * @author ShuiYu
 * Created on 11/1/21
 * Copyright © 2021 Alibaba-inc. All rights reserved.
 */

public class UnifiedCheckDialog extends Dialog {

    private TextView tvBookContent;
    private TextView tvExit;
    private TextView tvConfirm;
    private CheckDialogListener mCheckDialogListener;

    public UnifiedCheckDialog(@NonNull Context context,CheckDialogListener mCheckDialogListener) {
        super(context);
        this.mCheckDialogListener=mCheckDialogListener;

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pop_checkaddshelf);
        tvBookContent = (TextView) findViewById(R.id.tv_book_content);
        tvExit = (TextView) findViewById(R.id.tv_exit);
        tvExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UnifiedCheckDialog.this.dismiss();
            }
        });
        tvConfirm = (TextView) findViewById(R.id.tv_confirm_buttom);
        tvBookContent.setText(mCheckDialogListener.getContent());
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCheckDialogListener.confirmDialog();

            }
        });
    }


    public interface CheckDialogListener{
        String getContent();
        void confirmDialog();
    }
}
