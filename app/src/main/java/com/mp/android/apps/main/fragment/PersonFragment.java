package com.mp.android.apps.main.fragment;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.mp.android.apps.FeedbackActivity;
import com.mp.android.apps.R;
import com.mp.android.apps.SettingAboutActivity;
import com.mp.android.apps.main.MainActivity;
import com.mp.android.apps.monke.basemvplib.IPresenter;
import com.mp.android.apps.monke.basemvplib.impl.BaseFragment;

public class PersonFragment extends BaseFragment implements View.OnClickListener {
    ImageView personImageView;
    RelativeLayout guanyuwomen;

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.person_fragment, container, false);
    }

    @Override
    protected void bindEvent() {
        super.bindEvent();
        Glide.with(getContext()).load("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1591101217238&di=3ceb9a70573c3da62c42579d111c6319&imgtype=0&src=http%3A%2F%2Fattach.bbs.miui.com%2Fforum%2F201401%2F04%2F114458foyo99odqb8qjzg4.jpg").into(personImageView);

    }

    RelativeLayout weibo_sina;
    RelativeLayout maillayout;
    RelativeLayout yijianfankui;

    @Override
    protected void bindView() {
        super.bindView();
        personImageView = view.findViewById(R.id.personImageView);
        guanyuwomen = view.findViewById(R.id.guanyuwomen);
        guanyuwomen.setOnClickListener(this);
        weibo_sina = view.findViewById(R.id.weibo_sina);
        weibo_sina.setOnClickListener(this);
        maillayout = view.findViewById(R.id.maillayout);
        maillayout.setOnClickListener(this);
        yijianfankui = view.findViewById(R.id.yijianfankui);
        yijianfankui.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.guanyuwomen:
                Intent settingIntent = new Intent(getActivity(), SettingAboutActivity.class);
                getActivity().startActivity(settingIntent);
                break;
            case R.id.weibo_sina:
                Intent intent1 = new Intent("android.intent.action.VIEW");
                intent1.setData(Uri.parse("https://weibo.com/lijuntaosky/home"));
                startActivity(intent1);
                break;
            case R.id.maillayout:
                Intent data = new Intent(Intent.ACTION_SENDTO);
                data.setData(Uri.parse("mailto:314599558@qq.com"));
                data.putExtra(Intent.EXTRA_SUBJECT, "漫品客户端使用问题反馈");
                data.putExtra(Intent.EXTRA_TEXT, ((MainActivity) getActivity()).getHandSetInfo());
                startActivity(data);
                break;
            case R.id.yijianfankui:
                Intent feedbackIntent = new Intent(getActivity(), FeedbackActivity.class);
                startActivity(feedbackIntent);
            default:
                break;
        }
    }
}
