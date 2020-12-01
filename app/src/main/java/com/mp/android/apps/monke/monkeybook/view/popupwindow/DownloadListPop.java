
package com.mp.android.apps.monke.monkeybook.view.popupwindow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.mp.android.apps.R;
import com.mp.android.apps.monke.monkeybook.common.RxBusTag;

public class DownloadListPop extends PopupWindow {
    private Context mContext;
    private View view;

    private TextView tvNone;
    private LinearLayout llDownload;

    private ImageView ivCover;
    private TextView tvName;
    private TextView tvChapterName;
    private TextView tvCancel;
    private TextView tvDownload;

    public DownloadListPop(Context context) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mContext = context;
        view = LayoutInflater.from(mContext).inflate(R.layout.view_pop_downloadlist, null);
        this.setContentView(view);
        bindView();
        bindEvent();
        initWait();
        setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
        setFocusable(true);
        setTouchable(true);
        setAnimationStyle(R.style.anim_pop_checkaddshelf);
        RxBus.get().register(DownloadListPop.this);
    }

    private void bindEvent() {
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxBus.get().post(RxBusTag.CANCEL_DOWNLOAD, new Object());
                tvNone.setVisibility(View.VISIBLE);
            }
        });
        tvDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvDownload.getText().equals("开始下载")) {
                    RxBus.get().post(RxBusTag.START_DOWNLOAD, new Object());
                } else {
                    RxBus.get().post(RxBusTag.PAUSE_DOWNLOAD, new Object());
                }
            }
        });
    }

    private void bindView() {
        tvNone = (TextView) view.findViewById(R.id.tv_none);
        llDownload = (LinearLayout) view.findViewById(R.id.ll_download);
        ivCover = (ImageView) view.findViewById(R.id.iv_cover);
        tvName = (TextView) view.findViewById(R.id.tv_name);
        tvChapterName = (TextView) view.findViewById(R.id.tv_chapter_name);
        tvCancel = (TextView) view.findViewById(R.id.tv_cancel);
        tvDownload = (TextView) view.findViewById(R.id.tv_download);
    }

    private void initWait() {}

    public void onDestroy() {
        RxBus.get().unregister(DownloadListPop.this);
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.PAUSE_DOWNLOAD_LISTENER)
            }
    )
    public void pauseTask(Object o) {
        tvNone.setVisibility(View.GONE);
        llDownload.setVisibility(View.GONE);
        tvDownload.setText("开始下载");
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.FINISH_DOWNLOAD_LISTENER)
            }
    )
    public void finishTask(Object o) {
        tvNone.setVisibility(View.VISIBLE);
    }

//    @Subscribe(
//            thread = EventThread.MAIN_THREAD,
//            tags = {
//                    @Tag(RxBusTag.PROGRESS_DOWNLOAD_LISTENER)
//            }
//    )
//    public void progressTask(DownloadChapterBean downloadChapterBean) {
//        tvNone.setVisibility(View.GONE);
//        llDownload.setVisibility(View.VISIBLE);
//        tvDownload.setText("暂停下载");
//        Glide.with(mContext).load(downloadChapterBean.getCoverUrl()).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop().placeholder(R.drawable.img_cover_default).into(ivCover);
//        tvName.setText(downloadChapterBean.getBookName());
//        tvChapterName.setText(downloadChapterBean.getDurChapterName());
//    }

}
