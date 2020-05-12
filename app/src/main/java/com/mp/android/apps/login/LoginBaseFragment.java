package com.mp.android.apps.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mp.android.apps.R;
import com.mp.android.apps.explore.ExploreSquareActivity;
import com.mp.android.apps.login.bean.login.Data;
import com.mp.android.apps.login.bean.login.LoginRootBean;
import com.mp.android.apps.login.network.LoginAPI;
import com.mp.android.apps.login.utils.LoginManager;
import com.mp.android.apps.networkutils.FastJsonConverterFactory;
import com.mp.android.apps.utils.Logger;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.victor.loading.rotate.RotateLoading;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.content.Context.MODE_PRIVATE;
import static com.mp.android.apps.StoryboardActivity.LOGIN_SP_STATE;


public class LoginBaseFragment extends Fragment {
    RotateLoading rotateLoading;

    public void replaceLoginFragment(LoginBaseFragment loginBaseFragment) {
        ((LoginActivity) getActivity()).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.login_container, loginBaseFragment)
                .commit();
    }

    public boolean onBackPressed() {
        return false;
    }


    public void OnClickListener(View view, Activity activity) {
        rotateLoading = new RotateLoading(activity);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(80, 80);
        view.findViewById(R.id.weibo_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UMShareAPI.get(view.getContext()).getPlatformInfo(activity, SHARE_MEDIA.SINA, loginAuthListener);
            }
        });
        view.findViewById(R.id.qq_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UMShareAPI.get(view.getContext()).getPlatformInfo(activity, SHARE_MEDIA.QQ, loginAuthListener);
            }
        });
    }


    public UMAuthListener loginAuthListener = new UMAuthListener() {
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


            Map<String, String> loginInfo = new HashMap<>();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                loginInfo.put(entry.getKey(), entry.getValue());
                if (Logger.isDebug()) {
                    Logger.d("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                }
            }

            String uniqueID = platform.toString() + "_" + loginInfo.get("uid");
            String nickname = loginInfo.get("name");
            String sex = loginInfo.get("gender");
            String iconurl = loginInfo.get("iconurl");
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://aimanpin.com/")
                    .addConverterFactory(FastJsonConverterFactory.create())
                    .build();
            LoginAPI loginAPI = retrofit.create(LoginAPI.class);
            Call<LoginRootBean> uniqueLogin = loginAPI.quickLogin(uniqueID, nickname, sex, iconurl);
            uniqueLogin.enqueue(new Callback<LoginRootBean>() {
                @Override
                public void onResponse(Call<LoginRootBean> call, Response<LoginRootBean> response) {
                    LoginRootBean loginRootBean = response.body();
                    Data data = loginRootBean.getData();
                    LoginManager.getInstance().editLoginInfo(data);
                    ((LoginActivity) Objects.requireNonNull(getActivity())).startActivity();
                }

                @Override
                public void onFailure(Call<LoginRootBean> call, Throwable t) {

                }
            });
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

}
