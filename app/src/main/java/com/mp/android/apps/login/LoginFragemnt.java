package com.mp.android.apps.login;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mp.android.apps.R;
import com.mp.android.apps.login.bean.login.Data;
import com.mp.android.apps.login.bean.login.LoginRootBean;
import com.mp.android.apps.login.network.LoginAPI;
import com.mp.android.apps.login.utils.LoginManager;
import com.mp.android.apps.networkutils.FastJsonConverterFactory;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class LoginFragemnt extends LoginBaseFragment {
    private ImageView ivBack;
    private LoginMainFragment loginMainFragment;
    private EditText et_userName;
    private EditText et_password;
    private Button btn_login;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment_login, container, false);
        loginMainFragment = new LoginMainFragment();
        initView(view);
        return view;
    }

    public void initView(View view) {
        ivBack = (ImageView) view.findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceLoginFragment(loginMainFragment);
            }
        });
        et_userName = view.findViewById(R.id.et_userName);
        et_password = view.findViewById(R.id.et_password);
        btn_login = view.findViewById(R.id.btn_login);
        OnClickListener(view, getActivity());
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://aimanpin.com/")
                        .addConverterFactory(FastJsonConverterFactory.create())
                        .build();
                LoginAPI loginAPI = retrofit.create(LoginAPI.class);
                Call<LoginRootBean> dataclall = loginAPI.login(et_userName.getText().toString(),
                        et_password.getText().toString());
                dataclall.enqueue(new Callback<LoginRootBean>() {
                    @Override
                    public void onResponse(Call<LoginRootBean> call, Response<LoginRootBean> response) {
                        LoginRootBean loginRootBean = response.body();
                        if (loginRootBean.getCode() == 0) {
                            Toast.makeText(getActivity(), "登陆成功", Toast.LENGTH_SHORT).show();
                            Data data = loginRootBean.getData();
                            LoginManager.getInstance().editLoginInfo(data);
                            LoginFragemnt.super.onDestroy();
                            ((LoginActivity) Objects.requireNonNull(getActivity())).startActivity();
                        } else {
                            Toast.makeText(getActivity(), "登陆失败，请尝试快捷登陆", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginRootBean> call, Throwable t) {
                        Toast.makeText(getActivity(), "登陆失败，请尝试快捷登陆", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }

    @Override
    public boolean onBackPressed() {

        replaceLoginFragment(loginMainFragment);

        return true;
    }
}
