package com.mp.android.apps.login.fragment;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mp.android.apps.R;
import com.mp.android.apps.login.LoginActivity;
import com.mp.android.apps.login.LoginBaseFragment;
import com.mp.android.apps.login.bean.login.Data;
import com.mp.android.apps.login.bean.login.LoginRootBean;
import com.mp.android.apps.login.network.LoginAPI;
import com.mp.android.apps.login.utils.LoginManager;
import com.mp.android.apps.login.utils.LoginUtils;
import com.mp.android.apps.networkutils.FastJsonConverterFactory;

import java.util.Objects;

import androidx.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by shanxia on 2020/6/13.
 */

public class LoginDailogFragment extends LoginBaseFragment implements View.OnClickListener {

    private EditText loginUserName;
    private EditText loginPassword;
    private Button loginBtn;
    private CircleImageView loginHeader;
    private TextView tv_forgotPwd;
    private TextView tv_register;
    private ImageView img_hiddenPwd;

    private boolean isHideFirst = true;// 输入框密码是否是隐藏的，默认为true


    private RegisterDailogFragment registerDailogFragment;

    public static final String RESPONSE = "response";

    public static LoginDailogFragment dailogFragment;

    public static LoginDailogFragment newInstance() {

        if (dailogFragment == null) {
            dailogFragment = new LoginDailogFragment();
        }
        return dailogFragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        registerDailogFragment = new RegisterDailogFragment();
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.login_dialog_login, null);
        initView(view);
        return view;
    }

    public void initView(View view) {
        loginHeader = view.findViewById(R.id.login_header);
        loginUserName = view.findViewById(R.id.login_userName);
        loginPassword = view.findViewById(R.id.login_password);
        loginBtn = view.findViewById(R.id.login_btn);
        tv_forgotPwd = view.findViewById(R.id.tv_forgot_pwd);
        tv_register = view.findViewById(R.id.tv_register);
        img_hiddenPwd = view.findViewById(R.id.login_hidden_pwd);
        loginBtn.setOnClickListener(this);
        tv_forgotPwd.setOnClickListener(this);
        tv_register.setOnClickListener(this);
        img_hiddenPwd.setOnClickListener(this);
        OnClickListener(view, getActivity());

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                if (!LoginUtils.isValidUsername(loginUserName.getText().toString())) {
                    Toast.makeText(getActivity(), "用户名长度不能小于3个字符且不能包含空格", Toast.LENGTH_LONG).show();
                    return;
                } else if (!LoginUtils.isValidPassword(loginPassword.getText().toString())) {
                    Toast.makeText(getActivity(), "密码不合法,请更换密码(不能包含空格中文)", Toast.LENGTH_LONG).show();
                    return;
                }
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://aimanpin.com/")
                        .addConverterFactory(FastJsonConverterFactory.create())
                        .build();
                LoginAPI loginAPI = retrofit.create(LoginAPI.class);
                Call<LoginRootBean> dataclall = loginAPI.login(loginUserName.getText().toString(),
                        loginPassword.getText().toString());
                dataclall.enqueue(new Callback<LoginRootBean>() {
                    @Override
                    public void onResponse(Call<LoginRootBean> call, Response<LoginRootBean> response) {
                        LoginRootBean loginRootBean = response.body();
                        if (loginRootBean.getCode() == 0) {
                            Toast.makeText(getActivity(), "登陆成功", Toast.LENGTH_SHORT).show();
                            Data data = loginRootBean.getData();
                            LoginManager.getInstance().editLoginInfo(data);
                            onDestroy();
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
                break;
            case R.id.login_hidden_pwd:
                if (isHideFirst) {
                    img_hiddenPwd.setImageResource(R.mipmap.pwd_visible);
                    //密文
                    HideReturnsTransformationMethod method1 = HideReturnsTransformationMethod.getInstance();
                    loginPassword.setTransformationMethod(method1);
                    loginPassword.setSelection(loginPassword.getText().length());
                    isHideFirst = false;
                } else {
                    img_hiddenPwd.setImageResource(R.mipmap.pwd_invisible);
                    //密文
                    TransformationMethod method = PasswordTransformationMethod.getInstance();
                    loginPassword.setTransformationMethod(method);
                    loginPassword.setSelection(loginPassword.getText().length());
                    isHideFirst = true;
                }
                break;

            case R.id.tv_register:
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.login_container, registerDailogFragment).commitNow();
                break;

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        Objects.requireNonNull(getActivity()).setResult(-1);
        Objects.requireNonNull(getActivity()).overridePendingTransition(0, 0);
        Objects.requireNonNull(getActivity()).finish();
        return true;
    }

}
