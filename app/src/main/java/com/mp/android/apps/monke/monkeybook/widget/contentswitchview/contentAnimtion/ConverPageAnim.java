package com.mp.android.apps.monke.monkeybook.widget.contentswitchview.contentAnimtion;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import com.mp.android.apps.monke.monkeybook.ReadBookControl;
import com.mp.android.apps.monke.monkeybook.widget.contentswitchview.BookContentView;
import com.mp.android.apps.monke.monkeybook.widget.contentswitchview.ContentSwitchView;

import java.util.List;

public class ConverPageAnim extends HorizonPageAnim {
    float startX = -1;
    private Context context;

    public ConverPageAnim(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, onLayoutStatus onLayoutStatus, List<BookContentView> viewContents, ContentSwitchView contentSwitchView, ContentSwitchView.LoadDataListener loadDataListener) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                if (viewContents.size() > 1) {
                    if (startX == -1) {
                        startX = event.getX();
                    }
                    int durX = (int) (event.getX() - startX);

                    if (durX > 0 && (onLayoutStatus.preAndNext() || onLayoutStatus.onlyPre())) {
                        int tempX = durX - onLayoutStatus.getScreenWidth();
                        if (tempX < -onLayoutStatus.getScreenWidth())
                            tempX = -onLayoutStatus.getScreenWidth();
                        else if (tempX > 0)
                            tempX = 0;
                        viewContents.get(0).layout(tempX, viewContents.get(0).getTop(), tempX + onLayoutStatus.getScreenWidth(), viewContents.get(0).getBottom());

                    } else if (durX < 0 && (onLayoutStatus.preAndNext() || onLayoutStatus.onlyNext())) {
                        int tempX = durX;
                        if (tempX > 0)
                            tempX = 0;
                        else if (tempX < -onLayoutStatus.getScreenWidth())
                            tempX = -onLayoutStatus.getScreenWidth();
                        int tempIndex = (onLayoutStatus.preAndNext() ? 1 : 0);
                        viewContents.get(tempIndex).layout(tempX, viewContents.get(tempIndex).getTop(), tempX + onLayoutStatus.getScreenWidth(), viewContents.get(tempIndex).getBottom());
                    }
                }

                break;
            case MotionEvent.ACTION_CANCEL:  //小米8长按传送门会引导手势进入action_cancel
            case MotionEvent.ACTION_UP:
                int durWidth = onLayoutStatus.getScreenWidth() > 1400 ? 10 : 0;  //当分辨率过大时，添加横向滑动冗余值

                if (event.getX() - startX > durWidth) {
                    if (onLayoutStatus.preAndNext() || onLayoutStatus.onlyPre()) {
                        //注意冗余值
                        if (event.getX() - startX + durWidth > scrollX) {
                            //向前翻页成功
                            initMoveSuccessAnim(viewContents.get(0), 0, onLayoutStatus, viewContents, contentSwitchView, loadDataListener);
                        } else {
                            initMoveFailAnim(viewContents.get(0), -onLayoutStatus.getScreenWidth(), onLayoutStatus);
                        }
                    } else {
                        //没有上一页
                        noPre();
                    }
                } else if (event.getX() - startX < -durWidth) {
                    if (onLayoutStatus.preAndNext() || onLayoutStatus.onlyNext()) {
                        int tempIndex = (onLayoutStatus.preAndNext() ? 1 : 0);
                        //注意冗余值
                        if (startX - event.getX() - durWidth > scrollX) {
                            //向后翻页成功
                            initMoveSuccessAnim(viewContents.get(tempIndex), -onLayoutStatus.getScreenWidth(), onLayoutStatus, viewContents, contentSwitchView, loadDataListener);
                        } else {
                            initMoveFailAnim(viewContents.get(tempIndex), 0, onLayoutStatus);
                        }
                    } else {
                        //没有下一页
                        noNext();
                    }
                } else {
                    //点击事件
                    if (ReadBookControl.getInstance().getCanClickTurn() && event.getX() <= onLayoutStatus.getScreenWidth() / 3) {
                        //点击向前翻页
                        if (onLayoutStatus.preAndNext() || onLayoutStatus.onlyPre()) {
                            initMoveSuccessAnim(viewContents.get(0), 0, onLayoutStatus, viewContents, contentSwitchView, loadDataListener);
                        } else {
                            noPre();
                        }
                    } else if (ReadBookControl.getInstance().getCanClickTurn() && event.getX() >= onLayoutStatus.getScreenWidth() / 3 * 2) {
                        //点击向后翻页
                        if (onLayoutStatus.preAndNext() || onLayoutStatus.onlyNext()) {
                            int tempIndex = (onLayoutStatus.preAndNext() ? 1 : 0);
                            initMoveSuccessAnim(viewContents.get(tempIndex), -onLayoutStatus.getScreenWidth(), onLayoutStatus, viewContents, contentSwitchView, loadDataListener);
                        } else {
                            noNext();
                        }
                    }
                }
                break;
        }
        return false;
    }


    private final long animDuration = 300;

    public void initMoveSuccessAnim(final View view, final int orderX, onLayoutStatus onLayoutStatus,
                                    List<BookContentView> viewContents, ContentSwitchView contentSwitchView,
                                    ContentSwitchView.LoadDataListener loadDataListener) {
        if (null != view) {
            long temp = Math.abs(view.getLeft() - orderX) / (onLayoutStatus.getScreenWidth() / animDuration);
            ValueAnimator tempAnim = ValueAnimator.ofInt(view.getLeft(), orderX).setDuration(temp);
            tempAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (null != view) {
                        int value = (int) animation.getAnimatedValue();
                        view.layout(value, view.getTop(), value + onLayoutStatus.getScreenWidth(), view.getBottom());
                    }
                }
            });
            tempAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    onLayoutStatus.onAnimationEnd(animation, orderX);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            tempAnim.start();
        }
    }

    private void initMoveFailAnim(final View view, int orderX, onLayoutStatus onLayoutStatus) {
        if (null != view) {
            long temp = Math.abs(view.getLeft() - orderX) / (onLayoutStatus.getScreenWidth() / animDuration);
            ValueAnimator tempAnim = ValueAnimator.ofInt(view.getLeft(), orderX).setDuration(temp);
            tempAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (null != view) {
                        int value = (int) animation.getAnimatedValue();
                        view.layout(value, view.getTop(), value + onLayoutStatus.getScreenWidth(), view.getBottom());
                    }
                }
            });
            tempAnim.start();
        }
    }


}

