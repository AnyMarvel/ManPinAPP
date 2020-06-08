//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.mp.android.apps.monke.monkeybook.view.popupwindow;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;
import com.mp.android.apps.R;
import com.mp.android.apps.monke.monkeybook.ReadBookControl;

import de.hdodenhof.circleimageview.CircleImageView;

public class MoreSettingPop extends PopupWindow {
    private Context mContext;
    private View view;

    private SwitchButton sbKey;
    private SwitchButton sbClick;

    private FrameLayout flSmaller;
    private FrameLayout flBigger;
    private TextView tvTextSizedDefault;
    private TextView tvTextSize;
    private CircleImageView civBgWhite;
    private CircleImageView civBgYellow;
    private CircleImageView civBgGreen;
    private CircleImageView civBgBlack;

    private ReadBookControl readBookControl;

    public MoreSettingPop(Context context, OnChangeProListener changeProListener) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mContext = context;
        this.changeProListener = changeProListener;

        view = LayoutInflater.from(mContext).inflate(R.layout.view_pop_moresetting, null);
        this.setContentView(view);
        initData();
        bindView();
        bindEvent();

        setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
        setFocusable(true);
        setTouchable(true);
        setAnimationStyle(R.style.anim_pop_windowlight);
    }

    public interface OnChangeProListener {
        public void textChange(int index);

        public void bgChange(int index);

        void isDay(boolean isDay);
    }

    private OnChangeProListener changeProListener;

    private void bindEvent() {
        sbKey.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                readBookControl.setCanKeyTurn(isChecked);
            }
        });
        sbClick.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                readBookControl.setCanClickTurn(isChecked);
            }
        });
        flSmaller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateText(readBookControl.getTextKindIndex() - 1);
                changeProListener.textChange(readBookControl.getTextKindIndex());
            }
        });
        flBigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateText(readBookControl.getTextKindIndex() + 1);
                changeProListener.textChange(readBookControl.getTextKindIndex());
            }
        });
        tvTextSizedDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateText(ReadBookControl.DEFAULT_TEXT);
                changeProListener.textChange(readBookControl.getTextKindIndex());
            }
        });

        civBgWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBg(0);
                changeProListener.bgChange(readBookControl.getTextDrawableIndex());
            }
        });
        civBgYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBg(1);
                changeProListener.bgChange(readBookControl.getTextDrawableIndex());
            }
        });
        civBgGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBg(2);
                changeProListener.bgChange(readBookControl.getTextDrawableIndex());
            }
        });
        civBgBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBg(3);
                changeProListener.bgChange(readBookControl.getTextDrawableIndex());
            }
        });
    }

    private void bindView() {
        sbKey = (SwitchButton) view.findViewById(R.id.sb_key);
        sbClick = (SwitchButton) view.findViewById(R.id.sb_click);
        flSmaller = (FrameLayout) view.findViewById(R.id.fl_smaller);
        flBigger = (FrameLayout) view.findViewById(R.id.fl_bigger);
        tvTextSizedDefault = (TextView) view.findViewById(R.id.tv_textsize_default);
        tvTextSize = (TextView) view.findViewById(R.id.tv_dur_textsize);
        updateText(readBookControl.getTextKindIndex());

        civBgWhite = (CircleImageView) view.findViewById(R.id.civ_bg_white);
        civBgYellow = (CircleImageView) view.findViewById(R.id.civ_bg_yellow);
        civBgGreen = (CircleImageView) view.findViewById(R.id.civ_bg_green);
        civBgBlack = (CircleImageView) view.findViewById(R.id.civ_bg_black);
        updateBg(readBookControl.getTextDrawableIndex());

        if (readBookControl.getCanKeyTurn())
            sbKey.setCheckedImmediatelyNoEvent(true);
        else sbKey.setCheckedImmediatelyNoEvent(false);
        if (readBookControl.getCanClickTurn())
            sbClick.setCheckedImmediatelyNoEvent(true);
        else sbClick.setCheckedImmediatelyNoEvent(false);
    }

    public void updateBg(int index) {
        civBgWhite.setBorderColor(Color.parseColor("#00000000"));
        civBgYellow.setBorderColor(Color.parseColor("#00000000"));
        civBgGreen.setBorderColor(Color.parseColor("#00000000"));
        civBgBlack.setBorderColor(Color.parseColor("#00000000"));
        switch (index) {
            case 0:
                civBgWhite.setBorderColor(Color.parseColor("#F3B63F"));
                break;
            case 1:
                civBgYellow.setBorderColor(Color.parseColor("#F3B63F"));
                break;
            case 2:
                civBgGreen.setBorderColor(Color.parseColor("#F3B63F"));
                break;
            default:
                civBgBlack.setBorderColor(Color.parseColor("#F3B63F"));
                break;
        }
        readBookControl.setTextDrawableIndex(index);
        if (index != 3) {
            readBookControl.setDayColorIndex(index);
            changeProListener.isDay(true);
        } else {
            changeProListener.isDay(false);
        }

    }

    private void updateText(int textKindIndex) {
        if (textKindIndex == 0) {
            flSmaller.setEnabled(false);
            flBigger.setEnabled(true);
        } else if (textKindIndex == readBookControl.getTextKind().size() - 1) {
            flSmaller.setEnabled(true);
            flBigger.setEnabled(false);
        } else {
            flSmaller.setEnabled(true);
            flBigger.setEnabled(true);

        }
        if (textKindIndex == ReadBookControl.DEFAULT_TEXT) {
            tvTextSizedDefault.setEnabled(false);
        } else {
            tvTextSizedDefault.setEnabled(true);
        }
        tvTextSize.setText(String.valueOf(readBookControl.getTextKind().get(textKindIndex).get("textSize")));
        readBookControl.setTextKindIndex(textKindIndex);
    }

    private void initData() {
        readBookControl = ReadBookControl.getInstance();
    }
}
