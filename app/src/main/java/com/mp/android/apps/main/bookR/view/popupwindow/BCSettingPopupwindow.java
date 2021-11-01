package com.mp.android.apps.main.bookR.view.popupwindow;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import com.mp.android.apps.R;
import com.mp.android.apps.book.view.impl.BookSourceActivity;


/**
 * 图书的设置界面
 */

public class BCSettingPopupwindow extends PopupWindow {
    private View rootView;
    private Button backBooks;
    private Button recoveryBooks;
    private Button setBookSource;
    private Context context;

    public BCSettingPopupwindow(Context context) {
        super(context);
        this.context=context;
        rootView= LayoutInflater.from(context).inflate(R.layout.view_pop_window_bc_setting,null);
        this.setContentView(rootView);
        setFocusable(true);
        setTouchable(true);
        if (rootView!=null){
            initView();
        }
    }
    private void initView(){
            backBooks=rootView.findViewById(R.id.manpin_back_book_collection);
            recoveryBooks=rootView.findViewById(R.id.manpin_recovery_book_collection);
            setBookSource=rootView.findViewById(R.id.manpin_setting_book_source);
            //备份图书
            backBooks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            //还原图书
            recoveryBooks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            //设置书源
            setBookSource.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dissMissPopWindow();
                    Intent intent2 = new Intent(context, BookSourceActivity.class);
                    context.startActivity(intent2);
                }
            });

    }

    private void dissMissPopWindow(){
        this.dismiss();
    }
}
