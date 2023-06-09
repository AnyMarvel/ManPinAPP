package com.mp.android.apps.main.personal;

import android.content.Intent;
import android.net.Uri;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mp.android.apps.R;
import com.mp.android.apps.SettingAboutActivity;
import com.mp.android.apps.main.ManpinWXActivity;

import com.mp.android.apps.basemvplib.IPresenter;
import com.mp.android.apps.basemvplib.impl.BaseFragment;

import com.mp.android.apps.book.view.impl.BookSourceActivity;
import com.mp.android.apps.main.home.view.MyImageTextView;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;

import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import de.hdodenhof.circleimageview.CircleImageView;


public class PersonFragment extends BaseFragment implements View.OnClickListener {
    @Override
    protected IPresenter initInjector() {
        return null;
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


    MyImageTextView musiclayout;
    MyImageTextView travelLayout;
    MyImageTextView wallpaperLayout;
    MyImageTextView gamesLayout;

    LinearLayout personExternalLoginLayout;
    TextView personFragmentUsername;

    @Override
    protected void bindView() {
        super.bindView();


        musiclayout=view.findViewById(R.id.manpin_person_music_layout);
        musiclayout.setOnClickListener(this);

        travelLayout=view.findViewById(R.id.manpin_person_travel);
        travelLayout.setOnClickListener(this);

        wallpaperLayout=view.findViewById(R.id.manpin_person_wallpaper);
        wallpaperLayout.setOnClickListener(this);

        gamesLayout=view.findViewById(R.id.manpin_person_games);
        gamesLayout.setOnClickListener(this);

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


    }

    @Override
    protected void firstRequest() {
        super.firstRequest();

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        String linkUrl;
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

            case R.id.manpin_person_music_layout:
                jumpLinkUrl("http://tool.liumingye.cn/music/?page=searchPage");
                break;
            case R.id.manpin_person_travel:
                jumpLinkUrl("http://quanjingke.com/vmindex");
                break;
            case R.id.manpin_person_wallpaper:
                jumpLinkUrl("https://www.logosc.cn/so/");
                break;
            case R.id.manpin_person_games:
                jumpLinkUrl("https://www.yikm.net");
                break;

            default:
                break;
        }
    }
    private void jumpLinkUrl(String linkUrl){
        Intent intentUrl= new Intent();
        intentUrl.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(linkUrl);
        intentUrl.setData(content_url);
        startActivity(intentUrl);
    }


    @Override
    public void onResume() {
        super.onResume();
    }
}

