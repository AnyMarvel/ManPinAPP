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
    public HttpPostRaw(String requestURL, String charset, Map<String, String> headers, String postData) throws IOException {
        if (requestURL == null || requestURL.trim().isEmpty()) {
            throw new IllegalArgumentException("Request URL cannot be null or empty");
        }
        if (charset == null || charset.trim().isEmpty()) {
            throw new IllegalArgumentException("Charset cannot be null or empty");
        }
        
        try {
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
            httpConn.setRequestProperty("Content-Type", "text/plain; charset=" + charset);
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (key != null && value != null) {
                        httpConn.setRequestProperty(key, value);
                    }
                }
            }
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IOException("Failed to setup SSL connection", e);
        } catch (Exception e) {
            throw new IOException("Failed to initialize HTTP connection", e);
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
        if (httpConn != null && key != null && value != null) {
            httpConn.setRequestProperty(key, value);
        }
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
            android.util.Log.e("HttpPostRaw", "getParamsByte failed", e);
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
            android.util.Log.e("HttpPostRaw", "encodeParam failed", e);
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
        if (httpConn == null) {
            throw new IOException("HttpURLConnection is not initialized");
        }

        ByteArrayOutputStream result = null;
        try {
            byte[] postDataBytes = this.getParamsByte();
            if (postDataBytes != null && postDataBytes.length > 0) {
                httpConn.getOutputStream().write(postDataBytes);
                httpConn.getOutputStream().flush();
            }

            // 检查服务器返回状态
            int status = httpConn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = httpConn.getInputStream().read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                return result.toString(this.charset);
            } else {
                throw new IOException("Server returned non-OK status: " + status);
            }
        } finally {
            // 确保资源被正确关闭
            if (result != null) {
                try {
                    result.close();
                } catch (IOException e) {
                    android.util.Log.e("HttpPostRaw", "Error closing ByteArrayOutputStream", e);
                }
            }
            if (httpConn != null) {
                try {
                    httpConn.disconnect();
                } catch (Exception e) {
                    android.util.Log.e("HttpPostRaw", "Error disconnecting HttpURLConnection", e);
                }
            }
        }
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
