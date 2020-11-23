package com.mp.android.apps.monke.monkeybook.base;

import com.mp.android.apps.monke.basemvplib.EncodoConverter;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public abstract class MBaseModelImpl {
    protected OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(new ProxyInterceptor());

    TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        @Override
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] x509Certificates,
                String s) throws java.security.cert.CertificateException {
        }

        @Override
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] x509Certificates,
                String s) throws java.security.cert.CertificateException {
        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }
    }};

    protected Retrofit getRetrofitObject(String url) {
        clientBuilder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        //创建管理器
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            //为OkHttpClient设置sslSocketFactory
            clientBuilder.sslSocketFactory(sslContext.getSocketFactory());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Retrofit.Builder().baseUrl(url)
                //增加返回值为字符串的支持(以实体类返回)
                .addConverterFactory(ScalarsConverterFactory.create())
                //增加返回值为Oservable<T>的支持
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(clientBuilder.build())
                .build();
    }

    protected Retrofit getRetrofitString(String url, String encode) {
        return new Retrofit.Builder().baseUrl(url)
                //增加返回值为字符串的支持(以实体类返回)
                .addConverterFactory(EncodoConverter.create(encode))
                //增加返回值为Oservable<T>的支持
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(clientBuilder.build())
                .build();
    }


}