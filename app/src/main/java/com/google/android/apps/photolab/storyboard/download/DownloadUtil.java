package com.google.android.apps.photolab.storyboard.download;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.mp.android.apps.monke.monkeybook.base.MBaseModelImpl;
import com.mp.android.apps.utils.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Description：下载文件工具类
 */

public class DownloadUtil extends MBaseModelImpl {
    private static final String TAG = "DownloadUtil";
    public static final String PATH_CHALLENGE_VIDEO = Environment.getExternalStorageDirectory() + "/ManPinDownloadFile";
    //视频下载相关
    protected ApiInterface mApi;
    private Call<ResponseBody> mCall;
    private File mFile;
    private Thread mThread;
    private String mVideoPath; //下载到本地的视频路径

    public DownloadUtil(String host, String cookie) {
        if (mApi == null) {
            //初始化网络请求接口
            mApi = ApiHelper.getInstance().getRetrofitObject(host, cookie)
                    .create(ApiInterface.class);
        }
    }

    public void downloadFile(String url, String fileName, final DownloadListener downloadListener) {
        String name = url;
        //通过Url得到文件并创建本地文件
        if (FileUtils.createOrExistsDir(PATH_CHALLENGE_VIDEO)) {
            mVideoPath = PATH_CHALLENGE_VIDEO + "/" + fileName;
        }
        if (!MD5Utils.checkFileMd5(mVideoPath, "d7bc16e438bc4f9aeeeb96add8640522")) {
            new File(mVideoPath).delete();
        }

        if (TextUtils.isEmpty(mVideoPath)) {
            Logger.e(TAG, "downloadVideo: 存储路径为空了");
            return;
        }
        //建立一个文件
        mFile = new File(mVideoPath);
        if (!FileUtils.isFileExists(mFile) && FileUtils.createOrExistsFile(mFile)) {
            if (mApi == null) {
                Logger.e(TAG, "downloadVideo: 下载接口为空了");
                return;
            }
            mCall = mApi.downloadFile(url);
            mCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull final Response<ResponseBody> response) {
                    //下载文件放在子线程
                    mThread = new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            //保存到本地
                            writeFile2Disk(response, mFile, downloadListener);
                        }
                    };
                    mThread.start();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    downloadListener.onFailure(); //下载失败
                }
            });
        } else {
            downloadListener.onFinish(mVideoPath); //下载完成
        }
    }

    //将下载的文件写入本地存储
    private void writeFile2Disk(Response<ResponseBody> response, File file, DownloadListener downloadListener) {
        downloadListener.onStart();
        long currentLength = 0;
        OutputStream os = null;

        InputStream is = response.body().byteStream(); //获取下载输入流
        long totalLength = response.body().contentLength();

        try {
            os = new FileOutputStream(file); //输出流
            int len;
            byte[] buff = new byte[1024];
            while ((len = is.read(buff)) != -1) {
                os.write(buff, 0, len);
                currentLength += len;
                Logger.e(TAG, "当前进度: " + currentLength);
                //计算当前下载百分比，并经由回调传出
                downloadListener.onProgress((int) (100 * currentLength / totalLength));
                //当百分比为100时下载结束，调用结束回调，并传出下载后的本地路径
                if ((int) (100 * currentLength / totalLength) == 100) {
                    downloadListener.onFinish(mVideoPath); //下载完成
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close(); //关闭输出流
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close(); //关闭输入流
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
