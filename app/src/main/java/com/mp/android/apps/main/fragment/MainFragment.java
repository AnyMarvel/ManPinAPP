package com.mp.android.apps.main.fragment;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.google.android.apps.photolab.storyboard.activity.ComicSplash;
import com.mp.android.apps.R;
import com.mp.android.apps.explore.ExploreSquareActivity;
import com.mp.android.apps.livevblank.ChoiceItemActivity;
import com.mp.android.apps.login.LoginActivity;
import com.mp.android.apps.login.utils.LoginManager;
import com.mp.android.apps.main.MainActivity;
import com.mp.android.apps.main.cycleimage.BannerInfo;
import com.mp.android.apps.main.cycleimage.CycleViewPager;
import com.mp.android.apps.main.model.IMainFragmentModelImpl;
import com.mp.android.apps.main.view.MyImageTextView;
import com.mp.android.apps.monke.basemvplib.impl.BaseFragment;
import com.mp.android.apps.monke.monkeybook.base.observer.SimpleObserver;
import com.mp.android.apps.monke.monkeybook.bean.LibraryBean;
import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;
import com.mp.android.apps.monke.monkeybook.presenter.ILibraryPresenter;
import com.mp.android.apps.monke.monkeybook.presenter.impl.BookDetailPresenterImpl;
import com.mp.android.apps.monke.monkeybook.presenter.impl.LibraryPresenterImpl;
import com.mp.android.apps.monke.monkeybook.view.ILibraryView;
import com.mp.android.apps.monke.monkeybook.view.impl.BookDetailActivity;
import com.mp.android.apps.monke.monkeybook.view.impl.BookMainActivity;
import com.mp.android.apps.monke.monkeybook.view.impl.ChoiceBookActivity;
import com.mp.android.apps.monke.monkeybook.view.impl.SearchActivity;
import com.mp.android.apps.monke.monkeybook.widget.libraryview.LibraryKindBookListView;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainFragment extends BaseFragment<ILibraryPresenter> implements ILibraryView, View.OnClickListener {
    /**
     * 模拟请求后得到的数据
     */
    List<BannerInfo> mList = new ArrayList<>();
    /**
     * 轮播图
     */
    CycleViewPager mCycleViewPager;
    private LibraryKindBookListView lkbvKindbooklist;
    private MyImageTextView dongman;
    private MyImageTextView mingxinpian;
    private MyImageTextView xiaoshuo;
    private MyImageTextView guangchang;
    private FrameLayout searchImage;

    @Override
    protected ILibraryPresenter initInjector() {
        return new LibraryPresenterImpl();
    }

    /**
     * 初始化View
     */
    @Override
    protected void bindView() {
        super.bindView();
        mCycleViewPager = (CycleViewPager) view.findViewById(R.id.cycle_view);
        //设置选中和未选中时的图片
        assert mCycleViewPager != null;
        mCycleViewPager.setIndicators(R.mipmap.ad_select, R.mipmap.ad_unselect);
        //设置轮播间隔时间，默认为4000
        mCycleViewPager.setDelay(2000);
        mCycleViewPager.setData(mList, mAdCycleViewListener);
        lkbvKindbooklist = (LibraryKindBookListView) view.findViewById(R.id.lkbv_kindbooklist);
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

    /**
     * 轮播图点击监听
     */
    private CycleViewPager.ImageCycleViewListener mAdCycleViewListener = new CycleViewPager.ImageCycleViewListener() {

        @Override
        public void onImageClick(BannerInfo info, int position, View imageView) {

            if (mCycleViewPager.isCycle()) {
                position = position - 1;
            }
//            Toast.makeText(getActivity(), info.getTitle() + "选择了--" + position, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void bindEvent() {
        super.bindEvent();
        mPresenter.getLibraryData();

    }

    /**
     * 初始化数据
     */
    @Override
    protected void initData() {
        mList.clear();
        mList.add(new BannerInfo("", "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1591101217238&di=3ceb9a70573c3da62c42579d111c6319&imgtype=0&src=http%3A%2F%2Fattach.bbs.miui.com%2Fforum%2F201401%2F04%2F114458foyo99odqb8qjzg4.jpg"));
        IMainFragmentModelImpl.getInstance().getCycleImages().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<String>() {
            @Override
            public void onNext(String s) {
                List<String> list = (List<String>) JSON.parseObject(s).get("data");
                if (list != null && list.size() > 0) {
                    mList.clear();
                    for (int i = 0; i < list.size(); i++) {
                        mList.add(new BannerInfo("", list.get(i)));
                    }
                    mCycleViewPager.setData(mList, mAdCycleViewListener);
                    mCycleViewPager.refreshData();
                }
            }

            @Override
            public void onError(Throwable e) {

            }
        });

    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.main_fragment_layout, container, false);
    }


    @Override
    public void updateUI(LibraryBean library) {

        lkbvKindbooklist.updateData(library.getKindBooks(), new LibraryKindBookListView.OnItemListener() {
            @Override
            public void onClickMore(String title, String url) {
                ChoiceBookActivity.startChoiceBookActivity(getActivity(), title, url);
            }

            @Override
            public void onClickBook(ImageView animView, SearchBookBean searchBookBean) {
                Intent intent = new Intent(getActivity(), BookDetailActivity.class);
                intent.putExtra("from", BookDetailPresenterImpl.FROM_SEARCH);
                intent.putExtra("data", searchBookBean);
                intent.putExtra("start_with_share_ele", true);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity(), view, "img_cover").toBundle());

            }
        });

    }

    @Override
    public void finishRefresh() {

    }

    @Override
    public void updateNav(LinkedHashMap<String, String> linkedList) {

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


}
