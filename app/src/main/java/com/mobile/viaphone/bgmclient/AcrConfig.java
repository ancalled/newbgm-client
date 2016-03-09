package com.mobile.viaphone.bgmclient;

public class AcrConfig {

    private final String host;
    private final String accessKey;
    private final String accessSecret;

    public AcrConfig(String host, String accessKey, String accessSecret) {
        this.host = host;
        this.accessKey = accessKey;
        this.accessSecret = accessSecret;
    }

    public String getHost() {
        return host;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getAccessSecret() {
        return accessSecret;
    }
}
