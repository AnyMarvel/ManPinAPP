package com.mp.android.apps.livevblank;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.mp.android.apps.MainActivity;
import com.mp.android.apps.R;
import com.mp.android.apps.StoryboardActivity;
import com.mp.android.apps.explore.ExploreSquareActivity;
import com.mp.android.apps.livevblank.bean.ImageDesBean;
import com.mp.android.apps.livevblank.bean.UpdateImageBean;
import com.mp.android.apps.livevblank.network.ExploreAPI;
import com.mp.android.apps.livevblank.network.FileRequestBody;
import com.mp.android.apps.livevblank.network.RetrofitCallback;
import com.mp.android.apps.livevblank.util.ScreenUtil;
import com.mp.android.apps.login.LoginActivity;
import com.mp.android.apps.login.LoginConstans;
import com.mp.android.apps.login.bean.login.Data;
import com.mp.android.apps.login.utils.LoginManager;
import com.mp.android.apps.monke.monkeybook.widget.refreshview.RefreshProgressBar;
import com.mp.android.apps.networkutils.FastJsonConverterFactory;
import com.mp.android.apps.utils.GPSUtils;
import com.mp.android.apps.utils.Logger;
import com.mp.android.apps.utils.ManBitmapUtils;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PreviewActivity extends StoryboardActivity implements View.OnClickListener {
    FrameLayout fl_preview;
    ImageView iv_cancel;
    ImageView iv_card;
    ImageView iv_send;
    private String mBitmapPath;
    private ImageDesBean imageDesBean;
    LinearLayout handle_preview_live;
    TextView tv_title;
    ImageView iv_share;
    private RefreshProgressBar rpbProgress;
    public static final int PREVIEW_LOGIN_SHARE = 1000;
    public static final int PREVIEW_LOGIN_SEND = 1001;
    private Bundle imageBundle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        imageBundle = getIntent().getExtras();
        assert imageBundle != null;
        mBitmapPath = imageBundle.getString("content_bitmap");
        imageDesBean = (ImageDesBean) imageBundle.getSerializable("imageDes");
        initView();
        if (!TextUtils.isEmpty(mBitmapPath)) {
            Glide.with(this).load(mBitmapPath).dontTransform().into(iv_card);
            startPreview();
        }

    }

    private void initView() {
        fl_preview = (FrameLayout) findViewById(R.id.fl_preview);
        rpbProgress = (RefreshProgressBar) findViewById(R.id.rpb_progress);
        iv_card = (ImageView) findViewById(R.id.iv_card);
        ((FrameLayout.LayoutParams) iv_card.getLayoutParams()).height = (int) (((double) (ScreenUtil.getWidth(this) - (getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin) * 2))) * 1.45d);
        iv_cancel = (ImageView) findViewById(R.id.iv_cancel);
        iv_send = (ImageView) findViewById(R.id.iv_send);
        iv_share = (ImageView) findViewById(R.id.iv_share);
        tv_title = findViewById(R.id.tv_title);
        handle_preview_live = (LinearLayout) findViewById(R.id.handle_preview_live);
        iv_send.setOnClickListener(this);
        iv_share.setOnClickListener(this);
        iv_cancel.setOnClickListener(this);
        fl_preview.setVisibility(View.GONE);
        tv_title.setText("预览");

    }


    private void startPreview() {
        AnimatorSet set = new AnimatorSet();
        fl_preview.setVisibility(View.VISIBLE);
        ObjectAnimator anim0 = ObjectAnimator.ofFloat(fl_preview, "alpha", new float[]{0.0f, 1.0f});
        anim0.setDuration(500);
        int anim2StartY = ScreenUtil.getHeight(this);

        int anim1EndY = getResources().getDimensionPixelSize(R.dimen.edit_card_preview_btn_in_margin) + (getResources().getDimensionPixelSize(R.dimen.edit_card_preview_btn_size) * 3 / 2);
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(iv_card, "y", new float[]{-anim2StartY, (float) anim1EndY});
        anim1.setDuration(600);

        int anim2EndY = anim2StartY - ScreenUtil.dp2px(this, 100);

        ObjectAnimator anim2 = ObjectAnimator.ofFloat(handle_preview_live, "y", new float[]{(float) anim2StartY, (float) anim2EndY});
        anim2.setInterpolator(new BounceInterpolator());

        int anim3StartY = -getResources().getDimensionPixelSize(R.dimen.edit_card_preview_btn_size);
        int anim3EndY = anim1EndY + anim3StartY;
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(iv_cancel, "y", new float[]{(float) anim3StartY, anim3EndY});
        anim3.setInterpolator(new BounceInterpolator());
        anim3.setStartDelay(200);
        set.play(anim0).before(anim1);
        set.play(anim1).before(anim2);
        set.play(anim2).with(anim3);
        set.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_cancel:
                finish();
                break;
            case R.id.iv_send:
                Acp.getInstance(getApplicationContext()).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION).build(), new AcpListener() {
                    @Override
                    public void onGranted() {
                        sendImageToOSS(false);

                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                        Toast.makeText(getApplicationContext(), "上传失败,位置权限未开启,请开启位置权限后重新点击上传", Toast.LENGTH_LONG).show();
                    }
                });
                break;
            case R.id.iv_share:
                sendImageToOSS(true);
                break;
            default:
                break;
        }
    }

    private void sendImageToOSS(Boolean isShare) {
        RetrofitCallback<UpdateImageBean> callback1 = new RetrofitCallback<UpdateImageBean>() {

            @Override
            public void onFailure(Call<UpdateImageBean> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "上传失败", Toast.LENGTH_LONG).show();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rpbProgress.setIsAutoLoading(false);
                        iv_send.setClickable(true);
                    }
                });

            }

            @Override
            public void onSuccess(Call<UpdateImageBean> call, Response<UpdateImageBean> response) {
                UpdateImageBean updateImageBean = response.body();
                if (updateImageBean.getCode().equals("0")) {
                    Toast.makeText(getApplicationContext(), "上传成功", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "上传失败", Toast.LENGTH_LONG).show();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rpbProgress.setIsAutoLoading(false);
                        iv_send.setClickable(true);
                        Intent intent = new Intent(getApplicationContext(), ExploreSquareActivity.class);
                        intent.putExtra("toExplore","个人中心");
                        startActivity(intent);

                    }
                });
            }

            @Override
            public void onLoading(long total, long progress) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rpbProgress.setIsAutoLoading(true);
                        iv_send.setClickable(false);
                    }
                });
            }
        };

        if (LoginManager.getInstance().checkLoginInfo()) {
            if (isShare) {
                UMImage image = new UMImage(getApplicationContext(), BitmapFactory.decodeFile(mBitmapPath));//本地文件
                new ShareAction(PreviewActivity.this).withMedia(image)
                        .setDisplayList(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE
                                , SHARE_MEDIA.WEIXIN_FAVORITE, SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE
                                , SHARE_MEDIA.SINA
                        ).open();
            } else {
                Data data = LoginManager.getInstance().getLoginInfo();
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://aimanpin.com/")
                        .addConverterFactory(FastJsonConverterFactory.create())
                        .build();
                ExploreAPI exploreAPI = retrofit.create(ExploreAPI.class);
                File file = new File(ManBitmapUtils.saveBitmap(getApplicationContext(), ManBitmapUtils.compressImage(BitmapFactory.decodeFile(imageDesBean.getImageCachePath()))));
                RequestBody fileRQ = RequestBody.create(MediaType.parse("image/*"), file);
                FileRequestBody<UpdateImageBean> body = new FileRequestBody<>(fileRQ, callback1);
                MultipartBody.Part part = MultipartBody.Part.createFormData("picture", file.getName(), body);
                Map<String, RequestBody> map = new HashMap<String, RequestBody>();
                map.put("uniqueID", toRequestBody(data.getUniqueID()));
                map.put("nickname", toRequestBody(data.getNickname()));
                map.put("time", toRequestBody(String.valueOf(System.currentTimeMillis())));
//                获取位置
                Map<String, String> hashMap = GPSUtils.getInstance(getApplicationContext()).getAddress();
                if (map.size() > 0) {
                    map.put("city", toRequestBody(hashMap.get("locality")));
                    map.put("province", toRequestBody(hashMap.get("sublocality")));
                } else {
                    map.put("city", toRequestBody("未知"));
                    map.put("province", toRequestBody("未知"));
                }
                map.put("imageDes", toRequestBody(JSON.toJSONString(imageDesBean)));
                map.put("share", toRequestBody("0"));

                Call<UpdateImageBean> callback = exploreAPI.uploadImages(part, map);
                callback.enqueue(callback1);
            }
        } else {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            if (isShare) {
                startActivityForResult(intent, PREVIEW_LOGIN_SHARE);
            } else {
                startActivityForResult(intent, PREVIEW_LOGIN_SEND);
            }

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PREVIEW_LOGIN_SHARE && resultCode == 0) {
            sendImageToOSS(true);
        } else {
            sendImageToOSS(false);
        }
    }

    public RequestBody toRequestBody(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != KeyEvent.KEYCODE_BACK) {
            return super.onKeyDown(keyCode, event);
        }
        finish();
        overridePendingTransition(17432576, 17432577);
        return true;
    }


}
