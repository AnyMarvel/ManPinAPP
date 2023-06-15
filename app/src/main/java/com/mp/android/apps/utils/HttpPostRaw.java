package com.mp.android.apps.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpPostRaw {
    // 设置连接主机服务器的超时时间：15000毫秒
    private final int CONNECT_TIMEOUT = 15000;
    // 设置读取远程返回的数据时间：60000毫秒
    private final int READ_TIMEOUT = 60000;

    private HttpURLConnection httpConn;
    private String postData;
    private String charset;

    /**
     * 构造方法
     *
     * @param requestURL  请求地址
     * @param charset     请求的编码
     * @param headers     请求头
     * @param postData    请求字段
     * @throws IOException
     */
    public HttpPostRaw(String requestURL, String charset, Map<String, String> headers, String postData){
        try{
            this.charset = charset;
            this.postData = postData;
            URL url = new URL(requestURL);
            trustAllHttpsCertificates();
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setConnectTimeout(CONNECT_TIMEOUT);
            httpConn.setReadTimeout(READ_TIMEOUT);
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true);    // 表明是post请求
            httpConn.setDoInput(true);
            httpConn.setRequestProperty("Content-Type", "text/plain");
            if (headers != null && headers.size() > 0) {
                Iterator<String> it = headers.keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    String value = headers.get(key);
                    httpConn.setRequestProperty(key, value);
                }
            }
        }catch (Exception e){

        }


    }

    public HttpPostRaw(String requestURL, String charset, Map<String, String> headers) throws IOException {
        this(requestURL, charset, headers, null);
    }

    public HttpPostRaw(String requestURL, String charset) throws IOException {
        this(requestURL, charset, null, null);
    }

    /**
     * 添加请求头
     *
     * @param key
     * @param value
     */
    public void addHeader(String key, String value) {
        httpConn.setRequestProperty(key, value);
    }

    /**
     * 设置请求数据
     * @param postData
     */
    public void setPostData(String postData) {
        this.postData = postData;
    }

    /**
     * 将请求字段转化成byte数组
     *
     * @return
     */
    private byte[] getParamsByte() {
        byte[] result = null;
        try {
            result = this.postData.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 对键和值进行url编码
     *
     * @param data
     * @return
     */
    private String encodeParam(String data) {
        String result = "";
        try {
            result = URLEncoder.encode(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 完成请求，并接受服务器的回应
     *
     * @return 如果请求成功，状态码是200，返回服务器返回的字符串，否则抛出异常
     * @throws IOException
     */
    public String finish() throws IOException {
        String response = "";
        byte[] postDataBytes = this.getParamsByte();
        httpConn.getOutputStream().write(postDataBytes);
        // 检查服务器返回状态
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = httpConn.getInputStream().read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            response = result.toString(this.charset);
            httpConn.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }
        return response;
    }

    private static void trustAllHttpsCertificates() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[1];
        trustAllCerts[0] = new TrustAllManager();
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                return true;
            }
        });

    }

    private static class TrustAllManager implements X509TrustManager {

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        }
    }
}
