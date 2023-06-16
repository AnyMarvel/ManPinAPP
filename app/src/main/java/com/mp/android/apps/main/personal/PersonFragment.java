package com.mp.android.apps.main.personal;

import android.content.Intent;
import android.net.Uri;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mp.android.apps.R;

import com.mp.android.apps.basemvplib.IPresenter;
import com.mp.android.apps.basemvplib.impl.BaseFragment;

import com.mp.android.apps.book.view.impl.BookSourceActivity;
import com.mp.android.apps.downloadUtils.CheckUpdateUtils;
import com.mp.android.apps.main.ManpinWXActivity;
import com.mp.android.apps.main.config.AppConfigUtils;
import com.mp.android.apps.utils.GeneralTools;

import java.util.ArrayList;
import java.util.List;


public class PersonFragment extends BaseFragment implements View.OnClickListener {
    @Override
    protected IPresenter initInjector() {
        return null;
    }


    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.person_fragment, container, false);
    }

    TextView versionTitle;


    @Override
    protected void bindView() {
        super.bindView();
        versionTitle = view.findViewById(R.id.versionTips);
        versionTitle.setText("当前版本: " + GeneralTools.APP_VERSION);
        view.findViewById(R.id.kaiyuandizhi).setOnClickListener(this);
        RelativeLayout shuyuan = view.findViewById(R.id.shuyuanxuanze);
        shuyuan.setOnClickListener(this);
        RelativeLayout shequ = view.findViewById(R.id.jiarushequ);
        shequ.setOnClickListener(this);
        if (AppConfigUtils.business) {
            shuyuan.setVisibility(View.GONE);
            shequ.setVisibility(View.GONE);
        } else {
            shuyuan.setVisibility(View.VISIBLE);
            shequ.setVisibility(View.VISIBLE);
        }

        view.findViewById(R.id.checkupdate).setOnClickListener(this);
        view.findViewById(R.id.share_manpin).setOnClickListener(this);
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.jiarushequ:
                //加入社区
                Intent intent3 = new Intent(getActivity(), ManpinWXActivity.class);
                startActivity(intent3);
                break;
            case R.id.shuyuanxuanze:
                //配置书源
                Intent intent2 = new Intent(getActivity(), BookSourceActivity.class);
                startActivity(intent2);
                break;
            case R.id.checkupdate:
                //检查更新
                try {
                    CheckUpdateUtils.getInstance().checkUpdata(getActivity(),true);
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.kaiyuandizhi:
                //开源地址
                Intent intent1 = new Intent("android.intent.action.VIEW");
                intent1.setData(Uri.parse("https://github.com/AnyMarvel/ManPinAPP"));
                startActivity(intent1);
                break;
            case R.id.share_manpin:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "https://www.pgyer.com/manpin");
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, "漫品");
                startActivity(shareIntent);
                break;
        }

    }




    @Override
    public void onResume() {
        super.onResume();
    }
}

