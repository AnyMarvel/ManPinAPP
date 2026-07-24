
package com.mp.android.apps.book.view.impl;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.mp.android.apps.R;
import com.mp.android.apps.base.impl.BaseFragment;
import com.mp.android.apps.book.BitIntentDataManager;
import com.mp.android.apps.book.common.RxBusTag;
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
import com.mp.android.apps.book.widget.refreshview.OnRefreshWithProgressListener;
import com.mp.android.apps.book.widget.refreshview.RefreshRecyclerView;
import com.mp.android.apps.main.MainActivity;
import com.mp.android.apps.read.ReadActivity;
import com.mp.android.apps.read.bean.CollBookBean;
import com.victor.loading.rotate.RotateLoading;

import java.util.List;

import static com.mp.android.apps.base.impl.BaseActivity.start_share_ele;

public class BookCollectionFragment extends BaseFragment<IMainPresenter> implements IMainView {


    private ImageButton ibSettings;
    private ImageButton ibLibrary;
    private ImageButton ibAdd;
    private ImageButton ibDownload;
    private RefreshRecyclerView rfRvShelf;
    private BookShelfAdapter bookShelfAdapter;
    private RotateLoading rlLoading;




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

        rfRvShelf = (RefreshRecyclerView) view.findViewById(R.id.rf_rv_shelf);

        ibSettings = view.findViewById(R.id.ib_settings);
        ibLibrary = (ImageButton) view.findViewById(R.id.ib_library);
        ibAdd = (ImageButton) view.findViewById(R.id.ib_add);
        ibDownload = (ImageButton) view.findViewById(R.id.ib_download);

        rfRvShelf.setRefreshRecyclerViewAdapter(bookShelfAdapter, new LinearLayoutManager(getContext()));

        rlLoading = (RotateLoading) view.findViewById(R.id.rl_loading);

        ibSettings.setVisibility(View.VISIBLE);
        ibLibrary.setVisibility(View.VISIBLE);
        ibDownload.setVisibility(View.VISIBLE);
    }

    @Override
    protected void bindEvent() {
        bindRvShelfEvent();

        ibSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CrashLogActivity.class));
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
                    ((MainActivity) requireActivity()).showBookStore();
                else{
                    startActivityByAnim(new Intent(getActivity(), BookSourceGuideActivity.class), 0, 0);
                }

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
                if (mPresenter.bookSourceSwitch()){
                    ((MainActivity) requireActivity()).showBookStore();
                }
                else{
                    startActivityByAnim(new Intent(getActivity(), BookSourceGuideActivity.class), 0, 0);
                }
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

    }

    private void bindRvShelfEvent() {
        rfRvShelf.setBaseRefreshListener(new OnRefreshWithProgressListener() {
            @Override
            public int getMaxProgress() {
                return bookShelfAdapter.getBooks().size();
            }

            @Override
            public void startRefresh() {
                //手动下拉刷新：联网检查更新，并记录检查时间(用于首次进入的自动检查节流)
                markUpdateChecked();
                mPresenter.queryBookShelf(true);
            }
        });
    }

    /** 自动检查更新的节流间隔：距上次检查超过该时长，进入书架才自动联网检查一次 */
    private static final long AUTO_CHECK_INTERVAL = 6 * 60 * 60 * 1000L; // 6小时
    private static final String SP_SHELF = "shelf_pref";
    private static final String KEY_LAST_CHECK = "last_shelf_update_check";

    @Override
    protected void firstRequest() {
        //首次进入书架：距上次检查超过节流间隔则自动联网检查一次，否则仅读本地
        if (shouldAutoCheckUpdate()) {
            mPresenter.queryBookShelf(true);
        } else {
            mPresenter.queryBookShelf(false);
        }
    }

    /** 若距上次检查已超过节流间隔，则记录本次时间并返回 true（表示可自动检查） */
    private boolean shouldAutoCheckUpdate() {
        Context ctx = getContext();
        if (ctx == null) return false;
        SharedPreferences sp = ctx.getSharedPreferences(SP_SHELF, Context.MODE_PRIVATE);
        long last = sp.getLong(KEY_LAST_CHECK, 0L);
        if (System.currentTimeMillis() - last >= AUTO_CHECK_INTERVAL) {
            markUpdateChecked();
            return true;
        }
        return false;
    }

    /** 记录一次"检查更新"的时间 */
    private void markUpdateChecked() {
        Context ctx = getContext();
        if (ctx == null) return;
        ctx.getSharedPreferences(SP_SHELF, Context.MODE_PRIVATE)
                .edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply();
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
    public void onResume() {
        super.onResume();
        //onResume 仅从本地重载书架(不联网)，避免每次返回都对全部书发起更新检查；
        //联网检查更新只在用户主动下拉刷新时触发
        mPresenter.queryBookShelf(false);
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


    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.SHOW_COLLECTION_RLLODING)
            }
    )
    public void startRlLoading(Object o){
        if (rlLoading!=null){
            rlLoading.setVisibility(View.VISIBLE);
            rlLoading.start();
        }

    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.HIDE_COLLECTION_RLLODING)
            }
    )
    public void stopRlLoading(Object o){
        if (rlLoading!=null){
            rlLoading.stop();
            rlLoading.setVisibility(View.GONE);
        }
    }

}