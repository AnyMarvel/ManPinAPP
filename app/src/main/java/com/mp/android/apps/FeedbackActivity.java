package com.mp.android.apps;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FeedbackActivity extends StoryboardActivity {
    EditText feedbackContent;
    EditText contact_information;
    Button feedback_submit;
    Spinner feedback_type_spinner;
    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback_layout);
        feedbackContent = findViewById(R.id.feedback_content);
        contact_information = findViewById(R.id.contact_information);
        feedback_submit = findViewById(R.id.feedback_submit);
        feedback_type_spinner = findViewById(R.id.feedback_type_spinner);

        //获取手机号码
        findViewById(R.id.feedback_title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedbackActivity.super.onBackPressed();
            }
        });
        feedback_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String feedbackContentStr = feedbackContent.getText().toString();
                String contact_informationStr = contact_information.getText().toString();
                String errorType = feedback_type_spinner.getSelectedItem().toString();


                FormBody formBody = new FormBody.Builder().add("contactStr", contact_informationStr)
                        .add("contentStr", feedbackContentStr)
                        .add("mobilePhoneModel", android.os.Build.MODEL)
                        .add("typeSpinnerStr", errorType)
                        .add("sdkVersion", android.os.Build.VERSION.SDK)
                        .add("softwareVersion", getAppVersionString())
                        .add("systemVersion", android.os.Build.VERSION.RELEASE).build();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new OkHttpClient().newCall(
                                new Request.Builder().url("http://211.159.162.223/manpin/feedback")
                                        .post(formBody).build()).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(FeedbackActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String responses = response.body().string();
                                if (response.isSuccessful()) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(FeedbackActivity.this, "感谢反馈,我们将优先处理您的意见", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
                ).start();

            }
        });
    }

}
