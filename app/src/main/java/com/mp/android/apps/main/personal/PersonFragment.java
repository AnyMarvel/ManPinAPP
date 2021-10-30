package com.mp.android.apps.main.personal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.mp.android.apps.R;
import com.mp.android.apps.SettingAboutActivity;
import com.mp.android.apps.book.common.RxBusTag;
import com.mp.android.apps.livevblank.PreviewActivity;
import com.mp.android.apps.login.LoginActivity;
import com.mp.android.apps.login.bean.login.Data;
import com.mp.android.apps.login.fragment.imple.LoginBaseFragment;
import com.mp.android.apps.login.utils.LoginManager;
import com.mp.android.apps.main.MainActivity;
import com.mp.android.apps.main.ManpinWXActivity;
import com.mp.android.apps.main.home.model.IMainFragmentModelImpl;
import com.mp.android.apps.basemvplib.IPresenter;
import com.mp.android.apps.basemvplib.impl.BaseFragment;
import com.mp.android.apps.book.base.observer.SimpleObserver;
import com.mp.android.apps.book.view.impl.BookSourceActivity;
import com.mp.android.apps.main.home.view.MyImageTextView;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PersonFragment extends LoginBaseFragment implements View.OnClickListener {


    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.person_fragment, container, false);
    }

    CircleImageView mUserLogo;
    MyImageTextView manpin_weixin_xiaobian_layout;
    MyImageTextView booksource;
    MyImageTextView guanyuwomen;
    MyImageTextView fenxiang;
    LinearLayout personExternalLoginLayout;
    TextView personFragmentUsername;

    @Override
    protected void bindView() {
        super.bindView();

        guanyuwomen = view.findViewById(R.id.guanyuwomen);
        guanyuwomen.setOnClickListener(this);
        mUserLogo = view.findViewById(R.id.mUserLogo);

        manpin_weixin_xiaobian_layout = view.findViewById(R.id.manpin_weixin_xiaobian_layout);
        manpin_weixin_xiaobian_layout.setOnClickListener(this);
        fenxiang = view.findViewById(R.id.person_fenxiang_layout);
        fenxiang.setOnClickListener(this);


        booksource = view.findViewById(R.id.person_booksource_layout);
        booksource.setOnClickListener(this);

        personExternalLoginLayout = view.findViewById(R.id.person_external_login_layout);
        personExternalLoginLayout.setVisibility(View.VISIBLE);

        personFragmentUsername = view.findViewById(R.id.person_fragment_username);
        personFragmentUsername.setVisibility(View.GONE);

        OnClickListener(view, requireActivity());
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        loginSuccess(LoginManager.getInstance().getLoginInfo());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.guanyuwomen:
                Intent settingIntent = new Intent(getActivity(), SettingAboutActivity.class);
                requireActivity().startActivity(settingIntent);
                break;
            case R.id.manpin_weixin_xiaobian_layout:
                Intent intent = new Intent(getActivity(), ManpinWXActivity.class);
                startActivity(intent);
                break;
            case R.id.person_booksource_layout:
                Intent intent2 = new Intent(getActivity(), BookSourceActivity.class);
                startActivity(intent2);
                break;
            case R.id.person_fenxiang_layout:
                UMWeb weburl = new UMWeb("http://aimanpin.com");
                weburl.setDescription("官方网址:\n \n http://aimanpin.com ");
                weburl.setTitle("漫品官网");
                weburl.setThumb(new UMImage(requireContext(), R.drawable.ic_launcher_share_background));
                new ShareAction(requireActivity()).withMedia(weburl)
                        .setDisplayList(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE
                                , SHARE_MEDIA.WEIXIN_FAVORITE, SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE
                                , SHARE_MEDIA.SINA
                        ).open();
                break;
            default:
                break;
        }
    }

    /**
     * 基于RXBus获取登陆成功通知消息
     */
    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.LOGIN_SUCCESS)
            }
    )
    public void loginSuccess(Data loginInfo) {
        if (loginInfo != null) {
            if (!TextUtils.isEmpty(loginInfo.getNickname())) {
                personExternalLoginLayout.setVisibility(View.GONE);
                personFragmentUsername.setVisibility(View.VISIBLE);
                personFragmentUsername.setText(loginInfo.getNickname());
            }
            if (!TextUtils.isEmpty(loginInfo.getUsericon())) {
                mUserLogo.setVisibility(View.VISIBLE);
                Glide.with(requireContext()).load(loginInfo.getUsericon()).into(mUserLogo);
            }
        }else {
            personExternalLoginLayout.setVisibility(View.VISIBLE);
            personFragmentUsername.setVisibility(View.GONE);
            mUserLogo.setVisibility(View.GONE);

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        loginSuccess(LoginManager.getInstance().getLoginInfo());
    }
}

