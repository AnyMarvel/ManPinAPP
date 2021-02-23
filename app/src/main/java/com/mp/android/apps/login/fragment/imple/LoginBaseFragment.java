package com.mp.android.apps.login.fragment.imple;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;

import com.mp.android.apps.R;
import com.mp.android.apps.login.LoginActivity;
import com.mp.android.apps.login.bean.login.Data;
import com.mp.android.apps.login.bean.login.LoginRootBean;
import com.mp.android.apps.login.network.LoginAPI;
import com.mp.android.apps.login.utils.LoginManager;
import com.mp.android.apps.monke.basemvplib.IPresenter;
import com.mp.android.apps.monke.basemvplib.impl.BaseFragment;
import com.mp.android.apps.networkutils.FastJsonConverterFactory;
import com.mp.android.apps.utils.Logger;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public abstract class LoginBaseFragment<T extends IPresenter> extends BaseFragment<T> {
    @Override
    protected void bindEvent() {
        super.bindEvent();
        onBackPressed();
    }
    /**
     * fragment返回事件处理
     */
    public void onBackPressed() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().setResult(-1);
                requireActivity().overridePendingTransition(0, 0);
                requireActivity().finish();
            }
        });

    }



    public void OnClickListener(View view, Activity activity) {

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
                if (UMShareAPI.get(view.getContext()).isInstall(activity, SHARE_MEDIA.QQ)) {

                    UMShareAPI.get(view.getContext()).getPlatformInfo(activity, SHARE_MEDIA.QQ, loginAuthListener);
                } else {
                    Toast.makeText(getActivity(), "使用QQ快捷登录,请安装最新版本手机QQ客户端", Toast.LENGTH_SHORT).show();

                }
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
