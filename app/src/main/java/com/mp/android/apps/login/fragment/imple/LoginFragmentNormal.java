package com.mp.android.apps.login.fragment.imple;

import android.text.Editable;
import android.text.TextWatcher;
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
import com.mp.android.apps.login.fragment.ILoginFragmentNormalView;
import com.mp.android.apps.login.presenter.impl.LoginFragmentNormalPresenterImpl;
import com.mp.android.apps.login.utils.LoginUtils;

import com.victor.loading.rotate.RotateLoading;

import java.util.Objects;

public class LoginFragmentNormal extends LoginBaseFragment<LoginFragmentNormalPresenterImpl> implements ILoginFragmentNormalView, View.OnClickListener {
    TextView loginByVerify;
    private EditText loginUserName;
    private ImageView clearLoginInfo;
    private RotateLoading rotateLoading;
    private Button loginButton;
    private EditText loginPassword;

    @Override
    protected LoginFragmentNormalPresenterImpl initInjector() {
        return new LoginFragmentNormalPresenterImpl();
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.login_layout_normal, container, false);
    }

    @Override
    protected void bindView() {
        super.bindView();
        loginByVerify = view.findViewById(R.id.mp_login_by_verify);
        loginByVerify.setOnClickListener(this);
        loginUserName = view.findViewById(R.id.login_contract);
        clearLoginInfo = view.findViewById(R.id.login_clear_contract);
        clearLoginInfo.setOnClickListener(this);
        rotateLoading = view.findViewById(R.id.rotateloading);
        loginButton = view.findViewById(R.id.login_btn);
        loginButton.setOnClickListener(this);
        loginPassword = view.findViewById(R.id.login_password);
        OnClickListener(view, getActivity());
    }

    @Override
    protected void bindEvent() {
        super.bindEvent();
        loginUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    clearLoginInfo.setVisibility(View.VISIBLE);
                } else {
                    clearLoginInfo.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.mp_login_by_verify:
                ((LoginActivity) Objects.requireNonNull(getActivity())).showFragment(0);
                break;
            case R.id.login_clear_contract:
                loginUserName.setText("");
                break;
            case R.id.login_btn:
                if (!LoginUtils.isValidUsername(loginUserName.getText().toString().trim())) {
                    Toast.makeText(getActivity(), "用户名长度不能小于3个字符且不能包含空格", Toast.LENGTH_LONG).show();
                    return;
                } else if (!LoginUtils.isValidPassword(loginPassword.getText().toString().trim())) {
                    Toast.makeText(getActivity(), "密码不合法,请更换密码(不能包含空格中文)", Toast.LENGTH_LONG).show();
                    return;
                }
                rotateLoading.start();
                mPresenter.loginByUserName(loginUserName.getText().toString().trim()
                        , loginPassword.getText().toString().trim());

                break;

            default:
                break;
        }
    }

    @Override
    public void notifyLoginByUserNameResult(boolean success) {
        rotateLoading.stop();
        if (success) {
            Toast.makeText(getContext(), "登录成功", Toast.LENGTH_SHORT).show();
            onDestroy();
            ((LoginActivity) Objects.requireNonNull(getActivity())).startActivity();
        } else {
            Toast.makeText(getContext(), "登录失败,请使用验证码登录或反馈管理员", Toast.LENGTH_SHORT).show();
        }
    }
}
