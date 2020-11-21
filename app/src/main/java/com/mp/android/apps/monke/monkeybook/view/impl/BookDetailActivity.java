
package com.mp.android.apps.monke.monkeybook.view.impl;

import android.content.Intent;
import android.os.Build;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.mp.android.apps.R;
import com.mp.android.apps.monke.monkeybook.base.MBaseActivity;
import com.mp.android.apps.monke.monkeybook.presenter.IBookDetailPresenter;
import com.mp.android.apps.monke.monkeybook.presenter.impl.BookDetailPresenterImpl;
import com.mp.android.apps.monke.monkeybook.utils.BlurTransformation;
import com.mp.android.apps.monke.monkeybook.view.IBookDetailView;
import com.mp.android.apps.monke.readActivity.ReadActivity;


public class BookDetailActivity extends MBaseActivity<IBookDetailPresenter> implements IBookDetailView {
    private FrameLayout iflContent;
    private ImageView ivBlurCover;
    private ImageView ivCover;
    private TextView tvName;
    private TextView tvAuthor;
    private TextView tvOrigin;
    private TextView tvChapter;
    private TextView tvIntro;
    private TextView tvShelf;
    private TextView tvRead;
    private TextView tvLoading;

    private Animation animHideLoading;
    private Animation animShowInfo;

    @Override
    protected IBookDetailPresenter initInjector() {
        return new BookDetailPresenterImpl(getIntent());
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_detail);
    }

    @Override
    protected void initData() {
        animShowInfo = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        animHideLoading = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        animHideLoading.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tvLoading.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    @Override
    protected void bindView() {
        iflContent = (FrameLayout) findViewById(R.id.ifl_content);
        ivBlurCover = (ImageView) findViewById(R.id.iv_blur_cover);
        ivCover = (ImageView) findViewById(R.id.iv_cover);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvAuthor = (TextView) findViewById(R.id.tv_author);
        tvOrigin = (TextView) findViewById(R.id.tv_origin);
        tvChapter = (TextView) findViewById(R.id.tv_chapter);
        tvIntro = (TextView) findViewById(R.id.tv_intro);
        tvShelf = (TextView) findViewById(R.id.tv_shelf);
        tvRead = (TextView) findViewById(R.id.tv_read);
        tvLoading = (TextView) findViewById(R.id.tv_loading);

        tvIntro.setMovementMethod(ScrollingMovementMethod.getInstance());
        initView();
        updateView();
    }

    @Override
    public void updateView() {
        if (null != mPresenter.getCollBookBean()) {
            if (mPresenter.getInBookShelf()) {
                tvChapter.setText(String.format(getString(R.string.tv_searchbook_lastest), mPresenter.getCollBookBean().getLastChapter()));
                tvShelf.setText("移出书架");
                tvRead.setText("继续阅读");
                tvShelf.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //从书架移出
                        mPresenter.removeFromBookShelf();
                    }
                });
            } else {
                tvChapter.setText(String.format(getString(R.string.tv_searchbook_lastest), mPresenter.getCollBookBean().getLastChapter()));
                tvShelf.setText("放入书架");
                tvRead.setText("开始阅读");
                tvShelf.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //放入书架
                        mPresenter.addToBookShelf();
                    }
                });
            }
            if (tvIntro.getText().toString().trim().length() == 0) {
                tvIntro.setText(mPresenter.getCollBookBean().getShortIntro());
            }
            if (tvIntro.getVisibility() != View.VISIBLE) {
                tvIntro.setVisibility(View.VISIBLE);
                tvIntro.startAnimation(animShowInfo);
                tvLoading.startAnimation(animHideLoading);
            }
            if (mPresenter.getCollBookBean().getBookTag() != null) {
                tvOrigin.setVisibility(View.VISIBLE);
                String sourceWebsit = "来源:" + mPresenter.getCollBookBean().getBookTag();
                tvOrigin.setText(sourceWebsit);
            } else {
                tvOrigin.setVisibility(View.GONE);
            }
        } else {
            tvChapter.setText(String.format(getString(R.string.tv_searchbook_lastest), mPresenter.getSearchBook().getLastChapter()));
            tvShelf.setText("放入书架");
            tvRead.setText("开始阅读");
            tvIntro.setVisibility(View.INVISIBLE);
            tvLoading.setVisibility(View.VISIBLE);
            tvLoading.setText("加载中...");
        }
        tvLoading.setOnClickListener(null);
    }

    @Override
    public void getBookShelfError() {
        tvLoading.setVisibility(View.VISIBLE);
        tvLoading.setText("加载失败,点击重试");
        tvLoading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvLoading.setText("加载中...");
                tvLoading.setOnClickListener(null);
                mPresenter.getBookShelfInfo();
            }
        });
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        if (mPresenter.getOpenfrom() == BookDetailPresenterImpl.FROM_SEARCH && mPresenter.getCollBookBean() == null) {
            //网络请求
            mPresenter.getBookShelfInfo();
        }
    }

    private void initView() {
        String coverUrl;
        String name;
        String author;
        if (mPresenter.getOpenfrom() == BookDetailPresenterImpl.FROM_BOOKSHELF) {
            coverUrl = mPresenter.getCollBookBean().getCover();
            name = mPresenter.getCollBookBean().getTitle();
            author = mPresenter.getCollBookBean().getAuthor();
            if (mPresenter.getCollBookBean().getBookTag() != null) {
                tvOrigin.setVisibility(View.VISIBLE);
                String sourceWebsit = "来源:" + mPresenter.getCollBookBean().getBookTag();
                tvOrigin.setText(sourceWebsit);
            } else {
                tvOrigin.setVisibility(View.GONE);
            }
        } else {
            coverUrl = mPresenter.getSearchBook().getCoverUrl();
            name = mPresenter.getSearchBook().getName();
            author = mPresenter.getSearchBook().getAuthor();
            if (mPresenter.getSearchBook().getOrigin() != null && mPresenter.getSearchBook().getOrigin().length() > 0) {
                tvOrigin.setVisibility(View.VISIBLE);
                String sourceWebsit = "来源:" + mPresenter.getSearchBook().getOrigin();
                tvOrigin.setText(sourceWebsit);
            } else {
                tvOrigin.setVisibility(View.GONE);
            }
        }

        Glide.with(this).load(coverUrl).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop().placeholder(R.drawable.img_cover_default).into(ivCover);
        Glide.with(this).load(coverUrl).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop().transform(new BlurTransformation(this, 6)).into(ivBlurCover);
        tvName.setText(name);
        tvAuthor.setText(author);
    }

    @Override
    protected void bindEvent() {
        iflContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (getStart_share_ele()) {
                        finishAfterTransition();
                    } else {
                        finish();
                        overridePendingTransition(0, android.R.anim.fade_out);
                    }
                } else {
                    finish();
                    overridePendingTransition(0, android.R.anim.fade_out);
                }
            }
        });

        tvRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookDetailActivity.this, ReadActivity.class);
                intent.putExtra("extra_coll_book", mPresenter.getCollBookBean());
                intent.putExtra(ReadActivity.EXTRA_IS_COLLECTED, mPresenter.getInBookShelf());
                startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (getStart_share_ele()) {
                        finishAfterTransition();
                    } else {
                        finish();
                        overridePendingTransition(0, android.R.anim.fade_out);
                    }
                } else {
                    finish();
                    overridePendingTransition(0, android.R.anim.fade_out);
                }
            }
        });
    }


}
