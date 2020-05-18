package com.mp.android.apps.login;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import com.mp.android.apps.MainActivity;
import com.mp.android.apps.R;
import com.mp.android.apps.explore.ExploreSquareActivity;
import com.mp.android.apps.livevblank.view.IconButton;
import com.victor.loading.rotate.RotateLoading;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class LoginMainFragment extends LoginBaseFragment {
    FrameLayout fl_images_container;
    ImageView iv_login1;
    ImageView iv_login2;
    ImageView iv_login3;
    ImageView iv_login4;
    private ArrayList<AnimatorSet> mAnimatorSets = new ArrayList(4);
    private AnimatorHandler mHandler;
    private ArrayList<ImageView> mImageViews = new ArrayList();
    private int mIndex;
    private FrameLayout.LayoutParams mParams;
    private AnimatorSet mSet1;
    private AnimatorSet mSet2;
    private AnimatorSet mSet3;
    private AnimatorSet mSet4;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private int mTransDistance;
    TextView tv_sentence;
    private IconButton loginButton;
    private IconButton registerButton;

    private LoginFragemnt loginFragemnt;
    private RegisterFragment registerFragment;


    class AnimatorHandler extends Handler {
        private WeakReference<LoginMainFragment> mFragment;

        public AnimatorHandler(LoginMainFragment activity) {
            mFragment = new WeakReference(activity);
        }

        public void handleMessage(Message msg) {
            LoginMainFragment fragment = (LoginMainFragment) mFragment.get();
            if (fragment != null) {
                ((AnimatorSet) fragment.mAnimatorSets.get(msg.what)).start();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment_main, container, false);
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
        initView(view);
        return view;
    }


    private void initView(View view) {
        tv_sentence = (TextView) view.findViewById(R.id.tv_sentence);
        fl_images_container = (FrameLayout) view.findViewById(R.id.fl_images_container);
        loginButton = (IconButton) view.findViewById(R.id.login_button);
        registerButton = (IconButton) view.findViewById(R.id.register_button);
        loginFragemnt = new LoginFragemnt();
        registerFragment = new RegisterFragment();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                replaceLoginFragment(loginFragemnt);
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceLoginFragment(registerFragment);
            }
        });

        initAnim();
        palyAnimation();
        OnClickListener(view, getActivity());
    }


    private void initAnim() {
        int width = getResources().getDrawable(R.mipmap.login_bg1).getIntrinsicWidth();
        if (1071 == width) {
            width = 1224;
        }
        mParams = new FrameLayout.LayoutParams(width, width);
        mTransDistance = getTransDistance(width);
        iv_login1 = new ImageView(this.getContext());
        iv_login1.setImageResource(R.mipmap.login_bg1);
        iv_login1.setBackgroundColor(getResources().getColor(R.color.white));
        iv_login2 = new ImageView(this.getContext());
        iv_login2.setImageResource(R.mipmap.login_bg2);
        iv_login2.setBackgroundColor(getResources().getColor(R.color.white));
        iv_login3 = new ImageView(this.getContext());
        iv_login3.setImageResource(R.mipmap.login_bg3);
        iv_login3.setBackgroundColor(getResources().getColor(R.color.white));
        iv_login4 = new ImageView(this.getContext());
        iv_login4.setImageResource(R.mipmap.login_bg4);
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

    private int getTransDistance(int imageWidth) {
        DisplayMetrics metrics = new DisplayMetrics();
        this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels - imageWidth;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Iterator it = mImageViews.iterator();
        while (it.hasNext()) {
            ((ImageView) it.next()).setImageResource(0);
        }
        mTimer.cancel();
        mTimer.purge();
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

    @Override
    public boolean onBackPressed() {
        Objects.requireNonNull(getActivity()).setResult(-1);
        getActivity().overridePendingTransition(0, 0);
        getActivity().finish();
        return true;
    }
}
