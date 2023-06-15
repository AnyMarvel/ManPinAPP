package com.mp.android.apps.downloadUtils;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.azhon.appupdate.manager.DownloadManager;
import com.mp.android.apps.R;
import com.mp.android.apps.book.base.MBaseModelImpl;
import com.mp.android.apps.book.base.observer.SimpleObserver;
import com.mp.android.apps.utils.GeneralTools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Url;

public class CheckUpdateUtils extends MBaseModelImpl {
    private static CheckUpdateUtils downloadUtils;
    private CheckUpdateUtils(){}
    public static CheckUpdateUtils getInstance(){
        if (downloadUtils==null){
            synchronized (CheckUpdateUtils.class){
                if (downloadUtils==null){
                    downloadUtils=new CheckUpdateUtils();
                }
            }
        }
        return downloadUtils;
    }
    public static CheckUpdateBean checkUpdateBean;

    public void checkUpdata(Activity activity){
        WeakReference<Activity> activityWeakReference=new WeakReference<>(activity);
        Toast.makeText(activityWeakReference.get(), "新版本检测中...", Toast.LENGTH_SHORT).show();
        getRetrofitObject("https://gitee.com").
                create(DownloadInterface.class).
                checkUpdate()
               .flatMap(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(String value) throws Exception {
//                        {
//                            "apkName":"manpin.apk",
//                                "versionCode":21,
//                                "apkDescription":"1、修复实效源，增加红牛专线  2、优化搜索算法，加快查找速度  3、异常UI修复",
//                                "lanzouDownloadUrl":"https://wwtv.lanzoum.com/iGJCE0y3s10h",
//                                "apkMd5":"21436cd2c7240f8d111fa066ac06298c",
//                                "ApkSize":"12M"
//                        }
                        String test="{\n" +
                                "    \"apkName\":\"manpin.apk\",\n" +
                                "    \"versionCode\":60,\n" +
                                "    \"apkDescription\":\"1、修复实效源，增加红牛专线  2、优化搜索算法，加快查找速度  3、异常UI修复\",\n" +
                                "    \"lanzouDownloadUrl\":\"https://wwtv.lanzoum.com/iGJCE0y3s10h\",\n" +
                                "    \"versionName\":\"2.0.3\",\n" +
                                "    \"apkMd5\":\"21436cd2c7240f8d111fa066ac06298c\",\n" +
                                "    \"ApkSize\":\"12M\"\n" +
                                "}";
                        checkUpdateBean = JSONObject.parseObject(test,CheckUpdateBean.class);
                        if (checkUpdateBean!=null && GeneralTools.APP_VERSIONCODE < checkUpdateBean.getVersionCode()){
                            Uri uri=Uri.parse(checkUpdateBean.getLanzouDownloadUrl());
                            String host=uri.getHost();
                            String scheme=uri.getScheme();
                            String path=uri.getPath();
                            return getRetrofitObject(scheme+"://"+host).create(DownloadInterface.class).lanzouSpider(path);
                        }else {
                            Toast.makeText(activityWeakReference.get(), "当前已是最新版本", Toast.LENGTH_SHORT).show();
                            return null;
                        }
                    }
                }).flatMap(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(String s) throws Exception {
                        try {
                            if (!TextUtils.isEmpty(s)){
                                Document doc = Jsoup.parse(s);
                                String downloadUrl = doc.getElementsByClass("n_downlink").get(0).attr("src");
                                if (downloadUrl!=null){
                                    checkUpdateBean.setLanzouSignUrl(downloadUrl);
                                    return getRetrofitObject("https://www.lanzoum.com").create(DownloadInterface.class).lanzouSpider(downloadUrl);
                                }
                            }
                        }catch (Exception e){

                        }
                        return null;
                    }
                }).flatMap(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(String result) throws Exception {
                        try {
                            String action = "downprocess";

                            String signs = RegexUtils.getMatch(result, "var ajaxdata = '(.*?)';", false);
                            if (TextUtils.isEmpty(signs)) {
                                signs = RegexUtils.getMatch(result, "'sings':'(.*?)',", false);
                            }
                            String sign = RegexUtils.getMatch(result, "var s_sign = '(.*?)';", false);
                            if (TextUtils.isEmpty(sign)) {
                                sign = RegexUtils.getMatch(result, "'sign':'(.*?)',", false);
                            }
                            String websignkey = RegexUtils.getMatch(result, "var wsk_sign = '(.*?)';", false);
                            String ves="1";
                            try {
                                ves = RegexUtils.getMatch(result, "wsk_sign,'ves':(.*?) },", false);
                            }catch (Exception e){

                            }

                            Map<String, String> bodyMap =new HashMap<>();
                            bodyMap.put("action",action);
                            bodyMap.put("signs",signs);
                            bodyMap.put("websignkey",websignkey);
                            bodyMap.put("sign",sign);
                            bodyMap.put("ves",ves);
                            bodyMap.put("websign","");


                            Map<String, String> headers=new HashMap<>();
                            headers.put("Accept", " application/json, text/javascript, */*");
                            headers.put("Accept-Encoding", "gzip, deflate, br");
                            headers.put("Accept-Language", "zh-CN,zh;q=0.9");
                            headers.put("Cache-Control", "no-cache");
                            headers.put("Connection", "keep-alive");
                            headers.put("Content-Length", "151");
//                            headers.put("Content-Type", "application/x-www-form-urlencoded");//此处不能配置，否则下发数据未乱码
                            headers.put("Cookie", "codelen=1");
                            headers.put("Host", " lanzoux.com");
                            headers.put("Origin", "https://lanzoux.com");
                            headers.put("Pragma", "no-cache");
                            headers.put("Referer", "https://www.lanzoum.com" + checkUpdateBean.getLanzouSignUrl());
                            headers.put("sec-ch-ua","\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"");
                            headers.put("sec-ch-ua-mobile", "?0");
                            headers.put("sec-ch-ua-platform", "macOS");
                            headers.put("Sec-Fetch-Dest", "empty");
                            headers.put("Sec-Fetch-Mode", "cors");
                            headers.put("Sec-Fetch-Site", "same-origin");
                            headers.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
                            headers.put("X-Requested-With", "XMLHttpRequest");

                            Map<String, RequestBody> requestBodyMap=generateFormRequestBody(bodyMap);
                            return getRetrofitObject("https://lanzoux.com").create(DownloadInterface.class).lanzouSignPostFormData(headers,requestBodyMap);

                        } catch (Exception e) {

                        }
                        return null;
                    }
                }).flatMap(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(String s) throws Exception {
                        JSONObject jsonObject=JSONObject.parseObject(s);

                        String dom=jsonObject.getString("dom");
                        String path="/file/"+jsonObject.getString("url");
                        String downloadUrl = getResponseLocation(dom,path);
                        if (downloadUrl!=null){
                            return Observable.create(new ObservableOnSubscribe<String>() {
                                @Override
                                public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                                    emitter.onNext(downloadUrl);
                                }
                            });
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onNext(String value) {
                        if (value != null && activityWeakReference.get() != null){
                            showDownloadTips(activityWeakReference.get(),value);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }





    private String getResponseLocation(String dom,String path) {
        String location = null;
        try {
            URL url = new URL(dom+path);
            HttpURLConnection mConnection = (HttpURLConnection) url
                    .openConnection();
            mConnection.setInstanceFollowRedirects(false);
            mConnection.setRequestMethod("GET");
            mConnection.setRequestProperty( "authority", "developer.lanzoug.com");
            mConnection.setRequestProperty( "method", "GET");
            mConnection.setRequestProperty( "path", path);
            mConnection.setRequestProperty( "scheme", "https");
            mConnection.setRequestProperty( "accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            mConnection.setRequestProperty( "accept-encoding", "gzip, deflate, br");
            mConnection.setRequestProperty( "accept-language", "zh-CN,zh;q=0.9");
            mConnection.setRequestProperty( "cache-control", "no-cache");
            mConnection.setRequestProperty( "cookie", "down_ip=1");
            mConnection.setRequestProperty( "pragma", "no-cache");
            mConnection.setRequestProperty( "sec-ch-ua","\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"");
            mConnection.setRequestProperty( "sec-ch-ua-mobile", "?0");
            mConnection.setRequestProperty( "sec-ch-ua-platform", "macOS");
            mConnection.setRequestProperty( "sec-fetch-dest", "document");
            mConnection.setRequestProperty( "sec-fetch-mode", "navigate");
            mConnection.setRequestProperty( "sec-fetch-site", "none");
            mConnection.setRequestProperty( "sec-fetch-user", "?1");
            mConnection.setRequestProperty( "upgrade-insecure-requests", "1");
            mConnection.setRequestProperty( "user-agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
            mConnection.setConnectTimeout(5 * 1000);

            mConnection.connect();

            location = mConnection.getHeaderField("location");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return location;
    }


    public void showDownloadTips(Activity activity,String downloadUrl){
        if (checkUpdateBean!=null && !TextUtils.isEmpty(downloadUrl)){
            DownloadManager downloadManager = new DownloadManager.Builder(activity)
                    .apkUrl(downloadUrl)
                    .apkName(checkUpdateBean.getApkName())
                    .smallIcon(R.mipmap.ic_launcher)
                    //设置了此参数，那么内部会自动判断是否需要显示更新对话框，否则需要自己判断是否需要更新
                    .apkVersionCode(checkUpdateBean.getVersionCode())
                    .apkVersionName(checkUpdateBean.getVersionName())
                    .apkSize(checkUpdateBean.getApkSize())
                    .apkDescription(checkUpdateBean.getApkDescription())
                    .build();
            downloadManager.download();
        }

    }


public interface DownloadInterface{
    @GET("/dssljt/hgsdist/raw/master/update.json")
    @Headers({"Accept:text/html,application/xhtml+xml,application/xml",
            "User-Agent:Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3",
            "Accept-Charset:UTF-8",
            "Connection:close",
            "Cache-Control:no-cache"})
    Observable<String> checkUpdate();

    @GET
    @Headers({"Accept:text/html,application/xhtml+xml,application/xml",
            "User-Agent:Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3",
            "Accept-Charset:UTF-8",
            "Connection:close",
            "Cache-Control:no-cache"})
    Observable<String> lanzouSpider(@Url String url);

    @Multipart
    @POST("/ajaxm.php")
    Observable<String> lanzouSignPostFormData(@HeaderMap Map<String, String> headers,@PartMap Map<String, RequestBody> requestBodyMap);




}




}
