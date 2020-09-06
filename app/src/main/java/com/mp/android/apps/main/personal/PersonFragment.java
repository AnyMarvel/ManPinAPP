package com.mp.android.apps.main.personal;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.mp.android.apps.FeedbackActivity;
import com.mp.android.apps.R;
import com.mp.android.apps.SettingAboutActivity;
import com.mp.android.apps.login.LoginActivity;
import com.mp.android.apps.login.bean.login.Data;
import com.mp.android.apps.login.utils.LoginManager;
import com.mp.android.apps.main.MainActivity;
import com.mp.android.apps.main.home.model.IMainFragmentModelImpl;
import com.mp.android.apps.monke.basemvplib.IPresenter;
import com.mp.android.apps.monke.basemvplib.impl.BaseFragment;
import com.mp.android.apps.monke.monkeybook.base.observer.SimpleObserver;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PersonFragment extends BaseFragment implements View.OnClickListener {
    ImageView personBackground;
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
        IMainFragmentModelImpl.getInstance().getCycleImages().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<String>() {
            @Override
            public void onNext(String s) {
                List<String> list = (List<String>) JSON.parseObject(s).get("data");
                if (list != null && list.size() > 0) {
                    Random random = new Random();
                    int result = random.nextInt(list.size());
                    Glide.with(PersonFragment.this.getContext()).load(list.get(result)).into(personBackground);

                }
            }

            @Override
            public void onError(Throwable e) {
                Glide.with(PersonFragment.this.getContext()).load("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1591101217238&di=3ceb9a70573c3da62c42579d111c6319&imgtype=0&src=http%3A%2F%2Fattach.bbs.miui.com%2Fforum%2F201401%2F04%2F114458foyo99odqb8qjzg4.jpg").into(personBackground);
            }
        });


        if (LoginManager.getInstance().checkLoginInfo()) {
            Data loginInfo = LoginManager.getInstance().getLoginInfo();
            Glide.with(getContext()).load(loginInfo.getUsericon()).into(mUserLogo);
            person_text.setText(loginInfo.getNickname());
        } else {
            person_text.setText("未登录");
            mUserLogo.setImageResource(R.drawable.ic_camera_beauty);
        }

    }

    RelativeLayout weibo_sina;
    RelativeLayout maillayout;
    RelativeLayout yijianfankui;
    TextView person_text;
    LinearLayout person_login_statu;
    CircleImageView mUserLogo;

    @Override
    protected void bindView() {
        super.bindView();
        personBackground = view.findViewById(R.id.person_backgroundImage);
        guanyuwomen = view.findViewById(R.id.guanyuwomen);
        guanyuwomen.setOnClickListener(this);
        weibo_sina = view.findViewById(R.id.weibo_sina);
        weibo_sina.setOnClickListener(this);
        maillayout = view.findViewById(R.id.maillayout);
        maillayout.setOnClickListener(this);
        yijianfankui = view.findViewById(R.id.yijianfankui);
        yijianfankui.setOnClickListener(this);
        person_text = view.findViewById(R.id.person_text);
        mUserLogo = view.findViewById(R.id.mUserLogo);
        person_login_statu = view.findViewById(R.id.person_login_statu);
        person_login_statu.setOnClickListener(this);
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
            case R.id.person_login_statu:
                if (!LoginManager.getInstance().checkLoginInfo()) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivityForResult(intent, PERSON_LOGIN_REQUEST);
                }
                break;
            default:
                break;
        }
    }

    private final int PERSON_LOGIN_REQUEST = 10010;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERSON_LOGIN_REQUEST && resultCode == 0) {
            Data loginInfo = LoginManager.getInstance().getLoginInfo();
            Glide.with(Objects.requireNonNull(getContext())).load(loginInfo.getUsericon()).into(mUserLogo);
            person_text.setText(loginInfo.getNickname());
        }
    }
}

