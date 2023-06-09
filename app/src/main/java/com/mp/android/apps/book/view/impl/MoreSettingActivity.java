package com.mp.android.apps.book.view.impl;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mp.android.apps.R;
import com.mp.android.apps.book.base.MBaseActivity;
import com.mp.android.apps.book.bean.BookMoreSettingBean;
import com.mp.android.apps.book.presenter.IMoreSettingPresenter;
import com.mp.android.apps.book.presenter.impl.MoreSettingPresenterImpl;
import com.mp.android.apps.book.view.IMoreSettingView;
import com.mp.android.apps.book.view.adapter.MoreSettingAdapter;
import com.mp.android.apps.utils.SpacesItemDecoration;
import com.mp.android.apps.readActivity.local.ReadSettingManager;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import static com.mp.android.apps.readActivity.local.ReadSettingManager.SHARED_READ_VOLUME_TURN_PAGE;

public class MoreSettingActivity extends MBaseActivity<IMoreSettingPresenter> implements IMoreSettingView, MoreSettingAdapter.IMoreSettingClickListener {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_right_btn)
    TextView tvRightBtn;
    @BindView(R.id.mp_book_more_setting_recyclerView)
    RecyclerView mpBookMoreSettingRecyclerView;
    private MoreSettingAdapter moreSettingAdapter;
    private List<BookMoreSettingBean> source = new ArrayList<>();

    @Override
    protected IMoreSettingPresenter initInjector() {
        return new MoreSettingPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.mp_book_more_setting_layout);
    }

    @Override
    protected void bindEvent() {
        super.bindEvent();
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    @Override
    protected void initData() {
        // 音量键翻页
        source.add(new BookMoreSettingBean("音量键翻页", ReadSettingManager.getInstance().isVolumeTurnPage(), SHARED_READ_VOLUME_TURN_PAGE));


    }

    @Override
    protected void bindView() {
        super.bindView();
        tvTitle.setText("更多设置");
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        //设置布局管理器
        mpBookMoreSettingRecyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        //设置分隔线
        mpBookMoreSettingRecyclerView.addItemDecoration(new SpacesItemDecoration(15));
        //设置增加或删除条目的动画
        mpBookMoreSettingRecyclerView.setItemAnimator(new DefaultItemAnimator());

    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        moreSettingAdapter = new MoreSettingAdapter(source, this);
        mpBookMoreSettingRecyclerView.setAdapter(moreSettingAdapter);
    }


    @Override
    public void onclickListerer(BookMoreSettingBean bookMoreSettingBean, boolean isChecked) {
        switch (bookMoreSettingBean.getSettingTag()) {
            case ReadSettingManager.SHARED_READ_VOLUME_TURN_PAGE:
                ReadSettingManager.getInstance().setVolumeTurnPage(isChecked);
            default:
                break;
        }
    }
}
