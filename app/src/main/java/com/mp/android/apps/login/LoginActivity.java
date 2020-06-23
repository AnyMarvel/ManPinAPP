package com.mp.android.apps.login;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.WindowManager;

import com.mp.android.apps.R;
import com.mp.android.apps.StoryboardActivity;
import com.mp.android.apps.login.fragment.LoginDailogFragment;
import com.umeng.socialize.UMShareAPI;
import java.util.List;

public class LoginActivity extends StoryboardActivity {

    private LoginMainFragment loginMainFragment;

    private LoginDailogFragment loginDailogFragment;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_main);
        loginDailogFragment = new LoginDailogFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.login_container, loginDailogFragment).commitNow();
   }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onBackPressed() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();

        for (Fragment fragment : fragments) {
            /*如果是自己封装的Fragment的子类  判断是否需要处理返回事件*/
            if (fragment instanceof LoginBaseFragment) {
                if (((LoginBaseFragment) fragment).onBackPressed()) {
                    /*在Fragment中处理返回事件*/
                    return;
                }
            }
        }
        super.onBackPressed();
    }

    public void startActivity() {
        setResult(0);
        overridePendingTransition(0, 0);
        finish();
    }
}
