package com.google.android.apps.photolab.storyboard.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.apps.photolab.storyboard.pipeline.ComicIO;
import com.google.android.apps.photolab.storyboard.pipeline.MediaManager;

import com.mp.android.apps.StoryboardActivity;
import com.mp.android.apps.R;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;

import java.util.List;

/**
 * Activity启动页面
 */
public class ComicSplash extends StoryboardActivity implements OnClickListener {
    public static final String VIDEO_PICKER_EXTRA = "video picker";
    public static boolean hasSeenComic = false;

    /**
     * SPLASH_DISPLAY_LENGTH秒后进行操作
     * 确定权限无误后进行操作
     * <p>
     * 首次打开查看缓存目录中是否存在缓存内容 若有则展示缓存内容 若无则清空屏幕内容
     */
    void permissionSuccess() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
//                if (ComicIO.readTexture(ComicIO.LAST_FILTERED_FILENAME) != null) {
//                    ComicSplash.this.startActivity(new Intent(ComicSplash.this, ComicActivity.class));
//                    return;
//                }
                ComicSplash.this.moveToInstructionsScreen();
            }
        });
    }

    /**
     * 跳转到加载界面
     */
    void moveToInstructionsScreen() {
        setContentView(R.layout.load_video_screen);
        ((Button) findViewById(R.id.load_button)).setOnClickListener(this);
        ComicIO.getInstance().clearImageFolder();
        MediaManager.instance().clearAssets();
    }


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (hasSeenComic) {
            ComicActivity.openWithLoadVideoOverlay = true;
            moveToInstructionsScreen();
            return;
        }
        hasSeenComic = true;
        setContentView(R.layout.splash_screen);
        permissionSuccess();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.load_button:

                Acp.getInstance(ComicSplash.this).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).build(), new AcpListener() {
                    @Override
                    public void onGranted() {
                        selectVideo();
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                        Toast.makeText(ComicSplash.this, "读写权限被权限被拒绝,请到设置界面允许被拒绝权限", Toast.LENGTH_LONG).show();
                    }
                });

                break;
            default:
                break;
        }

    }

    /**
     * 视频选择后进行的回调事件
     * StoryboardActivity.PICK_VIDEO_REQUEST 为基类的触发事件
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == StoryboardActivity.PICK_VIDEO_REQUEST) {
            Intent mainIntent = new Intent(this, ComicActivity.class);
            mainIntent.putExtra(VIDEO_PICKER_EXTRA, data.getData());
            startActivity(mainIntent);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        overridePendingTransition(0,0);
        finish();
        return super.onKeyDown(keyCode, event);
    }
}
