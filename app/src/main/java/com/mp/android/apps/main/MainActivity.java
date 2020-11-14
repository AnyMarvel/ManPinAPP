package com.mp.android.apps.main;


import android.Manifest;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.mp.android.apps.R;

import com.mp.android.apps.StoryboardActivity;
import com.mp.android.apps.explore.ExploreSquareActivity;
import com.mp.android.apps.login.LoginActivity;
import com.mp.android.apps.login.utils.LoginManager;
import com.mp.android.apps.main.bookR.view.impl.BookRFragment;
import com.mp.android.apps.main.home.view.impl.MainFragment;
import com.mp.android.apps.main.personal.PersonFragment;
import com.mp.android.apps.main.home.view.MyImageTextView;
import com.mp.android.apps.monke.basemvplib.impl.BaseFragment;
import com.mp.android.apps.monke.readActivity.ReadActivity;
import com.mp.android.apps.monke.readActivity.bean.CollBookBean;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;

import java.util.List;


public class MainActivity extends StoryboardActivity implements View.OnClickListener {
    MainFragment mainFragment;
    PersonFragment personFragment;
    BookRFragment bookRFragment;
    MyImageTextView zhuye;
    MyImageTextView shujia;
    MyImageTextView quanzi;
    MyImageTextView wode;
    private FrameLayout flWarn;
    private ImageView flWarnClose;

    private void hidenFragment(FragmentTransaction transaction) {
        if (mainFragment != null) {
            transaction.hide(mainFragment);
        }
        if (personFragment != null) {
            transaction.hide(personFragment);
        }
        if (bookRFragment != null) {
            transaction.hide(bookRFragment);
        }
    }

    private void showFragment(BaseFragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hidenFragment(transaction);
        if (fragment != null) {
            if (fragment.isAdded()) {
                transaction.show(fragment);
            } else {
                transaction.add(R.id.main_contain, fragment).show(fragment);
            }
            transaction.commit();
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        LoginManager.getInstance().initSP(this).initData();
        mainFragment = new MainFragment();
        personFragment = new PersonFragment();
        bookRFragment = new BookRFragment();
        showFragment(mainFragment);
        initViews();
    }

    private void initViews() {
        zhuye = findViewById(R.id.zhuye);
        zhuye.setOnClickListener(this);
        shujia = findViewById(R.id.shujia);
        shujia.setOnClickListener(this);
        quanzi = findViewById(R.id.quanzi);
        quanzi.setOnClickListener(this);
        wode = findViewById(R.id.gerenzhongxin);
        wode.setOnClickListener(this);
        flWarn = findViewById(R.id.main_fl_warn);
        flWarnClose = findViewById(R.id.main_iv_warn_close);
        flWarnClose.setOnClickListener(this);
        flWarnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flWarn.setVisibility(View.GONE);
            }
        });
    }

    private static final int EXPLORESQUARE_LOGIN = 1005;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private long exitTime = 0;

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }

    public void gotoExplore(String toExplore) {
        Intent intent = new Intent(this, ExploreSquareActivity.class);
        intent.putExtra("toExplore", toExplore);
        Acp.getInstance(this).request(new AcpOptions.Builder()
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION).build(), new AcpListener() {
            @Override
            public void onGranted() {
                if (LoginManager.getInstance().checkLoginInfo()) {
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivityForResult(intent, EXPLORESQUARE_LOGIN);
                }
            }

            @Override
            public void onDenied(List<String> permissions) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EXPLORESQUARE_LOGIN && resultCode == 0) {
            gotoExplore("广场");
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.zhuye:
                changeNavImages(R.id.zhuye);
                showFragment(mainFragment);
                break;
            case R.id.shujia:
                //todo 改造书架为fragment
                changeNavImages(R.id.shujia);
//                Intent intentBook = new Intent(MainActivity.this, BookMainActivity.class);
//                startActivity(intentBook);
                showFragment(bookRFragment);
                break;
            case R.id.quanzi:
                //todo  改造圈子为fragment
//                changeNavImages(R.id.quanzi);
                gotoExplore("广场");
                break;
            case R.id.gerenzhongxin:
                changeNavImages(R.id.gerenzhongxin);
                Intent intent = new Intent(MainActivity.this, ReadActivity.class);
                intent.putExtra("extra_coll_book", getMyCollBookBean());
                startActivity(intent);
//                showFragment(personFragment);
                break;
            default:
                break;
        }
    }

    private void changeNavImages(int id) {
        zhuye.setImgResource(R.drawable.zhuye);
        shujia.setImgResource(R.drawable.shujia);
        quanzi.setImgResource(R.drawable.quanzi);
        wode.setImgResource(R.drawable.gerenzhongxin);
        switch (id) {
            case R.id.zhuye:
                zhuye.setImgResource(R.drawable.zhuye_selected);
                break;
            case R.id.shujia:
                shujia.setImgResource(R.drawable.shujia_selected);
                break;
            case R.id.quanzi:
                quanzi.setImgResource(R.drawable.quanzi_selected);
                break;
            case R.id.gerenzhongxin:
                wode.setImgResource(R.drawable.gerenzhongxin_selected);
                break;
            default:
                break;
        }
    }

    public CollBookBean getMyCollBookBean() {
        CollBookBean collBookBean = new CollBookBean();
        collBookBean.set_id("https://www.wxguan.com/wenzhang/145/145928/");
        collBookBean.setTitle("wpkFlowLog");
        collBookBean.setAuthor("xxxxx");
        collBookBean.setCover("/storage/emulated/0/wpkFlowLog.txt");
        collBookBean.setLastChapter("第一章节");
        collBookBean.setLastRead("2020-11-15T03:43:56");
        collBookBean.setUpdated("2019-08-26T04:54:33");
        collBookBean.setHasCp(false);
        collBookBean.setLocal(true);
        collBookBean.setUpdate(false);
        collBookBean.setShortIntro("五");
        return collBookBean;

    }
}
