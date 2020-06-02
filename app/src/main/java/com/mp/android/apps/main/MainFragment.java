package com.mp.android.apps.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mp.android.apps.R;
import com.mp.android.apps.main.cycleimage.BannerInfo;
import com.mp.android.apps.main.cycleimage.CycleViewPager;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {
    /**
     * 模拟请求后得到的数据
     */
    List<BannerInfo> mList = new ArrayList<>();
    /**
     * 轮播图
     */
    CycleViewPager mCycleViewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment_layout, container, false);
        initData();
        initView(view);

        return view;
    }

    /**
     * 初始化View
     */
    private void initView(View view) {
        mCycleViewPager = (CycleViewPager) view.findViewById(R.id.cycle_view);
        //设置选中和未选中时的图片
        assert mCycleViewPager != null;
        mCycleViewPager.setIndicators(R.mipmap.ad_select, R.mipmap.ad_unselect);
        //设置轮播间隔时间，默认为4000
        mCycleViewPager.setDelay(2000);
        mCycleViewPager.setData(mList, mAdCycleViewListener);
    }

    /**
     * 轮播图点击监听
     */
    private CycleViewPager.ImageCycleViewListener mAdCycleViewListener = new CycleViewPager.ImageCycleViewListener() {

        @Override
        public void onImageClick(BannerInfo info, int position, View imageView) {

            if (mCycleViewPager.isCycle()) {
                position = position - 1;
            }
            Toast.makeText(getContext(), info.getTitle() + "选择了--" + position, Toast.LENGTH_LONG).show();
        }
    };

    /**
     * 初始化数据
     */
    private void initData() {
        mList.add(new BannerInfo("标题1", "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1591101217238&di=3ceb9a70573c3da62c42579d111c6319&imgtype=0&src=http%3A%2F%2Fattach.bbs.miui.com%2Fforum%2F201401%2F04%2F114458foyo99odqb8qjzg4.jpg"));
        mList.add(new BannerInfo("标题2", "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1591101257866&di=42f88647f1fe83e9174a49a18c97c49a&imgtype=0&src=http%3A%2F%2Fb.hiphotos.baidu.com%2Fzhidao%2Fpic%2Fitem%2Fc2cec3fdfc03924547eae8438794a4c27d1e251a.jpg"));
        mList.add(new BannerInfo("标题3", "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=1379482854,1412074421&fm=26&gp=0.jpg"));
        mList.add(new BannerInfo("标题4", "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=3201077998,3110241921&fm=26&gp=0.jpg"));

    }

    public static View getImageView(Context context, String url) {
        RelativeLayout rl = new RelativeLayout(context);
        //添加一个ImageView，并加载图片
        View view = LayoutInflater.from(context).inflate(R.layout.main_layout_carousel_item, null, false);
        ImageView imageView = view.findViewById(R.id.carouselImageView);
//        ImageView imageView = new ImageView(context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
//        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        imageView.setLayoutParams(layoutParams);
        //使用Picasso来加载图片
        Glide.with(context).load(url).into(imageView);
        //在Imageview前添加一个半透明的黑色背景，防止文字和图片混在一起
        ImageView backGround = new ImageView(context);
        backGround.setLayoutParams(layoutParams);
        backGround.setBackgroundResource(R.color.cycle_image_bg);
        rl.addView(view);
//        rl.addView(backGround);
        return rl;
    }

}
