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
import android.widget.RadioGroup;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by shanxia on 2020/6/13.
 */

public class RegisterDailogFragment extends LoginBaseFragment implements View.OnClickListener {

    private EditText registerUserName;
    private EditText registerPassword;
    private EditText registerNickname;
    private RadioGroup radioGroup;
    private Button registerBtn;
    private CircleImageView registerHeader;
    private ImageView img_hiddenPwd;
    private String sexChoise;

    private boolean isHideFirst = true;// 输入框密码是否是隐藏的，默认为true
    private LoginDailogFragment loginDailogFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.login_dialog_register, null);
        initView(view);
        loginDailogFragment = new LoginDailogFragment();
        return view;
    }


    public void initView(View view) {
        registerHeader = view.findViewById(R.id.register_header);
        registerUserName = view.findViewById(R.id.register_userName);
        registerPassword = view.findViewById(R.id.register_password);
        registerNickname = view.findViewById(R.id.register_nickname);
        registerBtn = view.findViewById(R.id.btn_register);
        img_hiddenPwd = view.findViewById(R.id.register_hidden_pwd);
        radioGroup = view.findViewById(R.id.sex_group);
        registerBtn.setOnClickListener(this);
        img_hiddenPwd.setOnClickListener(this);
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

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                if (!LoginUtils.isValidUsername(registerUserName.getText().toString())) {
                    Toast.makeText(getActivity(), "用户名长度不能小于3个字符且不能包含空格", Toast.LENGTH_LONG).show();
                    return;
                } else if (!LoginUtils.isValidPassword(registerPassword.getText().toString())) {
                    Toast.makeText(getActivity(), "密码不合法,请更换密码(不能包含空格中文)", Toast.LENGTH_LONG).show();
                    return;
                } else if (!LoginUtils.isValidUsername(registerNickname.getText().toString())) {
                    Toast.makeText(getActivity(), "昵称长度不能小于3个字符且不能包含空格", Toast.LENGTH_LONG).show();
                    return;
                }


                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://aimanpin.com/")
                        .addConverterFactory(FastJsonConverterFactory.create())
                        .build();
                LoginAPI loginAPI = retrofit.create(LoginAPI.class);
                Call<LoginRootBean> dataclall = loginAPI.registAccount(registerUserName.getText().toString(),
                        registerPassword.getText().toString(), registerNickname.getText().toString(), sexChoise);
                dataclall.enqueue(new Callback<LoginRootBean>() {
                    @Override
                    public void onResponse(Call<LoginRootBean> call, Response<LoginRootBean> response) {
                        LoginRootBean registeRootBean = response.body();
                        if (registeRootBean.getCode() == 0) {
                            Toast.makeText(getActivity(), "注册成功，跳转登录", Toast.LENGTH_SHORT).show();
                            Data data = registeRootBean.getData();
                            LoginManager.getInstance().editLoginInfo(data);
                            onDestroy();
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
                break;
            case R.id.register_hidden_pwd:
                if (isHideFirst) {
                    img_hiddenPwd.setBackground(getResources().getDrawable(R.mipmap.pwd_visible));
                    //密文
                    HideReturnsTransformationMethod method1 = HideReturnsTransformationMethod.getInstance();
                    registerPassword.setTransformationMethod(method1);
                    registerPassword.setSelection(registerPassword.getText().length());
                    isHideFirst = false;
                } else {
                    img_hiddenPwd.setBackground(getResources().getDrawable(R.mipmap.pwd_invisible));
                    //密文
                    TransformationMethod method = PasswordTransformationMethod.getInstance();
                    registerPassword.setTransformationMethod(method);
                    registerPassword.setSelection(registerPassword.getText().length());
                    isHideFirst = true;
                }
                break;

        }
    }

    @Override
    public boolean onBackPressed() {
        replaceLoginFragment(loginDailogFragment);
        return true;
    }
}
