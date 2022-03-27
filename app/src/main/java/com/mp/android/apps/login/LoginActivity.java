package com.mp.android.apps.login;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.mp.android.apps.R;
import com.mp.android.apps.StoryboardActivity;
import com.mp.android.apps.login.fragment.imple.LoginForgetFragment;
import com.mp.android.apps.login.fragment.imple.LoginFragment;
import com.mp.android.apps.login.fragment.imple.LoginFragmentNormal;
import com.mp.android.apps.basemvplib.impl.BaseFragment;
import com.umeng.socialize.UMShareAPI;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends StoryboardActivity {
    FrameLayout fl_images_container;
    ImageView iv_login1;
    ImageView iv_login2;
    ImageView iv_login3;
    ImageView iv_login4;
    private FrameLayout.LayoutParams mParams;
    private int mTransDistance;
    private ArrayList<ImageView> mImageViews = new ArrayList();
    private ArrayList<AnimatorSet> mAnimatorSets = new ArrayList(4);
    private AnimatorSet mSet1;
    private AnimatorSet mSet2;
    private AnimatorSet mSet3;
    private AnimatorSet mSet4;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private AnimatorHandler mHandler;
    private int mIndex;


    private LoginFragment loginFragment;
    private LoginFragmentNormal loginFragmentNormal;
    private LoginForgetFragment loginForgetFragment;
    private ImageView iv_back;
    public static final int LOGINTYPE = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_main);
        loginFragmentNormal = new LoginFragmentNormal();
        loginFragment = new LoginFragment();
        loginForgetFragment = new LoginForgetFragment();
        showFragment(LOGINTYPE);

        fl_images_container = (FrameLayout) findViewById(R.id.fl_images_container);
        iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        mHandler = new AnimatorHandler(this);
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            public void run() {
                mHandler.sendEmptyMessage(mIndex);
                mIndex = mIndex + 1;
                if (4 == mIndex) {
                    mIndex = 0;
                }
            }
        };
        initAnim();
        palyAnimation();
    }

    private void hideFragment(FragmentTransaction transaction) {
        if (loginFragmentNormal != null) {
            transaction.hide(loginFragmentNormal);
        }
        if (loginFragment != null) {
            transaction.hide(loginFragment);
        }
        if (loginForgetFragment != null) {
            transaction.hide(loginForgetFragment);
        }
    }

    public void showFragment(int loginType) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragment(transaction);
        BaseFragment fragment = null;
        if (loginType == LOGINTYPE) {
            if (loginFragment != null) {
                fragment = loginFragment;
            }
        } else if (loginType == 1) {
            if (loginFragmentNormal != null) {
                fragment = loginFragmentNormal;
            }
        } else {
            if (loginForgetFragment != null) {
                fragment = loginForgetFragment;
            }
        }
        if (fragment != null) {
            if (fragment.isAdded()) {
                transaction.show(fragment);
            } else {
                transaction.add(R.id.login_container, fragment).show(fragment);
            }
            transaction.commit();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }


    public void startActivity() {
        setResult(0);
        overridePendingTransition(0, 0);
        finish();
    }

    private void initAnim() {
        int width = 1071;
        if (1071 == width) {
            width = 1224;
        }
        mParams = new FrameLayout.LayoutParams(width, width);
        mTransDistance = getTransDistance(width);
        iv_login1 = new ImageView(this);
        iv_login1.setBackgroundColor(getResources().getColor(R.color.white));
        iv_login2 = new ImageView(this);
        iv_login2.setBackgroundColor(getResources().getColor(R.color.white));
        iv_login3 = new ImageView(this);
        iv_login3.setBackgroundColor(getResources().getColor(R.color.white));
        iv_login4 = new ImageView(this);
        iv_login4.setBackgroundColor(getResources().getColor(R.color.white));
        mImageViews.add(iv_login4);
        mImageViews.add(iv_login3);
        mImageViews.add(iv_login2);
        mImageViews.add(iv_login1);
        for (int i = 0; i < 4; i++) {
            ((ImageView) mImageViews.get(i)).setAlpha(0.0f);
            fl_images_container.addView((View) mImageViews.get(i), mParams);
        }
    }

    private void palyAnimation() {
        mSet1 = getAnimatorSet(iv_login1);
        mAnimatorSets.add(mSet1);
        mSet2 = getAnimatorSet(iv_login2);
        mAnimatorSets.add(mSet2);
        mSet3 = getAnimatorSet(iv_login3);
        mAnimatorSets.add(mSet3);
        mSet4 = getAnimatorSet(iv_login4);
        mAnimatorSets.add(mSet4);
        mSet1.addListener(new BackgroundAnimationListener(fl_images_container, mParams, iv_login1));
        mSet2.addListener(new BackgroundAnimationListener(fl_images_container, mParams, iv_login2));
        mSet3.addListener(new BackgroundAnimationListener(fl_images_container, mParams, iv_login3));
        mSet4.addListener(new BackgroundAnimationListener(fl_images_container, mParams, iv_login4));
        mTimer.schedule(mTimerTask, 0, 2000);
    }

    private AnimatorSet getAnimatorSet(ImageView iv) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator translation = ObjectAnimator.ofFloat(iv, "x", new float[]{0.0f, (float) mTransDistance});
        translation.setDuration(4500);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(iv, "alpha", new float[]{0.0f, 1.0f});
        alpha.setDuration(500);
        ObjectAnimator alpha1 = ObjectAnimator.ofFloat(iv, "alpha", new float[]{1.0f, 0.0f});
        alpha1.setDuration(500);
        alpha1.setStartDelay(4000);
        set.play(translation).with(alpha);
        set.play(translation).with(alpha1);
        set.setInterpolator(new LinearInterpolator());
        return set;
    }

    private int getTransDistance(int imageWidth) {
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels - imageWidth;
    }

    static class BackgroundAnimationListener implements Animator.AnimatorListener {
        private FrameLayout mContainer;
        private ImageView mImageView;
        private FrameLayout.LayoutParams mParams;

        public BackgroundAnimationListener(FrameLayout container, FrameLayout.LayoutParams params, ImageView iv) {
            mContainer = container;
            mParams = params;
            mImageView = iv;
        }

        public void onAnimationStart(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            mContainer.removeView(mImageView);
            mImageView.setX(0.0f);
            mContainer.addView(mImageView, 0, mParams);
        }

        public void onAnimationCancel(Animator animation) {
        }

        public void onAnimationRepeat(Animator animation) {
        }
    }

    class AnimatorHandler extends Handler {
        private WeakReference<LoginActivity> mFragment;

        public AnimatorHandler(LoginActivity activity) {
            mFragment = new WeakReference(activity);
        }

        public void handleMessage(Message msg) {
            LoginActivity fragment = (LoginActivity) mFragment.get();
            if (fragment != null) {
                ((AnimatorSet) fragment.mAnimatorSets.get(msg.what)).start();
            }
        }
    }
}
