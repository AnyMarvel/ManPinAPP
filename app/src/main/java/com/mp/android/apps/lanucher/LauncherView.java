package com.mp.android.apps.lanucher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mp.android.apps.R;


/**
 * LauncherView简介
 * 启动动画,基于贝塞尔曲线绘制动画
 *
 * @author lijuntao
 * @date 2019-1-15 15:10:39
 *
 * 弃用贝塞尔曲线绘制界面
 */
@Deprecated
public class LauncherView extends RelativeLayout {
    private int mHeight;
    private int mWidth;
    private boolean mHasStart;

    public LauncherView(Context context) {
        super(context);

    }

    public LauncherView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LauncherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    Path redPath1, purplePath1, yellowPath1, bluePath1;

    private void initPath() {
        redPath1 = new Path(); //偏移坐标
        redPath1.moveTo(mWidth / 2, mHeight / 2);
        redPath1.lineTo(mWidth / 10, mHeight / 2);
        redPath1.cubicTo(-mWidth / 3, 0, mWidth / 2 + mWidth / 5 * 1, mHeight / 2 - mHeight / 3 * 2, mWidth / 2, mHeight / 2);

        purplePath1 = new Path(); //偏移坐标
        purplePath1.moveTo(mWidth / 2, mHeight / 2);
        purplePath1.lineTo(mWidth / 10 * 3, mHeight / 2);
        purplePath1.cubicTo(mWidth / 5, 0, mWidth / 2 + mWidth / 5 * 3, mHeight / 2 - mHeight / 9 * 5, mWidth / 2, mHeight / 2);

        yellowPath1 = new Path(); //偏移坐标
        yellowPath1.moveTo(mWidth / 2, mHeight / 2);
        yellowPath1.lineTo(mWidth / 10 * 6, mHeight / 2);
        yellowPath1.cubicTo(mWidth / 10 * 7, mHeight, -mWidth / 3, mHeight, mWidth / 2, mHeight / 2);

        bluePath1 = new Path(); //偏移坐标
        bluePath1.moveTo(mWidth / 2, mHeight / 2);
        bluePath1.lineTo(mWidth / 10 * 9, mHeight / 2);
        bluePath1.cubicTo(mWidth / 10 * 9, mHeight / 5 * 6, mWidth / 5 * 2, mHeight, mWidth / 2, mHeight / 2);

    }

    ImageView red, purple, yellow, blue;

    private void init() {
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(CENTER_HORIZONTAL, TRUE);//这里的TRUE 要注意 不是true
        lp.addRule(CENTER_VERTICAL, TRUE);

        purple = new ImageView(getContext());
        purple.setLayoutParams(lp);
        purple.setImageResource(R.drawable.shape_circle_purple);
        addView(purple);

        yellow = new ImageView(getContext());
        yellow.setLayoutParams(lp);
        yellow.setImageResource(R.drawable.shape_circle_yellow);
        addView(yellow);

        blue = new ImageView(getContext());
        blue.setLayoutParams(lp);
        blue.setImageResource(R.drawable.shape_circle_blue);
        addView(blue);

        red = new ImageView(getContext());
        red.setLayoutParams(lp);
        red.setImageResource(R.drawable.shape_circle_red);
        addView(red);

        mHeight = getResources().getDisplayMetrics().heightPixels;
        mWidth = getResources().getDisplayMetrics().widthPixels;
    }


    public void start(lanucherFinshImpl lanucherFinsh) {
        init();
        initPath();
        setAnimation(red, redPath1);
        setAnimation(purple, purplePath1);
        setAnimation(yellow, yellowPath1);
        setAnimation(blue, bluePath1);
        redAll.start();
        yellowAll.start();
        purpleAll.start();
        blueAll.start();
        blueAll.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                showLogo(lanucherFinsh);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void setAnimation(final ImageView target, Path path1) {
        //路径
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(target, target.X, target.Y, path1);
        anim1.setInterpolator(new AccelerateDecelerateInterpolator());
        anim1.setDuration(2600);
        //组合添加缩放透明效果
        addAnimation(anim1, target);
    }


    AnimatorSet redAll, purpleAll, yellowAll, blueAll;

    private void addAnimation(ObjectAnimator animator1, final ImageView target) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1, 1000);
        valueAnimator.setDuration(1800);
        valueAnimator.setStartDelay(1000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                float alpha = 1 - value / 2000;
                float scale = getScale(target) - 1;
                if (value <= 500) {
                    scale = 1 + (value / 500) * scale;
                } else {
                    scale = 1 + ((1000 - value) / 500) * scale;
                }
                target.setScaleX(scale);
                target.setScaleY(scale);
                target.setAlpha(alpha);
            }
        });
        valueAnimator.addListener(new AnimEndListener(target));
        if (target == red) {
            redAll = new AnimatorSet();
            redAll.playTogether(animator1, valueAnimator);
        }
        if (target == blue) {
            blueAll = new AnimatorSet();
            blueAll.playTogether(animator1, valueAnimator);
        }
        if (target == purple) {
            purpleAll = new AnimatorSet();
            purpleAll.playTogether(animator1, valueAnimator);
        }
        if (target == yellow) {
            yellowAll = new AnimatorSet();
            yellowAll.playTogether(animator1, valueAnimator);
        }

    }


    private float getScale(ImageView target) {
        if (target == red) {
            return 3.0f;
        }
        if (target == purple) {
            return 2.0f;
        }
        if (target == yellow) {
            return 4.5f;
        }
        if (target == blue) {
            return 3.5f;
        }
        return 2f;
    }


    private void showLogo(lanucherFinshImpl lanucherFinsh) {
        View view = View.inflate(getContext(), R.layout.widget_load_view, this);
        View logo = view.findViewById(R.id.iv_logo);
        final View slogo = view.findViewById(R.id.iv_slogo);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(logo, View.ALPHA, 0f, 1f);
        alpha.setDuration(800);

        alpha.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator alpha = ObjectAnimator.ofFloat(slogo, View.ALPHA, 0f, 1f);
                alpha.setDuration(200);
                alpha.start();
                alpha.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        lanucherFinsh.hasFinished();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }
        }, 400);
    }

    private class AnimEndListener extends AnimatorListenerAdapter {
        private View target;

        public AnimEndListener(View target) {
            this.target = target;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            removeView((target));
        }
    }


}


