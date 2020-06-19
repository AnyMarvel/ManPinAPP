package com.mp.android.apps.livevblank;

import android.content.Intent;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;


import com.mp.android.apps.R;
import com.mp.android.apps.StoryboardActivity;
import com.mp.android.apps.livevblank.ViewPager.MyTransformation;
import com.mp.android.apps.livevblank.bean.CallHandlerParameter;
import com.mp.android.apps.livevblank.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;

public class ChoiceItemActivity extends StoryboardActivity {

    private int pagerWidth;
    private List<ImageView> imageViewList;
    private CallHandlerParameter mParameter = new CallHandlerParameter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choice_item);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        imageViewList = new ArrayList<>();
        /**
         * 为imageview生成的带倒影的bitmap
         */
        ImageView pic_template1 = new ImageView(ChoiceItemActivity.this);
        pic_template1.setImageBitmap(ImageUtil.getReverseBitmapById(R.drawable.pic_template1, ChoiceItemActivity.this));
        ImageView pic_template2 = new ImageView(ChoiceItemActivity.this);
        pic_template2.setImageBitmap(ImageUtil.getReverseBitmapById(R.drawable.pic_template2, ChoiceItemActivity.this));
        ImageView pic_template3 = new ImageView(ChoiceItemActivity.this);
        pic_template3.setImageBitmap(ImageUtil.getReverseBitmapById(R.drawable.pic_template3, ChoiceItemActivity.this));
        ImageView pic_template4 = new ImageView(ChoiceItemActivity.this);
        pic_template4.setImageBitmap(ImageUtil.getReverseBitmapById(R.drawable.pic_template4, ChoiceItemActivity.this));
        ImageView pic_template5 = new ImageView(ChoiceItemActivity.this);
        pic_template5.setImageBitmap(ImageUtil.getReverseBitmapById(R.drawable.pic_template5, ChoiceItemActivity.this));
        ImageView pic_template6 = new ImageView(ChoiceItemActivity.this);
        pic_template6.setImageBitmap(ImageUtil.getReverseBitmapById(R.drawable.pic_template6, ChoiceItemActivity.this));
        ImageView pic_template7 = new ImageView(ChoiceItemActivity.this);
        pic_template7.setImageBitmap(ImageUtil.getReverseBitmapById(R.drawable.pic_template7, ChoiceItemActivity.this));
        ImageView pic_template8 = new ImageView(ChoiceItemActivity.this);
        pic_template8.setImageBitmap(ImageUtil.getReverseBitmapById(R.drawable.pic_template8, ChoiceItemActivity.this));
        ImageView pic_template9 = new ImageView(ChoiceItemActivity.this);
        pic_template9.setImageBitmap(ImageUtil.getReverseBitmapById(R.drawable.pic_template9, ChoiceItemActivity.this));
        imageViewList.add(pic_template1);
        imageViewList.add(pic_template2);
        imageViewList.add(pic_template3);
        imageViewList.add(pic_template4);
        imageViewList.add(pic_template5);
        imageViewList.add(pic_template6);
        imageViewList.add(pic_template7);
        imageViewList.add(pic_template8);
        imageViewList.add(pic_template9);
        viewPager.setOffscreenPageLimit(imageViewList.size());
        pagerWidth = (int) (getResources().getDisplayMetrics().widthPixels * 3.0f / 5.0f);
        ViewGroup.LayoutParams lp = viewPager.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(pagerWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            lp.width = pagerWidth;
        }
        viewPager.setLayoutParams(lp);
        viewPager.setPageMargin(-50);
        findViewById(R.id.choice_main).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return viewPager.dispatchTouchEvent(motionEvent);
            }
        });
        viewPager.setPageTransformer(true, new MyTransformation());
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return imageViewList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(imageViewList.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ImageView imageView = imageViewList.get(position);
                mParameter.themeName = Constants.TEMPLATES[position];
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toCardEditor(position);
                    }
                });
                container.addView(imageView, position);
                return imageView;
            }
        });
    }

    private void toCardEditor(int mCurrentPosition) {
        this.mParameter.themeName = Constants.TEMPLATES[mCurrentPosition];
        Constants.setCurrentTemplate(this.mParameter.themeName);
        Intent intent = new Intent(this, EditCardActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        overridePendingTransition(0,0);
        finish();
        return super.onKeyDown(keyCode, event);
    }
}
