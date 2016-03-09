package com.mobile.viaphone.bgmclient;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HttpHelper {

    //    private static final String host = "http://192.168.1.5/customer/";
    private static final String host = "http://ec2-54-213-41-197.us-west-2.compute.amazonaws.com:8080/newbgm/customer/";
    private static final String musicRecUrl = "music-rec";
    private static final String acrConfigUrl = "acr-data";

    private static AcrConfig acrConfig;

    public static AcrConfig getAcrConfig() {
        return acrConfig;
    }

    public static void setAcrConfig(AcrConfig acrConfig) {
        HttpHelper.acrConfig = acrConfig;
    }

    public static void getAcrData(final String auth) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String res = sendGet(host + acrConfigUrl, auth);
                setAcrConfig(new Gson().fromJson(res, AcrConfig.class));
            }
        }).start();
    }

    public static void sendMusic(final MusicRec music, final String auth) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendPost(host + musicRecUrl, new Gson().toJson(music), auth);
            }
        }).start();
    }

    private static String sendGet(String uri, String auth) {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet request = new HttpGet(uri);
            request.setHeader("Authorization", "Basic " + auth.replace("\n", "").replace("\r", ""));
            HttpResponse response = httpClient.execute(request);
            return parseResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String sendPost(String uri, String body, String auth) {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Basic " + auth.replace("\n", "").replace("\r", ""));
            StringEntity postBodyEntity = new StringEntity(body, HTTP.UTF_8);
            httpPost.setEntity(postBodyEntity);
            HttpResponse response = httpClient.execute(httpPost);
            return parseResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String parseResponse(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }
}
