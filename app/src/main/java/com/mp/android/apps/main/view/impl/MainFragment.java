package com.mp.android.apps.main.view.impl;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.google.android.apps.photolab.storyboard.activity.ComicSplash;
import com.mp.android.apps.R;
import com.mp.android.apps.livevblank.ChoiceItemActivity;
import com.mp.android.apps.main.MainActivity;
import com.mp.android.apps.main.cycleimage.BannerInfo;
import com.mp.android.apps.main.cycleimage.CycleViewPager;
import com.mp.android.apps.main.presenter.impl.MainFragmentPresenterImpl;
import com.mp.android.apps.main.view.IMainfragmentView;
import com.mp.android.apps.main.view.MyImageTextView;
import com.mp.android.apps.monke.basemvplib.impl.BaseFragment;
import com.mp.android.apps.monke.monkeybook.view.impl.BookMainActivity;
import com.mp.android.apps.monke.monkeybook.view.impl.SearchActivity;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends BaseFragment<MainFragmentPresenterImpl> implements IMainfragmentView, View.OnClickListener {

    /**
     * 轮播图
     */
    public CycleViewPager mCycleViewPager;
    private MyImageTextView dongman;
    private MyImageTextView mingxinpian;
    private MyImageTextView xiaoshuo;
    private MyImageTextView guangchang;
    private FrameLayout searchImage;
    private RecyclerView recyclerView;

    @Override
    protected MainFragmentPresenterImpl initInjector() {
        return new MainFragmentPresenterImpl();
    }

    /**
     * 初始化View
     */
    @Override
    protected void bindView() {
        super.bindView();
        mCycleViewPager = (CycleViewPager) view.findViewById(R.id.cycle_view);
        //导航设置点击事件
        dongman = view.findViewById(R.id.huojian);
        dongman.setOnClickListener(this);
        mingxinpian = view.findViewById(R.id.jingxuan);
        mingxinpian.setOnClickListener(this);
        xiaoshuo = view.findViewById(R.id.xiaoshuo);
        xiaoshuo.setOnClickListener(this);
        guangchang = view.findViewById(R.id.guangchang);
        guangchang.setOnClickListener(this);
        searchImage = view.findViewById(R.id.search_image);
        searchImage.setOnClickListener(this);
    }


    @Override
    protected void bindEvent() {
        super.bindEvent();
    }

    /**
     * 初始化数据
     */
    @Override
    protected void initData() {
        mPresenter.initmCycleViewPager(mCycleViewPager);

        mPresenter.initHomeData();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.main_fragment_layout, container, false);
    }



    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.huojian:
                Acp.getInstance(getActivity()).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).build(), new AcpListener() {
                    @Override
                    public void onGranted() {
                        Intent intentComic = new Intent(getActivity(), ComicSplash.class);
                        startActivity(intentComic);
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                    }
                });

                break;
            case R.id.jingxuan:
                Intent intentPostcard = new Intent(getActivity(), ChoiceItemActivity.class);
                startActivity(intentPostcard);
                break;
            case R.id.xiaoshuo:
                Intent intentBook = new Intent(getActivity(), BookMainActivity.class);
                startActivity(intentBook);
                break;
            case R.id.guangchang:
                ((MainActivity) getActivity()).gotoExplore("广场");
                break;
            case R.id.search_image:
                Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
                startActivity(searchIntent);
                break;
            default:
                break;
        }
    }


    /**
     * 处理首页轮播图逻辑,轮播图基于下发
     *
     * @param carouselImages 轮播图数据源
     */
    @Override
    public void updatemCycleViewPager(List<String> carouselImages) {
        List<BannerInfo> mList = new ArrayList<>();
        if (carouselImages != null && carouselImages.size() > 0) {
            mList.clear();
            for (int i = 0; i < carouselImages.size(); i++) {
                mList.add(new BannerInfo("", carouselImages.get(i)));
            }
        } else {
            mList.clear();
            //兜底数据源
            mList.add(new BannerInfo("", "http://inews.gtimg.com/newsapp_bt/0/7749546706/1000/0"));
        }
        //设置选中和未选中时的图片
        assert mCycleViewPager != null;
        mCycleViewPager.setIndicators(R.mipmap.ad_select, R.mipmap.ad_unselect);
        mCycleViewPager.setDelay(2000);
        mCycleViewPager.setData(mList, new CycleViewPager.ImageCycleViewListener() {
            @Override
            public void onImageClick(BannerInfo info, int position, View imageView) {

                if (mCycleViewPager.isCycle()) {
                    position = position - 1;
                }
            }
        });
        mCycleViewPager.refreshData();
    }

}
