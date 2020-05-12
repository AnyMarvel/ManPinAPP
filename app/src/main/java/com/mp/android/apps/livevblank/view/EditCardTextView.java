package com.mp.android.apps.livevblank.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;


import com.mp.android.apps.R;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import taobe.tec.jcc.JChineseConvertor;

public class EditCardTextView extends View {

    public static final int START_LEFT = 1;
    public static final int START_RIGHT = 2;

    public static final int VERTICAL = 1;
    public static final int HORIZONTAL = 2;

    /**
     * 1. 竖直 左侧
     * 2. 竖直 右侧
     * 3. 水平 左侧
     */


    @IntDef({START_LEFT, START_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface START_ORIENTATION {
    }

    @IntDef({VERTICAL, HORIZONTAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LINE_ORIENTATION {
    }

    /**
     * TextView默认文字大小
     */
    private float textSize = 16;
    /**
     * TextView默认文字颜色
     */
    private int textColor = Color.BLACK;
    /**
     * TextView默认文字内容
     */
    private String text = "";
    /**
     * 文字开始位置,默认从左侧开始
     */
    private int startOrientation = START_LEFT;
    /**
     * 划线方向,横向or竖直方向 默认为横向
     */
    private int lineOrientation = HORIZONTAL;
    /**
     * 划线宽度
     */
    private float lineWidth = dip2px(getContext(), 0.5f);
    /**
     * 划线颜色
     */
    private int lineColor = Color.BLACK;
    /**
     * 换行标识符号
     */
    private String cutChars = "\n";
    /**
     * 文字横向间距
     */
    private float textHorizontalMargin = dip2px(getContext(), 2);
    /**
     * 文字纵向间距
     */
    private float textVerticalMargin = dip2px(getContext(), 8);
    /**
     * 文字到的线之间的间距
     */
    private float line2TextMargin = -1;

    /**
     * maxRow 显示有且仅有3行
     */
    private int maxRow = 3;
    /**
     * maxChar 最多出现的字符数,包含空格和字符
     */
    private int maxChar = 48;

    /**
     * colNumber 每行由多少个字符
     */
    private int colNumber = 16;

    Paint paint;
    int width;
    int height = -1;

    public EditCardTextView(Context context) {
        super(context);
        init();
    }

    public EditCardTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EditCardTextView);
        int count = typedArray.getIndexCount();
        for (int i = 0; i < count; i++) {
            int index = typedArray.getIndex(i);
            if (index == R.styleable.EditCardTextView_v_start) {
                startOrientation = typedArray.getInt(index, START_LEFT);
            } else if (index == R.styleable.EditCardTextView_v_text) {
                text = typedArray.getString(index);
            } else if (index == R.styleable.EditCardTextView_v_textColor) {
                textColor = typedArray.getColor(index, Color.BLACK);
            } else if (index == R.styleable.EditCardTextView_v_textSize) {
                textSize = typedArray.getDimension(index, 16);
            } else if (index == R.styleable.EditCardTextView_v_cutChars) {
                cutChars = typedArray.getString(index);
            } else if (index == R.styleable.EditCardTextView_v_textVerticalMargin) {
                textVerticalMargin = typedArray.getDimension(index, textVerticalMargin);
            } else if (index == R.styleable.EditCardTextView_v_textHorizontalMargin) {
                textHorizontalMargin = typedArray.getDimension(index, textHorizontalMargin);
            } else if (index == R.styleable.EditCardTextView_v_line) {
                lineOrientation = typedArray.getInt(index, HORIZONTAL);
            } else if (index == R.styleable.EditCardTextView_v_lineWidth) {
                lineWidth = typedArray.getDimension(index, lineWidth);
            } else if (index == R.styleable.EditCardTextView_v_lineColor) {
                lineColor = typedArray.getColor(index, Color.BLACK);
            } else if (index == R.styleable.EditCardTextView_v_line2TextMargin) {
                line2TextMargin = textHorizontalMargin / 2 + lineWidth / 2 - typedArray.getDimension(index, 0);
            }
        }
        if (lineOrientation == VERTICAL) {
            maxRow = 5;
            colNumber = 9;
            textHorizontalMargin = dip2px(getContext(), 8);
            textVerticalMargin = dip2px(getContext(), 1);
        } else {
            maxRow = 3;
            colNumber = 16;
            textHorizontalMargin = dip2px(getContext(), 2);
            textVerticalMargin = dip2px(getContext(), 8);
        }


        init();
    }

    private void init() {
        paint = new Paint();
        if (textSize > 0) {
            paint.setTextSize(textSize);
        }
        paint.setColor(textColor);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (lineOrientation == HORIZONTAL) {
            width = (int) getOneWordWidth() * colNumber;
            height = (int) (getOneWordHeight() + lineWidth) * maxRow;
        } else {
            width = (int) (getOneWordWidth()) * maxRow;
            height = (int) getOneWordHeight() * colNumber;
        }
        setMeasuredDimension(width, height);
    }

    public void setLine2TextMargin(float line2TextMargin) {

        this.line2TextMargin = textHorizontalMargin / 2 + lineWidth / 2 - line2TextMargin;
        invalidate();
    }

    public void setStartOrientation(@LINE_ORIENTATION int startOrientation) {
        this.startOrientation = startOrientation;
        invalidate();
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
        invalidate();
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
        invalidate();
    }

    public void setTypeface(Typeface typeface) {
        paint.setTypeface(typeface);
        invalidate();
    }

    public void setTextHorizontalMargin(float textHorizontalMargin) {
        this.textHorizontalMargin = textHorizontalMargin;
        invalidate();
    }

    public void setTextVerticalMargin(float textVerticalMargin) {
        this.textVerticalMargin = textVerticalMargin;
        invalidate();
    }

    public void setLineOrientation(@LINE_ORIENTATION int lineOrientation) {
        this.lineOrientation = lineOrientation;
        invalidate();
    }

    public void setCutChars(String cutChars) {
        this.cutChars = cutChars;
        invalidate();
    }

    /**
     * 设置文字尺寸
     *
     * @param textSize
     */
    public void setTextSize(float textSize) {
        this.textSize = textSize;
        invalidate();
    }

    /**
     * 设置文字颜色
     *
     * @param textColor
     */
    public void setTextColor(int textColor) {
        this.textColor = textColor;
        invalidate();
    }

    /**
     * 设置文字
     *
     * @param text
     */
    public void setText(String text) {
        this.text = text;
        invalidate();
    }

    public String getText() {
        return text;
    }

    /**
     * 设置文字起始方向
     *
     * @param startOrientation
     */
    public void setStart(@START_ORIENTATION int startOrientation) {
        this.startOrientation = startOrientation;
        invalidate();
    }


    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {

            result = specSize;
        } else {
            return (int) measureTextWidth();
        }
        return result;
    }

    /**
     * 测量TextView的宽高,如果有明确值则使用明确值(具体值或match_parent),否则基于测量值(wrap_content)
     *
     * @param measureSpec
     * @return
     */
    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = (int) (getOneWordHeight() * text.length());
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private float measureTextWidth() {
        if (getColNum() == 1) {
            return getOneWordWidth() + getPaddingLeft() + getPaddingRight();
        }

        return getOneWordWidth() * getColNum() + getPaddingLeft() + getPaddingRight();
    }

    private float getTextBaseLine(RectF rect) {
        Paint.FontMetricsInt metricsInt = paint.getFontMetricsInt();
        return (rect.top + rect.bottom - metricsInt.top - metricsInt.bottom) / 2;
    }


    /**
     * @return 返回总行数
     */
    private int getColNum() {

        int colNum = 0;
        if (cutChars != null) {
            String[] textArray = text.split(cutChars);
            for (int i = 0; i < textArray.length; i++) {
                if (textArray[i].length() > colNumber) {
                    colNum += textArray[i].length() / colNumber;
                    if (textArray[i].length() % colNumber > 0) {
                        colNum++;
                    }
                } else {
                    colNum++;
                }
            }
        } else {
            colNum = text.length() / colNumber;
            if (text.length() % colNumber > 0) {
                colNum++;
            }
        }


        return colNum;
    }

    /**
     * 获取单个文字宽度
     *
     * @return
     */
    private float getOneWordWidth() {
        return paint.measureText("我") + textHorizontalMargin;
    }

    /**
     * 获取单个文字高度
     *
     * @return
     */
    private float getOneWordHeight() {
        Rect rect = new Rect();
        paint.getTextBounds("我", 0, 1, rect);
        return rect.height() + textVerticalMargin;
    }


    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getOneWordWidth();
        float h = getOneWordHeight();
        paint.setColor(lineColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth);
        drawLine(w, h, canvas, paint);

        int colNum = getColNum();

        String[] cutCharArray = cutChars == null ? null : cutChars.split("|");

        if (text.length() > maxChar) {
            String textTemp = text.substring(0, maxChar);
            text = textTemp;
        }

        if (cutCharArray != null) {
            String[] textArray = text.split(cutChars);
            int stepCol = 0;
            int tempRow;
            if (textArray.length > 3) {
                tempRow = maxRow;
            } else {
                tempRow = textArray.length;
            }
            for (int n = 0; n < tempRow; n++) {
                String text = textArray[n];
                int currentCol = 0;
                for (int i = 0; i < text.length(); i++) {
                    String str = String.valueOf(text.charAt(i));
                    int currentRow = i % colNumber;
                    if (colNum == 1) {
                        currentRow = i;
                    }
                    if (colNum > 1) {
                        currentCol = stepCol + (i / colNumber);
                    }
                    drawText(w, h, currentCol, currentRow, str, canvas);
                    if (i + 1 == text.length()) {
                        stepCol = currentCol + 1;
                    }
                }
            }
        } else {
            int currentCol = 0;
            for (int i = 0; i < text.length(); i++) {
                String str = String.valueOf(text.charAt(i));
                int currentRow = i % colNumber;
                if (colNum == 1) {
                    currentRow = i;
                }
                if (colNum > 1) {
                    currentCol = (i) / colNumber;
                }
                if (currentCol < maxRow) {
                    drawText(w, h, currentCol, currentRow, str, canvas);
                }
            }
        }

    }

