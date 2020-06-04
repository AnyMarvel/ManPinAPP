package com.mp.android.apps.explore;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mp.android.apps.R;
import com.mp.android.apps.StoryboardActivity;
import com.mp.android.apps.explore.fragment.DefaultExploreFragment;
import com.mp.android.apps.explore.fragment.StaggeredExploreFragment;
import com.mp.android.apps.login.LoginActivity;
import com.mp.android.apps.login.LoginBaseFragment;
import com.mp.android.apps.login.bean.login.Data;
import com.mp.android.apps.login.utils.LoginManager;
import com.mp.android.apps.main.MainActivity;
import com.mp.android.apps.utils.GPSUtils;
import com.mp.android.apps.utils.Logger;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ExploreSquareActivity extends StoryboardActivity {
    /**
     * 默认分享模式
     */
    DefaultExploreFragment defaultExloreFragment;
    /**
     * 瀑布流分享模式
     */
    StaggeredExploreFragment staggeredExploreFragment;
    /**
     * 登出按钮
     */
    RelativeLayout mLogOut;
    /**
     * 弹出抽屉布局
     */
    DrawerLayout exploreDrawerLayout;
    /**
     * 头像
     */
    CircleImageView mUserLogo;
    /**
     * 昵称
     */
    TextView mUsername;
    /**
     * 性别
     */
    TextView mUserSex;
    /**
     * 地区
     */
    TextView mUserArea;

    ImageView iv_menu;

    TextView tv_title;
    String toExplore;

    CheckBox cb_scan;

    Bundle exploreBundle;

    LinearLayout agreeclick;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        toExplore = getIntent().getStringExtra("toExplore");
        setContentView(R.layout.explore_activity);
        initActivityView();
        initFragment();

    }

    private void initActivityView() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText(toExplore);
        cb_scan = findViewById(R.id.cb_scan);
        agreeclick=findViewById(R.id.agreeclick);
        agreeclick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://www.aimanpin.com/app/privacy");//此处填链接
                intent.setData(content_url);
                startActivity(intent);
            }
        });
        exploreBundle = new Bundle();
        exploreBundle.putString("toExplore", toExplore);
        cb_scan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    staggeredExploreFragment.setArguments(exploreBundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.explore_contain, staggeredExploreFragment).commit();
                } else {
                    defaultExloreFragment.setArguments(exploreBundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.explore_contain, defaultExloreFragment).commit();
                }
            }
        });
    }

    private void initFragment() {
        staggeredExploreFragment = new StaggeredExploreFragment();
        defaultExloreFragment = new DefaultExploreFragment();
        defaultExloreFragment.setArguments(exploreBundle);
        getSupportFragmentManager().beginTransaction().add(R.id.explore_contain, defaultExloreFragment).commit();
        initView();
        initData();
    }

    private void initView() {
        mLogOut = (RelativeLayout) findViewById(R.id.mLogOut);
        exploreDrawerLayout = (DrawerLayout) findViewById(R.id.explore_drawerlayout);
        mUserLogo = (CircleImageView) findViewById(R.id.mUserLogo);
        mUsername = (TextView) findViewById(R.id.mUsername);
        mUserSex = (TextView) findViewById(R.id.mUserSex);
        iv_menu = (ImageView) findViewById(R.id.iv_menu);
        mUserArea = (TextView) findViewById(R.id.mUserArea);
        iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exploreDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    exploreDrawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    exploreDrawerLayout.openDrawer(Gravity.LEFT);
                }
            }
        });
        mLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (LoginManager.getInstance().checkLoginInfo()) {
                    Data data = LoginManager.getInstance().getLoginInfo();
                    if (data.getUniqueID().startsWith("manpin_")) {
                        LoginManager.getInstance().editLogoutInfo();
                        logoutAuthListener.onComplete(null,1,null);
                    } else if (data.getUniqueID().startsWith("QQ_")) {
                        UMShareAPI.get(getApplicationContext()).deleteOauth(ExploreSquareActivity.this, SHARE_MEDIA.QQ, logoutAuthListener);
                    } else if (data.getUniqueID().startsWith("SINA_")) {
                        UMShareAPI.get(getApplicationContext()).deleteOauth(ExploreSquareActivity.this, SHARE_MEDIA.SINA, logoutAuthListener);
                    }
                }
            }
        });
    }

    public UMAuthListener logoutAuthListener = new UMAuthListener() {
        /**
         * @desc 授权开始的回调
         * @param platform 平台名称
         */
        @Override
        public void onStart(SHARE_MEDIA platform) {

        }

        /**
         * @desc 授权成功的回调
         * @param platform 平台名称
         * @param action 行为序号，开发者用不上
         * @param data 用户资料返回
         */
        @Override
        public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
            LoginManager.getInstance().editLogoutInfo();
            Toast.makeText(getApplicationContext(), "注销登录成功", Toast.LENGTH_SHORT).show();
            finish();
            Intent intent = new Intent(ExploreSquareActivity.this, MainActivity.class);
            startActivity(intent);
        }

        /**
         * @desc 授权失败的回调
         * @param platform 平台名称
         * @param action 行为序号，开发者用不上
         * @param t 错误原因
         */
        @Override
        public void onError(SHARE_MEDIA platform, int action, Throwable t) {
            Logger.d("失败：" + t.getMessage());
        }

        /**
         * @desc 授权取消的回调
         * @param platform 平台名称
         * @param action 行为序号，开发者用不上
         */
        @Override
        public void onCancel(SHARE_MEDIA platform, int action) {
            Logger.d("取消回调");
        }
    };

    public void initData() {
        Data data = LoginManager.getInstance().getLoginInfo();
        if (!TextUtils.isEmpty(data.getUsericon())) {
            Glide.with(this).load(Uri.parse(data.getUsericon())).into(mUserLogo);
        }
        mUsername.setText(data.getNickname());
        if (!TextUtils.isEmpty(data.getSex())) {
            mUserSex.setText(data.getSex());
        }
        Map<String, String> map = GPSUtils.getInstance(this).getAddress();
        if (map.size() > 0) {
            String location = map.get("locality") + " " + map.get("sublocality");
            mUserArea.setText(location);
        } else {
            mUserArea.setText("");
        }

    }
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        Intent intent = new Intent(ExploreSquareActivity.this, MainActivity.class);
//        startActivity(intent);
//    }
}
