package com.mp.android.apps.main;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.mp.android.apps.R;

import com.mp.android.apps.StoryboardActivity;
import com.mp.android.apps.explore.ExploreSquareActivity;
import com.mp.android.apps.login.LoginActivity;
import com.mp.android.apps.login.utils.LoginManager;
import com.mp.android.apps.main.fragment.MainFragment;
import com.mp.android.apps.main.view.MyImageTextView;
import com.mp.android.apps.monke.monkeybook.view.impl.BookMainActivity;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;

import java.util.List;


public class MainActivity extends StoryboardActivity implements View.OnClickListener {
    MainFragment mainFragment;
    MyImageTextView zhuye;
    MyImageTextView shujia;
    MyImageTextView quanzi;
    MyImageTextView wode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        LoginManager.getInstance().initSP(this).initData();
        mainFragment = new MainFragment();
        getFragmentManager().beginTransaction().replace(R.id.main_contain, mainFragment).commit();
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
//                Intent intentzhuye = new Intent(MainActivity.this, MainActivity.class);
//                startActivity(intentzhuye);
                break;
            case R.id.shujia:
                Intent intentBook = new Intent(MainActivity.this, BookMainActivity.class);
                startActivity(intentBook);
                break;
            case R.id.quanzi:
                gotoExplore("广场");
                break;
            case R.id.gerenzhongxin:

                break;
            default:
                break;
        }
    }
}