    /**
     * 绘画底部线条
     * 水平划线文字只能从左侧开始
     * 竖直划线文字可以从左侧或者右侧开始
     *
     * @param w
     * @param h
     * @param canvas
     * @param paint
     */
    private void drawLine(float w, float h, Canvas canvas, Paint paint) {
        if (lineOrientation == HORIZONTAL) {
            for (int i = 1; i <= maxRow; i++) {
                canvas.drawLine(0, h * i, width, h * i, paint);
            }
        } else {
            if (startOrientation == START_LEFT) {
                for (int i = 1; i <= maxRow; i++) {
                    canvas.drawLine(w * i, 0, w * i, height, paint);
                }
            } else {
                for (int i = 0; i < maxRow; i++) {
                    canvas.drawLine(w * i, 0, w * i, height, paint);
                }

            }
        }

    }


    /**
     * draText方法写入text文字
     *
     * @param w          一个字符view的宽
     * @param h          一个字符view的高
     * @param currentCol 当前第几行
     * @param currentRow 当前第几个字
     * @param tempStr        当前字符
     * @param canvas     画布
     */
    private void drawText(float w, float h, int currentCol, int currentRow, String tempStr, Canvas canvas) {
        RectF rectF;
        float baseline;
        String str = "";
        try {
            str = JChineseConvertor.getInstance().s2t(tempStr);
        } catch (IOException e) {
            e.printStackTrace();
            str = tempStr;
        }
        if (lineOrientation == HORIZONTAL) {
            rectF = new RectF(currentRow * w, currentCol * h, currentRow * w + w, currentCol * h + h);
            baseline = getTextBaseLine(rectF) + textVerticalMargin / 2;
        } else {
            if (startOrientation == START_LEFT) {
                rectF = new RectF(currentCol * w, currentRow * h, currentCol * w + w, currentRow * h + h);
            } else {
                rectF = new RectF((width - (currentCol + 1) * w), currentRow * h, (width - (currentCol + 1) * w) + w, currentRow * h + h);
            }
            baseline = getTextBaseLine(rectF);
        }
        paint.setColor(textColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(str, rectF.centerX(), baseline, paint);
        paint.setColor(lineColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth);
        if (line2TextMargin == -1) {
            line2TextMargin = lineWidth * 1f / 2;
        }
    }
}