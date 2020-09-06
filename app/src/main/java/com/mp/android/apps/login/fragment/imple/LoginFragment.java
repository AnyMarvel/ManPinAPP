package com.mp.android.apps.login.fragment.imple;

import android.os.CountDownTimer;
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

import androidx.activity.OnBackPressedCallback;

import com.mp.android.apps.R;
import com.mp.android.apps.login.LoginActivity;
import com.mp.android.apps.login.fragment.ILoginFragmentView;
import com.mp.android.apps.login.presenter.ILoginFragmentPresenter;
import com.mp.android.apps.login.presenter.impl.LoginFragmentPresenterImpl;
import com.mp.android.apps.login.utils.LoginManager;
import com.mp.android.apps.utils.PhoneFormatCheckUtils;
import com.victor.loading.rotate.RotateLoading;

import java.util.Objects;


public class LoginFragment extends LoginBaseFragment<ILoginFragmentPresenter> implements ILoginFragmentView, View.OnClickListener {
    private TextView verificationCode;
    private EditText cantractInfo;
    private ImageView clearLoginInfo;
    private RotateLoading rotateLoading;
    private Button loginButton;
    private EditText loginPassworld;
    private TextView loginByUserName;

    @Override
    protected ILoginFragmentPresenter initInjector() {
        return new LoginFragmentPresenterImpl();
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.login_layout, container, false);
    }

    @Override
    protected void bindView() {
        super.bindView();
        verificationCode = view.findViewById(R.id.login_verification_code);
        verificationCode.setOnClickListener(this);
        cantractInfo = view.findViewById(R.id.login_contract);
        clearLoginInfo = view.findViewById(R.id.login_clear_contract);
        clearLoginInfo.setOnClickListener(this);
        rotateLoading = view.findViewById(R.id.rotateloading);
        loginButton = view.findViewById(R.id.login_btn);
        loginButton.setOnClickListener(this);
        loginPassworld = view.findViewById(R.id.login_password);
        loginByUserName = view.findViewById(R.id.mp_login_by_userName);
        loginByUserName.setOnClickListener(this);
        OnClickListener(view, getActivity());
    }

    @Override
    protected void bindEvent() {
        super.bindEvent();
        cantractInfo.addTextChangedListener(new TextWatcher() {
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
    public void updateViewByverification(boolean result) {
        if (result) {
            Toast.makeText(getContext(), "验证码发送成功", Toast.LENGTH_LONG).show();
            /* 倒计时60秒，一次1秒 */
            new CountDownTimer(60 * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    verificationCode.setText(millisUntilFinished / 1000 + "S后重发");
                }

                @Override
                public void onFinish() {
                    verificationCode.setClickable(true);
                    verificationCode.setText("获取验证码");
                }
            }.start();
        } else {
            verificationCode.setClickable(true);
            Toast.makeText(getContext(), "请确认电话号码或邮箱格式正确,\n若无法登录请到我的反馈开发者或微博留言", Toast.LENGTH_LONG).show();
        }
        rotateLoading.stop();
    }

    @Override
    public void notifyLoginStatus(boolean success) {
        rotateLoading.stop();
        if (success) {
            Toast.makeText(getContext(), "登录成功", Toast.LENGTH_SHORT).show();
            onDestroy();
            ((LoginActivity) Objects.requireNonNull(getActivity())).startActivity();
        } else {
            Toast.makeText(getContext(), "登录失败,请重试或反馈管理员", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        String userNameStr = cantractInfo.getText().toString();
        switch (id) {
            case R.id.login_verification_code:
                verificationCode.setClickable(false);
                if (!PhoneFormatCheckUtils.isPhoneLegal(userNameStr) && !PhoneFormatCheckUtils.checkEmailFormat(userNameStr)) {
                    Toast.makeText(getContext(), "请确认电话号码或邮箱格式正确", Toast.LENGTH_SHORT).show();
                    verificationCode.setClickable(true);
                } else {
                    rotateLoading.start();
                    mPresenter.requestVerificationCode(userNameStr);
                }
                break;
            case R.id.login_clear_contract:
                cantractInfo.setText("");
                break;
            case R.id.login_btn:
                if (!PhoneFormatCheckUtils.isPhoneLegal(userNameStr) && !PhoneFormatCheckUtils.checkEmailFormat(userNameStr)) {
                    Toast.makeText(getContext(), "请确认登录信息及验证码是否正确", Toast.LENGTH_LONG).show();
                } else {
                    if (LoginManager.getInstance().checkLoginCodeByCodeNumber(loginPassworld.getText().toString(), userNameStr)) {
                        rotateLoading.start();
                        mPresenter.loginByContractInfo(userNameStr);
                    } else {
                        Toast.makeText(getContext(), "验证码错误,请重新输入", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case R.id.mp_login_by_userName:
                ((LoginActivity) Objects.requireNonNull(getActivity())).showFragment(1);
                break;
            default:
                break;
        }
    }

}
