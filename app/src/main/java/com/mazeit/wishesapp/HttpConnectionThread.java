package com.mazeit.wishesapp;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by User on 11/3/2016.
 */

public  class HttpConnectionThread implements  Runnable {
    private static final String TAG = "HttpConnectionThread";
    private String mURL;
    private String outputData="";
    private String httpMethod;
    private String inputData;
    private String sessionId;
    public void setURL(final String url_id){
        mURL=url_id;
    }
    public void setURLMethod(final String method){
        httpMethod=method;
    }
    public void setInputData(final String data){
        inputData=data;
    }
    public void setSession(final String id){
        sessionId = id;
    }

    public void setHttpConnectionThreadListener(HttpConnectionThreadListener listener){
        this.listener=listener;
    }

    public interface HttpConnectionThreadListener {
       void getURLData(String s1, String sessionId);
    }

    private HttpConnectionThreadListener listener;
        public HttpConnectionThread(Certificate ca){
            this.listener=null;
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(keyStoreType);
        } catch (KeyStoreException e) {
            Log.d(TAG, e.getMessage());
        }

        try {
            if (keyStore != null) {
                keyStore.load(null, "client".toCharArray());
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        try {
            if (keyStore != null) {
                keyStore.setCertificateEntry("ca", ca);
            }
        } catch (KeyStoreException e) {
            Log.d(TAG, e.getMessage());
        }
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = null;
        try {
            tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            Log.d(TAG, e.getMessage());
        }
        try {
            if (tmf != null) {
                tmf.init(keyStore);
            }
        } catch (KeyStoreException e) {
            Log.d(TAG, e.getMessage());
        }
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
            SSLContext context = SSLContext.getInstance("TLS");
            if (tmf != null) {
                context.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());
            }
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
            Log.e(TAG, "Failed to establish SSL connection to server: " + ex.toString());
        }
    }
    public void run() {
        URL url = null;
        try {
            url = new URL(mURL);
        } catch (MalformedURLException e) {
            Log.d(TAG, e.getMessage());
        }
//        HttpsURLConnection urlConnection = null;
        HttpURLConnection urlConnection = null;
        InputStream in = null;
        try {
            urlConnection = (HttpURLConnection) (url != null ? url.openConnection() : null);
            urlConnection.setRequestMethod(httpMethod);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Cookie", "JSESSIONID="+sessionId);

            assert urlConnection != null;
//        urlConnection.getSSLSocketFactory();
            OutputStream os = null;

            if (inputData != null) {
                Log.d(TAG, "Data to be sent: "+inputData);
                urlConnection.setRequestProperty("Content-Type","application/json");
                byte[] outputInBytes = inputData.getBytes("UTF-8");
                os = urlConnection.getOutputStream();
                os.write(outputInBytes);
                os.close();
            }

            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "http response code is " + urlConnection.getResponseCode());
                listener.getURLData(outputData, null);
            }

            in = urlConnection.getInputStream();

            copyInputStreamToOutputStream(in, System.out);

            try {
                String cookie = urlConnection.getHeaderField("Set-Cookie");
                if (cookie.contains("JSESSIONID")) {
                    int index = cookie.indexOf("JSESSIONID=");

                    int endIndex = cookie.indexOf(";", index);

                    String sessionID = cookie.substring(
                            index + "JSESSIONID=".length(), endIndex);

                    Log.d(TAG, "session id: " + sessionID);

                    listener.getURLData(outputData, sessionID);
                }
                else
                    listener.getURLData(outputData, null);
            } catch (Exception e) {
                Log.d(TAG, "Cookie for JSESSIONID is not present in the response. " + e);
                listener.getURLData(outputData, null);
            }
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }
    private void copyInputStreamToOutputStream(InputStream in, PrintStream out) {
        InputStream inp = new BufferedInputStream(in);
        BufferedReader reader = null;
        outputData =new String();
        try {
            reader = new BufferedReader(new InputStreamReader(inp));
            String line = "";
    while ((line = reader.readLine()) != null) {
        Log.d("Output : ", line);
        outputData = outputData + line;
    }
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }
    }
}
