
package com.mp.android.apps.main.bookR.view.impl;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mp.android.apps.R;
import com.mp.android.apps.basemvplib.impl.BaseFragment;
import com.mp.android.apps.book.BitIntentDataManager;
import com.mp.android.apps.book.presenter.IMainPresenter;
import com.mp.android.apps.book.presenter.impl.BookDetailPresenterImpl;
import com.mp.android.apps.book.presenter.impl.MainPresenterImpl;
import com.mp.android.apps.book.view.IMainView;
import com.mp.android.apps.book.view.adapter.BookShelfAdapter;
import com.mp.android.apps.book.view.impl.BookDetailActivity;
import com.mp.android.apps.book.view.impl.BookSourceActivity;
import com.mp.android.apps.book.view.impl.BookSourceGuideActivity;
import com.mp.android.apps.book.view.impl.DownloadBookActivity;
import com.mp.android.apps.book.view.impl.ImportBookActivity;
import com.mp.android.apps.book.view.popupwindow.ProxyPop;
import com.mp.android.apps.book.widget.refreshview.OnRefreshWithProgressListener;
import com.mp.android.apps.book.widget.refreshview.RefreshRecyclerView;
import com.mp.android.apps.readActivity.ReadActivity;
import com.mp.android.apps.readActivity.bean.CollBookBean;

import java.util.List;

import static com.mp.android.apps.basemvplib.impl.BaseActivity.start_share_ele;

public class BookCollectionFragment extends BaseFragment<IMainPresenter> implements IMainView {


    private ImageButton ibSettings;
    private ImageButton ibLibrary;
    private ImageButton ibAdd;
    private ImageButton ibDownload;

    private RefreshRecyclerView rfRvShelf;
    private BookShelfAdapter bookShelfAdapter;

    private FrameLayout flWarn;
    private ImageView ivWarnClose;


    private ProxyPop proxyPop;

    @Override
    protected IMainPresenter initInjector() {
        return new MainPresenterImpl();
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.activity_book_main, container, false);
    }


    @Override
    protected void initData() {
        bookShelfAdapter = new BookShelfAdapter();

    }


    @Override
    protected void bindView() {
        proxyPop = new ProxyPop(getContext());


        rfRvShelf = (RefreshRecyclerView) view.findViewById(R.id.rf_rv_shelf);

        ibSettings = view.findViewById(R.id.ib_settings);
        ibLibrary = (ImageButton) view.findViewById(R.id.ib_library);
        ibAdd = (ImageButton) view.findViewById(R.id.ib_add);
        ibDownload = (ImageButton) view.findViewById(R.id.ib_download);

        rfRvShelf.setRefreshRecyclerViewAdapter(bookShelfAdapter, new LinearLayoutManager(getContext()));

        flWarn = (FrameLayout) view.findViewById(R.id.fl_warn);
        ivWarnClose = (ImageView) view.findViewById(R.id.iv_warn_close);
    }

    @Override
    protected void bindEvent() {
        bindRvShelfEvent();
        ibSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityByAnim(new Intent(getActivity(), BookSourceActivity.class), 0, 0);

//                proxyPop.showAsDropDown(ibSettings);
            }
        });
        ibDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), DownloadBookActivity.class));
            }
        });

        ibLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPresenter.bookSourceSwitch())
                    startActivityByAnim(new Intent(getActivity(), BookRActivity.class), 0, 0);
                else
                    startActivityByAnim(new Intent(getActivity(), BookSourceGuideActivity.class), 0, 0);
            }
        });
        ibAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击更多
                startActivityByAnim(new Intent(getActivity(), ImportBookActivity.class), 0, 0);
            }
        });
        bookShelfAdapter.setItemClickListener(new BookShelfAdapter.OnItemClickListener() {
            @Override
            public void toSearch() {
                //点击去选书
                if (mPresenter.bookSourceSwitch())
                    startActivityByAnim(new Intent(getActivity(), BookRActivity.class), 0, 0);
                else
                    startActivityByAnim(new Intent(getActivity(), BookSourceGuideActivity.class), 0, 0);

            }

            @Override
            public void onClick(CollBookBean collBookBean, int index) {
                Intent intent = new Intent(getActivity(), ReadActivity.class);

                intent.putExtra(ReadActivity.EXTRA_COLL_BOOK, collBookBean);
                intent.putExtra(ReadActivity.EXTRA_IS_COLLECTED, true);
                startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);
            }

            @Override
            public void onLongClick(View animView, CollBookBean bookShelfBean, int index) {
                Intent intent = new Intent(getActivity(), BookDetailActivity.class);
                intent.putExtra("from", BookDetailPresenterImpl.FROM_BOOKSHELF);
                String key = String.valueOf(System.currentTimeMillis());
                intent.putExtra("data_key", key);
                BitIntentDataManager.getInstance().putData(key, bookShelfBean);
                startActivityByAnim(intent, animView, "img_cover", android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        ivWarnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flWarn.setVisibility(View.GONE);
            }
        });
    }

    private void bindRvShelfEvent() {
        rfRvShelf.setBaseRefreshListener(new OnRefreshWithProgressListener() {
            @Override
            public int getMaxProgress() {
                return bookShelfAdapter.getBooks().size();
            }

            @Override
            public void startRefresh() {
                mPresenter.queryBookShelf(true);
            }
        });
    }

    @Override
    protected void firstRequest() {
        mPresenter.queryBookShelf(false);
    }


    @Override
    public void refreshBookShelf(List<CollBookBean> bookShelfBeanList) {
        bookShelfAdapter.replaceAll(bookShelfBeanList);
    }

    @Override
    public void activityRefreshView() {
        //执行刷新响应
        rfRvShelf.startRefresh();
    }

    @Override
    public void refreshFinish() {
        rfRvShelf.finishRefresh(false, true);
    }

    @Override
    public void refreshError(String error) {
        refreshFinish();
        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void refreshRecyclerViewItemAdd() {
        rfRvShelf.getRpb().setDurProgress(rfRvShelf.getRpb().getDurProgress() + 1);
    }

    @Override
    public void setRecyclerMaxProgress(int x) {
        rfRvShelf.getRpb().setMaxProgress(x);
    }

    private void startActivityByAnim(Intent intent, int animIn, int animExit) {
        startActivity(intent);
        getActivity().overridePendingTransition(animIn, animExit);
    }

    private void startActivityByAnim(Intent intent, @NonNull View view, @NonNull String transitionName, int animIn, int animExit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.putExtra(start_share_ele, true);
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity(), view, transitionName).toBundle());
        } else {
            startActivityByAnim(intent, animIn, animExit);
        }
    }
}