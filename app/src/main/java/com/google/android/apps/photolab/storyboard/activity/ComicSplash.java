package com.google.android.apps.photolab.storyboard.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.apps.photolab.storyboard.download.DownloadListener;
import com.google.android.apps.photolab.storyboard.download.DownloadUtil;
import com.google.android.apps.photolab.storyboard.download.ZipUtils;
import com.google.android.apps.photolab.storyboard.module.ComicSplashModuleImpl;
import com.google.android.apps.photolab.storyboard.pipeline.ComicIO;
import com.google.android.apps.photolab.storyboard.pipeline.MediaManager;

import com.google.android.apps.photolab.storyboard.soloader.SoFileUtils;
import com.google.android.apps.photolab.storyboard.soloader.SoStatus;
import com.google.android.apps.photolab.storyboard.views.FlikerProgressBar;
import com.google.android.apps.photolab.storyboard.views.StoryAlterDialog;
import com.mp.android.apps.StoryboardActivity;
import com.mp.android.apps.R;
import com.mp.android.apps.book.base.observer.SimpleObserver;
import com.mp.android.apps.utils.Logger;
import com.google.android.apps.photolab.storyboard.download.MD5Utils;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Activity启动页面
 */
public class ComicSplash extends StoryboardActivity implements OnClickListener {
    public static final String VIDEO_PICKER_EXTRA = "video picker";
    public static boolean hasSeenComic = false;
    FlikerProgressBar flikerProgressBar;
    Button button;

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
        button = ((Button) findViewById(R.id.load_button));
        button.setOnClickListener(this);
        ComicIO.getInstance().clearImageFolder();
        MediaManager.instance().clearAssets();
        flikerProgressBar = findViewById(R.id.round_flikerbar);
        flikerProgressBar.setVisibility(View.GONE);
        button.setVisibility(View.VISIBLE);

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

    StoryAlterDialog storyAlterDialog;

    /**
     * 初始化下载弹窗dialog
     */
    public void initStoryDialog() {
        storyAlterDialog = new StoryAlterDialog(ComicSplash.this);
        storyAlterDialog.show();
        storyAlterDialog.setCancleButtonOnclik(new OnClickListener() {
            @Override
            public void onClick(View v) {
                storyAlterDialog.dismiss();
            }
        });
        storyAlterDialog.setConfirmButtonOnclick(new OnClickListener() {
            @Override
            public void onClick(View v) {
                storyAlterDialog.dismiss();
                requestNativeSo();
            }
        });
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
                        String zipfilePath = DownloadUtil.PATH_CHALLENGE_VIDEO + "/" + downloadFileName;
                        String path1 = SoStatus.findNativeLibraryPath(ComicSplash.this, "objectdetector_native");
                        String path2 = SoStatus.findNativeLibraryPath(ComicSplash.this, "facedetector_native");
                        if (!TextUtils.isEmpty(path1) && !TextUtils.isEmpty(path2)) {
                            selectVideo();
                        } else {
                            reloadSoFile(zipfilePath, false);
                        }
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

    private final String downloadFileName = "mpnative.zip";
    private final String solibs = DownloadUtil.PATH_CHALLENGE_VIDEO + "/libs";

    private void requestNativeSo() {
        ComicSplashModuleImpl.getInstance().getDownloadUrl()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        Document doc = Jsoup.parse(s);
                        Elements elements = doc.getElementsByClass("directDownload");
                        if (elements != null && elements.size() > 0) {
                            String downloadUrl = doc.getElementsByClass("directDownload").get(0).getElementsByTag("a").attr("href");
                            Uri uri = Uri.parse(downloadUrl);
                            String host = uri.getScheme() + "://" + uri.getHost();
                            String path = downloadUrl.replace(host, "");
                            Logger.d("hostname", host);
                            Logger.d("path", path);

                            new DownloadUtil(host).downloadFile(path, downloadFileName, new DownloadListener() {
                                @Override
                                public void onStart() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            flikerProgressBar.setVisibility(View.VISIBLE);
                                            button.setVisibility(View.GONE);
                                        }
                                    });
                                    Logger.d("******************************" + "onStart");
                                }

                                @Override
                                public void onProgress(int currentLength) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            flikerProgressBar.setProgress(currentLength);
                                        }
                                    });

                                    Logger.d("******************************" + "onProgress:" + currentLength);
                                }

                                @Override
                                public void onFinish(String localPath) {
                                    Logger.d("******************************" + "localPath" + localPath);
                                    reloadSoFile(localPath, true);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            flikerProgressBar.finishLoad();
                                            flikerProgressBar.setVisibility(View.GONE);
                                            button.setVisibility(View.VISIBLE);
                                        }
                                    });


                                }

                                @Override
                                public void onFailure() {
                                    Logger.d("******************************" + "onFailure");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "加载组件失败,请点击重试", Toast.LENGTH_SHORT).show();
                                            flikerProgressBar.finishLoad();
                                            flikerProgressBar.setVisibility(View.GONE);
                                            button.setVisibility(View.VISIBLE);
                                        }
                                    });

                                }
                            });
                        }

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });


    }

    private void reloadSoFile(String localPath, boolean download) {
        if (SoFileUtils.hasLoadSofile(this)) {
            //APP data/data/***/aap_libs中存在要使用的两个so文件,直接修改native加载内存映射
            selectVideo();
        } else if (MD5Utils.checkFileMd5(solibs + "/libfacedetector_native.so", "c261e13774360980e194fa40c02016f8")
                && MD5Utils.checkFileMd5(solibs + "/libobjectdetector_native.so", "90c8085b4ac37a743d1530f0c1b29ebc")) {
            //APP data/data/***/aap_libs中 不存在要使用的两个so文件, sdk卡存在,则先复制,后修改native加载内存映射
            SoFileUtils.loadSoFile(getApplicationContext(), solibs);
            selectVideo();
        } else if (MD5Utils.checkFileMd5(localPath, "d7bc16e438bc4f9aeeeb96add8640522")) {
            // sdk不存在解压后的libs目录则解压md5校验通过的zip文件,复制到app_libs目录再进行加载
            try {
                ZipUtils.UnZipFolder(localPath, solibs);
                SoFileUtils.loadSoFile(getApplicationContext(), solibs);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!download) {
                selectVideo();
            }
        } else {
            //首次加载(sdk卡未获取到指定文件,网络请求下载)
            initStoryDialog();
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
        overridePendingTransition(0, 0);
        finish();
        return super.onKeyDown(keyCode, event);
    }


}
