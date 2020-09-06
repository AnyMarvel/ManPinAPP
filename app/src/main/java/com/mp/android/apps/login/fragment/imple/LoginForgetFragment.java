package com.mp.android.apps.login.fragment.imple;

import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mp.android.apps.R;
import com.mp.android.apps.login.LoginActivity;
import com.mp.android.apps.login.fragment.ILoginForgetFragmentView;
import com.mp.android.apps.login.presenter.impl.LoginForgetFragmentPresenterImpl;
import com.mp.android.apps.login.utils.LoginManager;
import com.mp.android.apps.utils.PhoneFormatCheckUtils;
import com.victor.loading.rotate.RotateLoading;

import java.util.Objects;

/**
 * 忘记密码fragment
 */
public class LoginForgetFragment extends LoginBaseFragment<LoginForgetFragmentPresenterImpl> implements ILoginForgetFragmentView, View.OnClickListener {
    private EditText loginForgetContract;
    private EditText passworldVerifyCode;
    private EditText newPassword;
    private EditText confirmNewPassword;
    private Button loginForgetCommit;
    private TextView verifyCode;
    private ImageView clearContractImage;
    private RotateLoading rotateLoading;
    private CheckBox CheckBox;
    @Override
    protected LoginForgetFragmentPresenterImpl initInjector() {
        return new LoginForgetFragmentPresenterImpl();
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.login_lyout_forget_password, container, false);
    }

    @Override
    protected void bindView() {
        super.bindView();
        loginForgetContract = view.findViewById(R.id.login_forget_contract);
        verifyCode = view.findViewById(R.id.login_forget_verification_code);
        verifyCode.setOnClickListener(this);
        newPassword = view.findViewById(R.id.login_new_password);
        confirmNewPassword = view.findViewById(R.id.login_confirm_new_password);
        loginForgetCommit = view.findViewById(R.id.login__forget_commit);
        loginForgetCommit.setOnClickListener(this);
        passworldVerifyCode = view.findViewById(R.id.mp_forget_passworld_verify_code);
        clearContractImage = view.findViewById(R.id.login__forget_clear_contract);
        rotateLoading = view.findViewById(R.id.rotateloading);
        CheckBox=view.findViewById(R.id.login_visible_passworld);
    }

    @Override
    protected void bindEvent() {
        super.bindEvent();
        loginForgetContract.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    clearContractImage.setVisibility(View.VISIBLE);
                } else {
                    clearContractImage.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    newPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    confirmNewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else {
                    newPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    confirmNewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }

            }
        });

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        String userNameStr = loginForgetContract.getText().toString();
        switch (id) {
            case R.id.login__forget_commit:
                if (!PhoneFormatCheckUtils.isPhoneLegal(userNameStr) && !PhoneFormatCheckUtils.checkEmailFormat(userNameStr)) {
                    Toast.makeText(getContext(), "请确认登录信息及验证码是否正确", Toast.LENGTH_LONG).show();
                } else if (!newPassword.getText().toString().equals(confirmNewPassword.getText().toString())) {
                    Toast.makeText(getContext(), "新密码两次输入不一致", Toast.LENGTH_LONG).show();
                } else {
                    if (LoginManager.getInstance().checkLoginCodeByCodeNumber(passworldVerifyCode.getText().toString(), userNameStr)) {
                        rotateLoading.start();
                        mPresenter.resetPasswordByVerifyCode(userNameStr, confirmNewPassword.getText().toString());
                    } else {
                        Toast.makeText(getContext(), "验证码错误,请重新输入", Toast.LENGTH_LONG).show();
                    }
                }

                break;
            case R.id.login_forget_verification_code:
                verifyCode.setClickable(false);
                if (!PhoneFormatCheckUtils.isPhoneLegal(userNameStr) && !PhoneFormatCheckUtils.checkEmailFormat(userNameStr)) {
                    Toast.makeText(getContext(), "请确认电话号码或邮箱格式正确", Toast.LENGTH_SHORT).show();
                    verifyCode.setClickable(true);
                } else {
                    rotateLoading.start();
                    mPresenter.requestVerificationCode(userNameStr);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void updateViewByverification(boolean result) {
        if (result) {
            Toast.makeText(getContext(), "验证码发送成功", Toast.LENGTH_LONG).show();
            /* 倒计时60秒，一次1秒 */
            new CountDownTimer(60 * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    verifyCode.setText(millisUntilFinished / 1000 + "S后重发");
                }

                @Override
                public void onFinish() {
                    verifyCode.setClickable(true);
                    verifyCode.setText("获取验证码");
                }
            }.start();
        } else {
            verifyCode.setClickable(true);
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
            Toast.makeText(getContext(), "未查找到该注册用户,请使用手机或邮箱登录,或联系管理员", Toast.LENGTH_SHORT).show();
        }
    }
}
