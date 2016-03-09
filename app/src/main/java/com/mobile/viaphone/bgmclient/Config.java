package com.mobile.viaphone.bgmclient;

import android.util.Base64;

public class Config {

    public String getAuth() {
        return Base64.encodeToString("islam:123".getBytes(), Base64.DEFAULT);
    }
}
