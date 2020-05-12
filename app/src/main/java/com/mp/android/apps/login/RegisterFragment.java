package com.mp.android.apps.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.mp.android.apps.R;


import com.mp.android.apps.explore.ExploreSquareActivity;
import com.mp.android.apps.login.bean.login.Data;
import com.mp.android.apps.login.bean.login.LoginRootBean;
import com.mp.android.apps.login.network.LoginAPI;
import com.mp.android.apps.login.utils.LoginManager;
import com.mp.android.apps.login.utils.LoginUtils;
import com.mp.android.apps.networkutils.FastJsonConverterFactory;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.content.Context.MODE_PRIVATE;
import static com.mp.android.apps.StoryboardActivity.LOGIN_SP_STATE;

public class RegisterFragment extends LoginBaseFragment {
    private ImageView ivBack;
    private LoginMainFragment loginMainFragment;
    private EditText et_userName;
    private EditText et_password;
    private EditText nickname;
    private RadioGroup radioGroup;
    private Button btn_register;
    private String sexChoise;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment_register, container, false);
        loginMainFragment = new LoginMainFragment();
        initView(view);
        return view;
    }

    public void initView(View view) {
        ivBack = view.findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceLoginFragment(loginMainFragment);
            }
        });
        et_userName = view.findViewById(R.id.et_userName);
        et_password = view.findViewById(R.id.et_password);
        nickname = view.findViewById(R.id.virtual_nicknamed);
        radioGroup = view.findViewById(R.id.sex_group);
        btn_register = view.findViewById(R.id.btn_register);
        sexChoise = "男";
        OnClickListener(view, getActivity());
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.sex_man) {
                    sexChoise = "男";
                } else {
                    sexChoise = "女";
                }
            }
        });
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!LoginUtils.isValidUsername(et_userName.getText().toString())) {
                    Toast.makeText(getActivity(), "用户名长度不能小于3个字符且不能包含空格", Toast.LENGTH_LONG).show();
                    return;
                } else if (!LoginUtils.isValidPassword(et_password.getText().toString())) {
                    Toast.makeText(getActivity(), "密码不合法,请更换密码(不能包含空格中文)", Toast.LENGTH_LONG).show();
                    return;
                } else if (!LoginUtils.isValidUsername(nickname.getText().toString())) {
                    Toast.makeText(getActivity(), "昵称长度不能小于3个字符且不能包含空格", Toast.LENGTH_LONG).show();
                    return;
                }


                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://aimanpin.com/")
                        .addConverterFactory(FastJsonConverterFactory.create())
                        .build();
                LoginAPI loginAPI = retrofit.create(LoginAPI.class);
                Call<LoginRootBean> dataclall = loginAPI.registAccount(et_userName.getText().toString(),
                        et_password.getText().toString(), nickname.getText().toString(), sexChoise);
                dataclall.enqueue(new Callback<LoginRootBean>() {
                    @Override
                    public void onResponse(Call<LoginRootBean> call, Response<LoginRootBean> response) {
                        LoginRootBean registeRootBean = response.body();
                        if (registeRootBean.getCode() == 0) {
                            Toast.makeText(getActivity(), "注册成功，跳转登录", Toast.LENGTH_SHORT).show();
                            Data data = registeRootBean.getData();
                            LoginManager.getInstance().editLoginInfo(data);
                            RegisterFragment.super.onDestroy();
                            ((LoginActivity) Objects.requireNonNull(getActivity())).startActivity();
                        } else if (registeRootBean.getCode() == -1) {
                            Toast.makeText(getActivity(), "用户已存在,请更换用户名", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "注册失败，请尝试快捷登陆", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginRootBean> call, Throwable t) {
                        Toast.makeText(getActivity(), "注册失败，请尝试快捷登陆", Toast.LENGTH_SHORT).show();

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
