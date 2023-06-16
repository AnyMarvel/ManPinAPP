package com.mp.android.apps.main;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import com.mp.android.apps.R;
import com.mp.android.apps.book.view.impl.BookRankListFragment;
import com.mp.android.apps.book.view.impl.BookCollectionFragment;
import com.mp.android.apps.main.config.AppConfigUtils;
import com.mp.android.apps.main.home.view.impl.MainFragment;
import com.mp.android.apps.main.personal.PersonFragment;
import com.mp.android.apps.main.home.view.MyImageTextView;
import com.mp.android.apps.basemvplib.impl.BaseFragment;
import com.umeng.analytics.MobclickAgent;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    MainFragment mainFragment;
    PersonFragment personFragment;
    BookCollectionFragment bookCollectionFragment;
    BookRankListFragment mBookRankListFragment;
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
        if (bookCollectionFragment != null) {
            transaction.hide(bookCollectionFragment);
        }
        if (mBookRankListFragment!=null){
            transaction.hide(mBookRankListFragment);
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
        mainFragment = new MainFragment();
        personFragment = new PersonFragment();
        bookCollectionFragment = new BookCollectionFragment();

        mBookRankListFragment=new BookRankListFragment();
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

        //底部转载服务提醒
        flWarn = findViewById(R.id.main_fl_warn);
        flWarnClose = findViewById(R.id.main_iv_warn_close);
        flWarnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flWarn.setVisibility(View.GONE);
            }
        });
        zhuye.setVisibility(View.VISIBLE);
        quanzi.setVisibility(View.VISIBLE);
        flWarn.setVisibility(View.VISIBLE);
        showMainFragment();
        configView();
    }

    private void configView(){
        new AppConfigUtils().getManPinAppConfig(new AppConfigUtils.IAppConfig() {
            @Override
            public void callback(boolean business) {
                if (business){
                    zhuye.setVisibility(View.GONE);
                    quanzi.setVisibility(View.GONE);
                    flWarn.setVisibility(View.GONE);
                    showShujiaFragment();
                }else {
                    zhuye.setVisibility(View.VISIBLE);
                    quanzi.setVisibility(View.VISIBLE);
                    flWarn.setVisibility(View.VISIBLE);
                    showMainFragment();

                }
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
            MobclickAgent.onKillProcess(this);
            finishAffinity();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.zhuye:
               showMainFragment();
                break;
            case R.id.shujia:
                showShujiaFragment();
                break;
            case R.id.quanzi:
                showBookStore();
                break;
            case R.id.gerenzhongxin:
                changeNavImages(R.id.gerenzhongxin);
                showFragment(personFragment);
                break;
            default:
                break;
        }
    }

    public void showMainFragment(){
        changeNavImages(R.id.zhuye);
        showFragment(mainFragment);
    }


    public void showShujiaFragment() {
        changeNavImages(R.id.shujia);
        showFragment(bookCollectionFragment);
    }

    public void showBookStore(){
        changeNavImages(R.id.quanzi);
        showFragment(mBookRankListFragment);
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



}
