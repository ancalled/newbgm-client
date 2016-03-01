package com.acrcloud.rec.demo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.acrcloud.rec.sdk.ACRCloudClient;
import com.acrcloud.rec.sdk.ACRCloudConfig;
import com.acrcloud.rec.sdk.IACRCloudListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements IACRCloudListener {

    private ACRCloudClient mClient;
    private ACRCloudConfig mConfig;

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
            this.mConfig = new ACRCloudConfig();
            this.mConfig.acrcloudListener = this;
            this.mConfig.context = this;
            this.mConfig.host = "ap-southeast-1.api.acrcloud.com";
            this.mConfig.accessKey = "8e59a4a51aca4cc1d865868eb8bea8cd";
            this.mConfig.accessSecret = "KWfTDefYGipu8lfjUryCDneZSuBsod6zHv4ns5SL";
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

        List<Music> musics = parseAcr(result);
        for (Music music: musics) {
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

    private List<Music> parseAcr(String result) {
        List<Music> musicList = new ArrayList<>();
        try {
            JSONObject j = new JSONObject(result);
            JSONObject j1 = j.getJSONObject("status");
            int j2 = j1.getInt("code");
            if (j2 == 0) {
                JSONObject metadata = j.getJSONObject("metadata");
                if (metadata.has("music")) {
                    JSONArray musics = metadata.getJSONArray("music");
                    for (int i = 0; i < musics.length(); i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        Music music = new Music();
                        music.setCustomer("islam");
                        music.setAcrId(tt.getString("acrid"));

                        JSONArray artistt = tt.getJSONArray("artists");
                        JSONObject art = (JSONObject) artistt.get(0);
                        music.setArtists(art.getString("name"));

                        if (!tt.isNull("title")) {
                            music.setTitle(tt.getString("title"));
                        }
                        if (!tt.isNull("genres")) {
                            music.setGenres(tt.getString("genres"));
                        }

                        if (!tt.isNull("album")) {
                            music.setAlbumName(tt.getJSONObject("album").getString("name"));
                        }

                        if (!tt.isNull("duration_ms")) {
                            music.setDuration(tt.getLong("duration_ms") / 1000);
                        }

                        if (!tt.isNull("label")) {
                            music.setLabel(tt.getString("label"));
                        }

//                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//                        if (!tt.isNull("release_date")) {
//                            music.setReleaseDate(format.parse(tt.getString("release_date")));
//                        }
//                        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                        music.setRecDate(format.parse(metadata.getString("timestamp_utc")));
                        musicList.add(music);
                    }
                }
                if (metadata.has("humming")) {
                    JSONArray hummings = metadata.getJSONArray("humming");
                    for (int i = 0; i < hummings.length(); i++) {
                        JSONObject tt = (JSONObject) hummings.get(i);
                        String title = tt.getString("title");
                        JSONArray artistt = tt.getJSONArray("artists");
                        JSONObject art = (JSONObject) artistt.get(0);
                        String artist = art.getString("name");
                    }
                }
                if (metadata.has("streams")) {
                    JSONArray musics = metadata.getJSONArray("streams");
                    for (int i = 0; i < musics.length(); i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        String title = tt.getString("title");
                        JSONArray channelId = tt.getJSONArray("channel_id");
//						tres = tres + (i+1) + ".  Title: " + title + "    Channel Id: " + channelId + "\n";
                    }
                }
                if (metadata.has("custom_files")) {
                    JSONArray musics = metadata.getJSONArray("custom_files");
                    for (int i = 0; i < musics.length(); i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        String title = tt.getString("title");
//						tres = tres + (i+1) + ".  Title: " + title + "\n";
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return musicList;
    }
}
