package com.mobile.viaphone.bgmclient;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.acrcloud.rec.sdk.ACRCloudClient;
import com.acrcloud.rec.sdk.ACRCloudConfig;
import com.acrcloud.rec.sdk.IACRCloudListener;
import com.mobile.viaphone.bgmclient.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {

    private ACRCloudListener acrCloudListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String path = Environment.getExternalStorageDirectory().toString() + "/acrcloud/model";

        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        TextView mVolume = (TextView) findViewById(R.id.volume);
        TextView mResult = (TextView) findViewById(R.id.result);
        TextView tv_time = (TextView) findViewById(R.id.time);

        Config config = new Config();
        HttpHelper.getAcrData(config.getAuth());
        acrCloudListener = new ACRCloudListener(config, this, mVolume, mResult);

        Button startBtn = (Button) findViewById(R.id.start);
        startBtn.setText(getResources().getString(R.string.start));

        Button stopBtn = (Button) findViewById(R.id.stop);
        stopBtn.setText(getResources().getString(R.string.stop));

        Button cancelBtn = (Button) findViewById(R.id.cancel);
        cancelBtn.setText(getResources().getString(R.string.cancel));

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                acrCloudListener.start();
            }
        });

        findViewById(R.id.stop).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        acrCloudListener.stopRecordToRecognize();
                    }
                });

        findViewById(R.id.cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        acrCloudListener.cancel();
                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("MainActivity", "release");
        acrCloudListener.destroy();
    }
}
