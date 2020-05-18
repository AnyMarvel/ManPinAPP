package com.mp.android.apps;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.apps.photolab.storyboard.activity.ComicSplash;
import com.mp.android.apps.explore.ExploreSquareActivity;
import com.mp.android.apps.livevblank.ChoiceItemActivity;
import com.mp.android.apps.login.LoginActivity;
import com.mp.android.apps.login.utils.LoginManager;
import com.mp.android.apps.monke.monkeybook.view.impl.BookMainActivity;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;

import java.util.List;


public class MainActivity extends StoryboardActivity implements View.OnClickListener {
    LadderLayout manhua;
    LadderLayout story;
    LadderLayout postcards;
    LadderLayout diagram;
    FrameLayout home_bottom;
    private String toExplore;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ladderlayout);
        LoginManager.getInstance().initSP(this).initData();
        manhua = findViewById(R.id.ldder1);
        story = findViewById(R.id.ldder2);
        postcards = findViewById(R.id.ldder3);
        diagram = findViewById(R.id.ldder4);
        home_bottom = findViewById(R.id.home_bottom);
        manhua.setOnClickListener(this);
        story.setOnClickListener(this);
        postcards.setOnClickListener(this);
        diagram.setOnClickListener(this);
        home_bottom.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.ldder1:
                Acp.getInstance(MainActivity.this).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).build(), new AcpListener() {
                    @Override
                    public void onGranted() {
                        Intent intentComic = new Intent(MainActivity.this, ComicSplash.class);
                        startActivity(intentComic);
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                    }
                });
                break;
            case R.id.ldder2:
                Intent intentBook = new Intent(MainActivity.this, BookMainActivity.class);
                startActivity(intentBook);
                break;
            case R.id.ldder3:
                Intent intentPostcard = new Intent(MainActivity.this, ChoiceItemActivity.class);
                startActivity(intentPostcard);
                break;
            case R.id.ldder4:
                toExplore = "广场";
                gotoExplore();
                break;
            case R.id.home_bottom:
                toExplore = "个人中心";
                gotoExplore();
                break;
            default:
                break;
        }
    }

    private static final int EXPLORESQUARE_LOGIN = 1005;

    private void gotoExplore() {
        Intent intent = new Intent(MainActivity.this, ExploreSquareActivity.class);
        intent.putExtra("toExplore", toExplore);
        Acp.getInstance(getApplicationContext()).request(new AcpOptions.Builder()
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION).build(), new AcpListener() {
            @Override
            public void onGranted() {
                if (LoginManager.getInstance().checkLoginInfo()) {
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivityForResult(intent, EXPLORESQUARE_LOGIN);
                }
            }

            @Override
            public void onDenied(List<String> permissions) {
            }
        });
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EXPLORESQUARE_LOGIN && resultCode == 0) {
            gotoExplore();
        }
    }
}
