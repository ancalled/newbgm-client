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

public class MainActivity extends Activity implements IACRCloudListener {

    private ACRCloudClient mClient;
    private ACRCloudConfig mConfig;
    private AcrConfig acrConfig;

    private TextView mVolume, mResult, tv_time;

    private boolean mProcessing = false;
    private boolean initState = false;

    private String path = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        path = Environment.getExternalStorageDirectory().toString()
                + "/acrcloud/model";

        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        String auth = Base64.encodeToString("islam:123".getBytes(), Base64.DEFAULT);
        HttpHelper.getAcrData(auth);

        mVolume = (TextView) findViewById(R.id.volume);
        mResult = (TextView) findViewById(R.id.result);
        tv_time = (TextView) findViewById(R.id.time);

        Button startBtn = (Button) findViewById(R.id.start);
        startBtn.setText(getResources().getString(R.string.start));

        Button stopBtn = (Button) findViewById(R.id.stop);
        stopBtn.setText(getResources().getString(R.string.stop));

        Button cancelBtn = (Button) findViewById(R.id.cancel);
        cancelBtn.setText(getResources().getString(R.string.cancel));

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                start();
            }
        });

        findViewById(R.id.stop).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        stopRecordToRecognize();
                    }
                });

        findViewById(R.id.cancel).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        cancel();
                    }
                });
    }


    public void start() {
        if (!this.initState) {
            AcrConfig acrConfig = HttpHelper.getAcrConfig();
            this.mConfig = new ACRCloudConfig();
            this.mConfig.acrcloudListener = this;
            this.mConfig.context = this;
            this.mConfig.host = acrConfig.getHost();
            this.mConfig.accessKey = acrConfig.getAccessKey();
            this.mConfig.accessSecret = acrConfig.getAccessSecret();
//            this.mConfig.host = "ap-southeast-1.api.acrcloud.com";
//            this.mConfig.accessKey = "8e59a4a51aca4cc1d865868eb8bea8cd";
//            this.mConfig.accessSecret = "KWfTDefYGipu8lfjUryCDneZSuBsod6zHv4ns5SL";
            this.mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE;

            this.mClient = new ACRCloudClient();
            this.initState = this.mClient.initWithConfig(this.mConfig);
            if (!this.initState) {
                Toast.makeText(this, "init error", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!mProcessing) {
            mProcessing = true;
            mVolume.setText("");
            mResult.setText("");
            if (this.mClient == null || !this.mClient.startRecognize()) {
                mProcessing = false;
                mResult.setText("start error!");
            }
        }
    }

    protected void stopRecordToRecognize() {
        if (mProcessing && this.mClient != null) {
            this.mClient.stopRecordToRecognize();
        }
        mProcessing = false;
    }

    protected void cancel() {
        if (mProcessing && this.mClient != null) {
            mProcessing = false;
            this.mClient.cancel();
            tv_time.setText("");
            mResult.setText("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onResult(String result) {
        if (this.mClient != null) {
            this.mClient.cancel();
            mProcessing = false;
        }
        mResult.setText(result);

        MusicRec music = parseAcr(result);
        if (music != null) {
            HttpHelper.sendMusic(music);
        }
    }

    @Override
    public void onVolumeChanged(double volume) {
        mVolume.setText(getResources().getString(R.string.volume) + volume);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("MainActivity", "release");
        if (this.mClient != null) {
            this.mClient.release();
            this.initState = false;
            this.mClient = null;
        }
    }

    private MusicRec parseAcr(String result) {

        try {
            JSONObject jResult = new JSONObject(result);
            JSONObject status = jResult.getJSONObject("status");
            if (status.getInt("code") == 0) {
                JSONObject metadata = jResult.getJSONObject("metadata");
                if (metadata.has("music")) {

                    MusicRec musicReq = new MusicRec();
                    musicReq.setCustomer("islam");

                    JSONArray musics = metadata.getJSONArray("music");
                    JSONObject tt = (JSONObject) musics.get(0);
                    Music music = new Music();
                    music.setAcrid(tt.getString("acrid"));

                    if (!tt.isNull("title")) {
                        music.setTitle(tt.getString("title"));
                    }

                    if (!tt.isNull("label")) {
                        music.setLabel(tt.getString("label"));
                    }

                    if (!tt.isNull("duration_ms")) {
                        music.setDuration(tt.getLong("duration_ms") / 1000);
                    }

                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    if (!tt.isNull("release_date")) {
                        music.setReleaseDate(format.parse(tt.getString("release_date")));
                    }

                    if (!tt.isNull("album")) {
                        music.setAlbum(tt.getJSONObject("album").getString("name"));
                    }

                    if (!tt.isNull("genres")) {
                        JSONArray genres = tt.getJSONArray("genres");
                        for (int j = 0; j < genres.length(); j++) {
                            JSONObject genre = (JSONObject) genres.get(j);
                            music.addGenre(genre.getString("name"));
                        }
                    }

                    if (!tt.isNull("artists")) {
                        JSONArray artists = tt.getJSONArray("artists");
                        for (int j = 0; j < artists.length(); j++) {
                            JSONObject artist = (JSONObject) artists.get(j);
                            music.addArtist(artist.getString("name"));
                        }
                    }

                    if (!tt.isNull("play_offset_ms")) {
                        music.setPlayOffset(tt.getLong("play_offset_ms"));
                    }

                    if (!tt.isNull("external_ids")) {
                        JSONObject externalIds = tt.getJSONObject("external_ids");
                        if (!externalIds.isNull("isrc")) {
                            music.setIsrcCode(externalIds.getString("isrc"));
                        }

                        if (!externalIds.isNull("upc")) {
                            music.setUpcCode(externalIds.getString("upc"));
                        }
                    }

                    musicReq.setMusic(music);
                    format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    musicReq.setRecDate(format.parse(metadata.getString("timestamp_utc")));
                    return musicReq;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
