package com.mp.android.apps.main;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.JsonObject;
import com.mp.android.apps.R;
import com.mp.android.apps.StoryboardActivity;
import com.mp.android.apps.book.base.observer.SimpleObserver;
import com.mp.android.apps.login.LoginActivity;
import com.mp.android.apps.login.utils.LoginManager;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.xinlan.imageeditlibrary.editimage.utils.BitmapUtils;
import com.xinlan.imageeditlibrary.picchooser.SquareImageView;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Url;

public class ManpinWXActivity extends StoryboardActivity implements View.OnClickListener {
    TextView textView;
    Button button;
    ImageView iv_back;
    ImageView weixinImage;
    String imageWXUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manpin_weixin);
        textView = findViewById(R.id.tv_title);
        textView.setText("微信群聊");
        button = findViewById(R.id.manpin_save_open_weixin);
        button.setOnClickListener(this);
        iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);
        weixinImage = findViewById(R.id.manpin_weixin_image);

        setWeixinImage();

    }

    private void setWeixinImage() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://aimanpin.com")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // 支持RxJava
                .build();

        retrofit.create(urlImageInterface.class).getWXImageUrl("/appview/wxImageUrl").
                subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        if (!TextUtils.isEmpty(s)) {
                            JSONObject jsonObject = JSON.parseObject(s);
                            String urlimage = jsonObject.getJSONObject("data").getString("wxImageUrl");
                            if (!TextUtils.isEmpty(urlimage)) {
                                imageWXUrl = urlimage;
                                Glide.with(ManpinWXActivity.this).load(urlimage).into(weixinImage);
                            } else {
                                Glide.with(ManpinWXActivity.this).load(R.drawable.manpin_weixin).into(weixinImage);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Glide.with(ManpinWXActivity.this).load(R.drawable.manpin_weixin).into(weixinImage);
                    }
                });

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.manpin_save_open_weixin:
                Acp.getInstance(this).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).build(), new AcpListener() {
                    @Override
                    public void onGranted() {
                        if (!TextUtils.isEmpty(imageWXUrl)) {
                            Glide.with(ManpinWXActivity.this).asBitmap().load(imageWXUrl).into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    MediaStore.Images.Media.insertImage(getContentResolver(), resource, "manpin_wx", "manpin_wx_picture");
                                }
                            });

                        } else {
                            Glide.with(ManpinWXActivity.this).asBitmap().load(R.drawable.manpin_weixin).into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    MediaStore.Images.Media.insertImage(getContentResolver(), resource, "manpin_wx", "manpin_wx_picture");
                                }
                            });
                        }


                        if (isWeixinAvilible()) {
                            Toast.makeText(ManpinWXActivity.this, "二维码保存成功,请使用微信扫描添加好友", Toast.LENGTH_LONG).show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
                                    startActivity(intent);
                                }
                            }, 1000);

                        } else {
                            Toast.makeText(ManpinWXActivity.this, "微信未安装,请使用微博,意见反馈等联系的作者", Toast.LENGTH_LONG).show();
                        }

                    }

                    @Override
                    public void onDenied(List<String> permissions) {

                    }
                });


                break;
            case R.id.iv_back:
                finish();
                break;
            default:
                break;
        }
    }


    /**
     * 判断 用户是否安装微信客户端
     */
    private boolean isWeixinAvilible() {
        final PackageManager packageManager = getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        return false;
    }

    interface urlImageInterface {
        @GET
        Observable<String> getWXImageUrl(@Url String url);
    }
}
