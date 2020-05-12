package com.mp.android.apps.livevblank.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.mp.android.apps.R;
import com.mp.android.apps.livevblank.util.NumberToCh;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class TimerTextView extends TextView {
    Paint mPain;
    TextPaint textPaint;
    String currentTime;
    private int drawFormat = 1;
    private static final int RECTFOTMAT = 1;
    private static final int LINEFORMAT = 2;
    private static final int SIMPLE_CHINESE = 3;

    public TimerTextView(Context context) {
        super(context);
        init();
    }


    public TimerTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimerTextView);
        int count = typedArray.getIndexCount();
        for (int i = 0; i < count; i++) {
            int index = typedArray.getIndex(i);
            if (index == R.styleable.TimerTextView_timer_format) {
                drawFormat = typedArray.getInt(index, RECTFOTMAT);
            }
        }
        init();
    }

    public void setTime(int year,
                        int monthOfYear, int dayOfMonth) {

        Date date = new Date(year - 1900, monthOfYear, dayOfMonth);
        setCurrentTime(date);

        invalidate();
    }
    public String getTime(){
        return currentTime;
    }

    private void init() {
        mPain = new Paint();
        mPain.setColor(Color.parseColor("#969696"));
        mPain.setStyle(Paint.Style.STROKE);
        mPain.setAntiAlias(true);
        textPaint = new TextPaint();
        textPaint.setTextSize(getTextSize());
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.parseColor("#969696"));
        setCurrentTime(new Date());
    }

    public String setCurrentTime(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("MMM. d  yyyy", Locale.ENGLISH);
        if (drawFormat == RECTFOTMAT) {
            currentTime = df.format(date).replaceAll("  ", "\n") + "\n\n +";
        } else if (drawFormat == SIMPLE_CHINESE) {
            SimpleDateFormat chdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            currentTime = NumberToCh.parseNum(chdf.format(date)).replaceAll("年", "年/");
        } else {
            currentTime = df.format(date);
        }
        return currentTime;
    }

    /**
     * 获取单个文字宽度
     *
     * @return
     */
    private float getOneWordWidth() {
        return textPaint.measureText("我") + 12;
    }

    /**
     * 获取单个文字高度
     *
     * @return
     */
    private float getOneWordHeight() {
        Rect rect = new Rect();
        textPaint.getTextBounds("我", 0, 1, rect);
        return rect.height()+8;
    }

    private float getTextBaseLine(RectF rect) {
        Paint.FontMetricsInt metricsInt = textPaint.getFontMetricsInt();
        return (rect.top + rect.bottom - metricsInt.top - metricsInt.bottom) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawFormat == RECTFOTMAT) {
            RectF rectf = new RectF();
            rectf.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
            canvas.drawRect(rectf, mPain);
            canvas.save();
            StaticLayout layout = new StaticLayout(currentTime, textPaint, 300, Layout.Alignment.ALIGN_LEFT, 1.0F, 0.0F, true);
            // 这里的参数300，表示字符串的长度，当满300时，就会换行，也可以使用“\r\n”来实现换行
            canvas.translate(0, 0);//从100，100开始画
            layout.draw(canvas);
            canvas.restore();//别忘了restore
        } else if (drawFormat == SIMPLE_CHINESE) {
            float w = getOneWordWidth();
            float h = getOneWordHeight();
            String[] str = currentTime.split("/");
            float baseline;
            //竖向写文字,i为行数,j为文字个数
            int len = str.length;
            for (int i = 0; i < len; i++) {
                String text = str[i];
                for (int j = 0; j < text.length(); j++) {
                    RectF rectf = new RectF(w * (len - 1 - i), h * j, w * (len - i), h * (j + 1));
                    baseline = getTextBaseLine(rectf);
                    String tempText = String.valueOf(text.charAt(j));
                    canvas.drawText(tempText, rectf.centerX(), baseline, textPaint);
                }
            }
        }
    }

    private String toChinese(String str) {
        String[] s1 = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        String[] s2 = {"十", "百", "千", "万", "十", "百", "千", "亿", "十", "百", "千"};
        String result = "";
        int n = str.length();
        for (int i = 0; i < n; i++) {
            int num = str.charAt(i) - '0';
            if (i != n - 1 && num != 0) {
                result += s1[num] + s2[n - 2 - i];
            } else {
                result += s1[num];
            }
            System.out.println("  " + result);
        }
        System.out.println(result);
        return result;
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

}
