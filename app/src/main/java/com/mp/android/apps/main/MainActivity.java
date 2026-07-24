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
import com.mp.android.apps.download.CheckUpdateUtils;
import com.mp.android.apps.main.personal.PersonFragment;
import com.mp.android.apps.main.home.view.MyImageTextView;
import com.mp.android.apps.base.impl.BaseFragment;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    PersonFragment personFragment;
    BookCollectionFragment bookCollectionFragment;
    BookRankListFragment mBookRankListFragment;
    MyImageTextView shujia;
    MyImageTextView quanzi;
    MyImageTextView wode;
    private FrameLayout flWarn;
    private ImageView flWarnClose;

    private void hidenFragment(FragmentTransaction transaction) {
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
        if (fragment == null) {
            return;
        }
        //状态已保存时不再提交，避免 onSaveInstanceState 之后提交崩溃
        if (getSupportFragmentManager().isStateSaved()) {
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hidenFragment(transaction);
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.main_contain, fragment).show(fragment);
        }
        //同步提交：确保 isAdded() 立即生效，避免快速连点导致对同一 Fragment 重复 add 而崩溃
        transaction.commitNowAllowingStateLoss();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setImmersiveStatusBar();
        setContentView(R.layout.main_layout);
        personFragment = new PersonFragment();
        bookCollectionFragment = new BookCollectionFragment();

        mBookRankListFragment=new BookRankListFragment();
        initViews();
        //检查更新
        try {
            CheckUpdateUtils.getInstance().checkUpdata(this,false);
        }catch (Exception e){
            android.util.Log.e("MainActivity", "checkUpdate failed", e);
        }
    }



    /**
     * 沉浸式状态栏：状态栏透明、内容延伸到状态栏下方(main_layout 根布局 fitsSystemWindows=true
     * 会自动为内容让出状态栏高度，故不会被遮挡)；因页面为浅色底，用 LIGHT_STATUS_BAR 让状态栏图标变深色可见。
     */
    private void setImmersiveStatusBar() {
        android.view.Window window = getWindow();
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        window.getDecorView().setSystemUiVisibility(flags);
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
    }

    private void initViews() {
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
        quanzi.setVisibility(View.VISIBLE);
        flWarn.setVisibility(View.VISIBLE);
        //首页已移除，默认落在书架
        showShujiaFragment();
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

    public void showShujiaFragment() {
        changeNavImages(R.id.shujia);
        showFragment(bookCollectionFragment);
    }

    public void showBookStore(){
        changeNavImages(R.id.quanzi);
        showFragment(mBookRankListFragment);
    }
    private void changeNavImages(int id) {
        shujia.setImgResource(R.drawable.shujia);
        quanzi.setImgResource(R.drawable.quanzi);
        wode.setImgResource(R.drawable.gerenzhongxin);
        switch (id) {
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
